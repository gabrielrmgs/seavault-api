package br.dev.irontech.seavault.reports.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class ReportResourceTest {

    private String tokenFor(String email) {
        given().contentType(ContentType.JSON)
                .body("""
                      {"name":"Report E2E","email":"%s","password":"senha1234","acceptTerms":true}
                      """.formatted(email))
                .when().post("/api/auth/register").then().statusCode(201);
        return given().contentType(ContentType.JSON)
                .body("""
                      {"email":"%s","password":"senha1234"}
                      """.formatted(email))
                .when().post("/api/auth/login").then().statusCode(200)
                .extract().path("accessToken");
    }

    private String documentTypeId() {
        return given().when().get("/api/reference/types?kind=DOCUMENT")
                .then().statusCode(200).extract().path("[0].id");
    }

    private void seedDocument(String token) {
        String expiry = LocalDate.now().plusYears(1).toString();
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"typeId":"%s","number":"123","issuer":"DPC","expiryDate":"%s"}
                      """.formatted(documentTypeId(), expiry))
                .when().post("/api/documents").then().statusCode(201);
    }

    @Test
    void requiresAuth() {
        given().when().get("/api/reports/documents").then().statusCode(401);
    }

    @Test
    void generatesJsonReport() {
        String token = tokenFor("report-json@example.com");
        seedDocument(token);

        given().auth().oauth2(token)
                .when().get("/api/reports/documents?format=JSON")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("type", equalTo("DOCUMENTS"))
                .body("sections", notNullValue())
                .body("sections[0].table.rows.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void generatesPdfReport() {
        String token = tokenFor("report-pdf@example.com");
        seedDocument(token);

        byte[] pdf = given().auth().oauth2(token)
                .when().get("/api/reports/documents?format=PDF")
                .then().statusCode(200)
                .contentType("application/pdf")
                .extract().asByteArray();

        // cabecalho magico %PDF-
        assert pdf.length > 100;
        assert pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F';
    }

    @Test
    void invalidTypeReturns400() {
        String token = tokenFor("report-badtype@example.com");
        given().auth().oauth2(token)
                .when().get("/api/reports/naoexiste")
                .then().statusCode(400)
                .body("code", equalTo("VALIDATION"));
    }

    @Test
    void historyIsRecordedAndIsolatedPerUser() {
        String tokenA = tokenFor("report-hist-a@example.com");
        String tokenB = tokenFor("report-hist-b@example.com");
        seedDocument(tokenA);

        given().auth().oauth2(tokenA).when().get("/api/reports/documents?format=JSON").then().statusCode(200);

        given().auth().oauth2(tokenA).when().get("/api/reports/history")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("[0].type", equalTo("DOCUMENTS"));

        // Usuario B nunca gerou nada -> historico vazio
        given().auth().oauth2(tokenB).when().get("/api/reports/history")
                .then().statusCode(200)
                .body("size()", equalTo(0));
    }
}

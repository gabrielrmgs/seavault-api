package br.dev.irontech.seavault.reports.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class Anexo1SResourceTest {

    private String tokenFor(String email) {
        given().contentType(ContentType.JSON)
                .body("""
                      {"name":"Anexo E2E","email":"%s","password":"senha1234","acceptTerms":true}
                      """.formatted(email))
                .when().post("/api/auth/register").then().statusCode(201);
        return given().contentType(ContentType.JSON)
                .body("""
                      {"email":"%s","password":"senha1234"}
                      """.formatted(email))
                .when().post("/api/auth/login").then().statusCode(200)
                .extract().path("accessToken");
    }

    private void completeProfile(String token) {
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"cir":"CIR123","cpf":"12345678901","nationality":"Brasileira"}
                      """)
                .when().put("/api/profile").then().statusCode(200);
    }

    private String createVessel(String token) {
        return given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"name":"Navio Teste"}
                      """)
                .when().post("/api/vessels").then().statusCode(201).extract().path("id");
    }

    private String createCompany(String token) {
        return given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"name":"Empresa Teste"}
                      """)
                .when().post("/api/companies").then().statusCode(201).extract().path("id");
    }

    private String createFinishedVoyage(String token) {
        String vesselId = createVessel(token);
        String companyId = createCompany(token);
        return given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"embarkDate":"2025-01-10","disembarkDate":"2025-03-10","vesselId":"%s","companyId":"%s","role":"Comandante","embarkPort":"Santos","disembarkPort":"Rio"}
                      """.formatted(vesselId, companyId))
                .when().post("/api/voyages").then().statusCode(201).extract().path("id");
    }

    @Test
    void requiresAuth() {
        given().contentType(ContentType.JSON)
                .body("""
                      {"voyageIds":[]}
                      """)
                .when().post("/api/reports/anexo-1s").then().statusCode(401);
    }

    @Test
    void incompleteReturns422WithFieldErrors() {
        String token = tokenFor("anexo-e2e-422@example.com");

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"voyageIds":[]}
                      """)
                .when().post("/api/reports/anexo-1s")
                .then().statusCode(422)
                .body("code", equalTo("BUSINESS_RULE"))
                .body("fieldErrors.size()", greaterThanOrEqualTo(1))
                .body("fieldErrors.field", hasItem("cpf"));
    }

    @Test
    void generatesPdfByDefault() {
        String token = tokenFor("anexo-e2e-pdf@example.com");
        completeProfile(token);
        String voyageId = createFinishedVoyage(token);

        byte[] pdf = given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"voyageIds":["%s"]}
                      """.formatted(voyageId))
                .when().post("/api/reports/anexo-1s")
                .then().statusCode(200)
                .contentType("application/pdf")
                .extract().asByteArray();

        assertTrue(pdf.length > 100);
        assertTrue(pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F');
    }

    @Test
    void generatesJsonWhenRequested() {
        String token = tokenFor("anexo-e2e-json@example.com");
        completeProfile(token);
        String voyageId = createFinishedVoyage(token);

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"voyageIds":["%s"]}
                      """.formatted(voyageId))
                .when().post("/api/reports/anexo-1s?format=JSON")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("type", equalTo("ANEXO_1S"))
                .body("sections", notNullValue());
    }

    @Test
    void generationIsRecordedInHistory() {
        String token = tokenFor("anexo-e2e-hist@example.com");
        completeProfile(token);
        String voyageId = createFinishedVoyage(token);

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"voyageIds":["%s"]}
                      """.formatted(voyageId))
                .when().post("/api/reports/anexo-1s?format=JSON").then().statusCode(200);

        given().auth().oauth2(token).when().get("/api/reports/history")
                .then().statusCode(200)
                .body("type", hasItem("ANEXO_1S"));
    }

    @Test
    void voyageOfAnotherUserReturns404() {
        String owner = tokenFor("anexo-e2e-owner@example.com");
        completeProfile(owner);
        String othersVoyage = createFinishedVoyage(owner);

        String intruder = tokenFor("anexo-e2e-intruder@example.com");
        completeProfile(intruder);

        given().auth().oauth2(intruder).contentType(ContentType.JSON)
                .body("""
                      {"voyageIds":["%s"]}
                      """.formatted(othersVoyage))
                .when().post("/api/reports/anexo-1s")
                .then().statusCode(404);
    }
}

package br.dev.irontech.seavault.alerts.api;

import br.dev.irontech.seavault.alerts.service.AlertService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
class AlertResourceTest {

    @Inject
    AlertService alertService;

    private String tokenFor(String email) {
        given().contentType(ContentType.JSON)
                .body("""
                      {"name":"Alert E2E","email":"%s","password":"senha1234","acceptTerms":true}
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

    // Cria documento a vencer e dispara o scan (scheduler off em teste) para semear um alerta do usuario.
    private void seedAlert(String token) {
        String soon = LocalDate.now().plusDays(10).toString();
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"typeId":"%s","number":"123","issuer":"DPC","expiryDate":"%s"}
                      """.formatted(documentTypeId(), soon))
                .when().post("/api/documents").then().statusCode(201);
        alertService.runDailyScan();
    }

    @Test
    void listRequiresAuth() {
        given().when().get("/api/alerts").then().statusCode(401);
    }

    @Test
    void listAndPatchStatus() {
        String token = tokenFor("alert-e2e-crud@example.com");
        seedAlert(token);

        String alertId = given().auth().oauth2(token)
                .when().get("/api/alerts?status=PENDENTE")
                .then().statusCode(200)
                .body("totalElements", greaterThanOrEqualTo(1))
                .extract().path("content[0].id");

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"status":"LIDO"}
                      """)
                .when().patch("/api/alerts/" + alertId)
                .then().statusCode(200)
                .body("status", equalTo("LIDO"));
    }

    @Test
    void patchToPendenteReturns400() {
        String token = tokenFor("alert-e2e-bad@example.com");
        seedAlert(token);
        String alertId = given().auth().oauth2(token)
                .when().get("/api/alerts").then().statusCode(200)
                .extract().path("content[0].id");

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"status":"PENDENTE"}
                      """)
                .when().patch("/api/alerts/" + alertId)
                .then().statusCode(400)
                .body("code", equalTo("VALIDATION"));
    }

    @Test
    void alertsAreIsolatedPerUser() {
        String tokenA = tokenFor("alert-e2e-iso-a@example.com");
        String tokenB = tokenFor("alert-e2e-iso-b@example.com");
        seedAlert(tokenA);
        String alertOfA = given().auth().oauth2(tokenA)
                .when().get("/api/alerts").then().statusCode(200)
                .extract().path("content[0].id");

        given().auth().oauth2(tokenB).contentType(ContentType.JSON)
                .body("""
                      {"status":"RESOLVIDO"}
                      """)
                .when().patch("/api/alerts/" + alertOfA)
                .then().statusCode(404);
    }
}

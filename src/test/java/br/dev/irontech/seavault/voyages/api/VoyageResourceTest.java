package br.dev.irontech.seavault.voyages.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class VoyageResourceTest {

    private String tokenFor(String email) {
        given().contentType(ContentType.JSON)
                .body("""
                      {"name":"Voyage E2E","email":"%s","password":"senha1234","acceptTerms":true}
                      """.formatted(email))
                .when().post("/api/auth/register").then().statusCode(201);
        return given().contentType(ContentType.JSON)
                .body("""
                      {"email":"%s","password":"senha1234"}
                      """.formatted(email))
                .when().post("/api/auth/login").then().statusCode(200)
                .extract().path("accessToken");
    }

    private String anyNavigationTypeId() {
        return given().when().get("/api/reference/types?kind=NAVIGATION")
                .then().statusCode(200)
                .extract().path("[0].id");
    }

    private String createVoyage(String token) {
        return given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"embarkDate":"2024-01-01","disembarkDate":"2024-01-10","role":"Comandante","embarkPort":"Santos"}
                      """)
                .when().post("/api/voyages").then().statusCode(201)
                .extract().path("id");
    }

    @Test
    void createWithoutTokenReturns401() {
        given().contentType(ContentType.JSON).body("{}")
                .when().post("/api/voyages").then().statusCode(401);
    }

    @Test
    void createWithoutEmbarkDateReturns400() {
        String token = tokenFor("voyage-noembark@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"role":"Comandante"}
                      """)
                .when().post("/api/voyages").then().statusCode(400)
                .body("code", equalTo("VALIDATION"));
    }

    @Test
    void createWithDisembarkBeforeEmbarkReturns422() {
        String token = tokenFor("voyage-baddate@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"embarkDate":"2024-01-10","disembarkDate":"2024-01-01"}
                      """)
                .when().post("/api/voyages").then().statusCode(422)
                .body("code", equalTo("BUSINESS_RULE"));
    }

    @Test
    void createWithNavigationTypePersists() {
        String token = tokenFor("voyage-nav@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"embarkDate":"2024-01-01","disembarkDate":"2024-01-10","navigationTypeId":"%s"}
                      """.formatted(anyNavigationTypeId()))
                .when().post("/api/voyages").then().statusCode(201)
                .body("id", notNullValue())
                .body("status", equalTo("FINISHED"))
                .body("effectiveDays", equalTo(10));
    }

    @Test
    void createWithUnknownVesselReturns404() {
        String token = tokenFor("voyage-badvessel@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"embarkDate":"2024-01-01","vesselId":"00000000-0000-0000-0000-000000000000"}
                      """)
                .when().post("/api/voyages").then().statusCode(404)
                .body("code", equalTo("NOT_FOUND"));
    }

    @Test
    void crudLifecycle() {
        String token = tokenFor("voyage-crud@example.com");
        String id = createVoyage(token);

        given().auth().oauth2(token).when().get("/api/voyages/" + id)
                .then().statusCode(200).body("embarkPort", equalTo("Santos"));

        given().auth().oauth2(token).when().get("/api/voyages")
                .then().statusCode(200).body("totalElements", equalTo(1));

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"embarkDate":"2024-02-01","disembarkDate":"2024-02-05","role":"Imediato"}
                      """)
                .when().put("/api/voyages/" + id).then().statusCode(200)
                .body("role", equalTo("Imediato"))
                .body("effectiveDays", equalTo(5));

        given().auth().oauth2(token).when().delete("/api/voyages/" + id).then().statusCode(204);
        given().auth().oauth2(token).when().get("/api/voyages/" + id).then().statusCode(404);
    }

    @Test
    void voyagesAreIsolatedPerUser() {
        String tokenA = tokenFor("voyage-iso-a@example.com");
        String tokenB = tokenFor("voyage-iso-b@example.com");
        String idOfA = createVoyage(tokenA);

        given().auth().oauth2(tokenB).when().get("/api/voyages/" + idOfA).then().statusCode(404);
    }

    @Test
    void attachListAndDetachFile() {
        String token = tokenFor("voyage-files@example.com");
        String voyageId = createVoyage(token);

        String fileId = given().auth().oauth2(token)
                .multiPart("file", "comprovante.pdf", "conteudo".getBytes(), "application/pdf")
                .when().post("/api/files").then().statusCode(201)
                .extract().path("id");

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"fileId":"%s"}
                      """.formatted(fileId))
                .when().post("/api/voyages/" + voyageId + "/files").then().statusCode(204);

        given().auth().oauth2(token).when().get("/api/voyages/" + voyageId + "/files")
                .then().statusCode(200).body("[0].id", equalTo(fileId));

        given().auth().oauth2(token).when().delete("/api/voyages/" + voyageId + "/files/" + fileId)
                .then().statusCode(204);
    }
}

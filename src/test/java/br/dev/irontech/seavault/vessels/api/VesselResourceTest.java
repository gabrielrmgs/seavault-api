package br.dev.irontech.seavault.vessels.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class VesselResourceTest {

    private String tokenFor(String email) {
        given().contentType(ContentType.JSON)
                .body("""
                      {"name":"Vessel E2E","email":"%s","password":"senha1234","acceptTerms":true}
                      """.formatted(email))
                .when().post("/api/auth/register").then().statusCode(201);
        return given().contentType(ContentType.JSON)
                .body("""
                      {"email":"%s","password":"senha1234"}
                      """.formatted(email))
                .when().post("/api/auth/login").then().statusCode(200)
                .extract().path("accessToken");
    }

    private String anyVesselTypeId() {
        return given().when().get("/api/reference/types?kind=VESSEL")
                .then().statusCode(200)
                .extract().path("[0].id");
    }

    private String createVessel(String token) {
        return given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"name":"Cargueiro X","imo":"IMO 1234567","flag":"Brasil","grossTonnage":50000.00}
                      """)
                .when().post("/api/vessels").then().statusCode(201)
                .extract().path("id");
    }

    @Test
    void createWithoutTokenReturns401() {
        given().contentType(ContentType.JSON).body("{}")
                .when().post("/api/vessels").then().statusCode(401);
    }

    @Test
    void createWithTypePersists() {
        String token = tokenFor("vessel-create@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"name":"Petroleiro Y","typeId":"%s"}
                      """.formatted(anyVesselTypeId()))
                .when().post("/api/vessels").then().statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Petroleiro Y"));
    }

    @Test
    void createWithoutNameReturns400() {
        String token = tokenFor("vessel-noname@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"flag":"Brasil"}
                      """)
                .when().post("/api/vessels").then().statusCode(400)
                .body("code", equalTo("VALIDATION"));
    }

    @Test
    void createWithUnknownTypeReturns404() {
        String token = tokenFor("vessel-badtype@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"name":"Z","typeId":"00000000-0000-0000-0000-000000000000"}
                      """)
                .when().post("/api/vessels").then().statusCode(404)
                .body("code", equalTo("NOT_FOUND"));
    }

    @Test
    void crudLifecycle() {
        String token = tokenFor("vessel-crud@example.com");
        String id = createVessel(token);

        given().auth().oauth2(token).when().get("/api/vessels/" + id)
                .then().statusCode(200).body("flag", equalTo("Brasil"));

        given().auth().oauth2(token).when().get("/api/vessels")
                .then().statusCode(200).body("totalElements", equalTo(1));

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"name":"Cargueiro Renomeado"}
                      """)
                .when().put("/api/vessels/" + id).then().statusCode(200)
                .body("name", equalTo("Cargueiro Renomeado"));

        given().auth().oauth2(token).when().delete("/api/vessels/" + id).then().statusCode(204);
        given().auth().oauth2(token).when().get("/api/vessels/" + id).then().statusCode(404);
    }

    @Test
    void vesselsAreIsolatedPerUser() {
        String tokenA = tokenFor("vessel-iso-a@example.com");
        String tokenB = tokenFor("vessel-iso-b@example.com");
        String idOfA = createVessel(tokenA);

        given().auth().oauth2(tokenB).when().get("/api/vessels/" + idOfA).then().statusCode(404);
    }
}

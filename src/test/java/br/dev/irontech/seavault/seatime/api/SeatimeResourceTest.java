package br.dev.irontech.seavault.seatime.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class SeatimeResourceTest {

    private String tokenFor(String email) {
        given().contentType(ContentType.JSON)
                .body("""
                      {"name":"Seatime E2E","email":"%s","password":"senha1234","acceptTerms":true}
                      """.formatted(email))
                .when().post("/api/auth/register").then().statusCode(201);
        return given().contentType(ContentType.JSON)
                .body("""
                      {"email":"%s","password":"senha1234"}
                      """.formatted(email))
                .when().post("/api/auth/login").then().statusCode(200)
                .extract().path("accessToken");
    }

    @Test
    void summaryWithoutTokenReturns401() {
        given().when().get("/api/seatime").then().statusCode(401);
    }

    @Test
    void summaryReflectsFinishedVoyage() {
        String token = tokenFor("seatime-e2e@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"embarkDate":"2024-01-01","disembarkDate":"2024-01-10","role":"Comandante"}
                      """)
                .when().post("/api/voyages").then().statusCode(201);

        given().auth().oauth2(token).when().get("/api/seatime")
                .then().statusCode(200)
                .body("totalDays", equalTo(10))
                .body("byRole[0].role", equalTo("Comandante"));
    }
}

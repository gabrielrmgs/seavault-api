package br.dev.irontech.seavault.dashboard.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class DashboardResourceTest {

    private String tokenFor(String email) {
        given().contentType(ContentType.JSON)
                .body("""
                      {"name":"Dash E2E","email":"%s","password":"senha1234","acceptTerms":true}
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
    void requiresAuth() {
        given().when().get("/api/dashboard").then().statusCode(401);
    }

    @Test
    void returnsConsolidatedSummary() {
        String token = tokenFor("dash-e2e@example.com");
        given().auth().oauth2(token).when().get("/api/dashboard")
                .then().statusCode(200)
                .body("documents", notNullValue())
                .body("certificates", notNullValue())
                .body("seatime", notNullValue())
                .body("courses", notNullValue())
                .body("upcomingAlerts", notNullValue());
    }
}

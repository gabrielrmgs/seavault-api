package br.dev.irontech.seavault.auth.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class AuthResourceTest {

    @Test
    void registerThenLoginFlow() {
        String email = "e2e@example.com";

        given().contentType("application/json")
                .body("""
                      {"name":"E2E","email":"%s","password":"senha1234","acceptTerms":true}
                      """.formatted(email))
                .when().post("/api/auth/register")
                .then().statusCode(201)
                .body("email", notNullValue());

        given().contentType("application/json")
                .body("""
                      {"email":"%s","password":"senha1234"}
                      """.formatted(email))
                .when().post("/api/auth/login")
                .then().statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    void registerWithInvalidBodyReturns400() {
        given().contentType("application/json")
                .body("""
                      {"name":"","email":"naoemail","password":"123","acceptTerms":false}
                      """)
                .when().post("/api/auth/register")
                .then().statusCode(400)
                .body("code", org.hamcrest.Matchers.equalTo("VALIDATION"));
    }

    @Test
    void duplicateRegisterReturns409() {
        String body = """
                      {"name":"Dup","email":"dupe2e@example.com","password":"senha1234","acceptTerms":true}
                      """;
        given().contentType("application/json").body(body).when().post("/api/auth/register").then().statusCode(201);
        given().contentType("application/json").body(body).when().post("/api/auth/register")
                .then().statusCode(409)
                .body("code", org.hamcrest.Matchers.equalTo("CONFLICT"));
    }
}

package br.dev.irontech.seavault.profile.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class ProfileResourceTest {

    /** Registra e loga um usuário, devolvendo o accessToken. */
    private String tokenFor(String email) {
        given().contentType(ContentType.JSON)
                .body("""
                      {"name":"Perfil E2E","email":"%s","password":"senha1234","acceptTerms":true}
                      """.formatted(email))
                .when().post("/api/auth/register")
                .then().statusCode(201);

        return given().contentType(ContentType.JSON)
                .body("""
                      {"email":"%s","password":"senha1234"}
                      """.formatted(email))
                .when().post("/api/auth/login")
                .then().statusCode(200)
                .extract().path("accessToken");
    }

    @Test
    void getWithoutTokenReturns401() {
        given().when().get("/api/profile").then().statusCode(401);
    }

    @Test
    void getCreatesEmptyProfileForOwner() {
        String token = tokenFor("profile-get@example.com");
        given().auth().oauth2(token)
                .when().get("/api/profile")
                .then().statusCode(200)
                .body("userId", notNullValue())
                .body("completionPercent", equalTo(0));
    }

    @Test
    void updateThenGetReturnsUpdatedData() {
        String token = tokenFor("profile-update@example.com");

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"cir":"12345","cpf":"00000000000","nationality":"Brasileira"}
                      """)
                .when().put("/api/profile")
                .then().statusCode(200)
                .body("cir", equalTo("12345"))
                .body("completionPercent", equalTo(43)); // 3 de 7 campos core

        given().auth().oauth2(token)
                .when().get("/api/profile")
                .then().statusCode(200)
                .body("cir", equalTo("12345"))
                .body("nationality", equalTo("Brasileira"));
    }

    @Test
    void updateWithUnknownCategoryReturns404() {
        String token = tokenFor("profile-badcat@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"categoryId":"00000000-0000-0000-0000-000000000000"}
                      """)
                .when().put("/api/profile")
                .then().statusCode(404)
                .body("code", equalTo("NOT_FOUND"));
    }

    @Test
    void profilesAreIsolatedPerUser() {
        String tokenA = tokenFor("isolation-a@example.com");
        String tokenB = tokenFor("isolation-b@example.com");

        given().auth().oauth2(tokenA).contentType(ContentType.JSON)
                .body("""
                      {"cir":"AAA-111"}
                      """)
                .when().put("/api/profile").then().statusCode(200);

        // B nunca enxerga o dado de A
        given().auth().oauth2(tokenB)
                .when().get("/api/profile")
                .then().statusCode(200)
                .body("cir", nullValue());
    }
}

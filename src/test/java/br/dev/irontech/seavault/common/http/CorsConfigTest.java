package br.dev.irontech.seavault.common.http;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class CorsConfigTest {

    @Test
    void allowsConfiguredOrigin() {
        given()
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .when().options("/api/auth/login")
                .then()
                .statusCode(200)
                .header("access-control-allow-origin", equalTo("http://localhost:3000"));
    }
}

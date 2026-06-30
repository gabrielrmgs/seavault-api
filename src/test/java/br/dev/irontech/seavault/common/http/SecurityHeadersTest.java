package br.dev.irontech.seavault.common.http;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class SecurityHeadersTest {

    @Test
    void securityHeadersPresent() {
        given()
                .contentType("application/json")
                .body("{}")
                .when().post("/api/auth/login")
                .then()
                .header("X-Content-Type-Options", equalTo("nosniff"))
                .header("X-Frame-Options", equalTo("DENY"))
                .header("Referrer-Policy", equalTo("strict-origin-when-cross-origin"));
    }
}

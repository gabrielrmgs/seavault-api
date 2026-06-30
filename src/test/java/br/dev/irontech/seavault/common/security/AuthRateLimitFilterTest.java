package br.dev.irontech.seavault.common.security;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestProfile(AuthRateLimitFilterTest.RateLimitProfile.class)
class AuthRateLimitFilterTest {

    @Test
    void blocksAfterTooManyLoginAttempts() {
        String body = "{\"email\":\"ratelimit@example.com\",\"password\":\"wrongpass123\"}";

        for (int i = 0; i < 5; i++) {
            given().contentType("application/json").body(body)
                    .when().post("/api/auth/login")
                    .then().statusCode(401);
        }

        given().contentType("application/json").body(body)
                .when().post("/api/auth/login")
                .then().statusCode(429);
    }

    @Test
    void doesNotTrustForwardedForFromDirectClients() {
        String body = "{\"email\":\"spoof-" + System.nanoTime() + "@example.com\"}";

        for (int i = 0; i < 5; i++) {
            given().header("X-Forwarded-For", "203.0.113." + i)
                    .contentType("application/json").body(body)
                    .when().post("/api/auth/request-password-reset")
                    .then().statusCode(202);
        }

        given().header("X-Forwarded-For", "203.0.113.250")
                .contentType("application/json").body(body)
                .when().post("/api/auth/request-password-reset")
                .then().statusCode(429);
    }

    public static class RateLimitProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "seavault.auth.rate-limit.max-attempts", "5",
                    "seavault.auth.rate-limit.trusted-proxies", "");
        }
    }
}

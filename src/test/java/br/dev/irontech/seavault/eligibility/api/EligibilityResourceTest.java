package br.dev.irontech.seavault.eligibility.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class EligibilityResourceTest {

    private String tokenFor(String email) {
        given().contentType(ContentType.JSON)
                .body("""
                      {"name":"Elig E2E","email":"%s","password":"senha1234","acceptTerms":true}
                      """.formatted(email))
                .when().post("/api/auth/register").then().statusCode(201);
        return given().contentType(ContentType.JSON)
                .body("""
                      {"email":"%s","password":"senha1234"}
                      """.formatted(email))
                .when().post("/api/auth/login").then().statusCode(200)
                .extract().path("accessToken");
    }

    private String categoryId(String code) {
        return given().when().get("/api/reference/categories")
                .then().statusCode(200)
                .extract().path("find { it.code == '%s' }.id".formatted(code));
    }

    private String courseId(String code) {
        return given().when().get("/api/reference/course-catalog")
                .then().statusCode(200)
                .extract().path("find { it.code == '%s' }.id".formatted(code));
    }

    @Test
    void withoutTokenReturns401() {
        given().when().get("/api/eligibility").then().statusCode(401);
    }

    @Test
    void withTargetCategoryReturnsRequirements() {
        String token = tokenFor("elig-e2e-cat@example.com");
        given().auth().oauth2(token)
                .queryParam("targetCategoryId", categoryId("MOCO_CONVES"))
                .when().get("/api/eligibility")
                .then().statusCode(200)
                .body("eligible", equalTo(false))
                .body("targetName", equalTo("Moço de Convés"))
                .body("requirements.size()", equalTo(3));
    }

    @Test
    void noParamUsesProfileTarget() {
        String token = tokenFor("elig-e2e-fallback@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"targetCategoryId":"%s"}
                      """.formatted(categoryId("MOCO_CONVES")))
                .when().put("/api/profile").then().statusCode(200);

        given().auth().oauth2(token)
                .when().get("/api/eligibility")
                .then().statusCode(200)
                .body("targetCategoryId", notNullValue())
                .body("requirements.size()", equalTo(3));
    }

    @Test
    void bothParamsReturns400() {
        String token = tokenFor("elig-e2e-both@example.com");
        given().auth().oauth2(token)
                .queryParam("targetCategoryId", categoryId("MOCO_CONVES"))
                .queryParam("targetCourseId", courseId("CACI"))
                .when().get("/api/eligibility")
                .then().statusCode(400)
                .body("code", equalTo("VALIDATION"));
    }

    @Test
    void noTargetAndNoProfileReturns422() {
        String token = tokenFor("elig-e2e-notarget@example.com");
        given().auth().oauth2(token)
                .when().get("/api/eligibility")
                .then().statusCode(422)
                .body("code", equalTo("BUSINESS_RULE"));
    }

    @Test
    void unknownTargetReturns404() {
        String token = tokenFor("elig-e2e-unknown@example.com");
        given().auth().oauth2(token)
                .queryParam("targetCategoryId", "00000000-0000-0000-0000-000000000000")
                .when().get("/api/eligibility")
                .then().statusCode(404)
                .body("code", equalTo("NOT_FOUND"));
    }
}

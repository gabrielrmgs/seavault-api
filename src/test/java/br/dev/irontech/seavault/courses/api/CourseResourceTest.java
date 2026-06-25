package br.dev.irontech.seavault.courses.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class CourseResourceTest {

    private String tokenFor(String email) {
        given().contentType(ContentType.JSON)
                .body("""
                      {"name":"Course E2E","email":"%s","password":"senha1234","acceptTerms":true}
                      """.formatted(email))
                .when().post("/api/auth/register").then().statusCode(201);
        return given().contentType(ContentType.JSON)
                .body("""
                      {"email":"%s","password":"senha1234"}
                      """.formatted(email))
                .when().post("/api/auth/login").then().statusCode(200)
                .extract().path("accessToken");
    }

    private String createCourse(String token) {
        return given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"name":"CFAQ","institution":"EFOMM","modality":"PRESENCIAL","workloadHours":200}
                      """)
                .when().post("/api/courses").then().statusCode(201)
                .extract().path("id");
    }

    private String uploadFile(String token) {
        return given().auth().oauth2(token)
                .multiPart("file", "curso.pdf", "%PDF-1.4 x".getBytes(StandardCharsets.UTF_8), "application/pdf")
                .when().post("/api/files").then().statusCode(201)
                .extract().path("id");
    }

    @Test
    void createWithoutTokenReturns401() {
        given().contentType(ContentType.JSON).body("{}")
                .when().post("/api/courses").then().statusCode(401);
    }

    @Test
    void createDefaultsStatusToPlanejado() {
        String token = tokenFor("course-create@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"name":"CBSP"}
                      """)
                .when().post("/api/courses").then().statusCode(201)
                .body("id", notNullValue())
                .body("status", equalTo("PLANEJADO"));
    }

    @Test
    void createWithoutNameReturns400() {
        String token = tokenFor("course-noname@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"institution":"X"}
                      """)
                .when().post("/api/courses").then().statusCode(400)
                .body("code", equalTo("VALIDATION"));
    }

    @Test
    void crudAndAttachLifecycle() {
        String token = tokenFor("course-crud@example.com");
        String id = createCourse(token);

        given().auth().oauth2(token).when().get("/api/courses/" + id)
                .then().statusCode(200).body("workloadHours", equalTo(200));

        given().auth().oauth2(token).when().get("/api/courses")
                .then().statusCode(200).body("totalElements", equalTo(1));

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"name":"CFAQ","status":"CONCLUIDO"}
                      """)
                .when().put("/api/courses/" + id).then().statusCode(200)
                .body("status", equalTo("CONCLUIDO"));

        String fileId = uploadFile(token);
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"fileId":"%s"}
                      """.formatted(fileId))
                .when().post("/api/courses/" + id + "/files").then().statusCode(204);
        given().auth().oauth2(token).when().get("/api/courses/" + id + "/files")
                .then().statusCode(200).body("size()", equalTo(1));

        given().auth().oauth2(token).when().delete("/api/courses/" + id).then().statusCode(204);
        given().auth().oauth2(token).when().get("/api/courses/" + id).then().statusCode(404);
    }

    @Test
    void coursesAreIsolatedPerUser() {
        String tokenA = tokenFor("course-iso-a@example.com");
        String tokenB = tokenFor("course-iso-b@example.com");
        String idOfA = createCourse(tokenA);

        given().auth().oauth2(tokenB).when().get("/api/courses/" + idOfA).then().statusCode(404);
    }
}

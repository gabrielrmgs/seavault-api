package br.dev.irontech.seavault.files.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class FileResourceTest {

    private String tokenFor(String email) {
        given().contentType(ContentType.JSON)
                .body("""
                      {"name":"Files E2E","email":"%s","password":"senha1234","acceptTerms":true}
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

    private byte[] pdf() {
        return "%PDF-1.4 conteudo".getBytes(StandardCharsets.UTF_8);
    }

    private String uploadAndGetId(String token, String fileName) {
        return given().auth().oauth2(token)
                .multiPart("file", fileName, pdf(), "application/pdf")
                .when().post("/api/files")
                .then().statusCode(201)
                .extract().path("id");
    }

    @Test
    void uploadWithoutTokenReturns401() {
        given().multiPart("file", "x.pdf", pdf(), "application/pdf")
                .when().post("/api/files")
                .then().statusCode(401);
    }

    @Test
    void uploadReturnsCreatedMetadata() {
        String token = tokenFor("files-upload@example.com");
        given().auth().oauth2(token)
                .multiPart("file", "cir.pdf", pdf(), "application/pdf")
                .when().post("/api/files")
                .then().statusCode(201)
                .body("id", notNullValue())
                .body("originalName", equalTo("cir.pdf"))
                .body("contentType", equalTo("application/pdf"))
                .body("sizeBytes", equalTo(pdf().length))
                .body("sha256", notNullValue());
    }

    @Test
    void uploadRejectsDisallowedType() {
        String token = tokenFor("files-badtype@example.com");
        given().auth().oauth2(token)
                .multiPart("file", "note.txt", "hello".getBytes(StandardCharsets.UTF_8), "text/plain")
                .when().post("/api/files")
                .then().statusCode(422)
                .body("code", equalTo("BUSINESS_RULE"));
    }

    @Test
    void getMetadataAndDownloadContent() {
        String token = tokenFor("files-download@example.com");
        String id = uploadAndGetId(token, "doc.pdf");

        given().auth().oauth2(token)
                .when().get("/api/files/" + id)
                .then().statusCode(200)
                .body("originalName", equalTo("doc.pdf"));

        byte[] body = given().auth().oauth2(token)
                .when().get("/api/files/" + id + "/content")
                .then().statusCode(200)
                .header("Content-Disposition", containsString("doc.pdf"))
                .extract().asByteArray();
        org.junit.jupiter.api.Assertions.assertArrayEquals(pdf(), body);
    }

    @Test
    void listReturnsUploadedFiles() {
        String token = tokenFor("files-list@example.com");
        uploadAndGetId(token, "a.pdf");
        uploadAndGetId(token, "b.pdf");

        given().auth().oauth2(token)
                .when().get("/api/files")
                .then().statusCode(200)
                .body("totalElements", equalTo(2))
                .body("content.size()", equalTo(2));
    }

    @Test
    void deleteThenGetReturns404() {
        String token = tokenFor("files-delete@example.com");
        String id = uploadAndGetId(token, "tmp.pdf");

        given().auth().oauth2(token).when().delete("/api/files/" + id).then().statusCode(204);
        given().auth().oauth2(token).when().get("/api/files/" + id).then().statusCode(404);
        given().auth().oauth2(token).when().get("/api/files/" + id + "/content").then().statusCode(404);
    }

    @Test
    void filesAreIsolatedPerUser() {
        String tokenA = tokenFor("files-iso-a@example.com");
        String tokenB = tokenFor("files-iso-b@example.com");
        String idOfA = uploadAndGetId(tokenA, "secret.pdf");

        given().auth().oauth2(tokenB).when().get("/api/files/" + idOfA).then().statusCode(404);
        given().auth().oauth2(tokenB).when().get("/api/files/" + idOfA + "/content").then().statusCode(404);
    }
}

package br.dev.irontech.seavault.documents.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class DocumentResourceTest {

    private String tokenFor(String email) {
        given().contentType(ContentType.JSON)
                .body("""
                      {"name":"Doc E2E","email":"%s","password":"senha1234","acceptTerms":true}
                      """.formatted(email))
                .when().post("/api/auth/register").then().statusCode(201);
        return given().contentType(ContentType.JSON)
                .body("""
                      {"email":"%s","password":"senha1234"}
                      """.formatted(email))
                .when().post("/api/auth/login").then().statusCode(200)
                .extract().path("accessToken");
    }

    private String anyDocumentTypeId() {
        return given().when().get("/api/reference/types?kind=DOCUMENT")
                .then().statusCode(200)
                .extract().path("[0].id");
    }

    private String createDoc(String token, String typeId) {
        return given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"typeId":"%s","number":"REG-1","issuer":"Marinha","expiryDate":"2020-01-01"}
                      """.formatted(typeId))
                .when().post("/api/documents").then().statusCode(201)
                .extract().path("id");
    }

    private String uploadFile(String token) {
        return given().auth().oauth2(token)
                .multiPart("file", "anexo.pdf", "%PDF-1.4 x".getBytes(StandardCharsets.UTF_8), "application/pdf")
                .when().post("/api/files").then().statusCode(201)
                .extract().path("id");
    }

    @Test
    void createWithoutTokenReturns401() {
        given().contentType(ContentType.JSON).body("{}")
                .when().post("/api/documents").then().statusCode(401);
    }

    @Test
    void createReturnsDerivedStatus() {
        String token = tokenFor("doc-create@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"typeId":"%s","number":"REG-9","expiryDate":"2020-01-01"}
                      """.formatted(anyDocumentTypeId()))
                .when().post("/api/documents").then().statusCode(201)
                .body("id", notNullValue())
                .body("number", equalTo("REG-9"))
                .body("status", equalTo("VENCIDO"));
    }

    @Test
    void createWithoutTypeReturns400() {
        String token = tokenFor("doc-notype@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"number":"X"}
                      """)
                .when().post("/api/documents").then().statusCode(400)
                .body("code", equalTo("VALIDATION"));
    }

    @Test
    void createWithUnknownTypeReturns404() {
        String token = tokenFor("doc-unknowntype@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"typeId":"00000000-0000-0000-0000-000000000000"}
                      """)
                .when().post("/api/documents").then().statusCode(404)
                .body("code", equalTo("NOT_FOUND"));
    }

    @Test
    void crudLifecycle() {
        String token = tokenFor("doc-crud@example.com");
        String type = anyDocumentTypeId();
        String id = createDoc(token, type);

        given().auth().oauth2(token).when().get("/api/documents/" + id)
                .then().statusCode(200).body("issuer", equalTo("Marinha"));

        given().auth().oauth2(token).when().get("/api/documents")
                .then().statusCode(200).body("totalElements", equalTo(1));

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"typeId":"%s","number":"REG-2"}
                      """.formatted(type))
                .when().put("/api/documents/" + id).then().statusCode(200)
                .body("number", equalTo("REG-2"));

        given().auth().oauth2(token).when().delete("/api/documents/" + id).then().statusCode(204);
        given().auth().oauth2(token).when().get("/api/documents/" + id).then().statusCode(404);
    }

    @Test
    void attachListDetachFiles() {
        String token = tokenFor("doc-files@example.com");
        String id = createDoc(token, anyDocumentTypeId());
        String fileId = uploadFile(token);

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"fileId":"%s"}
                      """.formatted(fileId))
                .when().post("/api/documents/" + id + "/files").then().statusCode(204);

        given().auth().oauth2(token).when().get("/api/documents/" + id + "/files")
                .then().statusCode(200).body("size()", equalTo(1)).body("[0].id", equalTo(fileId));

        given().auth().oauth2(token).when().delete("/api/documents/" + id + "/files/" + fileId)
                .then().statusCode(204);
        given().auth().oauth2(token).when().get("/api/documents/" + id + "/files")
                .then().statusCode(200).body("size()", equalTo(0));
    }

    @Test
    void documentsAreIsolatedPerUser() {
        String tokenA = tokenFor("doc-iso-a@example.com");
        String tokenB = tokenFor("doc-iso-b@example.com");
        String idOfA = createDoc(tokenA, anyDocumentTypeId());

        given().auth().oauth2(tokenB).when().get("/api/documents/" + idOfA).then().statusCode(404);
    }
}

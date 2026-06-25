package br.dev.irontech.seavault.certificates.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class CertificateResourceTest {

    private String tokenFor(String email) {
        given().contentType(ContentType.JSON)
                .body("""
                      {"name":"Cert E2E","email":"%s","password":"senha1234","acceptTerms":true}
                      """.formatted(email))
                .when().post("/api/auth/register").then().statusCode(201);
        return given().contentType(ContentType.JSON)
                .body("""
                      {"email":"%s","password":"senha1234"}
                      """.formatted(email))
                .when().post("/api/auth/login").then().statusCode(200)
                .extract().path("accessToken");
    }

    private String createCert(String token) {
        return given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"name":"Basic Safety","code":"STCW","institution":"CIAGA","expiryDate":"2099-01-01"}
                      """)
                .when().post("/api/certificates").then().statusCode(201)
                .extract().path("id");
    }

    private String uploadFile(String token) {
        return given().auth().oauth2(token)
                .multiPart("file", "cert.pdf", "%PDF-1.4 x".getBytes(StandardCharsets.UTF_8), "application/pdf")
                .when().post("/api/files").then().statusCode(201)
                .extract().path("id");
    }

    @Test
    void createWithoutTokenReturns401() {
        given().contentType(ContentType.JSON).body("{}")
                .when().post("/api/certificates").then().statusCode(401);
    }

    @Test
    void createReturnsValidStatus() {
        String token = tokenFor("cert-create@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"name":"BST","expiryDate":"2099-01-01"}
                      """)
                .when().post("/api/certificates").then().statusCode(201)
                .body("id", notNullValue())
                .body("status", equalTo("VALIDO"));
    }

    @Test
    void createWithoutNameReturns400() {
        String token = tokenFor("cert-noname@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"code":"X"}
                      """)
                .when().post("/api/certificates").then().statusCode(400)
                .body("code", equalTo("VALIDATION"));
    }

    @Test
    void crudAndAttachLifecycle() {
        String token = tokenFor("cert-crud@example.com");
        String id = createCert(token);

        given().auth().oauth2(token).when().get("/api/certificates/" + id)
                .then().statusCode(200).body("institution", equalTo("CIAGA"));

        given().auth().oauth2(token).when().get("/api/certificates")
                .then().statusCode(200).body("totalElements", equalTo(1));

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"name":"BST Renovado"}
                      """)
                .when().put("/api/certificates/" + id).then().statusCode(200)
                .body("name", equalTo("BST Renovado"));

        String fileId = uploadFile(token);
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"fileId":"%s"}
                      """.formatted(fileId))
                .when().post("/api/certificates/" + id + "/files").then().statusCode(204);
        given().auth().oauth2(token).when().get("/api/certificates/" + id + "/files")
                .then().statusCode(200).body("size()", equalTo(1));

        given().auth().oauth2(token).when().delete("/api/certificates/" + id).then().statusCode(204);
        given().auth().oauth2(token).when().get("/api/certificates/" + id).then().statusCode(404);
    }

    @Test
    void certificatesAreIsolatedPerUser() {
        String tokenA = tokenFor("cert-iso-a@example.com");
        String tokenB = tokenFor("cert-iso-b@example.com");
        String idOfA = createCert(tokenA);

        given().auth().oauth2(tokenB).when().get("/api/certificates/" + idOfA).then().statusCode(404);
    }
}

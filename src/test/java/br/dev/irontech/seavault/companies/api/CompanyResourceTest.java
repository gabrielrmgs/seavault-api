package br.dev.irontech.seavault.companies.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class CompanyResourceTest {

    private String tokenFor(String email) {
        given().contentType(ContentType.JSON)
                .body("""
                      {"name":"Company E2E","email":"%s","password":"senha1234","acceptTerms":true}
                      """.formatted(email))
                .when().post("/api/auth/register").then().statusCode(201);
        return given().contentType(ContentType.JSON)
                .body("""
                      {"email":"%s","password":"senha1234"}
                      """.formatted(email))
                .when().post("/api/auth/login").then().statusCode(200)
                .extract().path("accessToken");
    }

    private String anyCompanyTypeId() {
        return given().when().get("/api/reference/types?kind=COMPANY")
                .then().statusCode(200)
                .extract().path("[0].id");
    }

    private String createCompany(String token) {
        return given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"name":"Armadora X","cnpj":"12.345.678/0001-99","email":"rh@armadora.com","phone":"+55 21 99999-0000"}
                      """)
                .when().post("/api/companies").then().statusCode(201)
                .extract().path("id");
    }

    @Test
    void createWithoutTokenReturns401() {
        given().contentType(ContentType.JSON).body("{}")
                .when().post("/api/companies").then().statusCode(401);
    }

    @Test
    void createWithTypePersists() {
        String token = tokenFor("company-create@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"name":"Agencia Y","typeId":"%s"}
                      """.formatted(anyCompanyTypeId()))
                .when().post("/api/companies").then().statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Agencia Y"));
    }

    @Test
    void createWithoutNameReturns400() {
        String token = tokenFor("company-noname@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"cnpj":"12.345.678/0001-99"}
                      """)
                .when().post("/api/companies").then().statusCode(400)
                .body("code", equalTo("VALIDATION"));
    }

    @Test
    void createWithInvalidEmailReturns400() {
        String token = tokenFor("company-bademail@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"name":"Z","email":"nao-eh-email"}
                      """)
                .when().post("/api/companies").then().statusCode(400)
                .body("code", equalTo("VALIDATION"));
    }

    @Test
    void createWithUnknownTypeReturns404() {
        String token = tokenFor("company-badtype@example.com");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"name":"Z","typeId":"00000000-0000-0000-0000-000000000000"}
                      """)
                .when().post("/api/companies").then().statusCode(404)
                .body("code", equalTo("NOT_FOUND"));
    }

    @Test
    void crudLifecycle() {
        String token = tokenFor("company-crud@example.com");
        String id = createCompany(token);

        given().auth().oauth2(token).when().get("/api/companies/" + id)
                .then().statusCode(200).body("cnpj", equalTo("12.345.678/0001-99"));

        given().auth().oauth2(token).when().get("/api/companies")
                .then().statusCode(200).body("totalElements", equalTo(1));

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("""
                      {"name":"Armadora Renomeada"}
                      """)
                .when().put("/api/companies/" + id).then().statusCode(200)
                .body("name", equalTo("Armadora Renomeada"));

        given().auth().oauth2(token).when().delete("/api/companies/" + id).then().statusCode(204);
        given().auth().oauth2(token).when().get("/api/companies/" + id).then().statusCode(404);
    }

    @Test
    void companiesAreIsolatedPerUser() {
        String tokenA = tokenFor("company-iso-a@example.com");
        String tokenB = tokenFor("company-iso-b@example.com");
        String idOfA = createCompany(tokenA);

        given().auth().oauth2(tokenB).when().get("/api/companies/" + idOfA).then().statusCode(404);
    }
}

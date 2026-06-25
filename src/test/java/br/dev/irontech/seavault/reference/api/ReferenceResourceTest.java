package br.dev.irontech.seavault.reference.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;

@QuarkusTest
class ReferenceResourceTest {

    @Test
    void listsGroups() {
        given().when().get("/api/reference/groups")
                .then().statusCode(200)
                .body("size()", equalTo(5))
                .body("code", hasItem("MARITIMOS"));
    }

    @Test
    void listsCoursesAndTypes() {
        given().when().get("/api/reference/course-catalog")
                .then().statusCode(200)
                .body("size()", equalTo(5));

        given().queryParam("kind", "document")
                .when().get("/api/reference/types")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(5))
                .body("kind", hasItem("DOCUMENT"));
    }

    @Test
    void typesWithoutKindReturns422() {
        given().when().get("/api/reference/types")
                .then().statusCode(422)
                .body("code", equalTo("BUSINESS_RULE"));
    }
}

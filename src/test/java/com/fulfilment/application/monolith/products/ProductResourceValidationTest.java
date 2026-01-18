package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ProductResourceValidationTest {

  @Test
  void rejectsCreateWhenIdIsProvided() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"id\":10,\"name\":\"BAD\",\"stock\":1}")
        .when()
        .post("/product")
        .then()
        .statusCode(422);
  }

  @Test
  void rejectsUpdateWhenNameMissing() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"description\":\"missing\",\"stock\":1}")
        .when()
        .put("/product/1")
        .then()
        .statusCode(422);
  }
}

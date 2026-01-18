package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProductEndpointCoverageTest {

  private static final String PATH = "product";

  @Test
  public void shouldCreateUpdateGetAndDeleteProduct() {
    Product product = new Product();
    product.name = "TEST_WIDGET";
    product.description = "Initial description";
    product.price = new BigDecimal("12.34");
    product.stock = 7;

    Long id =
        given()
            .contentType("application/json")
            .body(product)
            .when()
            .post(PATH)
            .then()
            .statusCode(201)
            .body("name", equalTo("TEST_WIDGET"))
            .extract()
            .jsonPath()
            .getLong("id");

    Product update = new Product();
    update.name = "TEST_WIDGET_UPDATED";
    update.description = "Updated description";
    update.price = new BigDecimal("45.00");
    update.stock = 11;

    given()
        .contentType("application/json")
        .body(update)
        .when()
        .put(PATH + "/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo("TEST_WIDGET_UPDATED"))
        .body("description", equalTo("Updated description"))
        .body("stock", equalTo(11));

    given()
        .when()
        .get(PATH + "/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo("TEST_WIDGET_UPDATED"));

    given().when().delete(PATH + "/" + id).then().statusCode(204);
  }

  @Test
  public void shouldRejectCreateWithPresetId() {
    Product product = new Product();
    product.id = 99L;
    product.name = "TEST_BAD_ID";

    given()
        .contentType("application/json")
        .body(product)
        .when()
        .post(PATH)
        .then()
        .statusCode(422)
        .body("error", containsString("Id was invalidly set on request."))
        .body("code", equalTo(422));
  }

  @Test
  public void shouldRejectUpdateWithoutName() {
    Product update = new Product();
    update.description = "Missing name";

    given()
        .contentType("application/json")
        .body(update)
        .when()
        .put(PATH + "/1")
        .then()
        .statusCode(422)
        .body("error", containsString("Product Name was not set on request."))
        .body("code", equalTo(422));
  }

  @Test
  public void shouldReturnNotFoundForMissingProduct() {
    given()
        .when()
        .get(PATH + "/999999")
        .then()
        .statusCode(404)
        .body("error", containsString("does not exist"))
        .body("code", equalTo(404));

    given()
        .when()
        .delete(PATH + "/999999")
        .then()
        .statusCode(404)
        .body("error", containsString("does not exist"))
        .body("code", equalTo(404));
  }
}

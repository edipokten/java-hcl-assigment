package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
public class WarehouseEndpointIT {

  private static final String PATH = "/warehouse"; // leading slash is usually safest

  @Test
  public void testSimpleListWarehouses() {
    given()
            .when()
            .get(PATH)
            .then()
            .statusCode(200)
            .body(
                    containsString("MWH.001"),
                    containsString("MWH.012"),
                    containsString("MWH.023"));
  }

  @Test
  public void testSimpleCheckingArchivingWarehouses() {
    // List all
    given()
            .when()
            .get(PATH)
            .then()
            .statusCode(200)
            .body(
                    containsString("MWH.001"),
                    containsString("MWH.012"),
                    containsString("MWH.023"),
                    containsString("ZWOLLE-001"),
                    containsString("AMSTERDAM-001"),
                    containsString("TILBURG-001"));

    // Archive the ZWOLLE-001 (id=1)
    given()
            .when()
            .delete(PATH + "/1")
            .then()
            .statusCode(204);

    // ZWOLLE-001 should be missing now
    given()
            .when()
            .get(PATH)
            .then()
            .statusCode(200)
            .body(
                    not(containsString("ZWOLLE-001")),
                    containsString("AMSTERDAM-001"),
                    containsString("TILBURG-001"));
  }
}

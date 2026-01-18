package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ProductTest {

  @Test
  void defaultConstructorLeavesNameNull() {
    Product product = new Product();

    assertNull(product.name);
  }

  @Test
  void constructorSetsName() {
    Product product = new Product("Desk");

    assertEquals("Desk", product.name);
  }
}

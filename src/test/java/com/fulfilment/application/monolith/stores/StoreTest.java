package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class StoreTest {

  @Test
  void defaultConstructorLeavesNameNull() {
    Store store = new Store();

    assertNull(store.name);
  }

  @Test
  void constructorSetsName() {
    Store store = new Store("Flagship");

    assertEquals("Flagship", store.name);
  }
}

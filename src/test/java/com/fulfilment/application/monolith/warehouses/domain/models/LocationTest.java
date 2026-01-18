package com.fulfilment.application.monolith.warehouses.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LocationTest {

  @Test
  void setsLocationFields() {
    Location location = new Location("ZWOLLE-001", 2, 50);

    assertEquals("ZWOLLE-001", location.identification);
    assertEquals(2, location.maxNumberOfWarehouses);
    assertEquals(50, location.maxCapacity);
  }
}

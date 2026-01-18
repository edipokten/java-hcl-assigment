package com.fulfilment.application.monolith.warehouses.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class WarehouseTest {

  @Test
  void allowsSettingFields() {
    Warehouse warehouse = new Warehouse();
    LocalDateTime now = LocalDateTime.now();

    warehouse.businessUnitCode = "BU";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 10;
    warehouse.stock = 2;
    warehouse.createdAt = now;
    warehouse.archivedAt = null;

    assertEquals("BU", warehouse.businessUnitCode);
    assertEquals("ZWOLLE-001", warehouse.location);
    assertEquals(10, warehouse.capacity);
    assertEquals(2, warehouse.stock);
    assertEquals(now, warehouse.createdAt);
  }
}

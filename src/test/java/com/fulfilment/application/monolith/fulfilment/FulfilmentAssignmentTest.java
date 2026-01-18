package com.fulfilment.application.monolith.fulfilment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class FulfilmentAssignmentTest {

  @Test
  void constructsAssignmentWithFields() {
    LocalDateTime now = LocalDateTime.now();
    FulfilmentAssignment assignment = new FulfilmentAssignment(1L, 2L, 3L, now);

    assertEquals(1L, assignment.storeId);
    assertEquals(2L, assignment.productId);
    assertEquals(3L, assignment.warehouseId);
    assertEquals(now, assignment.createdAt);
  }

  @Test
  void defaultConstructorLeavesCreatedAtNull() {
    FulfilmentAssignment assignment = new FulfilmentAssignment();

    assertNotNull(assignment);
  }
}

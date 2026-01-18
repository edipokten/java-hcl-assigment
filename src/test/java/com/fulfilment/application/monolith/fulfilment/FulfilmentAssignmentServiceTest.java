package com.fulfilment.application.monolith.fulfilment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.fulfilment.FulfilmentAssignmentService.FulfilmentAssignmentResponse;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class FulfilmentAssignmentServiceTest {

  @Inject FulfilmentAssignmentService service;
  @Inject FulfilmentAssignmentRepository repository;
  @Inject WarehouseRepository warehouseRepository;
  @Inject EntityManager entityManager;

  @BeforeEach
  @Transactional
  void clearAssignments() {
    repository.deleteAll();
  }

  @Test
  @TestTransaction
  void rejectsInvalidStoreId() {
    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> service.assign(null, 1L, "MWH.001"));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  @TestTransaction
  void rejectsInvalidProductId() {
    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> service.assign(1L, 0L, "MWH.001"));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  @TestTransaction
  void rejectsInvalidWarehouseCode() {
    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> service.assign(1L, 1L, " "));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  @TestTransaction
  void throwsWhenStoreMissing() {
    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> service.assign(999L, 1L, "MWH.001"));

    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  @TestTransaction
  void throwsWhenProductMissing() {
    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> service.assign(1L, 999L, "MWH.001"));

    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  @TestTransaction
  void throwsWhenWarehouseMissing() {
    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> service.assign(1L, 1L, "MWH.999"));

    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  @TestTransaction
  void rejectsDuplicateAssignments() {
    persistAssignment(1L, 1L, warehouseIdFor("MWH.001"));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> service.assign(1L, 1L, "MWH.001"));

    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  @TestTransaction
  void enforcesWarehouseLimitPerStoreProduct() {
    persistAssignment(1L, 1L, warehouseIdFor("MWH.001"));
    persistAssignment(1L, 1L, warehouseIdFor("MWH.012"));

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> service.assign(1L, 1L, "MWH.023"));

    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  @TestTransaction
  void enforcesWarehouseLimitPerStore() {
    persistAssignment(1L, 1L, warehouseIdFor("MWH.001"));
    persistAssignment(1L, 1L, warehouseIdFor("MWH.012"));
    persistAssignment(1L, 1L, warehouseIdFor("MWH.023"));

    DbWarehouse extraWarehouse = new DbWarehouse();
    extraWarehouse.businessUnitCode = "MWH.099";
    extraWarehouse.location = "ZWOLLE-002";
    extraWarehouse.capacity = 10;
    extraWarehouse.stock = 1;
    extraWarehouse.createdAt = LocalDateTime.now();
    entityManager.persist(extraWarehouse);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> service.assign(1L, 2L, "MWH.099"));

    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  @TestTransaction
  void enforcesProductLimitPerWarehouse() {
    long warehouseId = warehouseIdFor("MWH.001");
    for (int i = 0; i < 2; i++) {
      Product extra = new Product("Extra-" + i);
      extra.stock = 1;
      entityManager.persist(extra);
    }

    Product extraProduct = new Product("Extra-2");
    extraProduct.stock = 1;
    entityManager.persist(extraProduct);

    long[] productIds =
        entityManager
            .createQuery("select p.id from Product p", Long.class)
            .getResultList()
            .stream()
            .mapToLong(Long::longValue)
            .toArray();

    for (int i = 0; i < 5; i++) {
      persistAssignment(1L, productIds[i], warehouseId);
    }

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> service.assign(1L, productIds[5], "MWH.001"));

    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  @TestTransaction
  void assignsWarehouseSuccessfully() {
    FulfilmentAssignmentResponse response = service.assign(1L, 1L, "  MWH.001 ");

    assertEquals(1L, response.storeId());
    assertEquals(1L, response.productId());
    assertEquals("MWH.001", response.warehouseBusinessUnitCode());
    assertNotNull(response.createdAt());
    assertEquals(1L, repository.count());
  }

  private long warehouseIdFor(String businessUnitCode) {
    DbWarehouse warehouse = warehouseRepository.findActiveDbByBusinessUnitCode(businessUnitCode);
    return warehouse.id;
  }

  private void persistAssignment(Long storeId, Long productId, Long warehouseId) {
    FulfilmentAssignment assignment =
        new FulfilmentAssignment(storeId, productId, warehouseId, LocalDateTime.now());
    repository.persist(assignment);
  }
}

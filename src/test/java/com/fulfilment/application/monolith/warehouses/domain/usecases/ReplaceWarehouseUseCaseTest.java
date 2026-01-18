package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReplaceWarehouseUseCaseTest {

  @Test
  void rejectsMissingWarehouse() {
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(new StubStore(), id -> null);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(null));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  void rejectsMissingBusinessUnitCode() {
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(new StubStore(), id -> null);
    Warehouse warehouse = new Warehouse();
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 5;
    warehouse.stock = 1;

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(warehouse));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  void rejectsMissingLocation() {
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(new StubStore(), id -> null);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU";
    warehouse.capacity = 5;
    warehouse.stock = 1;

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(warehouse));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  void rejectsMissingCapacity() {
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(new StubStore(), id -> null);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU";
    warehouse.location = "ZWOLLE-001";
    warehouse.stock = 1;

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(warehouse));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  void rejectsMissingStock() {
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(new StubStore(), id -> null);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 5;

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(warehouse));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  void rejectsInvalidCapacityOrStock() {
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(new StubStore(), id -> null);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 0;
    warehouse.stock = -1;

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(warehouse));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  void rejectsWhenCurrentWarehouseMissing() {
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(new StubStore(), id -> null);
    Warehouse warehouse = warehouse("BU", "ZWOLLE-001", 5, 1);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(warehouse));

    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  void rejectsWhenLocationInvalid() {
    StubStore store = new StubStore();
    store.current = warehouse("BU", "ZWOLLE-001", 5, 1);
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, id -> null);
    Warehouse warehouse = warehouse("BU", "UNKNOWN", 5, 1);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(warehouse));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  void rejectsWhenNewCapacityBelowCurrentStock() {
    StubStore store = new StubStore();
    Warehouse current = warehouse("BU", "ZWOLLE-001", 10, 6);
    store.current = current;
    ReplaceWarehouseUseCase useCase =
        new ReplaceWarehouseUseCase(store, id -> new Location("ZWOLLE-001", 3, 50));
    Warehouse warehouse = warehouse("BU", "ZWOLLE-001", 5, 6);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(warehouse));

    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void rejectsWhenStockMismatch() {
    StubStore store = new StubStore();
    store.current = warehouse("BU", "ZWOLLE-001", 10, 6);
    ReplaceWarehouseUseCase useCase =
        new ReplaceWarehouseUseCase(store, id -> new Location("ZWOLLE-001", 3, 50));
    Warehouse warehouse = warehouse("BU", "ZWOLLE-001", 10, 5);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(warehouse));

    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void rejectsWhenMovingLocationExceedsCount() {
    StubStore store = new StubStore();
    store.current = warehouse("BU", "ZWOLLE-001", 10, 6);
    store.all.add(store.current);
    store.all.add(warehouse("BU-2", "AMSTERDAM-001", 10, 1));
    store.all.add(warehouse("BU-3", "AMSTERDAM-001", 10, 1));
    Location target = new Location("AMSTERDAM-001", 2, 50);
    ReplaceWarehouseUseCase useCase =
        new ReplaceWarehouseUseCase(store, id -> target);
    Warehouse warehouse = warehouse("BU", "AMSTERDAM-001", 10, 6);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(warehouse));

    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void rejectsWhenCapacityExceedsLocationLimit() {
    StubStore store = new StubStore();
    store.current = warehouse("BU", "ZWOLLE-001", 10, 6);
    store.all.add(store.current);
    Location target = new Location("ZWOLLE-001", 3, 5);
    ReplaceWarehouseUseCase useCase =
        new ReplaceWarehouseUseCase(store, id -> target);
    Warehouse warehouse = warehouse("BU", "ZWOLLE-001", 6, 6);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(warehouse));

    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void rejectsWhenResultingCapacityExceedsLocationLimit() {
    StubStore store = new StubStore();
    Warehouse current = warehouse("BU", "ZWOLLE-001", 10, 6);
    store.current = current;
    store.all.add(current);
    store.all.add(warehouse("BU-2", "ZWOLLE-001", 40, 1));
    Location target = new Location("ZWOLLE-001", 3, 45);
    ReplaceWarehouseUseCase useCase =
        new ReplaceWarehouseUseCase(store, id -> target);
    Warehouse warehouse = warehouse("BU", "ZWOLLE-001", 10, 6);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.replace(warehouse));

    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void replacesWarehouseInSameLocation() {
    StubStore store = new StubStore();
    Warehouse current = warehouse("BU", "ZWOLLE-001", 10, 6);
    current.createdAt = LocalDateTime.now().minusDays(1);
    store.current = current;
    store.all.add(current);
    Location target = new Location("ZWOLLE-001", 3, 50);
    ReplaceWarehouseUseCase useCase =
        new ReplaceWarehouseUseCase(store, id -> target);
    Warehouse warehouse = warehouse(" BU ", " ZWOLLE-001 ", 12, 6);

    useCase.replace(warehouse);

    assertNotNull(store.updated);
    assertNotNull(store.created);
    assertNotNull(store.updated.archivedAt);
    assertEquals("BU", store.created.businessUnitCode);
    assertEquals("ZWOLLE-001", store.created.location);
  }

  @Test
  void replacesWarehouseWhenMovingLocation() {
    StubStore store = new StubStore();
    Warehouse current = warehouse("BU", "ZWOLLE-001", 10, 6);
    current.createdAt = LocalDateTime.now().minusDays(1);
    store.current = current;
    store.all.add(current);
    Location target = new Location("AMSTERDAM-001", 3, 50);
    ReplaceWarehouseUseCase useCase =
        new ReplaceWarehouseUseCase(store, id -> target);
    Warehouse warehouse = warehouse("BU", "AMSTERDAM-001", 12, 6);

    useCase.replace(warehouse);

    assertNotNull(store.updated);
    assertNotNull(store.created);
    assertEquals("AMSTERDAM-001", store.created.location);
  }

  private Warehouse warehouse(String bu, String location, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = bu;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }

  private static final class StubStore implements WarehouseStore {
    private Warehouse current;
    private Warehouse created;
    private Warehouse updated;
    private final List<Warehouse> all = new ArrayList<>();

    @Override
    public List<Warehouse> getAll() {
      return all;
    }

    @Override
    public void create(Warehouse warehouse) {
      this.created = warehouse;
    }

    @Override
    public void update(Warehouse warehouse) {
      this.updated = warehouse;
    }

    @Override
    public void remove(Warehouse warehouse) {
      // no-op
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return current;
    }
  }
}

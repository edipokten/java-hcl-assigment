package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateWarehouseUseCaseTest {

  @Test
  void rejectsMissingWarehouse() {
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(new StubStore(), id -> null);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.create(null));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  void rejectsMissingBusinessUnitCode() {
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(new StubStore(), id -> null);
    Warehouse warehouse = new Warehouse();
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 5;
    warehouse.stock = 1;

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.create(warehouse));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  void rejectsMissingLocation() {
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(new StubStore(), id -> null);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU";
    warehouse.capacity = 5;
    warehouse.stock = 1;

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.create(warehouse));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  void rejectsMissingCapacity() {
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(new StubStore(), id -> null);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU";
    warehouse.location = "ZWOLLE-001";
    warehouse.stock = 1;

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.create(warehouse));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  void rejectsMissingStock() {
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(new StubStore(), id -> null);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 5;

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.create(warehouse));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  void rejectsInvalidCapacityOrStock() {
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(new StubStore(), id -> null);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 0;
    warehouse.stock = -1;

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.create(warehouse));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  void rejectsDuplicateBusinessUnitCode() {
    StubStore store = new StubStore();
    store.existing = new Warehouse();
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, id -> null);
    Warehouse warehouse = warehouse(" BU ", "ZWOLLE-001", 5, 1);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.create(warehouse));

    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void rejectsUnknownLocation() {
    CreateWarehouseUseCase useCase =
        new CreateWarehouseUseCase(new StubStore(), id -> null);
    Warehouse warehouse = warehouse("BU", "UNKNOWN", 5, 1);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.create(warehouse));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  void rejectsCapacityBelowStock() {
    Location location = new Location("ZWOLLE-001", 2, 50);
    CreateWarehouseUseCase useCase =
        new CreateWarehouseUseCase(new StubStore(), id -> location);
    Warehouse warehouse = warehouse("BU", "ZWOLLE-001", 5, 6);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.create(warehouse));

    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void rejectsCapacityAboveLocationMaximum() {
    Location location = new Location("ZWOLLE-001", 2, 5);
    CreateWarehouseUseCase useCase =
        new CreateWarehouseUseCase(new StubStore(), id -> location);
    Warehouse warehouse = warehouse("BU", "ZWOLLE-001", 6, 1);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.create(warehouse));

    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void rejectsWhenLocationMaxWarehouseCountReached() {
    Location location = new Location("ZWOLLE-001", 1, 50);
    StubStore store = new StubStore();
    store.all.add(warehouse("BU-OLD", "ZWOLLE-001", 10, 1));
    CreateWarehouseUseCase useCase =
        new CreateWarehouseUseCase(store, id -> location);
    Warehouse warehouse = warehouse("BU", "ZWOLLE-001", 10, 1);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.create(warehouse));

    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void rejectsWhenLocationCapacityExceeded() {
    Location location = new Location("ZWOLLE-001", 2, 15);
    StubStore store = new StubStore();
    store.all.add(warehouse("BU-OLD", "ZWOLLE-001", 10, 1));
    CreateWarehouseUseCase useCase =
        new CreateWarehouseUseCase(store, id -> location);
    Warehouse warehouse = warehouse("BU", "ZWOLLE-001", 10, 1);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.create(warehouse));

    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void createsWarehouseSuccessfully() {
    Location location = new Location("ZWOLLE-001", 2, 50);
    StubStore store = new StubStore();
    CreateWarehouseUseCase useCase =
        new CreateWarehouseUseCase(store, id -> location);
    Warehouse warehouse = warehouse(" BU ", " ZWOLLE-001 ", 10, 1);

    useCase.create(warehouse);

    assertNotNull(store.created);
    assertEquals("BU", store.created.businessUnitCode);
    assertEquals("ZWOLLE-001", store.created.location);
    assertNotNull(store.created.createdAt);
    assertNull(store.created.archivedAt);
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
    private Warehouse existing;
    private Warehouse created;
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
      // no-op
    }

    @Override
    public void remove(Warehouse warehouse) {
      // no-op
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return existing;
    }
  }
}

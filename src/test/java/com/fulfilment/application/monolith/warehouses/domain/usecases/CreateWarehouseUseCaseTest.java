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
  void shouldCreateWarehouseWithTrimmedFields() {
    FakeWarehouseStore store = new FakeWarehouseStore();
    LocationResolver resolver = id -> new Location("AMSTERDAM-001", 5, 100);
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    Warehouse input = new Warehouse();
    input.businessUnitCode = "  BU-001 ";
    input.location = " AMSTERDAM-001 ";
    input.capacity = 20;
    input.stock = 10;

    useCase.create(input);

    assertEquals(1, store.entries.size());
    Warehouse created = store.entries.get(0);
    assertEquals("BU-001", created.businessUnitCode);
    assertEquals("AMSTERDAM-001", created.location);
    assertNotNull(created.createdAt);
    assertNull(created.archivedAt);
  }

  @Test
  void shouldRejectDuplicateBusinessUnitCode() {
    FakeWarehouseStore store = new FakeWarehouseStore();
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "BU-001";
    existing.location = "AMSTERDAM-001";
    existing.capacity = 20;
    existing.stock = 10;
    store.entries.add(existing);

    LocationResolver resolver = id -> new Location("AMSTERDAM-001", 5, 100);
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    Warehouse input = new Warehouse();
    input.businessUnitCode = "BU-001";
    input.location = "AMSTERDAM-001";
    input.capacity = 20;
    input.stock = 10;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.create(input));
    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void shouldRejectInvalidLocation() {
    FakeWarehouseStore store = new FakeWarehouseStore();
    LocationResolver resolver = id -> null;
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    Warehouse input = new Warehouse();
    input.businessUnitCode = "BU-002";
    input.location = "UNKNOWN";
    input.capacity = 20;
    input.stock = 10;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.create(input));
    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  void shouldRejectCapacityBelowStock() {
    FakeWarehouseStore store = new FakeWarehouseStore();
    LocationResolver resolver = id -> new Location("AMSTERDAM-001", 5, 100);
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    Warehouse input = new Warehouse();
    input.businessUnitCode = "BU-003";
    input.location = "AMSTERDAM-001";
    input.capacity = 5;
    input.stock = 10;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.create(input));
    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void shouldRejectWhenMaxWarehousesReachedForLocation() {
    FakeWarehouseStore store = new FakeWarehouseStore();
    LocationResolver resolver = id -> new Location("AMSTERDAM-001", 2, 100);
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    Warehouse existingOne = new Warehouse();
    existingOne.businessUnitCode = "BU-010";
    existingOne.location = "AMSTERDAM-001";
    existingOne.capacity = 20;
    existingOne.stock = 10;
    store.entries.add(existingOne);

    Warehouse existingTwo = new Warehouse();
    existingTwo.businessUnitCode = "BU-011";
    existingTwo.location = "AMSTERDAM-001";
    existingTwo.capacity = 20;
    existingTwo.stock = 10;
    store.entries.add(existingTwo);

    Warehouse input = new Warehouse();
    input.businessUnitCode = "BU-012";
    input.location = "AMSTERDAM-001";
    input.capacity = 10;
    input.stock = 5;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.create(input));
    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void shouldRejectWhenWarehouseCapacityExceedsLocationMax() {
    FakeWarehouseStore store = new FakeWarehouseStore();
    LocationResolver resolver = id -> new Location("AMSTERDAM-001", 5, 100);
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    Warehouse input = new Warehouse();
    input.businessUnitCode = "BU-020";
    input.location = "AMSTERDAM-001";
    input.capacity = 120;
    input.stock = 10;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.create(input));
    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void shouldRejectWhenTotalCapacityExceedsLocationMax() {
    FakeWarehouseStore store = new FakeWarehouseStore();
    LocationResolver resolver = id -> new Location("AMSTERDAM-001", 5, 100);
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "BU-021";
    existing.location = "AMSTERDAM-001";
    existing.capacity = 60;
    existing.stock = 10;
    store.entries.add(existing);

    Warehouse input = new Warehouse();
    input.businessUnitCode = "BU-022";
    input.location = "AMSTERDAM-001";
    input.capacity = 50;
    input.stock = 10;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.create(input));
    assertEquals(409, ex.getResponse().getStatus());
  }

  private static final class FakeWarehouseStore implements WarehouseStore {
    private final List<Warehouse> entries = new ArrayList<>();

    @Override
    public List<Warehouse> getAll() {
      return entries.stream().filter(w -> w.archivedAt == null).toList();
    }

    @Override
    public void create(Warehouse warehouse) {
      entries.add(warehouse);
    }

    @Override
    public void update(Warehouse warehouse) {
      Warehouse current = findByBusinessUnitCode(warehouse.businessUnitCode);
      if (current != null) {
        current.location = warehouse.location;
        current.capacity = warehouse.capacity;
        current.stock = warehouse.stock;
        current.archivedAt = warehouse.archivedAt;
      }
    }

    @Override
    public void remove(Warehouse warehouse) {
      entries.removeIf(w -> w.businessUnitCode.equals(warehouse.businessUnitCode));
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return entries.stream()
          .filter(w -> w.archivedAt == null)
          .filter(w -> w.businessUnitCode.equals(buCode))
          .findFirst()
          .orElse(null);
    }
  }
}

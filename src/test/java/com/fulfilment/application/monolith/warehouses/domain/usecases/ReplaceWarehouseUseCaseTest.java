package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReplaceWarehouseUseCaseTest {

  @Test
  void shouldReplaceWarehouseAndArchiveCurrent() {
    FakeWarehouseStore store = new FakeWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU-100";
    current.location = "AMSTERDAM-002";
    current.capacity = 20;
    current.stock = 10;
    store.entries.add(current);

    LocationResolver resolver = id -> new Location("AMSTERDAM-002", 3, 75);
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "BU-100";
    replacement.location = "AMSTERDAM-002";
    replacement.capacity = 30;
    replacement.stock = 10;

    useCase.replace(replacement);

    assertEquals(2, store.entries.size());
    Warehouse archived = store.entries.stream()
        .filter(w -> w.archivedAt != null)
        .findFirst()
        .orElseThrow();
    assertNotNull(archived.archivedAt);

    Warehouse active = store.findByBusinessUnitCode("BU-100");
    assertNotNull(active);
    assertEquals(30, active.capacity);
  }

  @Test
  void shouldRejectStockMismatch() {
    FakeWarehouseStore store = new FakeWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU-200";
    current.location = "AMSTERDAM-002";
    current.capacity = 20;
    current.stock = 10;
    store.entries.add(current);

    LocationResolver resolver = id -> new Location("AMSTERDAM-002", 3, 75);
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "BU-200";
    replacement.location = "AMSTERDAM-002";
    replacement.capacity = 30;
    replacement.stock = 12;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.replace(replacement));
    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void shouldRejectInvalidLocation() {
    FakeWarehouseStore store = new FakeWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU-250";
    current.location = "AMSTERDAM-002";
    current.capacity = 20;
    current.stock = 10;
    store.entries.add(current);

    LocationResolver resolver = id -> null;
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "BU-250";
    replacement.location = "UNKNOWN";
    replacement.capacity = 30;
    replacement.stock = 10;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.replace(replacement));
    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  void shouldRejectLocationCapacityOverflow() {
    FakeWarehouseStore store = new FakeWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU-300";
    current.location = "AMSTERDAM-002";
    current.capacity = 20;
    current.stock = 10;
    store.entries.add(current);

    Warehouse other = new Warehouse();
    other.businessUnitCode = "BU-301";
    other.location = "AMSTERDAM-002";
    other.capacity = 60;
    other.stock = 10;
    store.entries.add(other);

    LocationResolver resolver = id -> new Location("AMSTERDAM-002", 3, 75);
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "BU-300";
    replacement.location = "AMSTERDAM-002";
    replacement.capacity = 30;
    replacement.stock = 10;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.replace(replacement));
    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void shouldRejectWhenNewCapacityBelowCurrentStock() {
    FakeWarehouseStore store = new FakeWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU-350";
    current.location = "AMSTERDAM-002";
    current.capacity = 20;
    current.stock = 15;
    store.entries.add(current);

    LocationResolver resolver = id -> new Location("AMSTERDAM-002", 3, 75);
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "BU-350";
    replacement.location = "AMSTERDAM-002";
    replacement.capacity = 10;
    replacement.stock = 15;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.replace(replacement));
    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void shouldRejectWhenReplacementCapacityExceedsLocationMax() {
    FakeWarehouseStore store = new FakeWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU-360";
    current.location = "AMSTERDAM-002";
    current.capacity = 20;
    current.stock = 10;
    store.entries.add(current);

    LocationResolver resolver = id -> new Location("AMSTERDAM-002", 3, 75);
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "BU-360";
    replacement.location = "AMSTERDAM-002";
    replacement.capacity = 80;
    replacement.stock = 10;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.replace(replacement));
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
      Warehouse current = entries.stream()
          .filter(w -> w.archivedAt == null)
          .filter(w -> w.businessUnitCode.equals(warehouse.businessUnitCode))
          .findFirst()
          .orElse(null);
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

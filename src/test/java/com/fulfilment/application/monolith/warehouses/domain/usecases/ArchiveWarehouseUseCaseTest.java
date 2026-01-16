package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArchiveWarehouseUseCaseTest {

  @Test
  void shouldArchiveWarehouse() {
    FakeWarehouseStore store = new FakeWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU-001";
    current.location = "AMSTERDAM-001";
    current.capacity = 20;
    current.stock = 10;
    store.entries.add(current);

    ArchiveWarehouseUseCase useCase = new ArchiveWarehouseUseCase(store);

    useCase.archive(current);

    Warehouse updated = store.findByBusinessUnitCode("BU-001");
    assertNull(updated);
    assertNotNull(store.entries.get(0).archivedAt);
  }

  @Test
  void shouldRejectAlreadyArchivedWarehouse() {
    FakeWarehouseStore store = new FakeWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU-002";
    current.location = "AMSTERDAM-001";
    current.capacity = 20;
    current.stock = 10;
    current.archivedAt = java.time.LocalDateTime.now();
    store.entries.add(current);

    ArchiveWarehouseUseCase useCase = new ArchiveWarehouseUseCase(store);

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.archive(current));
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

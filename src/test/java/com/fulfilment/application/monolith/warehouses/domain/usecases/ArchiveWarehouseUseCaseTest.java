package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArchiveWarehouseUseCaseTest {

  @Test
  void archivesWarehouse() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    ArchiveWarehouseUseCase useCase = new ArchiveWarehouseUseCase(store);

    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU1";

    useCase.archive(warehouse);

    assertEquals(1, store.updated.size());
    assertNotNull(store.updated.get(0).archivedAt);
  }

  @Test
  void rejectsAlreadyArchivedWarehouse() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    ArchiveWarehouseUseCase useCase = new ArchiveWarehouseUseCase(store);

    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU1";
    warehouse.archivedAt = java.time.LocalDateTime.now();

    WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> useCase.archive(warehouse));

    assertEquals(409, exception.getResponse().getStatus());
  }

  @Test
  void rejectsMissingWarehouse() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    ArchiveWarehouseUseCase useCase = new ArchiveWarehouseUseCase(store);

    WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> useCase.archive(null));

    assertEquals(422, exception.getResponse().getStatus());
  }

  private static final class InMemoryWarehouseStore implements WarehouseStore {

    private final List<Warehouse> updated = new ArrayList<>();

    @Override
    public List<Warehouse> getAll() {
      return List.of();
    }

    @Override
    public void create(Warehouse warehouse) {
      // no-op for tests
    }

    @Override
    public void update(Warehouse warehouse) {
      updated.add(warehouse);
    }

    @Override
    public void remove(Warehouse warehouse) {
      // no-op for tests
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return null;
    }

    @Override
    public Warehouse findAnyByBusinessUnitCode(String buCode) {
      return null;
    }
  }
}

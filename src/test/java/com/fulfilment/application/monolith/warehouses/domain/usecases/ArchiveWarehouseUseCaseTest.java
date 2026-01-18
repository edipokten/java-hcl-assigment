package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArchiveWarehouseUseCaseTest {

  @Test
  void rejectsNullWarehouse() {
    ArchiveWarehouseUseCase useCase = new ArchiveWarehouseUseCase(new StubStore());

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.archive(null));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  void rejectsAlreadyArchivedWarehouse() {
    ArchiveWarehouseUseCase useCase = new ArchiveWarehouseUseCase(new StubStore());
    Warehouse warehouse = new Warehouse();
    warehouse.archivedAt = LocalDateTime.now();

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> useCase.archive(warehouse));

    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void archivesWarehouse() {
    StubStore store = new StubStore();
    ArchiveWarehouseUseCase useCase = new ArchiveWarehouseUseCase(store);
    Warehouse warehouse = new Warehouse();

    useCase.archive(warehouse);

    assertNotNull(warehouse.archivedAt);
    assertEquals(warehouse, store.updated);
  }

  private static final class StubStore implements WarehouseStore {
    private Warehouse updated;

    @Override
    public List<Warehouse> getAll() {
      return List.of();
    }

    @Override
    public void create(Warehouse warehouse) {
      // no-op
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
      return null;
    }
  }
}

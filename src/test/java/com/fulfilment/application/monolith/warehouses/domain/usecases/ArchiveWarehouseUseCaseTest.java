package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import java.util.*;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

    @Test
    void archive_shouldSetArchivedAt() {
        var store = new InMemoryWarehouseStore();
        var useCase = new ArchiveWarehouseUseCase(store);

        var w = new Warehouse();
        w.businessUnitCode = "MWH.001";
        w.location = "ZWOLLE-001";
        w.capacity = 40;
        w.stock = 10;
        store.create(w);

        Warehouse stored = store.findByBusinessUnitCode("MWH.001");
        assertNotNull(stored);
        assertNull(stored.archivedAt);

        useCase.archive(stored);

        Warehouse updated = store.findByBusinessUnitCode("MWH.001");
        assertNotNull(updated.archivedAt);
    }

    @Test
    void archive_whenAlreadyArchived_should409() {
        var store = new InMemoryWarehouseStore();
        var useCase = new ArchiveWarehouseUseCase(store);

        var w = new Warehouse();
        w.businessUnitCode = "MWH.002";
        w.archivedAt = java.time.LocalDateTime.now();
        store.create(w);

        var ex = assertThrows(WebApplicationException.class, () -> useCase.archive(w));
        assertEquals(409, ex.getResponse().getStatus());
    }

    // --- minimal in-memory store ---
    static class InMemoryWarehouseStore implements WarehouseStore {
        private final Map<String, Warehouse> db = new HashMap<>();

        @Override
        public List<Warehouse> getAll() {
            return db.values().stream().filter(w -> w.archivedAt == null).toList();
        }

        @Override
        public void create(Warehouse warehouse) {
            db.put(warehouse.businessUnitCode, warehouse);
        }

        @Override
        public void update(Warehouse warehouse) {
            db.put(warehouse.businessUnitCode, warehouse);
        }

        @Override
        public void remove(Warehouse warehouse) {
            db.remove(warehouse.businessUnitCode);
        }

        @Override
        public Warehouse findByBusinessUnitCode(String buCode) {
            return db.get(buCode);
        }
    }
}

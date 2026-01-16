package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import java.util.*;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

    @Test
    void replace_happyPath_shouldArchiveOldAndCreateNew() {
        var store = new InMemoryWarehouseStore();
        var resolver =
                new MapLocationResolver(Map.of("AMSTERDAM-001", new Location("AMSTERDAM-001", 5, 100)));
        var useCase = new ReplaceWarehouseUseCase(store, resolver);

        var current = new Warehouse();
        current.businessUnitCode = "MWH.001";
        current.location = "AMSTERDAM-001";
        current.capacity = 50;
        current.stock = 40;
        current.archivedAt = null;
        store.create(current);

        var replacement = new Warehouse();
        replacement.businessUnitCode = "MWH.001";
        replacement.location = "AMSTERDAM-001";
        replacement.capacity = 60;
        replacement.stock = 40; // must match old stock

        useCase.replace(replacement);

        // Active warehouse should be the "new one"
        Warehouse active = store.findByBusinessUnitCode("MWH.001");
        assertNotNull(active);
        assertNull(active.archivedAt);
        assertEquals(60, active.capacity);
        assertEquals("AMSTERDAM-001", active.location);
        assertEquals(40, active.stock);

        // History should contain 2 entries: old archived + new active
        List<Warehouse> history = store.findAllByBusinessUnitCode("MWH.001");
        assertEquals(2, history.size());

        assertTrue(history.stream().anyMatch(w -> w.archivedAt != null)); // old one archived
        assertTrue(history.stream().anyMatch(w -> w.archivedAt == null)); // new one active
    }

    @Test
    void replace_whenStockMismatch_should409() {
        var store = new InMemoryWarehouseStore();
        var resolver =
                new MapLocationResolver(Map.of("AMSTERDAM-001", new Location("AMSTERDAM-001", 5, 100)));
        var useCase = new ReplaceWarehouseUseCase(store, resolver);

        var current = new Warehouse();
        current.businessUnitCode = "MWH.010";
        current.location = "AMSTERDAM-001";
        current.capacity = 50;
        current.stock = 40;
        store.create(current);

        var replacement = new Warehouse();
        replacement.businessUnitCode = "MWH.010";
        replacement.location = "AMSTERDAM-001";
        replacement.capacity = 60;
        replacement.stock = 41; // mismatch

        var ex = assertThrows(WebApplicationException.class, () -> useCase.replace(replacement));
        assertEquals(409, ex.getResponse().getStatus());

        // should not create a new history entry
        assertEquals(1, store.findAllByBusinessUnitCode("MWH.010").size());
    }

    @Test
    void replace_whenCapacityCannotAccommodateExistingStock_should409() {
        var store = new InMemoryWarehouseStore();
        var resolver =
                new MapLocationResolver(Map.of("AMSTERDAM-001", new Location("AMSTERDAM-001", 5, 100)));
        var useCase = new ReplaceWarehouseUseCase(store, resolver);

        var current = new Warehouse();
        current.businessUnitCode = "MWH.020";
        current.location = "AMSTERDAM-001";
        current.capacity = 50;
        current.stock = 40;
        store.create(current);

        var replacement = new Warehouse();
        replacement.businessUnitCode = "MWH.020";
        replacement.location = "AMSTERDAM-001";
        replacement.capacity = 39; // too small for existing stock
        replacement.stock = 40;

        var ex = assertThrows(WebApplicationException.class, () -> useCase.replace(replacement));
        assertEquals(409, ex.getResponse().getStatus());

        // should not create a new history entry
        assertEquals(1, store.findAllByBusinessUnitCode("MWH.020").size());
    }

    @Test
    void replace_whenInvalidLocation_should422() {
        var store = new InMemoryWarehouseStore();
        var resolver = new MapLocationResolver(Collections.emptyMap());
        var useCase = new ReplaceWarehouseUseCase(store, resolver);

        var current = new Warehouse();
        current.businessUnitCode = "MWH.030";
        current.location = "AMSTERDAM-001";
        current.capacity = 50;
        current.stock = 40;
        store.create(current);

        var replacement = new Warehouse();
        replacement.businessUnitCode = "MWH.030";
        replacement.location = "UNKNOWN";
        replacement.capacity = 60;
        replacement.stock = 40;

        var ex = assertThrows(WebApplicationException.class, () -> useCase.replace(replacement));
        assertEquals(422, ex.getResponse().getStatus());
    }

    @Test
    void replace_whenTargetLocationMaxWarehousesReached_should409() {
        var store = new InMemoryWarehouseStore();
        var resolver =
                new MapLocationResolver(Map.of("ZWOLLE-002", new Location("ZWOLLE-002", 1, 50)));
        var useCase = new ReplaceWarehouseUseCase(store, resolver);

        var current = new Warehouse();
        current.businessUnitCode = "MWH.040";
        current.location = "AMSTERDAM-001";
        current.capacity = 20;
        current.stock = 10;
        store.create(current);

        var existingAtTarget = new Warehouse();
        existingAtTarget.businessUnitCode = "MWH.041";
        existingAtTarget.location = "ZWOLLE-002";
        existingAtTarget.capacity = 20;
        existingAtTarget.stock = 10;
        store.create(existingAtTarget);

        var replacement = new Warehouse();
        replacement.businessUnitCode = "MWH.040";
        replacement.location = "ZWOLLE-002";
        replacement.capacity = 20;
        replacement.stock = 10;

        var ex = assertThrows(WebApplicationException.class, () -> useCase.replace(replacement));
        assertEquals(409, ex.getResponse().getStatus());
    }

    @Test
    void replace_whenTargetLocationCapacityExceeded_should409() {
        var store = new InMemoryWarehouseStore();
        var resolver =
                new MapLocationResolver(Map.of("AMSTERDAM-002", new Location("AMSTERDAM-002", 3, 75)));
        var useCase = new ReplaceWarehouseUseCase(store, resolver);

        var current = new Warehouse();
        current.businessUnitCode = "MWH.050";
        current.location = "AMSTERDAM-002";
        current.capacity = 50;
        current.stock = 40;
        store.create(current);

        var replacement = new Warehouse();
        replacement.businessUnitCode = "MWH.050";
        replacement.location = "AMSTERDAM-002";
        replacement.capacity = 80;
        replacement.stock = 40;

        var ex = assertThrows(WebApplicationException.class, () -> useCase.replace(replacement));
        assertEquals(409, ex.getResponse().getStatus());
    }

    // --- minimal fakes ---

    static class MapLocationResolver implements LocationResolver {
        private final Map<String, Location> map;

        MapLocationResolver(Map<String, Location> map) {
            this.map = map;
        }

        @Override
        public Location resolveByIdentifier(String identifier) {
            return map.get(identifier);
        }
    }

    static class InMemoryWarehouseStore implements WarehouseStore {
        // store history: BU code -> list of versions
        private final Map<String, List<Warehouse>> db = new HashMap<>();

        @Override
        public List<Warehouse> getAll() {
            // only active
            return db.values().stream()
                    .flatMap(List::stream)
                    .filter(w -> w.archivedAt == null)
                    .toList();
        }

        @Override
        public void create(Warehouse warehouse) {
            db.computeIfAbsent(warehouse.businessUnitCode, k -> new ArrayList<>()).add(cloneWarehouse(warehouse));
        }

        @Override
        public void update(Warehouse warehouse) {
            // update the ACTIVE entry (archivedAt == null) for this BU code
            List<Warehouse> versions = db.get(warehouse.businessUnitCode);
            if (versions == null) {
                throw new IllegalStateException("Warehouse not found for businessUnitCode=" + warehouse.businessUnitCode);
            }
            for (int i = 0; i < versions.size(); i++) {
                if (versions.get(i).archivedAt == null) {
                    versions.set(i, cloneWarehouse(warehouse));
                    return;
                }
            }
            throw new IllegalStateException("Active warehouse not found for businessUnitCode=" + warehouse.businessUnitCode);
        }

        @Override
        public void remove(Warehouse warehouse) {
            List<Warehouse> versions = db.get(warehouse.businessUnitCode);
            if (versions == null) return;
            versions.removeIf(w -> w.archivedAt == null);
        }

        @Override
        public Warehouse findByBusinessUnitCode(String buCode) {
            List<Warehouse> versions = db.get(buCode);
            if (versions == null) return null;
            return versions.stream().filter(w -> w.archivedAt == null).findFirst().map(InMemoryWarehouseStore::cloneWarehouse).orElse(null);
        }

        // test helper (not part of the interface)
        public List<Warehouse> findAllByBusinessUnitCode(String buCode) {
            return db.getOrDefault(buCode, List.of()).stream().map(InMemoryWarehouseStore::cloneWarehouse).toList();
        }

        private static Warehouse cloneWarehouse(Warehouse w) {
            Warehouse c = new Warehouse();
            c.businessUnitCode = w.businessUnitCode;
            c.location = w.location;
            c.capacity = w.capacity;
            c.stock = w.stock;
            c.createdAt = w.createdAt;
            c.archivedAt = w.archivedAt;
            return c;
        }
    }
}

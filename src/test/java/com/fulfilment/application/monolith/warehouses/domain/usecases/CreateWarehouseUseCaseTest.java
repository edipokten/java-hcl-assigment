package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import java.util.*;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

    @Test
    void create_happyPath_shouldPersist() {
        var store = new InMemoryWarehouseStore();
        var resolver = new MapLocationResolver(Map.of("AMSTERDAM-001", new Location("AMSTERDAM-001", 5, 100)));
        var useCase = new CreateWarehouseUseCase(store, resolver);

        var w = new Warehouse();
        w.businessUnitCode = "MWH.NEW";
        w.location = "AMSTERDAM-001";
        w.capacity = 60;
        w.stock = 10;

        useCase.create(w);

        Warehouse created = store.findByBusinessUnitCode("MWH.NEW");
        assertNotNull(created);
        assertNotNull(created.createdAt);
        assertNull(created.archivedAt);
    }

    @Test
    void create_whenBusinessUnitExists_should409() {
        var store = new InMemoryWarehouseStore();
        var resolver = new MapLocationResolver(Map.of("AMSTERDAM-001", new Location("AMSTERDAM-001", 5, 100)));
        var useCase = new CreateWarehouseUseCase(store, resolver);

        var existing = new Warehouse();
        existing.businessUnitCode = "MWH.001";
        existing.location = "AMSTERDAM-001";
        existing.capacity = 10;
        existing.stock = 1;
        store.create(existing);

        var w = new Warehouse();
        w.businessUnitCode = "MWH.001";
        w.location = "AMSTERDAM-001";
        w.capacity = 20;
        w.stock = 2;

        var ex = assertThrows(WebApplicationException.class, () -> useCase.create(w));
        assertEquals(409, ex.getResponse().getStatus());
    }

    @Test
    void create_whenInvalidLocation_should422() {
        var store = new InMemoryWarehouseStore();
        var resolver = new MapLocationResolver(Collections.emptyMap());
        var useCase = new CreateWarehouseUseCase(store, resolver);

        var w = new Warehouse();
        w.businessUnitCode = "MWH.X";
        w.location = "UNKNOWN";
        w.capacity = 10;
        w.stock = 1;

        var ex = assertThrows(WebApplicationException.class, () -> useCase.create(w));
        assertEquals(422, ex.getResponse().getStatus());
    }

    @Test
    void create_whenStockExceedsCapacity_should409() {
        var store = new InMemoryWarehouseStore();
        var resolver = new MapLocationResolver(Map.of("ZWOLLE-001", new Location("ZWOLLE-001", 1, 40)));
        var useCase = new CreateWarehouseUseCase(store, resolver);

        var w = new Warehouse();
        w.businessUnitCode = "MWH.BAD";
        w.location = "ZWOLLE-001";
        w.capacity = 10;
        w.stock = 11;

        var ex = assertThrows(WebApplicationException.class, () -> useCase.create(w));
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

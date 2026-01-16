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
import java.util.Map;
import org.junit.jupiter.api.Test;

class CreateWarehouseUseCaseTest {

  @Test
  void createsWarehouseWhenValid() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    LocationResolver resolver = new MapLocationResolver(
        Map.of("NYC", new Location("NYC", 3, 500))
    );
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = " BU1 ";
    warehouse.location = " NYC ";
    warehouse.capacity = 200;
    warehouse.stock = 50;

    useCase.create(warehouse);

    assertEquals(1, store.created.size());
    Warehouse created = store.created.get(0);
    assertEquals("BU1", created.businessUnitCode);
    assertEquals("NYC", created.location);
    assertNotNull(created.createdAt);
    assertNull(created.archivedAt);
  }

  @Test
  void rejectsDuplicateBusinessUnitCode() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "BU1";
    store.warehouses.add(existing);

    LocationResolver resolver = new MapLocationResolver(
        Map.of("NYC", new Location("NYC", 3, 500))
    );
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    Warehouse incoming = new Warehouse();
    incoming.businessUnitCode = "BU1";
    incoming.location = "NYC";
    incoming.capacity = 100;
    incoming.stock = 10;

    WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> useCase.create(incoming));

    assertEquals(409, exception.getResponse().getStatus());
  }

  @Test
  void rejectsWhenCapacityBelowStock() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    LocationResolver resolver = new MapLocationResolver(
        Map.of("NYC", new Location("NYC", 3, 500))
    );
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    Warehouse incoming = new Warehouse();
    incoming.businessUnitCode = "BU1";
    incoming.location = "NYC";
    incoming.capacity = 10;
    incoming.stock = 50;

    WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> useCase.create(incoming));

    assertEquals(409, exception.getResponse().getStatus());
  }

  @Test
  void rejectsWhenMaxWarehousesReached() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "BU1";
    existing.location = "NYC";
    existing.capacity = 100;
    existing.stock = 10;
    store.warehouses.add(existing);

    LocationResolver resolver = new MapLocationResolver(
        Map.of("NYC", new Location("NYC", 1, 500))
    );
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    Warehouse incoming = new Warehouse();
    incoming.businessUnitCode = "BU2";
    incoming.location = "NYC";
    incoming.capacity = 100;
    incoming.stock = 10;

    WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> useCase.create(incoming));

    assertEquals(409, exception.getResponse().getStatus());
  }

  private static final class MapLocationResolver implements LocationResolver {

    private final Map<String, Location> locations;

    private MapLocationResolver(Map<String, Location> locations) {
      this.locations = locations;
    }

    @Override
    public Location resolveByIdentifier(String identifier) {
      return locations.get(identifier);
    }
  }

  private static final class InMemoryWarehouseStore implements WarehouseStore {

    private final List<Warehouse> warehouses = new ArrayList<>();
    private final List<Warehouse> created = new ArrayList<>();

    @Override
    public List<Warehouse> getAll() {
      return new ArrayList<>(warehouses);
    }

    @Override
    public void create(Warehouse warehouse) {
      warehouses.add(warehouse);
      created.add(warehouse);
    }

    @Override
    public void update(Warehouse warehouse) {
      // no-op for tests
    }

    @Override
    public void remove(Warehouse warehouse) {
      warehouses.remove(warehouse);
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return warehouses.stream()
          .filter(warehouse -> warehouse.businessUnitCode.equals(buCode))
          .findFirst()
          .orElse(null);
    }
  }
}

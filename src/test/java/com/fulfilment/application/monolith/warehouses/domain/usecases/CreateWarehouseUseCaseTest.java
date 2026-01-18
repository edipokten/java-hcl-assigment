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

  @Test
  void rejectsMissingRequestBody() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    LocationResolver resolver = new MapLocationResolver(
        Map.of("NYC", new Location("NYC", 3, 500))
    );
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> useCase.create(null));

    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  void rejectsInvalidLocation() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    LocationResolver resolver = new MapLocationResolver(
        Map.of("NYC", new Location("NYC", 3, 500))
    );
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    Warehouse incoming = new Warehouse();
    incoming.businessUnitCode = "BU1";
    incoming.location = "MISSING";
    incoming.capacity = 100;
    incoming.stock = 10;

    WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> useCase.create(incoming));

    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  void rejectsCapacityExceedingLocationLimit() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    LocationResolver resolver = new MapLocationResolver(
        Map.of("NYC", new Location("NYC", 3, 100))
    );
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    Warehouse incoming = new Warehouse();
    incoming.businessUnitCode = "BU1";
    incoming.location = "NYC";
    incoming.capacity = 150;
    incoming.stock = 10;

    WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> useCase.create(incoming));

    assertEquals(409, exception.getResponse().getStatus());
  }

  @Test
  void rejectsWhenTotalCapacityWouldExceedLocation() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "BU1";
    existing.location = "NYC";
    existing.capacity = 450;
    existing.stock = 10;
    store.warehouses.add(existing);

    LocationResolver resolver = new MapLocationResolver(
        Map.of("NYC", new Location("NYC", 3, 500))
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

  @Test
  void rejectsInvalidCapacityOrStockValues() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    LocationResolver resolver = new MapLocationResolver(
        Map.of("NYC", new Location("NYC", 3, 500))
    );
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    Warehouse invalidCapacity = new Warehouse();
    invalidCapacity.businessUnitCode = "BU1";
    invalidCapacity.location = "NYC";
    invalidCapacity.capacity = 0;
    invalidCapacity.stock = 10;

    WebApplicationException capacityException = assertThrows(WebApplicationException.class,
        () -> useCase.create(invalidCapacity));
    assertEquals(422, capacityException.getResponse().getStatus());

    Warehouse invalidStock = new Warehouse();
    invalidStock.businessUnitCode = "BU2";
    invalidStock.location = "NYC";
    invalidStock.capacity = 10;
    invalidStock.stock = -1;

    WebApplicationException stockException = assertThrows(WebApplicationException.class,
        () -> useCase.create(invalidStock));
    assertEquals(422, stockException.getResponse().getStatus());
  }

  @Test
  void rejectsMissingRequiredFields() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    LocationResolver resolver = new MapLocationResolver(
        Map.of("NYC", new Location("NYC", 3, 500))
    );
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    Warehouse missingBusinessUnit = new Warehouse();
    missingBusinessUnit.businessUnitCode = " ";
    missingBusinessUnit.location = "NYC";
    missingBusinessUnit.capacity = 100;
    missingBusinessUnit.stock = 10;

    WebApplicationException businessUnitException = assertThrows(WebApplicationException.class,
        () -> useCase.create(missingBusinessUnit));
    assertEquals(422, businessUnitException.getResponse().getStatus());

    Warehouse missingLocation = new Warehouse();
    missingLocation.businessUnitCode = "BU1";
    missingLocation.location = " ";
    missingLocation.capacity = 100;
    missingLocation.stock = 10;

    WebApplicationException locationException = assertThrows(WebApplicationException.class,
        () -> useCase.create(missingLocation));
    assertEquals(422, locationException.getResponse().getStatus());

    Warehouse missingCapacity = new Warehouse();
    missingCapacity.businessUnitCode = "BU1";
    missingCapacity.location = "NYC";
    missingCapacity.capacity = null;
    missingCapacity.stock = 10;

    WebApplicationException capacityException = assertThrows(WebApplicationException.class,
        () -> useCase.create(missingCapacity));
    assertEquals(422, capacityException.getResponse().getStatus());

    Warehouse missingStock = new Warehouse();
    missingStock.businessUnitCode = "BU1";
    missingStock.location = "NYC";
    missingStock.capacity = 100;
    missingStock.stock = null;

    WebApplicationException stockException = assertThrows(WebApplicationException.class,
        () -> useCase.create(missingStock));
    assertEquals(422, stockException.getResponse().getStatus());
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

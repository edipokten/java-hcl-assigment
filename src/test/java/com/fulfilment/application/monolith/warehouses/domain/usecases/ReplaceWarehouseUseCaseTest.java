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
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReplaceWarehouseUseCaseTest {

  @Test
  void replacesWarehouseInSameLocation() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU1";
    current.location = "LOC1";
    current.capacity = 100;
    current.stock = 10;
    store.warehouses.add(current);

    Warehouse other = new Warehouse();
    other.businessUnitCode = "BU2";
    other.location = "LOC1";
    other.capacity = 50;
    other.stock = 5;
    store.warehouses.add(other);

    LocationResolver resolver = new MapLocationResolver(
        Map.of("LOC1", new Location("LOC1", 5, 500))
    );

    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "BU1";
    replacement.location = "LOC1";
    replacement.capacity = 120;
    replacement.stock = 10;

    useCase.replace(replacement);

    assertEquals(1, store.updated.size());
    assertNotNull(store.updated.get(0).archivedAt);
    assertEquals(1, store.created.size());
    assertEquals("BU1", store.created.get(0).businessUnitCode);
    assertEquals("LOC1", store.created.get(0).location);
  }

  @Test
  void rejectsWhenCurrentWarehouseMissing() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    LocationResolver resolver = new MapLocationResolver(
        Map.of("LOC1", new Location("LOC1", 5, 500))
    );
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "BU1";
    replacement.location = "LOC1";
    replacement.capacity = 120;
    replacement.stock = 10;

    WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> useCase.replace(replacement));

    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  void replacesWhenCurrentCapacityIsNull() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU1";
    current.location = "LOC1";
    current.capacity = null;
    current.stock = 10;
    store.warehouses.add(current);

    Warehouse other = new Warehouse();
    other.businessUnitCode = "BU2";
    other.location = "LOC1";
    other.capacity = 20;
    other.stock = 5;
    store.warehouses.add(other);

    LocationResolver resolver = new MapLocationResolver(
        Map.of("LOC1", new Location("LOC1", 5, 100))
    );

    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "BU1";
    replacement.location = "LOC1";
    replacement.capacity = 30;
    replacement.stock = 10;

    useCase.replace(replacement);

    assertEquals(1, store.updated.size());
    assertEquals(1, store.created.size());
  }

  @Test
  void rejectsWhenMovingToFullLocation() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU1";
    current.location = "LOC1";
    current.capacity = 100;
    current.stock = 10;
    store.warehouses.add(current);

    Warehouse existingAtTarget = new Warehouse();
    existingAtTarget.businessUnitCode = "BU2";
    existingAtTarget.location = "LOC2";
    existingAtTarget.capacity = 50;
    existingAtTarget.stock = 5;
    store.warehouses.add(existingAtTarget);

    LocationResolver resolver = new MapLocationResolver(
        Map.of(
            "LOC1", new Location("LOC1", 5, 500),
            "LOC2", new Location("LOC2", 1, 500)
        )
    );

    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "BU1";
    replacement.location = "LOC2";
    replacement.capacity = 120;
    replacement.stock = 10;

    WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> useCase.replace(replacement));

    assertEquals(409, exception.getResponse().getStatus());
  }

  @Test
  void rejectsWhenStockWasNotPreviouslySet() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU1";
    current.location = "LOC1";
    current.capacity = 100;
    current.stock = null;
    store.warehouses.add(current);

    LocationResolver resolver = new MapLocationResolver(
        Map.of("LOC1", new Location("LOC1", 5, 500))
    );

    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "BU1";
    replacement.location = "LOC1";
    replacement.capacity = 120;
    replacement.stock = 10;

    WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> useCase.replace(replacement));

    assertEquals(409, exception.getResponse().getStatus());
  }

  @Test
  void rejectsWhenStockMismatch() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU1";
    current.location = "LOC1";
    current.capacity = 100;
    current.stock = 10;
    store.warehouses.add(current);

    LocationResolver resolver = new MapLocationResolver(
        Map.of("LOC1", new Location("LOC1", 5, 500))
    );

    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "BU1";
    replacement.location = "LOC1";
    replacement.capacity = 120;
    replacement.stock = 9;

    WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> useCase.replace(replacement));

    assertEquals(409, exception.getResponse().getStatus());
  }

  @Test
  void rejectsWhenLocationDoesNotExist() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU1";
    current.location = "LOC1";
    current.capacity = 100;
    current.stock = 10;
    store.warehouses.add(current);

    LocationResolver resolver = new MapLocationResolver(Map.of());
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "BU1";
    replacement.location = "MISSING";
    replacement.capacity = 120;
    replacement.stock = 10;

    WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> useCase.replace(replacement));

    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  void rejectsWhenNewCapacityBelowCurrentStock() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU1";
    current.location = "LOC1";
    current.capacity = 100;
    current.stock = 25;
    store.warehouses.add(current);

    LocationResolver resolver = new MapLocationResolver(
        Map.of("LOC1", new Location("LOC1", 5, 500))
    );
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "BU1";
    replacement.location = "LOC1";
    replacement.capacity = 20;
    replacement.stock = 25;

    WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> useCase.replace(replacement));

    assertEquals(409, exception.getResponse().getStatus());
  }

  @Test
  void rejectsWhenCapacityExceedsLocationMaximum() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU1";
    current.location = "LOC1";
    current.capacity = 100;
    current.stock = 10;
    store.warehouses.add(current);

    LocationResolver resolver = new MapLocationResolver(
        Map.of("LOC1", new Location("LOC1", 5, 150))
    );

    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "BU1";
    replacement.location = "LOC1";
    replacement.capacity = 200;
    replacement.stock = 10;

    WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> useCase.replace(replacement));

    assertEquals(409, exception.getResponse().getStatus());
  }

  @Test
  void rejectsWhenResultingCapacityExceedsLocationLimit() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU1";
    current.location = "LOC1";
    current.capacity = 100;
    current.stock = 10;
    store.warehouses.add(current);

    Warehouse other = new Warehouse();
    other.businessUnitCode = "BU2";
    other.location = "LOC1";
    other.capacity = 350;
    other.stock = 10;
    store.warehouses.add(other);

    LocationResolver resolver = new MapLocationResolver(
        Map.of("LOC1", new Location("LOC1", 5, 400))
    );

    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "BU1";
    replacement.location = "LOC1";
    replacement.capacity = 150;
    replacement.stock = 10;

    WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> useCase.replace(replacement));

    assertEquals(409, exception.getResponse().getStatus());
  }

  @Test
  void rejectsInvalidCapacityOrStockValues() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU1";
    current.location = "LOC1";
    current.capacity = 100;
    current.stock = 10;
    store.warehouses.add(current);

    LocationResolver resolver = new MapLocationResolver(
        Map.of("LOC1", new Location("LOC1", 5, 500))
    );

    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    Warehouse invalidCapacity = new Warehouse();
    invalidCapacity.businessUnitCode = "BU1";
    invalidCapacity.location = "LOC1";
    invalidCapacity.capacity = 0;
    invalidCapacity.stock = 10;

    WebApplicationException capacityException = assertThrows(WebApplicationException.class,
        () -> useCase.replace(invalidCapacity));
    assertEquals(422, capacityException.getResponse().getStatus());

    Warehouse invalidStock = new Warehouse();
    invalidStock.businessUnitCode = "BU1";
    invalidStock.location = "LOC1";
    invalidStock.capacity = 10;
    invalidStock.stock = -1;

    WebApplicationException stockException = assertThrows(WebApplicationException.class,
        () -> useCase.replace(invalidStock));
    assertEquals(422, stockException.getResponse().getStatus());
  }

  @Test
  void rejectsMissingBusinessUnitCode() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU1";
    current.location = "LOC1";
    current.capacity = 100;
    current.stock = 10;
    store.warehouses.add(current);

    LocationResolver resolver = new MapLocationResolver(
        Map.of("LOC1", new Location("LOC1", 5, 500))
    );

    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = " ";
    replacement.location = "LOC1";
    replacement.capacity = 120;
    replacement.stock = 10;

    WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> useCase.replace(replacement));

    assertEquals(422, exception.getResponse().getStatus());
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
    private final List<Warehouse> updated = new ArrayList<>();

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
      updated.add(warehouse);
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

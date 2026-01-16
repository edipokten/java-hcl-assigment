package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    validateRequiredFields(warehouse);

    warehouse.businessUnitCode = warehouse.businessUnitCode.trim();
    warehouse.location = warehouse.location.trim();

    // Business Unit Code must be unique
    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing != null) {
      throw new WebApplicationException(
              "Warehouse businessUnitCode already exists: " + warehouse.businessUnitCode, 409);
    }

    // Location must exist
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new WebApplicationException("Invalid warehouse location: " + warehouse.location, 422);
    }

    // Validate capacity/stock per warehouse
    validateCapacityAndStock(warehouse, location);

    // Validate feasibility in that location (count + summed capacity)
    List<Warehouse> activeWarehouses = warehouseStore.getAll();
    long activeCountAtLocation =
            activeWarehouses.stream().filter(w -> warehouse.location.equals(w.location)).count();

    if (activeCountAtLocation >= location.maxNumberOfWarehouses) {
      throw new WebApplicationException(
              "Max number of warehouses reached for location: " + warehouse.location, 409);
    }

    int totalCapacityAtLocation =
            activeWarehouses.stream()
                    .filter(w -> warehouse.location.equals(w.location))
                    .map(w -> w.capacity)
                    .filter(c -> c != null)
                    .mapToInt(Integer::intValue)
                    .sum();

    if (totalCapacityAtLocation + warehouse.capacity > location.maxCapacity) {
      throw new WebApplicationException(
              "Location capacity exceeded for location: " + warehouse.location, 409);
    }

    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;

    warehouseStore.create(warehouse);
  }

  private void validateRequiredFields(Warehouse warehouse) {
    if (warehouse == null) {
      throw new WebApplicationException("Request body was not set.", 422);
    }
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new WebApplicationException("Warehouse businessUnitCode was not set on request.", 422);
    }
    if (warehouse.location == null || warehouse.location.isBlank()) {
      throw new WebApplicationException("Warehouse location was not set on request.", 422);
    }
    if (warehouse.capacity == null) {
      throw new WebApplicationException("Warehouse capacity was not set on request.", 422);
    }
    if (warehouse.stock == null) {
      throw new WebApplicationException("Warehouse stock was not set on request.", 422);
    }
    if (warehouse.capacity <= 0) {
      throw new WebApplicationException("Warehouse capacity must be > 0.", 422);
    }
    if (warehouse.stock < 0) {
      throw new WebApplicationException("Warehouse stock must be >= 0.", 422);
    }
  }

  private void validateCapacityAndStock(Warehouse warehouse, Location location) {
    if (warehouse.capacity < warehouse.stock) {
      throw new WebApplicationException("Warehouse capacity must accommodate stock.", 409);
    }
    if (warehouse.capacity > location.maxCapacity) {
      throw new WebApplicationException(
              "Warehouse capacity cannot exceed location max capacity.", 409);
    }
  }
}

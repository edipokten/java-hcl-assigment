package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import java.util.Objects;

final class WarehouseUseCaseSupport {

  private WarehouseUseCaseSupport() {}

  static void validateRequiredFields(Warehouse warehouse) {
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

  static void normalizeWarehouse(Warehouse warehouse) {
    warehouse.businessUnitCode = warehouse.businessUnitCode.trim();
    warehouse.location = warehouse.location.trim();
  }

  static Location requireLocation(LocationResolver locationResolver, Warehouse warehouse) {
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new WebApplicationException("Invalid warehouse location: " + warehouse.location, 422);
    }
    return location;
  }

  static void validateCapacityAndStock(Warehouse warehouse, Location location) {
    if (warehouse.capacity < warehouse.stock) {
      throw new WebApplicationException("Warehouse capacity must accommodate stock.", 409);
    }
    validateCapacityNotExceedingLocation(warehouse, location);
  }

  static void validateCapacityNotExceedingLocation(Warehouse warehouse, Location location) {
    if (warehouse.capacity > location.maxCapacity) {
      throw new WebApplicationException(
              "Warehouse capacity cannot exceed location max capacity.", 409);
    }
  }

  static long countActiveAtLocation(List<Warehouse> activeWarehouses, String location) {
    return activeWarehouses.stream().filter(w -> location.equals(w.location)).count();
  }

  static int sumCapacityAtLocation(List<Warehouse> activeWarehouses, String location) {
    return activeWarehouses.stream()
            .filter(w -> location.equals(w.location))
            .map(w -> w.capacity)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();
  }
}

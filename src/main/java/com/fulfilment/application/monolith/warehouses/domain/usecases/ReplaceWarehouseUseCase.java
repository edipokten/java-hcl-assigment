package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    validateRequiredFields(newWarehouse);

    // normalize
    newWarehouse.businessUnitCode = newWarehouse.businessUnitCode.trim();
    newWarehouse.location = newWarehouse.location.trim();

    Warehouse current = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (current == null) {
      throw new WebApplicationException(
              "Active warehouse not found for businessUnitCode=" + newWarehouse.businessUnitCode, 404);
    }

    // Location must exist
    Location targetLocation = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (targetLocation == null) {
      throw new WebApplicationException("Invalid warehouse location: " + newWarehouse.location, 422);
    }

    // --- replacement validations ---
    // Null-safe stock/capacity comparisons
    int currentStock = current.stock == null ? 0 : current.stock;

    // 1) New capacity must accommodate old stock
    if (newWarehouse.capacity < currentStock) {
      throw new WebApplicationException("New capacity must accommodate existing stock.", 409);
    }

    // 2) Stock must match the previous warehouse
    if (!newWarehouse.stock.equals(current.stock)) {
      throw new WebApplicationException("New warehouse stock must match current warehouse stock.", 409);
    }

    List<Warehouse> activeWarehouses = warehouseStore.getAll();
    boolean movingLocation = !newWarehouse.location.equals(current.location);

    long countAtTarget =
            activeWarehouses.stream().filter(w -> newWarehouse.location.equals(w.location)).count();

    if (movingLocation && countAtTarget >= targetLocation.maxNumberOfWarehouses) {
      throw new WebApplicationException(
              "Max number of warehouses reached for location: " + newWarehouse.location, 409);
    }

    int sumCapacityAtTarget =
            activeWarehouses.stream()
                    .filter(w -> newWarehouse.location.equals(w.location))
                    .map(w -> w.capacity)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

    // ensure a single warehouse can't exceed the location cap
    if (newWarehouse.capacity > targetLocation.maxCapacity) {
      throw new WebApplicationException("Warehouse capacity cannot exceed location max capacity.", 409);
    }

    int resultingCapacityAtTarget;
    if (movingLocation) {
      resultingCapacityAtTarget = sumCapacityAtTarget + newWarehouse.capacity;
    } else {
      int currentCap = current.capacity == null ? 0 : current.capacity;
      resultingCapacityAtTarget = sumCapacityAtTarget - currentCap + newWarehouse.capacity;
    }

    if (resultingCapacityAtTarget > targetLocation.maxCapacity) {
      throw new WebApplicationException(
              "Location capacity exceeded for location: " + newWarehouse.location, 409);
    }

    // --- archive + create (history) ---
    LocalDateTime now = LocalDateTime.now();

    current.archivedAt = now;
    warehouseStore.update(current);

    Warehouse created = new Warehouse();
    created.businessUnitCode = newWarehouse.businessUnitCode;
    created.location = newWarehouse.location;
    created.capacity = newWarehouse.capacity;
    created.stock = newWarehouse.stock;
    created.createdAt = now;
    created.archivedAt = null;

    warehouseStore.create(created);
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

}

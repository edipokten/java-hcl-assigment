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
    WarehouseUseCaseSupport.validateRequiredFields(newWarehouse);

    // normalize
    WarehouseUseCaseSupport.normalizeWarehouse(newWarehouse);

    Warehouse current = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (current == null) {
      throw new WebApplicationException(
              "Active warehouse not found for businessUnitCode=" + newWarehouse.businessUnitCode, 404);
    }

    // Location must exist
    Location targetLocation = WarehouseUseCaseSupport.requireLocation(locationResolver, newWarehouse);

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
            WarehouseUseCaseSupport.countActiveAtLocation(activeWarehouses, newWarehouse.location);

    if (movingLocation && countAtTarget >= targetLocation.maxNumberOfWarehouses) {
      throw new WebApplicationException(
              "Max number of warehouses reached for location: " + newWarehouse.location, 409);
    }

    // ensure a single warehouse can't exceed the location cap
    WarehouseUseCaseSupport.validateCapacityNotExceedingLocation(newWarehouse, targetLocation);

    int sumCapacityAtTarget =
            WarehouseUseCaseSupport.sumCapacityAtLocation(activeWarehouses, newWarehouse.location);

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
}

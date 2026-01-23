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
    WarehouseUseCaseSupport.validateRequiredFields(warehouse);

    WarehouseUseCaseSupport.normalizeWarehouse(warehouse);

    // Business Unit Code must be unique
    Warehouse existing = warehouseStore.findAnyByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing != null) {
      throw new WebApplicationException(
              "Warehouse businessUnitCode already exists: " + warehouse.businessUnitCode, 409);
    }

    // Location must exist
    Location location = WarehouseUseCaseSupport.requireLocation(locationResolver, warehouse);

    // Validate capacity/stock per warehouse
    WarehouseUseCaseSupport.validateCapacityAndStock(warehouse, location);

    // Validate feasibility in that location (count + summed capacity)
    List<Warehouse> activeWarehouses = warehouseStore.getAll();
    long activeCountAtLocation =
            WarehouseUseCaseSupport.countActiveAtLocation(activeWarehouses, warehouse.location);

    if (activeCountAtLocation >= location.maxNumberOfWarehouses) {
      throw new WebApplicationException(
              "Max number of warehouses reached for location: " + warehouse.location, 409);
    }

    int totalCapacityAtLocation =
            WarehouseUseCaseSupport.sumCapacityAtLocation(activeWarehouses, warehouse.location);

    if (totalCapacityAtLocation + warehouse.capacity > location.maxCapacity) {
      throw new WebApplicationException(
              "Location capacity exceeded for location: " + warehouse.location, 409);
    }

    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;

    warehouseStore.create(warehouse);
  }
}

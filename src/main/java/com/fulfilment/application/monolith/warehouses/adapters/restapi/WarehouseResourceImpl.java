package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject WarehouseRepository warehouseRepository;

  @Inject CreateWarehouseUseCase createWarehouseUseCase;
  @Inject ReplaceWarehouseUseCase replaceWarehouseUseCase;
  @Inject ArchiveWarehouseUseCase archiveWarehouseUseCase;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domain = toDomainWarehouse(data);

    createWarehouseUseCase.create(domain);

    // return what was stored/created
    return toWarehouseResponse(domain);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    String code = requireNonBlank(id, "Warehouse id was not set.");

    var warehouse = warehouseRepository.findByBusinessUnitCode(code);
    if (warehouse == null || warehouse.archivedAt != null) {
      throw new WebApplicationException("Warehouse not found: " + id, 404);
    }

    return toWarehouseResponse(warehouse);
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    String code = requireNonBlank(id, "Warehouse id was not set.");

    var warehouse = warehouseRepository.findByBusinessUnitCode(code);
    if (warehouse == null || warehouse.archivedAt != null) {
      throw new WebApplicationException("Active warehouse not found: " + id, 404);
    }

    archiveWarehouseUseCase.archive(warehouse);
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(String businessUnitCode, @NotNull Warehouse data) {
    String code = requireNonBlank(businessUnitCode, "Warehouse businessUnitCode was not set.");

    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domain = toDomainWarehouse(data);

    // enforce path param as the identifier to replace
    domain.businessUnitCode = code;

    replaceWarehouseUseCase.replace(domain);

    // return updated state from DB (source of truth)
    var updated = warehouseRepository.findByBusinessUnitCode(domain.businessUnitCode);
    if (updated == null || updated.archivedAt != null) {
      throw new WebApplicationException("Warehouse not found after replacement.", 500);
    }

    return toWarehouseResponse(updated);
  }

  private Warehouse toWarehouseResponse(
          com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {

    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);
    return response;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomainWarehouse(Warehouse data) {
    if (data == null) {
      throw new WebApplicationException("Request body was not set.", 422);
    }

    var w = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    w.businessUnitCode = data.getBusinessUnitCode();
    w.location = data.getLocation();
    w.capacity = data.getCapacity();
    w.stock = data.getStock();
    return w;
  }

  private String requireNonBlank(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new WebApplicationException(message, 422);
    }
    return value.trim();
  }
}
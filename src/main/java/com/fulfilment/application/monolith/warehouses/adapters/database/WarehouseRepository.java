package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    // Active warehouses only
    return this.list("archivedAt is null").stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    var entity = new DbWarehouse();
    entity.businessUnitCode = warehouse.businessUnitCode;
    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.createdAt = (warehouse.createdAt != null) ? warehouse.createdAt : LocalDateTime.now();
    entity.archivedAt = warehouse.archivedAt; // should be null for active

    this.persist(entity);
  }

  @Override
  public void update(Warehouse warehouse) {
    // IMPORTANT: update only the ACTIVE warehouse row to keep history rows immutable
    DbWarehouse entity =
            this.find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
                    .firstResult();

    if (entity == null) {
      throw new IllegalStateException(
              "Active warehouse not found for businessUnitCode=" + warehouse.businessUnitCode);
    }

    // Keep createdAt stable once persisted
    if (entity.createdAt == null) {
      entity.createdAt = (warehouse.createdAt != null) ? warehouse.createdAt : LocalDateTime.now();
    }

    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.archivedAt = warehouse.archivedAt;
  }

  @Override
  public void remove(Warehouse warehouse) {
    // Remove only the ACTIVE record (do not delete history by accident)
    DbWarehouse entity =
            this.find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
                    .firstResult();

    if (entity != null) {
      this.delete(entity);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    if (buCode == null || buCode.isBlank()) {
      return null;
    }

    // Return only the ACTIVE warehouse for this BU code
    DbWarehouse entity =
            this.find("businessUnitCode = ?1 and archivedAt is null", buCode.trim()).firstResult();

    return entity == null ? null : entity.toWarehouse();
  }

  public DbWarehouse findActiveDbByBusinessUnitCode(String buCode) {
    if (buCode == null || buCode.isBlank()) {
      return null;
    }
    return find("businessUnitCode = ?1 and archivedAt is null", buCode.trim()).firstResult();
  }
}

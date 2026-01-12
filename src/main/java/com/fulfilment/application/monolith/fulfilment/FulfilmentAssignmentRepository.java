package com.fulfilment.application.monolith.fulfilment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FulfilmentAssignmentRepository implements PanacheRepository<FulfilmentAssignment> {

    public boolean existsAssignment(Long storeId, Long productId, Long warehouseId) {
        return count("storeId = ?1 and productId = ?2 and warehouseId = ?3", storeId, productId, warehouseId) > 0;
    }

    public long countDistinctWarehousesForStoreProduct(Long storeId, Long productId) {
        return (Long)
                getEntityManager()
                        .createQuery(
                                "select count(distinct f.warehouseId) from FulfilmentAssignment f where f.storeId = :s and f.productId = :p")
                        .setParameter("s", storeId)
                        .setParameter("p", productId)
                        .getSingleResult();
    }

    public long countDistinctWarehousesForStore(Long storeId) {
        return (Long)
                getEntityManager()
                        .createQuery(
                                "select count(distinct f.warehouseId) from FulfilmentAssignment f where f.storeId = :s")
                        .setParameter("s", storeId)
                        .getSingleResult();
    }

    public long countDistinctProductsForWarehouse(Long warehouseId) {
        return (Long)
                getEntityManager()
                        .createQuery(
                                "select count(distinct f.productId) from FulfilmentAssignment f where f.warehouseId = :w")
                        .setParameter("w", warehouseId)
                        .getSingleResult();
    }

    public boolean isWarehouseAlreadyUsedByStore(Long storeId, Long warehouseId) {
        return (Long)
                getEntityManager()
                        .createQuery(
                                "select count(f) from FulfilmentAssignment f where f.storeId = :s and f.warehouseId = :w")
                        .setParameter("s", storeId)
                        .setParameter("w", warehouseId)
                        .getSingleResult()
                > 0;
    }

    public boolean isProductAlreadyUsedByWarehouse(Long warehouseId, Long productId) {
        return (Long)
                getEntityManager()
                        .createQuery(
                                "select count(f) from FulfilmentAssignment f where f.warehouseId = :w and f.productId = :p")
                        .setParameter("w", warehouseId)
                        .setParameter("p", productId)
                        .getSingleResult()
                > 0;
    }
}

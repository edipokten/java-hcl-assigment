package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;

@ApplicationScoped
public class FulfilmentAssignmentService {

    @Inject FulfilmentAssignmentRepository repo;
    @Inject WarehouseRepository warehouseRepository;
    @Inject EntityManager em;

    public FulfilmentAssignmentResponse assign(Long storeId, Long productId, String warehouseBuCode) {
        validateInputs(storeId, productId, warehouseBuCode);

        Store store = Store.findById(storeId);
        if (store == null) {
            throw new WebApplicationException("Store not found: " + storeId, 404);
        }

        Product product = em.find(Product.class, productId);
        if (product == null) {
            throw new WebApplicationException("Product not found: " + productId, 404);
        }

        DbWarehouse warehouse = warehouseRepository.findActiveDbByBusinessUnitCode(warehouseBuCode);
        if (warehouse == null) {
            throw new WebApplicationException("Active warehouse not found: " + warehouseBuCode, 404);
        }

        Long warehouseId = warehouse.id;

        // Duplicate assignment (keep strict + explicit)
        if (repo.existsAssignment(storeId, productId, warehouseId)) {
            throw new WebApplicationException("Assignment already exists.", 409);
        }

        // Constraint 1: Each Product can be fulfilled by max 2 Warehouses per Store
        long whCountForStoreProduct = repo.countDistinctWarehousesForStoreProduct(storeId, productId);
        if (whCountForStoreProduct >= 2) {
            throw new WebApplicationException("A product can be fulfilled by max 2 warehouses per store.", 409);
        }

        // Constraint 2: Each Store can be fulfilled by max 3 different Warehouses
        boolean warehouseAlreadyForStore = repo.isWarehouseAlreadyUsedByStore(storeId, warehouseId);
        if (!warehouseAlreadyForStore) {
            long distinctWhForStore = repo.countDistinctWarehousesForStore(storeId);
            if (distinctWhForStore >= 3) {
                throw new WebApplicationException("A store can be fulfilled by max 3 warehouses.", 409);
            }
        }

        // Constraint 3: Each Warehouse can store max 5 types of Products
        boolean productAlreadyForWarehouse = repo.isProductAlreadyUsedByWarehouse(warehouseId, productId);
        if (!productAlreadyForWarehouse) {
            long distinctProductsForWarehouse = repo.countDistinctProductsForWarehouse(warehouseId);
            if (distinctProductsForWarehouse >= 5) {
                throw new WebApplicationException("A warehouse can fulfil max 5 product types.", 409);
            }
        }

        FulfilmentAssignment assignment =
                new FulfilmentAssignment(storeId, productId, warehouseId, LocalDateTime.now());
        repo.persist(assignment);

        return new FulfilmentAssignmentResponse(storeId, productId, warehouseBuCode.trim(), assignment.createdAt);
    }

    private void validateInputs(Long storeId, Long productId, String warehouseBuCode) {
        if (storeId == null || storeId <= 0) {
            throw new WebApplicationException("storeId is invalid.", 422);
        }
        if (productId == null || productId <= 0) {
            throw new WebApplicationException("productId is invalid.", 422);
        }
        if (warehouseBuCode == null || warehouseBuCode.isBlank()) {
            throw new WebApplicationException("warehouseBuCode is invalid.", 422);
        }
    }

    public record FulfilmentAssignmentResponse(
            Long storeId, Long productId, String warehouseBusinessUnitCode, LocalDateTime createdAt) {}
}

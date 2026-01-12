package com.fulfilment.application.monolith.fulfilment;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "fulfilment_assignment",
        uniqueConstraints =
        @UniqueConstraint(
                name = "uq_store_product_warehouse",
                columnNames = {"storeId", "productId", "warehouseId"}))
public class FulfilmentAssignment {

    @Id @GeneratedValue public Long id;

    @Column(nullable = false)
    public Long storeId;

    @Column(nullable = false)
    public Long productId;

    @Column(nullable = false)
    public Long warehouseId;

    @Column(nullable = false)
    public LocalDateTime createdAt;

    public FulfilmentAssignment() {}

    public FulfilmentAssignment(Long storeId, Long productId, Long warehouseId, LocalDateTime createdAt) {
        this.storeId = storeId;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.createdAt = createdAt;
    }
}

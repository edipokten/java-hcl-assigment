package com.fulfilment.application.monolith.fulfilment;

import static io.restassured.RestAssured.given;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class FulfilmentResourceTest {

    @Inject EntityManager em;
    @Inject FulfilmentAssignmentRepository assignmentRepo;
    @Inject WarehouseRepository warehouseRepo;

    @BeforeEach
    @Transactional
    void clean() {
        // Always clean only the bonus feature table
        assignmentRepo.deleteAll();

        // Clean ONLY test-created rows (do NOT touch seed data)
        em.createQuery("delete from Product p where p.name like 'TEST_%'").executeUpdate();
        Store.delete("name like ?1", "TEST_%");
        em.createQuery("delete from DbWarehouse w where w.businessUnitCode like 'TEST_%'").executeUpdate();

        em.flush();
    }


    @Test
    void shouldEnforceMax2WarehousesPerStoreProduct() {
        Long storeId = createStore("S1");
        Long productId = createProduct("P1");
        createWarehouse("W1");
        createWarehouse("W2");
        createWarehouse("W3");

        postAssign(storeId, productId, "W1", 201);
        postAssign(storeId, productId, "W2", 201);
        postAssign(storeId, productId, "W3", 409); // third warehouse for same store+product
    }

    @Test
    void shouldEnforceMax3WarehousesPerStore() {
        Long storeId = createStore("S1");
        Long p1 = createProduct("P1");
        Long p2 = createProduct("P2");
        Long p3 = createProduct("P3");
        Long p4 = createProduct("P4");

        createWarehouse("W1");
        createWarehouse("W2");
        createWarehouse("W3");
        createWarehouse("W4");

        postAssign(storeId, p1, "W1", 201);
        postAssign(storeId, p2, "W2", 201);
        postAssign(storeId, p3, "W3", 201);
        postAssign(storeId, p4, "W4", 409); // 4th distinct warehouse for store
    }

    @Test
    void shouldEnforceMax5ProductsPerWarehouse() {
        Long storeId = createStore("S1");
        createWarehouse("W1");

        Long p1 = createProduct("P1");
        Long p2 = createProduct("P2");
        Long p3 = createProduct("P3");
        Long p4 = createProduct("P4");
        Long p5 = createProduct("P5");
        Long p6 = createProduct("P6");

        postAssign(storeId, p1, "W1", 201);
        postAssign(storeId, p2, "W1", 201);
        postAssign(storeId, p3, "W1", 201);
        postAssign(storeId, p4, "W1", 201);
        postAssign(storeId, p5, "W1", 201);
        postAssign(storeId, p6, "W1", 409); // 6th product type for same warehouse
    }

    private void postAssign(Long storeId, Long productId, String warehouseBuCode, int expectedStatus) {
        String bu = warehouseBuCode.startsWith("TEST_") ? warehouseBuCode : "TEST_" + warehouseBuCode;

        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/fulfilment/stores/{s}/products/{p}/warehouses/{w}", storeId, productId, bu)
                .then()
                .statusCode(expectedStatus);
    }


    @Transactional
    Long createStore(String name) {
        Store s = new Store();
        s.name = "TEST_" + name;
        s.quantityProductsInStock = 0;
        s.persist();
        em.flush();
        return s.id;
    }

    @Transactional
    Long createProduct(String name) {
        Product p = new Product();
        p.name = "TEST_" + name;
        p.description = "d";
        p.price = BigDecimal.TEN;
        p.stock = 1;
        em.persist(p);
        em.flush();
        return p.id;
    }

    @Transactional
    void createWarehouse(String buCode) {
        DbWarehouse w = new DbWarehouse();
        w.businessUnitCode = "TEST_" + buCode;
        w.location = "AMSTERDAM-001";
        w.capacity = 50;
        w.stock = 10;
        w.createdAt = java.time.LocalDateTime.now();
        w.archivedAt = null;
        em.persist(w);
        em.flush();
    }

}

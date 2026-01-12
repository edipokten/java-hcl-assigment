package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreResourceLegacySyncTest {

    private static final RecordingLegacyStoreManagerGateway recorder =
            new RecordingLegacyStoreManagerGateway();

    @BeforeAll
    static void installMock() {
        // QuarkusMock is not tied to Mockito; it can install any instance as the CDI bean. :contentReference[oaicite:1]{index=1}
        QuarkusMock.installMockForType(recorder, LegacyStoreManagerGateway.class);
    }

    @BeforeEach
    @Transactional
    void reset() {
        Store.deleteAll();
        recorder.reset();
    }

    @Test
    void shouldCallLegacyOnSuccessfulCreate() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"STORE-A\",\"quantityProductsInStock\":10}")
                .when()
                .post("/store")
                .then()
                .statusCode(201);

        assertEquals(1, recorder.createCalls.get());
        assertEquals("STORE-A", recorder.lastStore.name);
        assertEquals(10, recorder.lastStore.quantityProductsInStock);
    }

    @Test
    void shouldNotCallLegacyWhenCreateFailsToCommit() {
        // first create succeeds
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"DUPLICATE\",\"quantityProductsInStock\":1}")
                .when()
                .post("/store")
                .then()
                .statusCode(201);

        recorder.reset();

        // second create violates unique constraint -> commit fails -> legacy must NOT be called
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"DUPLICATE\",\"quantityProductsInStock\":2}")
                .when()
                .post("/store")
                .then()
                .statusCode(500);

        assertEquals(0, recorder.createCalls.get());
    }

    static class RecordingLegacyStoreManagerGateway extends LegacyStoreManagerGateway {
        final AtomicInteger createCalls = new AtomicInteger();
        final AtomicInteger updateCalls = new AtomicInteger();
        volatile Store lastStore;

        @Override
        public void createStoreOnLegacySystem(Store store) {
            createCalls.incrementAndGet();
            lastStore = store;
        }

        @Override
        public void updateStoreOnLegacySystem(Store store) {
            updateCalls.incrementAndGet();
            lastStore = store;
        }

        void reset() {
            createCalls.set(0);
            updateCalls.set(0);
            lastStore = null;
        }
    }
}

package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class LegacyStoreManagerGatewayTest {

    @Test
    void createAndUpdateWriteToTemporaryFile() {
        LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway();
        Store store = new Store("LEGACY-STORE");
        store.quantityProductsInStock = 12;

        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
        assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
    }
}

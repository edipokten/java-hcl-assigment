package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class LegacyStoreManagerGatewayTest {

  @Test
  void writesTempFileForCreateAndUpdate() {
    LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway();
    Store store = new Store("ValidStore");
    store.quantityProductsInStock = 4;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
    assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
  }

  @Test
  void handlesFileErrorsGracefully() {
    LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway();
    Store store = new Store("aa");
    store.quantityProductsInStock = 1;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
  }
}

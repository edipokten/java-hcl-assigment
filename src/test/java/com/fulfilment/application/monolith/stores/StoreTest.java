package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StoreTest {

    @Test
    void constructorsInitializeFields() {
        Store named = new Store("SHOP-1");
        assertEquals("SHOP-1", named.name);
        assertEquals(0, named.quantityProductsInStock);

        Store empty = new Store();
        empty.name = "SHOP-2";
        empty.quantityProductsInStock = 7;

        assertEquals("SHOP-2", empty.name);
        assertEquals(7, empty.quantityProductsInStock);
    }
}

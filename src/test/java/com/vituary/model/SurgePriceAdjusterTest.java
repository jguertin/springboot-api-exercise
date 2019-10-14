package com.vituary.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SurgePriceAdjusterTest {
    @Test
    void getAdjustedPriceForViewWillProvideAdjustedPriceWhenFrequencyIsReached() {
        SurgePriceAdjuster adjuster = new SurgePriceAdjuster(0.10, Duration.ofMillis(100), 3);
        int basePrice = 99;
        Item item = new Item("name", "desc", basePrice, 1, UUID.randomUUID());

        int actual = adjuster.getAdjustedPriceForView(item);
        assertEquals(basePrice, actual);
        actual = adjuster.getAdjustedPriceForView(item);
        assertEquals(basePrice, actual);
        actual = adjuster.getAdjustedPriceForView(item);
        assertEquals(109, actual);
    }

    @Test
    void getAdjustedPriceForViewWillRevertWhenSurgeWindowCloses() throws Exception {
        SurgePriceAdjuster adjuster = new SurgePriceAdjuster(0.10, Duration.ofMillis(100), 2);
        int basePrice = 99;
        Item item = new Item("name", "desc", basePrice, 1, UUID.randomUUID());

        for (int i = 0; i < 2; i++) {
            adjuster.getAdjustedPriceForView(item);
        }
        // Now that pricing has surged, wait for the surge window to expire
        Thread.sleep(101);

        int actual = adjuster.getAdjustedPriceForView(item);
        assertEquals(basePrice, actual);
    }

    @Test
    void getAdjustedPriceForPurchaseWillProvideSurgePriceWhenReached() {
        SurgePriceAdjuster adjuster = new SurgePriceAdjuster(0.10, Duration.ofMillis(100), 2);
        int basePrice = 74;
        Item item = new Item("name", "desc", basePrice, 1, UUID.randomUUID());

        for (int i = 0; i < 2; i++) {
            adjuster.getAdjustedPriceForView(item);
        }
        int actual = adjuster.getAdjustedPriceForPurchase(item);

        assertEquals(81, actual);
    }

    @Test
    void getAdjustedPriceForPurchaseDoesNotIncrementForSurge() {
        SurgePriceAdjuster adjuster = new SurgePriceAdjuster(0.10, Duration.ofMillis(100), 2);
        int basePrice = 99;
        Item item = new Item("name", "desc", basePrice, 1, UUID.randomUUID());

        for (int i = 0; i < 10; i++) {
            adjuster.getAdjustedPriceForPurchase(item);
        }
        int actual = adjuster.getAdjustedPriceForPurchase(item);

        assertEquals(basePrice, actual);
    }
}

package com.vituary.model;

public interface ItemPriceAdjuster {
    int getAdjustedPriceForView(Item item);
    int getAdjustedPriceForPurchase(Item item);
}

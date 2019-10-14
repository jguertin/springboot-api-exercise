package com.vituary.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

public class SurgePriceAdjuster implements ItemPriceAdjuster {
    private double surgeMultiplier;
    private Duration timeWindow;
    private int frequencyTrigger;

    // I opted to use a Hashtable here to keep an in memory cache. I picked Hashtable and Vector since
    // they are both thread safe to access.  In a production system I would avoid keeping an in-memory
    // cache. Instead, I would likely have used a tool like Redis to hold this detail since it can
    // expire entries itself and can be shared by multiple API instances.
    private Hashtable<UUID, Vector<LocalDateTime>> accessCache = new Hashtable<>();

    public SurgePriceAdjuster(double increase, Duration timeWindow, int frequencyTrigger) {
        this.surgeMultiplier = 1 + increase;
        this.timeWindow = timeWindow;
        this.frequencyTrigger = frequencyTrigger;
    }

    /**
     * Increments the view frequency and checks if surge pricing should apply.  If so, the surge multiplier will
     * be applied to the provided price and rounded to the nearest integer.
     * @param item - The item to retrieve the adjusted price for
     * @return int - the price with the adjustment applied as appropriate
     */
    @Override
    public int getAdjustedPriceForView(Item item) {
        return getAdjustedPrice(item, true);
    }

    /**
     * Checks if surge pricing should apply.  If so, the surge multiplier will be applied to the provided price
     * and rounded to the nearest integer. This access does not count as a view.
     * @param item - The item to retrieve the adjusted price for
     * @return int - the price with the adjustment applied as appropriate
     */
    @Override
    public int getAdjustedPriceForPurchase(Item item) {
        return getAdjustedPrice(item, false);
    }

    private List<LocalDateTime> getCachedViewTimes(UUID id) {
        accessCache.putIfAbsent(id, new Vector<>());
        return accessCache.get(id);
    }

    private void updateCache(List<LocalDateTime> accessTimes, boolean includeNow) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime surgeWindowStart = now.minus(timeWindow);
        accessTimes.removeIf(previous -> previous.isBefore(surgeWindowStart));
        if (includeNow) {
            accessTimes.add(now);
        }
    }

    private int getAdjustedPrice(Item item, boolean incrementViewCount) {
        List<LocalDateTime> accessTimes = getCachedViewTimes(item.getId());
        updateCache(accessTimes, incrementViewCount);

        if (accessTimes.size() >= frequencyTrigger) {
            return (int) Math.round(item.getPrice() * surgeMultiplier);
        }
        return item.getPrice();
    }
}


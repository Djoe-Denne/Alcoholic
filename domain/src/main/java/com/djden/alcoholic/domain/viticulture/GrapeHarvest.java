package com.djden.alcoholic.domain.viticulture;

import java.util.Objects;

public record GrapeHarvest<I>(
        Vine<I> vine,
        double quantity,
        double quality,
        double sugar,
        double acidity
) {
    public GrapeHarvest {
        Objects.requireNonNull(vine, "vine");
        if (vine.growthStage() != VineGrowthStage.DORMANT) {
            throw new IllegalArgumentException("a harvested vine must be dormant");
        }
        if (!Double.isFinite(quantity) || quantity < 0.0) {
            throw new IllegalArgumentException("quantity must be non-negative and finite");
        }
        requireUnitInterval(quality, "quality");
        requireUnitInterval(sugar, "sugar");
        requireUnitInterval(acidity, "acidity");
    }

    public Vine<I> postHarvestVine() {
        return vine;
    }

    private static void requireUnitInterval(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must be between 0 and 1");
        }
    }
}

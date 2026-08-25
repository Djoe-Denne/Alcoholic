package com.djden.alcoholic.domain.viticulture;

import java.util.Objects;

public record Vine<I>(
        VineVariety<I> variety,
        VineGrowthStage growthStage,
        int ageCycles,
        boolean hasEstablished,
        VineHealth health,
        PruningLevel pruningLevel,
        double growthProgress,
        long lastHarvest
) {
    public static final long NO_HARVEST = -1L;

    public Vine {
        Objects.requireNonNull(variety, "variety");
        Objects.requireNonNull(growthStage, "growthStage");
        Objects.requireNonNull(health, "health");
        Objects.requireNonNull(pruningLevel, "pruningLevel");
        if (ageCycles < 0) {
            throw new IllegalArgumentException("ageCycles must not be negative");
        }
        if (ageCycles > 0 && !hasEstablished) {
            throw new IllegalArgumentException("aged vines must have established");
        }
        if (!Double.isFinite(growthProgress)
                || growthProgress < 0.0
                || growthProgress >= 1.0) {
            throw new IllegalArgumentException("growthProgress must be between 0 inclusive and 1 exclusive");
        }
        if (lastHarvest < NO_HARVEST) {
            throw new IllegalArgumentException("lastHarvest must be -1 or a non-negative time");
        }
        if (ageCycles == 0 && lastHarvest != NO_HARVEST) {
            throw new IllegalArgumentException("a never-harvested vine must use NO_HARVEST");
        }
        if (ageCycles > 0 && lastHarvest == NO_HARVEST) {
            throw new IllegalArgumentException("a harvested vine must have a harvest time");
        }
        if (growthStage == VineGrowthStage.PLANTED
                && (ageCycles != 0 || hasEstablished)) {
            throw new IllegalArgumentException("a planted vine must be new and unestablished");
        }
        if (growthStage == VineGrowthStage.DORMANT
                && (!hasEstablished || ageCycles == 0)) {
            throw new IllegalArgumentException("a dormant vine must have completed a cycle");
        }
        if (hasEstablished && (growthStage == VineGrowthStage.ESTABLISHING
                || growthStage == VineGrowthStage.VEGETATIVE)) {
            throw new IllegalArgumentException(
                    "an established vine cannot return to an early growth stage"
            );
        }
    }

    public Vine(
            VineVariety<I> variety,
            VineGrowthStage growthStage,
            int ageCycles,
            boolean hasEstablished,
            VineHealth health,
            PruningLevel pruningLevel
    ) {
        this(
                variety,
                growthStage,
                ageCycles,
                hasEstablished,
                health,
                pruningLevel,
                0.0,
                defaultLastHarvest(ageCycles)
        );
    }

    public Vine(
            VineVariety<I> variety,
            VineGrowthStage growthStage,
            int ageCycles,
            boolean hasEstablished,
            VineHealth health
    ) {
        this(variety, growthStage, ageCycles, hasEstablished, health, PruningLevel.BALANCED);
    }

    public static <I> Vine<I> planted(VineVariety<I> variety) {
        return new Vine<>(
                variety,
                VineGrowthStage.PLANTED,
                0,
                false,
                VineHealth.HEALTHY,
                PruningLevel.BALANCED,
                0.0,
                NO_HARVEST
        );
    }

    public Vine<I> withHealth(VineHealth newHealth) {
        return new Vine<>(
                variety,
                growthStage,
                ageCycles,
                hasEstablished,
                newHealth,
                pruningLevel,
                growthProgress,
                lastHarvest
        );
    }

    public Vine<I> withPruningLevel(PruningLevel newPruningLevel) {
        return new Vine<>(
                variety,
                growthStage,
                ageCycles,
                hasEstablished,
                health,
                newPruningLevel,
                growthProgress,
                lastHarvest
        );
    }

    Vine<I> transitionTo(VineGrowthStage nextStage) {
        return new Vine<>(
                variety,
                nextStage,
                ageCycles,
                hasEstablished,
                health,
                pruningLevel,
                0.0,
                lastHarvest
        );
    }

    Vine<I> withGrowthProgress(double newGrowthProgress) {
        return new Vine<>(
                variety,
                growthStage,
                ageCycles,
                hasEstablished,
                health,
                pruningLevel,
                newGrowthProgress,
                lastHarvest
        );
    }

    Vine<I> afterHarvest(long harvestTime) {
        return new Vine<>(
                variety,
                VineGrowthStage.DORMANT,
                ageCycles + 1,
                true,
                health,
                pruningLevel,
                0.0,
                harvestTime
        );
    }

    private static long defaultLastHarvest(int ageCycles) {
        return ageCycles == 0 ? NO_HARVEST : 0L;
    }
}

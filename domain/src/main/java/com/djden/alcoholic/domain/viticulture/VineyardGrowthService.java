package com.djden.alcoholic.domain.viticulture;

import java.util.Objects;

public final class VineyardGrowthService {
    private final VineGrowthConfig config;

    public VineyardGrowthService() {
        this(VineGrowthConfig.defaults());
    }

    public VineyardGrowthService(VineGrowthConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    public VineGrowthConfig config() {
        return config;
    }

    public <I> Vine<I> advance(Vine<I> vine, VineGrowthParameters parameters) {
        Objects.requireNonNull(vine, "vine");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.roll() >= growthChance(vine, parameters)) {
            return vine;
        }

        double updatedProgress = vine.growthProgress() + config.progressIncrement();
        if (updatedProgress < 1.0) {
            return vine.withGrowthProgress(updatedProgress);
        }

        VineGrowthStage nextStage = nextStage(vine);
        return nextStage == vine.growthStage() ? vine : vine.transitionTo(nextStage);
    }

    public <I> Vine<I> grow(Vine<I> vine, VineGrowthParameters parameters) {
        return advance(vine, parameters);
    }

    public <I> Vine<I> grow(
            Vine<I> vine,
            VineEnvironment environment,
            double roll
    ) {
        return advance(vine, new VineGrowthParameters(environment, roll));
    }

    public <I> Vine<I> fertilize(Vine<I> vine) {
        Objects.requireNonNull(vine, "vine");
        if (vine.growthStage() == VineGrowthStage.HARVEST_READY) {
            return vine;
        }
        VineGrowthStage nextStage = nextStage(vine);
        return nextStage == vine.growthStage() ? vine : vine.transitionTo(nextStage);
    }

    public <I> double growthChance(Vine<I> vine, VineGrowthParameters parameters) {
        Objects.requireNonNull(vine, "vine");
        Objects.requireNonNull(parameters, "parameters");
        if (vine.growthStage() == VineGrowthStage.HARVEST_READY) {
            return 0.0;
        }
        double chance = config.baseGrowthChance()
                * config.climateProfile().suitability(parameters.environment())
                * vine.health().growthMultiplier()
                * parameters.trellisingMultiplier();
        return clamp(chance, 0.0, 1.0);
    }

    public <I> double growthChance(
            Vine<I> vine,
            VineEnvironment environment,
            double trellisingMultiplier
    ) {
        return growthChance(
                vine,
                new VineGrowthParameters(environment, trellisingMultiplier, 0.0)
        );
    }

    private static VineGrowthStage nextStage(Vine<?> vine) {
        return switch (vine.growthStage()) {
            case PLANTED -> VineGrowthStage.ESTABLISHING;
            case ESTABLISHING -> VineGrowthStage.VEGETATIVE;
            case VEGETATIVE, DORMANT -> VineGrowthStage.FLOWERING;
            case FLOWERING -> VineGrowthStage.GREEN_FRUIT;
            case GREEN_FRUIT -> VineGrowthStage.RIPENING;
            case RIPENING -> VineGrowthStage.HARVEST_READY;
            case HARVEST_READY -> VineGrowthStage.HARVEST_READY;
        };
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}

package com.djden.alcoholic.domain.viticulture;

import java.util.Objects;

public final class VineyardHarvestService {
    private final GrapeHarvestConfig config;

    public VineyardHarvestService() {
        this(GrapeHarvestConfig.defaults());
    }

    public VineyardHarvestService(GrapeHarvestConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    public GrapeHarvestConfig config() {
        return config;
    }

    public <I> GrapeHarvest<I> harvest(
            Vine<I> vine,
            GrapeHarvestParameters parameters
    ) {
        Objects.requireNonNull(vine, "vine");
        Objects.requireNonNull(parameters, "parameters");
        if (vine.growthStage() != VineGrowthStage.HARVEST_READY) {
            throw new IllegalStateException("only a harvest-ready vine can be harvested");
        }

        double suitability = config.climateProfile().suitability(parameters.environment());
        double warmth = config.climateProfile().warmth(parameters.environment());
        PruningProfile pruning = config.pruningProfile(vine.pruningLevel());

        double quantity = clamp(
                config.baseQuantity()
                        * vine.health().yieldMultiplier()
                        * pruning.yieldMultiplier()
                        * parameters.trellisingMultiplier()
                        * (0.5 + 0.5 * suitability),
                0.0,
                config.maximumQuantity()
        );
        double quality = clamp(
                (config.baseQuality()
                        + config.suitabilityQualityBonus() * suitability
                        + vine.health().qualityModifier()
                        + config.trellisingQualityEffect()
                        * (parameters.trellisingMultiplier() - 1.0))
                        * pruning.qualityMultiplier(),
                0.0,
                1.0
        );
        double sugar = clamp(
                config.baseSugar() + config.warmthSugarEffect() * warmth,
                0.0,
                1.0
        );
        double acidity = clamp(
                config.baseAcidity() - config.warmthAcidityEffect() * warmth,
                0.0,
                1.0
        );

        return new GrapeHarvest<>(
                vine.afterHarvest(parameters.harvestTime()),
                quantity,
                quality,
                sugar,
                acidity
        );
    }

    public <I> GrapeHarvest<I> harvest(
            Vine<I> vine,
            VineEnvironment environment
    ) {
        return harvest(vine, new GrapeHarvestParameters(environment));
    }

    public <I> GrapeHarvest<I> harvest(
            Vine<I> vine,
            VineEnvironment environment,
            long harvestTime
    ) {
        return harvest(vine, GrapeHarvestParameters.at(environment, harvestTime));
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}

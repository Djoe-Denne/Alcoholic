package com.djden.alcoholic.minecraft.viticulture;

import com.djden.alcoholic.application.viticulture.VineVarieties;
import com.djden.alcoholic.domain.viticulture.ClimateProfile;
import com.djden.alcoholic.domain.viticulture.GrapeHarvestConfig;
import com.djden.alcoholic.domain.viticulture.PruningLevel;
import com.djden.alcoholic.domain.viticulture.PruningProfile;
import com.djden.alcoholic.domain.viticulture.VineEnvironment;
import com.djden.alcoholic.domain.viticulture.VineGrowthConfig;
import com.djden.alcoholic.domain.viticulture.VineVariety;
import com.djden.alcoholic.api.ResourceId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable, reloadable viticulture configuration.
 */
public record ViticultureSettings(
        Map<ResourceId, VarietySettings> varieties,
        TrainingMultipliers untrained,
        TrainingMultipliers trained,
        int maxWireDistance
) {
    public static final int DEFAULT_MAX_WIRE_DISTANCE = 32;

    public ViticultureSettings {
        Objects.requireNonNull(varieties, "varieties");
        Objects.requireNonNull(untrained, "untrained");
        Objects.requireNonNull(trained, "trained");
        if (maxWireDistance < 2 || maxWireDistance > 256) {
            throw new IllegalArgumentException("maxWireDistance must be between 2 and 256");
        }

        LinkedHashMap<ResourceId, VarietySettings> copy = new LinkedHashMap<>();
        varieties.forEach((id, settings) -> {
            Objects.requireNonNull(id, "variety id");
            Objects.requireNonNull(settings, "variety settings");
            if (!id.equals(settings.variety().id())) {
                throw new IllegalArgumentException(
                        "variety settings key does not match its id: " + id
                );
            }
            copy.put(id, settings);
        });
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("at least one vine variety is required");
        }
        varieties = Map.copyOf(copy);
    }

    public static ViticultureSettings defaults() {
        VarietySettings red = defaultsFor(
                VineVarieties.RED_GRAPE,
                new ClimateProfile(
                        new VineEnvironment(25.0, 0.50, 0.78),
                        11.0,
                        0.35,
                        0.50
                )
        );
        VarietySettings white = defaultsFor(
                VineVarieties.WHITE_GRAPE,
                new ClimateProfile(
                        new VineEnvironment(18.0, 0.65, 0.72),
                        9.0,
                        0.30,
                        0.50
                )
        );
        return new ViticultureSettings(
                Map.of(
                        red.variety().id(), red,
                        white.variety().id(), white
                ),
                new TrainingMultipliers(0.70, 0.85),
                new TrainingMultipliers(1.0, 1.0),
                DEFAULT_MAX_WIRE_DISTANCE
        );
    }

    public VarietySettings forVariety(ResourceId id) {
        VarietySettings settings = varieties.get(Objects.requireNonNull(id, "id"));
        if (settings == null) {
            throw new IllegalArgumentException("unknown vine variety: " + id);
        }
        return settings;
    }

    public VarietySettings forVariety(VineVariety<ResourceId> variety) {
        return forVariety(Objects.requireNonNull(variety, "variety").id());
    }

    public TrainingMultipliers training(boolean isTrained) {
        return isTrained ? trained : untrained;
    }

    public ViticultureSettings withVariety(VarietySettings settings) {
        Objects.requireNonNull(settings, "settings");
        LinkedHashMap<ResourceId, VarietySettings> updated = new LinkedHashMap<>(varieties);
        updated.put(settings.variety().id(), settings);
        return new ViticultureSettings(updated, untrained, trained, maxWireDistance);
    }

    public ViticultureSettings withInfrastructure(
            TrainingMultipliers newUntrained,
            TrainingMultipliers newTrained,
            int newMaxWireDistance
    ) {
        return new ViticultureSettings(
                varieties,
                newUntrained,
                newTrained,
                newMaxWireDistance
        );
    }

    private static VarietySettings defaultsFor(
            VineVariety<ResourceId> variety,
            ClimateProfile climate
    ) {
        VineGrowthConfig growth = new VineGrowthConfig(0.35, climate, 0.25);
        GrapeHarvestConfig harvestDefaults = GrapeHarvestConfig.defaults();
        GrapeHarvestConfig harvest = new GrapeHarvestConfig(
                harvestDefaults.baseQuantity(),
                harvestDefaults.maximumQuantity(),
                harvestDefaults.baseQuality(),
                harvestDefaults.baseSugar(),
                harvestDefaults.baseAcidity(),
                harvestDefaults.suitabilityQualityBonus(),
                harvestDefaults.warmthSugarEffect(),
                harvestDefaults.warmthAcidityEffect(),
                harvestDefaults.trellisingQualityEffect(),
                climate,
                Map.of(
                        PruningLevel.LIGHT, new PruningProfile(1.20, 0.92),
                        PruningLevel.BALANCED, new PruningProfile(1.00, 1.00),
                        PruningLevel.SEVERE, new PruningProfile(0.75, 1.12)
                )
        );
        return new VarietySettings(variety, growth, harvest);
    }

    public record VarietySettings(
            VineVariety<ResourceId> variety,
            VineGrowthConfig growth,
            GrapeHarvestConfig harvest
    ) {
        public VarietySettings {
            Objects.requireNonNull(variety, "variety");
            Objects.requireNonNull(growth, "growth");
            Objects.requireNonNull(harvest, "harvest");
        }
    }

    public record TrainingMultipliers(double yield, double quality) {
        public TrainingMultipliers {
            requireMultiplier(yield, "yield");
            requireMultiplier(quality, "quality");
        }

        private static void requireMultiplier(double value, String name) {
            if (!Double.isFinite(value) || value < 0.0 || value > 2.0) {
                throw new IllegalArgumentException(name + " must be between 0 and 2");
            }
        }
    }
}

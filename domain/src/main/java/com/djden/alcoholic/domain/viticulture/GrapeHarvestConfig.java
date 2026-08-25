package com.djden.alcoholic.domain.viticulture;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record GrapeHarvestConfig(
        double baseQuantity,
        double maximumQuantity,
        double baseQuality,
        double baseSugar,
        double baseAcidity,
        double suitabilityQualityBonus,
        double warmthSugarEffect,
        double warmthAcidityEffect,
        double trellisingQualityEffect,
        ClimateProfile climateProfile,
        Map<PruningLevel, PruningProfile> pruningProfiles
) {
    public GrapeHarvestConfig {
        requireNonNegativeFinite(baseQuantity, "baseQuantity");
        if (!Double.isFinite(maximumQuantity) || maximumQuantity <= 0.0) {
            throw new IllegalArgumentException("maximumQuantity must be positive and finite");
        }
        if (baseQuantity > maximumQuantity) {
            throw new IllegalArgumentException("baseQuantity must not exceed maximumQuantity");
        }
        requireUnitInterval(baseQuality, "baseQuality");
        requireUnitInterval(baseSugar, "baseSugar");
        requireUnitInterval(baseAcidity, "baseAcidity");
        requireUnitInterval(suitabilityQualityBonus, "suitabilityQualityBonus");
        requireUnitInterval(warmthSugarEffect, "warmthSugarEffect");
        requireUnitInterval(warmthAcidityEffect, "warmthAcidityEffect");
        requireUnitInterval(trellisingQualityEffect, "trellisingQualityEffect");
        Objects.requireNonNull(climateProfile, "climateProfile");
        Objects.requireNonNull(pruningProfiles, "pruningProfiles");

        EnumMap<PruningLevel, PruningProfile> profiles =
                new EnumMap<>(PruningLevel.class);
        profiles.putAll(pruningProfiles);
        for (PruningLevel level : PruningLevel.values()) {
            if (profiles.get(level) == null) {
                throw new IllegalArgumentException("missing pruning profile for " + level);
            }
        }
        pruningProfiles = Map.copyOf(profiles);
    }

    public static GrapeHarvestConfig defaults() {
        return new GrapeHarvestConfig(
                12.0,
                30.0,
                0.45,
                0.50,
                0.50,
                0.25,
                0.20,
                0.20,
                0.08,
                ClimateProfile.TEMPERATE,
                Map.of(
                        PruningLevel.LIGHT, new PruningProfile(1.20, 0.90),
                        PruningLevel.BALANCED, new PruningProfile(1.00, 1.00),
                        PruningLevel.SEVERE, new PruningProfile(0.70, 1.15)
                )
        );
    }

    public PruningProfile pruningProfile(PruningLevel level) {
        return pruningProfiles.get(Objects.requireNonNull(level, "level"));
    }

    private static void requireNonNegativeFinite(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be non-negative and finite");
        }
    }

    private static void requireUnitInterval(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must be between 0 and 1");
        }
    }
}

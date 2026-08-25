package com.djden.alcoholic.domain.process;

import com.djden.alcoholic.api.ResourceId;

import java.util.Objects;

/**
 * Data-driven kiln intensity for solid malting. Addons add more profiles
 * through process JSON; this type has no drink-family subclasses.
 */
public record KilnProfile(
        ResourceId id,
        double colorPotential,
        double fermentablePotential,
        double roastIntensity
) {
    public KilnProfile {
        Objects.requireNonNull(id, "id");
        colorPotential = clampUnit(colorPotential);
        fermentablePotential = clampUnit(fermentablePotential);
        roastIntensity = clampUnit(roastIntensity);
    }

    public static KilnProfile pale() {
        return new KilnProfile(ResourceId.parse("alcoholic:pale"), 0.12, 0.85, 0.15);
    }

    public static KilnProfile amber() {
        return new KilnProfile(ResourceId.parse("alcoholic:amber"), 0.35, 0.78, 0.45);
    }

    public static KilnProfile dark() {
        return new KilnProfile(ResourceId.parse("alcoholic:dark"), 0.62, 0.70, 0.80);
    }

    private static double clampUnit(double value) {
        if (!Double.isFinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}

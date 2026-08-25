package com.djden.alcoholic.integration.vinery;

import com.djden.alcoholic.application.viticulture.GrapeProviderPort;
import com.djden.alcoholic.domain.ingredient.GrapeColor;
import com.djden.alcoholic.domain.viticulture.VineVariety;
import com.djden.alcoholic.api.ResourceId;

import java.util.Objects;
import java.util.function.Predicate;

public final class VineryGrapeProvider implements GrapeProviderPort {
    private final Predicate<ResourceId> resourceAvailability;

    public VineryGrapeProvider(Predicate<ResourceId> resourceAvailability) {
        this.resourceAvailability = Objects.requireNonNull(
                resourceAvailability,
                "resourceAvailability"
        );
    }

    @Override
    public ResourceId getPlantingMaterial(VineVariety<ResourceId> variety) {
        return switch (grapeColor(variety)) {
            case RED -> VineryIntegration.RED_GRAPE_SEEDS;
            case WHITE -> VineryIntegration.WHITE_GRAPE_SEEDS;
        };
    }

    @Override
    public ResourceId getHarvestItem(VineVariety<ResourceId> variety) {
        return switch (grapeColor(variety)) {
            case RED -> VineryIntegration.RED_GRAPE;
            case WHITE -> VineryIntegration.WHITE_GRAPE;
        };
    }

    @Override
    public boolean isAvailable(VineVariety<ResourceId> variety) {
        return resourceAvailability.test(getPlantingMaterial(variety))
                && resourceAvailability.test(getHarvestItem(variety));
    }

    private static GrapeColor grapeColor(VineVariety<ResourceId> variety) {
        return Objects.requireNonNull(variety, "variety").grapeColor();
    }
}

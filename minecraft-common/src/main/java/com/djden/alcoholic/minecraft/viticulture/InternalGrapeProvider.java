package com.djden.alcoholic.minecraft.viticulture;

import com.djden.alcoholic.application.viticulture.GrapeProviderPort;
import com.djden.alcoholic.application.viticulture.VineVarieties;
import com.djden.alcoholic.domain.viticulture.VineVariety;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.api.ResourceId;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Resolves Alcoholic's built-in grape and cutting items.
 */
public final class InternalGrapeProvider implements GrapeProviderPort {
    @Override
    public ResourceId getPlantingMaterial(VineVariety<ResourceId> variety) {
        return isRed(variety)
                ? AlcoholicIds.RED_GRAPE_CUTTING
                : requireWhite(variety, AlcoholicIds.WHITE_GRAPE_CUTTING);
    }

    @Override
    public ResourceId getHarvestItem(VineVariety<ResourceId> variety) {
        return isRed(variety)
                ? AlcoholicIds.RED_GRAPES
                : requireWhite(variety, AlcoholicIds.WHITE_GRAPES);
    }

    @Override
    public boolean isAvailable(VineVariety<ResourceId> variety) {
        try {
            return isRegistered(getPlantingMaterial(variety))
                    && isRegistered(getHarvestItem(variety));
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private static boolean isRegistered(ResourceId id) {
        return Registry.ITEM.getOptional(toMinecraft(id)).isPresent();
    }

    private static boolean isRed(VineVariety<ResourceId> variety) {
        Objects.requireNonNull(variety, "variety");
        return VineVarieties.RED_GRAPE.id().equals(variety.id());
    }

    private static ResourceId requireWhite(
            VineVariety<ResourceId> variety,
            ResourceId result
    ) {
        Objects.requireNonNull(variety, "variety");
        if (!VineVarieties.WHITE_GRAPE.id().equals(variety.id())) {
            throw new IllegalArgumentException("unsupported internal vine variety: " + variety.id());
        }
        return result;
    }

    static ResourceLocation toMinecraft(ResourceId id) {
        return ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path());
    }
}

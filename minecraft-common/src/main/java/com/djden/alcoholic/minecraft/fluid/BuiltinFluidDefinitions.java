package com.djden.alcoholic.minecraft.fluid;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;

import java.util.List;
import java.util.Set;

public final class BuiltinFluidDefinitions {
    public static final FluidFlowProfile SUGAR_RICH = new FluidFlowProfile(
            1060,
            300,
            1800,
            8,
            4,
            1,
            false
    );
    public static final FluidFlowProfile FERMENTED = new FluidFlowProfile(
            1000,
            300,
            1200,
            5,
            4,
            1,
            false
    );

    public static final Set<ResourceId> PAINTED_ANIMATED = Set.of(
            AlcoholicIds.RED_GRAPE_MUST,
            AlcoholicIds.WHITE_GRAPE_MUST,
            AlcoholicIds.HOPPED_WORT,
            AlcoholicIds.BEER
    );

    private static final int PAINTED_TEXTURE_TINT = 0xFFFFFFFF;
    private static final ResourceId WATER_STILL = ResourceId.parse("minecraft:block/water_still");
    private static final ResourceId WATER_FLOW = ResourceId.parse("minecraft:block/water_flow");
    private static final List<FluidDefinition> ALL = List.of(
            painted(AlcoholicIds.RED_GRAPE_MUST, SUGAR_RICH),
            painted(AlcoholicIds.WHITE_GRAPE_MUST, SUGAR_RICH),
            tintedWater(AlcoholicIds.YOUNG_RED_WINE, 0xFF5A1226, FERMENTED),
            tintedWater(AlcoholicIds.YOUNG_WHITE_WINE, 0xFFE8D36B, FERMENTED),
            tintedWater(AlcoholicIds.RED_WINE, 0xFF4A0E1C, FERMENTED),
            tintedWater(AlcoholicIds.WHITE_WINE, 0xFFE6C85A, FERMENTED),
            tintedWater(AlcoholicIds.WORT, 0xFFC9A227, SUGAR_RICH),
            painted(AlcoholicIds.HOPPED_WORT, SUGAR_RICH),
            painted(AlcoholicIds.BEER, FERMENTED)
    );

    private BuiltinFluidDefinitions() {
    }

    public static List<FluidDefinition> all() {
        return ALL;
    }

    private static FluidDefinition painted(ResourceId id, FluidFlowProfile profile) {
        return new FluidDefinition(
                id,
                new ResourceId(AlcoholicIds.MOD_ID, "block/" + id.path() + "_still"),
                new ResourceId(AlcoholicIds.MOD_ID, "block/" + id.path() + "_flow"),
                PAINTED_TEXTURE_TINT,
                profile
        );
    }

    private static FluidDefinition tintedWater(ResourceId id, int tint, FluidFlowProfile profile) {
        return new FluidDefinition(id, WATER_STILL, WATER_FLOW, tint, profile);
    }
}

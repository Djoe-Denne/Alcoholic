package com.djden.alcoholic.minecraft.fluid;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;

import java.util.List;

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

    private static final ResourceId WATER_STILL = ResourceId.parse("minecraft:block/water_still");
    private static final ResourceId WATER_FLOW = ResourceId.parse("minecraft:block/water_flow");
    private static final List<FluidDefinition> ALL = List.of(
            fluid(AlcoholicIds.RED_GRAPE_MUST, 0xFF7A1F3A, SUGAR_RICH),
            fluid(AlcoholicIds.WHITE_GRAPE_MUST, 0xFFE6D56A, SUGAR_RICH),
            fluid(AlcoholicIds.YOUNG_RED_WINE, 0xFF5A1226, FERMENTED),
            fluid(AlcoholicIds.YOUNG_WHITE_WINE, 0xFFE8D36B, FERMENTED),
            fluid(AlcoholicIds.RED_WINE, 0xFF4A0E1C, FERMENTED),
            fluid(AlcoholicIds.WHITE_WINE, 0xFFE6C85A, FERMENTED),
            fluid(AlcoholicIds.WORT, 0xFFC9A227, SUGAR_RICH),
            fluid(AlcoholicIds.HOPPED_WORT, 0xFFB8860B, SUGAR_RICH),
            fluid(AlcoholicIds.BEER, 0xFFD4A017, FERMENTED)
    );

    private BuiltinFluidDefinitions() {
    }

    public static List<FluidDefinition> all() {
        return ALL;
    }

    private static FluidDefinition fluid(ResourceId id, int tint, FluidFlowProfile profile) {
        return new FluidDefinition(id, WATER_STILL, WATER_FLOW, tint, profile);
    }
}

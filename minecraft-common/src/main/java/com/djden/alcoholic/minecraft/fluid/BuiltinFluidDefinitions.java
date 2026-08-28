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

    private static final int PAINTED_TEXTURE_TINT = 0xFFFFFFFF;
    private static final List<FluidDefinition> ALL = List.of(
            fluid(AlcoholicIds.RED_GRAPE_MUST, SUGAR_RICH),
            fluid(AlcoholicIds.WHITE_GRAPE_MUST, SUGAR_RICH),
            fluid(AlcoholicIds.YOUNG_RED_WINE, FERMENTED),
            fluid(AlcoholicIds.YOUNG_WHITE_WINE, FERMENTED),
            fluid(AlcoholicIds.RED_WINE, FERMENTED),
            fluid(AlcoholicIds.WHITE_WINE, FERMENTED),
            fluid(AlcoholicIds.WORT, SUGAR_RICH),
            fluid(AlcoholicIds.HOPPED_WORT, SUGAR_RICH),
            fluid(AlcoholicIds.BEER, FERMENTED)
    );

    private BuiltinFluidDefinitions() {
    }

    public static List<FluidDefinition> all() {
        return ALL;
    }

    private static FluidDefinition fluid(ResourceId id, FluidFlowProfile profile) {
        return new FluidDefinition(
                id,
                new ResourceId(AlcoholicIds.MOD_ID, "block/" + id.path() + "_still"),
                new ResourceId(AlcoholicIds.MOD_ID, "block/" + id.path() + "_flow"),
                PAINTED_TEXTURE_TINT,
                profile
        );
    }
}

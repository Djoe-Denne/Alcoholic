package com.djden.alcoholic.application.viticulture;

import com.djden.alcoholic.domain.ingredient.GrapeColor;
import com.djden.alcoholic.domain.viticulture.VineVariety;
import com.djden.alcoholic.api.ResourceId;

public final class VineVarieties {
    public static final String MOD_ID = "alcoholic";
    public static final VineVariety<ResourceId> RED_GRAPE = new VineVariety<>(
            new ResourceId(MOD_ID, "red_grape"),
            GrapeColor.RED
    );
    public static final VineVariety<ResourceId> WHITE_GRAPE = new VineVariety<>(
            new ResourceId(MOD_ID, "white_grape"),
            GrapeColor.WHITE
    );

    private VineVarieties() {
    }
}

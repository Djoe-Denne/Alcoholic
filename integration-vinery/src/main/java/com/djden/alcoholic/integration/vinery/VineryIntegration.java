package com.djden.alcoholic.integration.vinery;

import com.djden.alcoholic.api.ResourceId;

/**
 * Stable identifiers consumed by generated compatibility data.
 */
public final class VineryIntegration {
    public static final String MOD_ID = "vinery";
    public static final ResourceId RED_GRAPE_SEEDS =
            new ResourceId(MOD_ID, "red_grape_seeds");
    public static final ResourceId WHITE_GRAPE_SEEDS =
            new ResourceId(MOD_ID, "white_grape_seeds");
    public static final ResourceId RED_GRAPE = new ResourceId(MOD_ID, "red_grape");
    public static final ResourceId WHITE_GRAPE = new ResourceId(MOD_ID, "white_grape");
    public static final ResourceId RED_GRAPES_TAG = RED_GRAPE;
    public static final ResourceId WHITE_GRAPES_TAG = WHITE_GRAPE;

    private VineryIntegration() {
    }
}

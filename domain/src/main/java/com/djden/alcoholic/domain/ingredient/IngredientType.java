package com.djden.alcoholic.domain.ingredient;

import com.djden.alcoholic.api.ResourceId;

import java.util.Locale;

/**
 * Built-in agricultural categories retained for viticulture compatibility.
 * Beverage pipelines resolve open {@link ResourceId} categories instead.
 */
public enum IngredientType {
    GRAPE,
    RED_GRAPE,
    WHITE_GRAPE,
    BARLEY,
    HOPS,
    YEAST;

    public ResourceId categoryId() {
        return new ResourceId("alcoholic", name().toLowerCase(Locale.ROOT));
    }
}

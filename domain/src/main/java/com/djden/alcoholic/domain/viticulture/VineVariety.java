package com.djden.alcoholic.domain.viticulture;

import com.djden.alcoholic.domain.ingredient.GrapeColor;

import java.util.Objects;

public record VineVariety<I>(I id, GrapeColor grapeColor) {
    public VineVariety {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(grapeColor, "grapeColor");
    }
}

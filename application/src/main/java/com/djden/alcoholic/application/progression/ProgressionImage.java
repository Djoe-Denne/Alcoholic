package com.djden.alcoholic.application.progression;

import java.util.Objects;

public record ProgressionImage(String sprite, String hoverKey, double x, double y) {
    public ProgressionImage {
        Objects.requireNonNull(sprite, "sprite");
        Objects.requireNonNull(hoverKey, "hoverKey");
    }

    public String atlasSprite() {
        return "alcoholic:item/ftbquests/" + sprite;
    }
}

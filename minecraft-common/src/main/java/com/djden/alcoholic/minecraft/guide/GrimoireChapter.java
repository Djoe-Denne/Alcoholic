package com.djden.alcoholic.minecraft.guide;

import java.util.List;
import java.util.Objects;

public record GrimoireChapter(String illustrationId, String titleKey, List<String> pageKeys) {
    public GrimoireChapter {
        Objects.requireNonNull(illustrationId, "illustrationId");
        Objects.requireNonNull(titleKey, "titleKey");
        pageKeys = List.copyOf(Objects.requireNonNull(pageKeys, "pageKeys"));
        if (illustrationId.isBlank()) {
            throw new IllegalArgumentException("illustrationId is blank");
        }
        if (pageKeys.isEmpty()) {
            throw new IllegalArgumentException("pageKeys is empty");
        }
    }
}

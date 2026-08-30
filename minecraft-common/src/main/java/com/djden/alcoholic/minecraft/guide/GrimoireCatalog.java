package com.djden.alcoholic.minecraft.guide;

import com.djden.alcoholic.minecraft.content.AlcoholicIds;

import java.util.ArrayList;
import java.util.List;

public final class GrimoireCatalog {
    public static final String PLACEHOLDER_TEXTURE = "textures/gui/grimoire/placeholder.png";
    public static final int ILLUSTRATION_WIDTH = 100;
    public static final int ILLUSTRATION_HEIGHT = 56;

    private static final List<GrimoireChapter> WINE = List.of(
            chapter(GrimoireKind.WINE, 0, "frontispiece", 2),
            chapter(GrimoireKind.WINE, 1, "vineyard", 3),
            chapter(GrimoireKind.WINE, 2, "workshop", 2),
            chapter(GrimoireKind.WINE, 3, "press", 2),
            chapter(GrimoireKind.WINE, 4, "ferment", 2),
            chapter(GrimoireKind.WINE, 5, "cellar", 2),
            chapter(GrimoireKind.WINE, 6, "bottle", 2),
            chapter(GrimoireKind.WINE, 7, "warnings", 2),
            chapter(GrimoireKind.WINE, 8, "industrial", 2)
    );
    private static final List<GrimoireChapter> BEER = List.of(
            chapter(GrimoireKind.BEER, 0, "frontispiece", 2),
            chapter(GrimoireKind.BEER, 1, "fields", 3),
            chapter(GrimoireKind.BEER, 2, "brewery", 2),
            chapter(GrimoireKind.BEER, 3, "malt", 2),
            chapter(GrimoireKind.BEER, 4, "mill", 2),
            chapter(GrimoireKind.BEER, 5, "mash", 2),
            chapter(GrimoireKind.BEER, 6, "boil", 2),
            chapter(GrimoireKind.BEER, 7, "ferment", 2),
            chapter(GrimoireKind.BEER, 8, "heat", 2),
            chapter(GrimoireKind.BEER, 9, "bottle", 2),
            chapter(GrimoireKind.BEER, 10, "warnings", 2)
    );

    private GrimoireCatalog() {
    }

    public static List<GrimoireChapter> chapters(GrimoireKind kind) {
        return switch (kind) {
            case WINE -> WINE;
            case BEER -> BEER;
        };
    }

    public static String illustrationTexture(GrimoireKind kind, String illustrationId) {
        return "textures/gui/grimoire/" + kind.path() + "/" + illustrationId + ".png";
    }

    public static String itemTranslationKey(GrimoireKind kind) {
        return switch (kind) {
            case WINE -> "item.alcoholic." + AlcoholicIds.WINE_GRIMOIRE.path();
            case BEER -> "item.alcoholic." + AlcoholicIds.BEER_GRIMOIRE.path();
        };
    }

    private static GrimoireChapter chapter(GrimoireKind kind, int index, String illustrationId, int pages) {
        String prefix = "grimoire.alcoholic." + kind.path() + ".ch" + index;
        List<String> pageKeys = new ArrayList<>(pages);
        for (int page = 0; page < pages; page++) {
            pageKeys.add(prefix + ".p" + page);
        }
        return new GrimoireChapter(illustrationId, prefix + ".title", pageKeys);
    }
}

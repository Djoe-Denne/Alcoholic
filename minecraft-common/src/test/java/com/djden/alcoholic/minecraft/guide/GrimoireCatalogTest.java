package com.djden.alcoholic.minecraft.guide;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrimoireCatalogTest {
    @Test
    void everyChapterHasIllustrationAndPages() {
        for (GrimoireKind kind : GrimoireKind.values()) {
            Set<String> illustrations = new HashSet<>();
            int index = 0;
            for (GrimoireChapter chapter : GrimoireCatalog.chapters(kind)) {
                assertFalse(chapter.illustrationId().isBlank(), kind + " chapter " + index);
                assertTrue(
                        illustrations.add(chapter.illustrationId()),
                        "Duplicate illustration id " + chapter.illustrationId()
                );
                assertEquals(
                        "grimoire.alcoholic." + kind.path() + ".ch" + index + ".title",
                        chapter.titleKey()
                );
                assertFalse(chapter.pageKeys().isEmpty(), kind + " chapter " + index);
                for (int page = 0; page < chapter.pageKeys().size(); page++) {
                    assertEquals(
                            "grimoire.alcoholic." + kind.path() + ".ch" + index + ".p" + page,
                            chapter.pageKeys().get(page)
                    );
                }
                index++;
            }
        }
    }

    @Test
    void wineAndBeerKeepPlannedChapterCounts() {
        assertEquals(9, GrimoireCatalog.chapters(GrimoireKind.WINE).size());
        assertEquals(11, GrimoireCatalog.chapters(GrimoireKind.BEER).size());
    }
}

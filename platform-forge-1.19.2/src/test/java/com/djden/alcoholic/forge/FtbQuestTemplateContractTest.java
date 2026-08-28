package com.djden.alcoholic.forge;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FtbQuestTemplateContractTest {
    private static final Set<String> ADVANCEMENTS = Set.of(
            "alcoholic:root",
            "alcoholic:harvest_grapes",
            "alcoholic:harvest_hops",
            "alcoholic:produce_must",
            "alcoholic:ferment_beverage",
            "alcoholic:age_wine",
            "alcoholic:blend",
            "alcoholic:bottle"
    );
    private static final String[] FLIPBOOKS = {
            "press",
            "mash_tun",
            "fermenter",
            "barrel",
            "crock",
            "bottle"
    };
    private static final String[] HOVER_KEYS = {
            "ftbquests.alcoholic.chapter.title",
            "ftbquests.alcoholic.chapter.subtitle",
            "ftbquests.alcoholic.hover.press",
            "ftbquests.alcoholic.hover.mash_tun",
            "ftbquests.alcoholic.hover.fermenter",
            "ftbquests.alcoholic.hover.barrel",
            "ftbquests.alcoholic.hover.crock",
            "ftbquests.alcoholic.hover.bottle"
    };

    @Test
    void chapterWatchesExactlyTheAdr033Advancements() throws IOException {
        String snbt = Files.readString(chapterSnbt(), StandardCharsets.UTF_8);
        Matcher matcher = Pattern.compile("advancement:\\s*\"([^\"]+)\"").matcher(snbt);
        Set<String> found = new LinkedHashSet<>();
        while (matcher.find()) {
            found.add(matcher.group(1));
        }
        assertEquals(ADVANCEMENTS, found);
        assertEquals(8, snbt.split("type: \"advancement\"").length - 1);
        assertFalse(snbt.contains("criterion: \"harvest"));
        assertTrue(snbt.contains("criterion: \"\""));
        for (String flipbook : FLIPBOOKS) {
            assertTrue(
                    snbt.contains("alcoholic:item/ftbquests/" + flipbook),
                    "Missing chapter image " + flipbook
            );
        }
    }

    @Test
    void flipbooksAreAnimatedItemAtlasStrips() throws IOException {
        ClassLoader loader = FtbQuestTemplateContractTest.class.getClassLoader();
        for (String flipbook : FLIPBOOKS) {
            String pngPath = "assets/alcoholic/textures/item/ftbquests/" + flipbook + ".png";
            String metaPath = pngPath + ".mcmeta";
            try (InputStream png = loader.getResourceAsStream(pngPath);
                    InputStream meta = loader.getResourceAsStream(metaPath)) {
                assertNotNull(png, "Missing " + pngPath);
                assertNotNull(meta, "Missing " + metaPath);
                BufferedImage image = ImageIO.read(png);
                assertNotNull(image, "Unreadable " + pngPath);
                assertEquals(128, image.getWidth(), flipbook + " width");
                assertEquals(0, image.getHeight() % 128, flipbook + " height");
                int frames = image.getHeight() / 128;
                assertTrue(frames >= 4 && frames <= 8, flipbook + " frames=" + frames);
                JsonObject animation = JsonParser.parseReader(
                        new InputStreamReader(meta, StandardCharsets.UTF_8)
                ).getAsJsonObject().getAsJsonObject("animation");
                assertNotNull(animation, flipbook + " .mcmeta animation");
                assertTrue(animation.getAsJsonArray("frames").size() >= 4);
            }
        }
    }

    @Test
    void hoverKeysExistInEnglishAndFrench() throws IOException {
        for (String language : new String[] {"en_us", "fr_fr"}) {
            JsonObject translations = lang(language);
            for (String key : HOVER_KEYS) {
                assertTrue(translations.has(key), "Missing " + language + " " + key);
                assertFalse(translations.get(key).getAsString().isBlank(), key);
            }
        }
    }

    private static Path chapterSnbt() {
        Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        for (int i = 0; i < 6; i++) {
            Path candidate = dir.resolve("modpack/ftbquests/quests/chapters/alcoholic.snbt");
            if (Files.exists(candidate)) {
                return candidate;
            }
            Path parent = dir.getParent();
            if (parent == null) {
                break;
            }
            dir = parent;
        }
        throw new AssertionError(
                "Cannot find modpack/ftbquests/quests/chapters/alcoholic.snbt from "
                        + System.getProperty("user.dir")
        );
    }

    private static JsonObject lang(String language) throws IOException {
        String path = "assets/alcoholic/lang/" + language + ".json";
        ClassLoader loader = FtbQuestTemplateContractTest.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing " + path);
            return JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)
            ).getAsJsonObject();
        }
    }
}

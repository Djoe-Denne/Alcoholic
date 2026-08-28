package com.djden.alcoholic.forge;

import com.djden.alcoholic.application.progression.FtbQuestSnbt;
import com.djden.alcoholic.application.progression.ProgressionCatalog;
import com.djden.alcoholic.application.progression.ProgressionChapter;
import com.djden.alcoholic.application.progression.ProgressionLine;
import com.djden.alcoholic.application.progression.ProgressionNode;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FtbQuestTemplateContractTest {
    private static final String[] FLIPBOOKS = {
            "press",
            "mash_tun",
            "fermenter",
            "barrel",
            "crock",
            "bottle"
    };
    private static final String[] INDUSTRIAL_FLIPBOOKS = {
            "form_press",
            "form_vat",
            "form_tank",
            "form_malt_house",
            "form_roller_mill",
            "form_mash_tun",
            "form_kettle",
            "form_conditioning"
    };

    @Test
    void artisanalChapterMatchesCatalog() throws IOException {
        ProgressionCatalog catalog = ProgressionCatalog.official();
        String snbt = readNormalized(chapterSnbt());
        assertEquals(
                normalize(FtbQuestSnbt.render(
                        catalog.chapter(ProgressionChapter.ARTISANAL),
                        catalog.nodes(ProgressionChapter.ARTISANAL)
                )),
                snbt
        );
        assertTrue(snbt.contains("criterion: \"\""));
        assertFalse(snbt.contains("criterion: \"harvest"));
        assertColumns(catalog, ProgressionChapter.ARTISANAL, snbt);
        assertTrue(snbt.contains("min_required_dependencies: 1"));
        for (String flipbook : FLIPBOOKS) {
            assertTrue(snbt.contains("alcoholic:item/ftbquests/" + flipbook), "Missing " + flipbook);
        }
    }

    @Test
    void industrialChapterMatchesCatalog() throws IOException {
        ProgressionCatalog catalog = ProgressionCatalog.official();
        String snbt = readNormalized(industrialChapterSnbt());
        assertEquals(
                normalize(FtbQuestSnbt.render(
                        catalog.chapter(ProgressionChapter.INDUSTRIAL),
                        catalog.nodes(ProgressionChapter.INDUSTRIAL)
                )),
                snbt
        );
        assertTrue(snbt.contains("id: \"A1C0A01C00000002\""));
        assertColumns(catalog, ProgressionChapter.INDUSTRIAL, snbt);
        assertTrue(snbt.contains("min_required_dependencies: 1"));
        for (String flipbook : INDUSTRIAL_FLIPBOOKS) {
            assertTrue(snbt.contains("alcoholic:item/ftbquests/" + flipbook), "Missing " + flipbook);
        }
    }

    @Test
    void flipbooksAreAnimatedItemAtlasStrips() throws IOException {
        assertFlipbooks(FLIPBOOKS);
    }

    @Test
    void industrialFlipbooksAreAnimatedItemAtlasStrips() throws IOException {
        assertFlipbooks(INDUSTRIAL_FLIPBOOKS);
    }

    @Test
    void hoverKeysExistInEnglishAndFrench() throws IOException {
        ProgressionCatalog catalog = ProgressionCatalog.official();
        for (String language : new String[] {"en_us", "fr_fr"}) {
            JsonObject translations = lang(language);
            for (var spec : catalog.chapters()) {
                assertKey(translations, language, spec.titleKey());
                assertKey(translations, language, spec.subtitleKey());
                spec.images().forEach(image -> assertKey(translations, language, image.hoverKey()));
            }
        }
    }

    private static void assertColumns(ProgressionCatalog catalog, ProgressionChapter chapter, String snbt) {
        for (ProgressionNode node : catalog.nodes(chapter)) {
            Pattern pattern = Pattern.compile(
                    "id: \"" + node.questHex() + "\"\\s+x: (-?[0-9.]+)d",
                    Pattern.MULTILINE
            );
            Matcher matcher = pattern.matcher(snbt);
            assertTrue(matcher.find(), "Missing canvas x for " + node.id());
            double x = Double.parseDouble(matcher.group(1));
            if (node.line() == ProgressionLine.WINE) {
                assertTrue(x < 0, node.id());
            } else if (node.line() == ProgressionLine.SHARED) {
                assertEquals(0.0, x, 1e-9, node.id());
            } else {
                assertTrue(x > 0, node.id());
            }
        }
        assertEquals(
                catalog.nodes(chapter).stream().map(ProgressionNode::advancementId).collect(Collectors.toSet()).size(),
                catalog.nodes(chapter).size()
        );
    }

    private static void assertFlipbooks(String[] flipbooks) throws IOException {
        ClassLoader loader = FtbQuestTemplateContractTest.class.getClassLoader();
        for (String flipbook : flipbooks) {
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

    private static void assertKey(JsonObject translations, String language, String key) {
        assertTrue(translations.has(key), "Missing " + language + " " + key);
        assertFalse(translations.get(key).getAsString().isBlank(), key);
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

    private static Path industrialChapterSnbt() {
        return chapterSnbt().resolveSibling("alcoholic_industrial.snbt");
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

    private static String readNormalized(Path path) throws IOException {
        return normalize(Files.readString(path, StandardCharsets.UTF_8));
    }

    private static String normalize(String text) {
        return text.replace("\r\n", "\n");
    }
}

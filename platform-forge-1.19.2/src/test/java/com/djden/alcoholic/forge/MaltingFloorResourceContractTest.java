package com.djden.alcoholic.forge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaltingFloorResourceContractTest {
    private static final String MODEL_PATH =
            "assets/alcoholic/models/block/malting_floor.json";
    private static final String TEXTURE_PATH =
            "assets/alcoholic/textures/block/malting_floor.png";

    @Test
    void blockstateAndItemPointOnlyAtTheStaticMaltingFloorModel() throws IOException {
        JsonObject blockstate = resource("assets/alcoholic/blockstates/malting_floor.json");
        JsonObject variants = blockstate.getAsJsonObject("variants");
        assertEquals(1, variants.size());
        assertEquals(
                "alcoholic:block/malting_floor",
                variants.getAsJsonObject("").get("model").getAsString()
        );

        JsonObject item = resource("assets/alcoholic/models/item/malting_floor.json");
        assertEquals("alcoholic:block/malting_floor", item.get("parent").getAsString());
    }

    @Test
    void modelIsCompactStaticJavaGeometryWithRecessedMaltingLayers() throws IOException {
        JsonObject model = resource(MODEL_PATH);
        assertEquals("minecraft:block/block", model.get("parent").getAsString());
        assertFalse(model.has("format_version"), "Java 1.19.2 models must not use Bedrock metadata");
        assertFalse(model.has("texture_size"), "Java 1.19.2 models use normalized UV coordinates");
        assertFalse(model.has("loader"));
        assertFalse(model.has("bones"));
        assertFalse(model.has("animation"));
        assertFalse(model.has("animations"));

        JsonObject textures = model.getAsJsonObject("textures");
        assertEquals("alcoholic:block/malting_floor", textures.get("0").getAsString());
        assertEquals("alcoholic:block/malting_floor", textures.get("particle").getAsString());

        JsonArray elements = model.getAsJsonArray("elements");
        assertEquals(18, elements.size());

        Set<String> names = new HashSet<>();
        double maximumFrameHeight = 0.0;
        double barleyTop = 0.0;
        for (JsonElement value : elements) {
            JsonObject element = value.getAsJsonObject();
            names.add(element.get("name").getAsString());
            assertFalse(element.has("rotation"), "Static model must not export pivots or rotations");

            JsonArray from = element.getAsJsonArray("from");
            JsonArray to = element.getAsJsonArray("to");
            for (int axis = 0; axis < 3; axis++) {
                double lower = from.get(axis).getAsDouble();
                double upper = to.get(axis).getAsDouble();
                assertTrue(upper > lower, "Every cuboid dimension must be positive");
                if (axis == 1) {
                    assertTrue(lower >= 0.0 && upper <= 4.25, "Unexpected vertical extent");
                } else {
                    assertTrue(lower >= -0.25 && upper <= 16.25, "Unexpected visual overflow");
                }
            }

            if (element.get("name").getAsString().startsWith("oak_")) {
                maximumFrameHeight = Math.max(maximumFrameHeight, to.get(1).getAsDouble());
            }
            if (element.get("name").getAsString().equals("damp_barley")) {
                barleyTop = to.get(1).getAsDouble();
            }

            JsonObject faces = element.getAsJsonObject("faces");
            assertFalse(faces.entrySet().isEmpty());
            for (var faceEntry : faces.entrySet()) {
                JsonObject face = faceEntry.getValue().getAsJsonObject();
                assertEquals("#0", face.get("texture").getAsString());
                JsonArray uv = face.getAsJsonArray("uv");
                assertEquals(4, uv.size());
                assertFalse(
                        uv.get(0).getAsDouble() == uv.get(2).getAsDouble()
                                || uv.get(1).getAsDouble() == uv.get(3).getAsDouble(),
                        "UV islands must have non-zero area"
                );
                for (JsonElement coordinate : uv) {
                    double valueCoordinate = coordinate.getAsDouble();
                    assertTrue(valueCoordinate >= 0.0 && valueCoordinate <= 16.0);
                }
            }
        }

        assertTrue(names.containsAll(Set.of(
                "oak_post_front_left",
                "oak_post_front_right",
                "oak_post_back_left",
                "oak_post_back_right",
                "reed_mat",
                "damp_barley"
        )));
        assertTrue(barleyTop < maximumFrameHeight, "Barley must remain recessed below the frame");

        JsonObject display = model.getAsJsonObject("display");
        assertTrue(display.keySet().containsAll(Set.of(
                "thirdperson_righthand",
                "thirdperson_lefthand",
                "firstperson_righthand",
                "firstperson_lefthand",
                "ground",
                "gui",
                "fixed"
        )));
    }

    @Test
    void textureIsAReadableDeterministicPixelArtAtlas() throws IOException {
        ClassLoader loader = MaltingFloorResourceContractTest.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(TEXTURE_PATH)) {
            assertNotNull(stream, "Missing malting floor texture");
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, "Invalid malting floor PNG");
            assertEquals(64, image.getWidth());
            assertEquals(64, image.getHeight());

            Set<Integer> colors = new HashSet<>();
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    colors.add(image.getRGB(x, y));
                }
            }
            assertTrue(colors.size() >= 12, "Texture must distinguish wood, iron, reed and barley");
        }
    }

    private static JsonObject resource(String path) throws IOException {
        ClassLoader loader = MaltingFloorResourceContractTest.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing resource " + path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        }
    }
}

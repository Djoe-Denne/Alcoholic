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

class BrewingKettleResourceContractTest {
    private static final String MODEL_PATH = "assets/alcoholic/models/block/brewing_kettle.json";
    private static final String TEXTURE_PATH = "assets/alcoholic/textures/block/brewing_kettle.png";
    private static final Set<Double> JAVA_ANGLES = Set.of(-45.0, -22.5, 0.0, 22.5, 45.0);

    @Test
    void blockstateKeepsTheFourFacingVariants() throws IOException {
        JsonObject variants = resource("assets/alcoholic/blockstates/brewing_kettle.json")
                .getAsJsonObject("variants");
        assertEquals(4, variants.size());
        assertEquals(
                "alcoholic:block/brewing_kettle",
                variants.getAsJsonObject("facing=north").get("model").getAsString()
        );
        assertEquals(180, variants.getAsJsonObject("facing=south").get("y").getAsInt());
        assertEquals(270, variants.getAsJsonObject("facing=west").get("y").getAsInt());
        assertEquals(90, variants.getAsJsonObject("facing=east").get("y").getAsInt());

        JsonObject item = resource("assets/alcoholic/models/item/brewing_kettle.json");
        assertEquals("alcoholic:block/brewing_kettle", item.get("parent").getAsString());
    }

    @Test
    void modelIsAStaticJavaKettleWithFrontSpigot() throws IOException {
        JsonObject model = assertJavaBlockModel(MODEL_PATH);
        assertEquals(72, model.getAsJsonArray("elements").size());

        Set<String> names = new HashSet<>();
        boolean spigotFacesNegativeZ = false;
        for (JsonElement value : model.getAsJsonArray("elements")) {
            JsonObject element = value.getAsJsonObject();
            String name = element.get("name").getAsString();
            names.add(name);
            JsonArray from = element.getAsJsonArray("from");
            if (name.startsWith("copper_pipe") || name.startsWith("copper_tap") || name.equals("copper_elbow")) {
                if (from.get(2).getAsDouble() < 0.0) {
                    spigotFacesNegativeZ = true;
                }
            }
        }
        assertTrue(names.containsAll(Set.of(
                "copper_body_ew",
                "copper_body_ns",
                "copper_body_oct",
                "iron_hoop_bot_ew",
                "iron_hoop_top_ew",
                "copper_pipe_out",
                "copper_elbow",
                "copper_tap_body",
                "lid_low_ew",
                "lid_handle",
                "gauge_press_needle",
                "gauge_temp_needle",
                "sight_glass"
        )));
        assertTrue(spigotFacesNegativeZ, "Functional face must stay -Z");
    }

    @Test
    void textureIsThe64DefaultDownsampledFromThe512Master() throws IOException {
        ClassLoader loader = BrewingKettleResourceContractTest.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(TEXTURE_PATH)) {
            assertNotNull(stream, "Missing brewing kettle texture");
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, "Invalid brewing kettle PNG");
            assertEquals(64, image.getWidth());
            assertEquals(64, image.getHeight());

            Set<Integer> colors = new HashSet<>();
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    colors.add(image.getRGB(x, y));
                }
            }
            assertTrue(colors.size() >= 12, "Texture must distinguish copper and iron");
        }
    }

    private static JsonObject assertJavaBlockModel(String path) throws IOException {
        JsonObject model = resource(path);
        assertEquals("minecraft:block/block", model.get("parent").getAsString());
        assertFalse(model.has("format_version"), "Java 1.19.2 models must not use Bedrock metadata");
        assertFalse(model.has("texture_size"), "Java 1.19.2 models use normalized UV coordinates");
        assertFalse(model.has("loader"));
        assertFalse(model.has("bones"));
        assertFalse(model.has("animation"));
        assertFalse(model.has("animations"));

        JsonObject textures = model.getAsJsonObject("textures");
        assertEquals("alcoholic:block/brewing_kettle", textures.get("0").getAsString());
        assertEquals("alcoholic:block/brewing_kettle", textures.get("particle").getAsString());

        JsonArray elements = model.getAsJsonArray("elements");
        for (JsonElement value : elements) {
            JsonObject element = value.getAsJsonObject();
            String name = element.get("name").getAsString();
            JsonArray from = element.getAsJsonArray("from");
            JsonArray to = element.getAsJsonArray("to");
            for (int axis = 0; axis < 3; axis++) {
                assertTrue(to.get(axis).getAsDouble() > from.get(axis).getAsDouble(), name);
            }
            if (element.has("rotation")) {
                JsonObject rotation = element.getAsJsonObject("rotation");
                assertTrue(JAVA_ANGLES.contains(rotation.get("angle").getAsDouble()), name);
                assertTrue(Set.of("x", "y", "z").contains(rotation.get("axis").getAsString()));
            }
            JsonObject faces = element.getAsJsonObject("faces");
            assertFalse(faces.entrySet().isEmpty());
            for (var faceEntry : faces.entrySet()) {
                JsonObject face = faceEntry.getValue().getAsJsonObject();
                assertEquals("#0", face.get("texture").getAsString());
                JsonArray uv = face.getAsJsonArray("uv");
                assertEquals(4, uv.size());
                for (JsonElement coordinate : uv) {
                    double valueCoordinate = coordinate.getAsDouble();
                    assertTrue(valueCoordinate >= 0.0 && valueCoordinate <= 16.0, name);
                }
            }
        }

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
        return model;
    }

    private static JsonObject resource(String path) throws IOException {
        ClassLoader loader = BrewingKettleResourceContractTest.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing resource " + path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        }
    }
}

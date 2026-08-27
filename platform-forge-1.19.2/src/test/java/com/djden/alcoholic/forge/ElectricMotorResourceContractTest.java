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

class ElectricMotorResourceContractTest {
    private static final String OFF_MODEL_PATH = "assets/alcoholic/models/block/electric_motor.json";
    private static final String ON_MODEL_PATH = "assets/alcoholic/models/block/electric_motor_on.json";
    private static final String SHAFT_MODEL_PATH = "assets/alcoholic/models/block/electric_motor_shaft.json";
    private static final String OFF_TEXTURE_PATH = "assets/alcoholic/textures/block/electric_motor.png";
    private static final String ON_TEXTURE_PATH = "assets/alcoholic/textures/block/electric_motor_on.png";
    private static final Set<Double> JAVA_ANGLES = Set.of(-45.0, -22.5, 0.0, 22.5, 45.0);
    private static final Set<String> BODY_PARTS = Set.of(
            "steel_body",
            "plate",
            "fin_0",
            "copper_winding",
            "jbox",
            "copper_term_l",
            "vent_glow_0",
            "foot_fl"
    );
    private static final Set<String> SHAFT_PARTS = Set.of("iron_shaft", "iron_shaft_key");

    @Test
    void blockstateSwitchesOffAndOnModels() throws IOException {
        JsonObject variants = resource("assets/alcoholic/blockstates/electric_motor.json")
                .getAsJsonObject("variants");
        assertEquals(8, variants.size());
        assertEquals(
                "alcoholic:block/electric_motor",
                variants.getAsJsonObject("facing=north,lit=false").get("model").getAsString()
        );
        assertEquals(
                "alcoholic:block/electric_motor_on",
                variants.getAsJsonObject("facing=north,lit=true").get("model").getAsString()
        );
        assertEquals(180, variants.getAsJsonObject("facing=south,lit=false").get("y").getAsInt());
        assertEquals(180, variants.getAsJsonObject("facing=south,lit=true").get("y").getAsInt());
        assertEquals(270, variants.getAsJsonObject("facing=west,lit=false").get("y").getAsInt());
        assertEquals(90, variants.getAsJsonObject("facing=east,lit=true").get("y").getAsInt());

        JsonObject item = resource("assets/alcoholic/models/item/electric_motor.json");
        assertEquals("alcoholic:block/electric_motor", item.get("parent").getAsString());
    }

    @Test
    void offAndOnModelsShareTheApprovedSilhouette() throws IOException {
        JsonObject off = assertJavaBlockModel(OFF_MODEL_PATH, "alcoholic:block/electric_motor", true);
        JsonObject on = assertJavaBlockModel(ON_MODEL_PATH, "alcoholic:block/electric_motor_on", true);
        JsonObject shaft = assertJavaBlockModel(SHAFT_MODEL_PATH, "alcoholic:block/electric_motor_on", false);
        assertEquals(35, off.getAsJsonArray("elements").size());
        assertEquals(33, on.getAsJsonArray("elements").size());
        assertEquals(2, shaft.getAsJsonArray("elements").size());
        assertFalse(shaft.has("display"));

        Set<String> offNames = elementNames(off);
        Set<String> onNames = elementNames(on);
        Set<String> shaftNames = elementNames(shaft);
        assertTrue(offNames.containsAll(BODY_PARTS));
        assertTrue(offNames.containsAll(SHAFT_PARTS));
        assertTrue(onNames.containsAll(BODY_PARTS));
        assertFalse(onNames.contains("iron_shaft"));
        assertFalse(onNames.contains("iron_shaft_key"));
        assertEquals(SHAFT_PARTS, shaftNames);

        Set<String> litParts = new HashSet<>(onNames);
        litParts.addAll(shaftNames);
        assertEquals(offNames, litParts);
        assertTrue(shaftFacesNegativeZ(off));
        assertTrue(shaftFacesNegativeZ(shaft));
    }

    @Test
    void texturesAreThe64DefaultDownsampledFromThe512Master() throws IOException {
        assertPainted64(OFF_TEXTURE_PATH);
        assertPainted64(ON_TEXTURE_PATH);
    }

    private static JsonObject assertJavaBlockModel(String path, String texture, boolean requireDisplay)
            throws IOException {
        JsonObject model = resource(path);
        assertEquals("minecraft:block/block", model.get("parent").getAsString());
        assertFalse(model.has("format_version"), "Java 1.19.2 models must not use Bedrock metadata");
        assertFalse(model.has("texture_size"), "Java 1.19.2 models use normalized UV coordinates");
        assertFalse(model.has("loader"));
        assertFalse(model.has("bones"));
        assertFalse(model.has("animation"));
        assertFalse(model.has("animations"));

        JsonObject textures = model.getAsJsonObject("textures");
        assertEquals(texture, textures.get("0").getAsString());
        assertEquals(texture, textures.get("particle").getAsString());

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

        if (requireDisplay) {
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
        return model;
    }

    private static Set<String> elementNames(JsonObject model) {
        Set<String> names = new HashSet<>();
        for (JsonElement value : model.getAsJsonArray("elements")) {
            names.add(value.getAsJsonObject().get("name").getAsString());
        }
        return names;
    }

    private static boolean shaftFacesNegativeZ(JsonObject model) {
        for (JsonElement value : model.getAsJsonArray("elements")) {
            JsonObject element = value.getAsJsonObject();
            String name = element.get("name").getAsString();
            JsonArray from = element.getAsJsonArray("from");
            if (name.startsWith("iron_shaft") && from.get(2).getAsDouble() < 0.3) {
                return true;
            }
        }
        return false;
    }

    private static void assertPainted64(String path) throws IOException {
        ClassLoader loader = ElectricMotorResourceContractTest.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing " + path);
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, "Invalid PNG " + path);
            assertEquals(64, image.getWidth());
            assertEquals(64, image.getHeight());

            Set<Integer> colors = new HashSet<>();
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    colors.add(image.getRGB(x, y));
                }
            }
            assertTrue(colors.size() >= 12, path + " must distinguish steel, iron, copper and glow");
        }
    }

    private static JsonObject resource(String path) throws IOException {
        ClassLoader loader = ElectricMotorResourceContractTest.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing resource " + path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        }
    }
}

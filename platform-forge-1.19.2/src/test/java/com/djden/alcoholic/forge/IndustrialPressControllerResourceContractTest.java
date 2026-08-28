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

class IndustrialPressControllerResourceContractTest {
    private static final String MODEL_PATH = "assets/alcoholic/models/block/industrial_press_controller.json";
    private static final String TEXTURE_PATH = "assets/alcoholic/textures/block/industrial_press_controller.png";
    private static final Set<Double> JAVA_ANGLES = Set.of(-45.0, -22.5, 0.0, 22.5, 45.0);
    private static final Set<String> REQUIRED_PARTS = Set.of(
            "steel_core",
            "frame_n_bot",
            "plate_e",
            "slot_well",
            "lever_knob",
            "lamp_u_lens",
            "pic_plate",
            "icon_stem",
            "accent_line",
            "stripe_w",
            "rivet_n_bl_head"
    );

    @Test
    void blockstateKeepsFormedVariantsOnTheSameUnformedCube() throws IOException {
        JsonObject variants = resource("assets/alcoholic/blockstates/industrial_press_controller.json")
                .getAsJsonObject("variants");
        assertEquals(2, variants.size());
        assertEquals(
                "alcoholic:block/industrial_press_controller",
                variants.getAsJsonObject("formed=false").get("model").getAsString()
        );
        assertEquals(
                "alcoholic:block/industrial_press_controller",
                variants.getAsJsonObject("formed=true").get("model").getAsString()
        );
        for (var entry : variants.entrySet()) {
            assertFalse(entry.getKey().contains("facing"), entry.getKey());
        }

        JsonObject item = resource("assets/alcoholic/models/item/industrial_press_controller.json");
        assertEquals("alcoholic:block/industrial_press_controller", item.get("parent").getAsString());
    }

    @Test
    void modelIsAClosedDeskFacingNegativeZ() throws IOException {
        JsonObject model = assertJavaBlockModel(MODEL_PATH);
        assertEquals(118, model.getAsJsonArray("elements").size());

        Set<String> names = new HashSet<>();
        boolean deskFacesNegativeZ = false;
        for (JsonElement value : model.getAsJsonArray("elements")) {
            JsonObject element = value.getAsJsonObject();
            String name = element.get("name").getAsString();
            names.add(name);
            JsonArray from = element.getAsJsonArray("from");
            if (name.equals("lever_knob") && from.get(2).getAsDouble() < 0.0) {
                deskFacesNegativeZ = true;
            }
        }
        assertTrue(names.containsAll(REQUIRED_PARTS));
        assertTrue(deskFacesNegativeZ, "Functional desk must stay -Z");
        assertFalse(names.contains("plate_n"));
        assertTrue(names.contains("plate_s"));
        assertTrue(names.contains("lamp_d_ring"));
        assertTrue(names.contains("icon_top"));
    }

    @Test
    void textureIsThe64DefaultDownsampledFromThe512Master() throws IOException {
        ClassLoader loader = IndustrialPressControllerResourceContractTest.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(TEXTURE_PATH)) {
            assertNotNull(stream, "Missing press controller texture");
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, "Invalid press controller PNG");
            assertEquals(64, image.getWidth());
            assertEquals(64, image.getHeight());

            Set<Integer> colors = new HashSet<>();
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    colors.add(image.getRGB(x, y));
                }
            }
            assertTrue(colors.size() >= 12, "Texture must distinguish steel, iron and red accent");
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
        assertEquals("alcoholic:block/industrial_press_controller", textures.get("0").getAsString());
        assertEquals("alcoholic:block/industrial_press_controller", textures.get("particle").getAsString());

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
        ClassLoader loader = IndustrialPressControllerResourceContractTest.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing resource " + path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        }
    }
}

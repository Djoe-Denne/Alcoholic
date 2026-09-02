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

class SimpleIndustrialControllerResourceContractTest {
    private static final Set<Double> JAVA_ANGLES = Set.of(-45.0, -22.5, 0.0, 22.5, 45.0);
    private static final Case[] CONTROLLERS = {
            new Case("industrial_vat_controller", 121, "vat_mid", "lock_bulb"),
            new Case("industrial_tank_controller", 120, "tank_body", "gauge_needle"),
            new Case("industrial_malt_house_controller", 118, "floor_tray", "flame_a"),
            new Case("industrial_mash_tun_controller", 117, "tun_body", "paddle_blade"),
            new Case("industrial_brewing_kettle_controller", 120, "kettle_dome", "steam_a"),
            new Case("industrial_conditioning_vessel_controller", 119, "vessel_body", "flake_c"),
            new Case("industrial_aging_vessel_controller", 119, "vessel_body", "flake_c")
    };

    @Test
    void blockstatesKeepTheUntintedCubeVariant() throws IOException {
        for (Case controller : CONTROLLERS) {
            JsonObject variants = resource("assets/alcoholic/blockstates/" + controller.id + ".json")
                    .getAsJsonObject("variants");
            assertEquals(1, variants.size(), controller.id);
            assertEquals(
                    "alcoholic:block/" + controller.id,
                    variants.getAsJsonObject("").get("model").getAsString(),
                    controller.id
            );
            for (var entry : variants.entrySet()) {
                assertFalse(entry.getKey().contains("facing"), controller.id + " " + entry.getKey());
                assertFalse(entry.getKey().contains("mode"), controller.id + " " + entry.getKey());
            }

            JsonObject item = resource("assets/alcoholic/models/item/" + controller.id + ".json");
            assertEquals("alcoholic:block/" + controller.id, item.get("parent").getAsString(), controller.id);
        }
    }

    @Test
    void modelsAreClosedDesksFacingNegativeZ() throws IOException {
        for (Case controller : CONTROLLERS) {
            JsonObject model = assertJavaBlockModel(controller.id);
            assertEquals(controller.cubes, model.getAsJsonArray("elements").size(), controller.id);

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
            assertTrue(names.contains("steel_core"), controller.id);
            assertTrue(names.contains("frame_n_bot"), controller.id);
            assertTrue(names.contains("plate_e"), controller.id);
            assertTrue(names.contains("lever_knob"), controller.id);
            assertTrue(names.contains("rivet_n_bl_head"), controller.id);
            assertTrue(names.contains(controller.uniqueA), controller.id);
            assertTrue(names.contains(controller.uniqueB), controller.id);
            assertTrue(deskFacesNegativeZ, controller.id + " desk must stay -Z");
            assertFalse(names.contains("plate_n"), controller.id);
            assertTrue(names.contains("plate_s"), controller.id);
        }
    }

    @Test
    void texturesAreThe64DefaultDownsampledFromThe512Master() throws IOException {
        ClassLoader loader = SimpleIndustrialControllerResourceContractTest.class.getClassLoader();
        for (Case controller : CONTROLLERS) {
            String path = "assets/alcoholic/textures/block/" + controller.id + ".png";
            try (InputStream stream = loader.getResourceAsStream(path)) {
                assertNotNull(stream, "Missing " + controller.id + " texture");
                BufferedImage image = ImageIO.read(stream);
                assertNotNull(image, "Invalid " + controller.id + " PNG");
                assertEquals(64, image.getWidth(), controller.id);
                assertEquals(64, image.getHeight(), controller.id);

                Set<Integer> colors = new HashSet<>();
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        colors.add(image.getRGB(x, y));
                    }
                }
                assertTrue(colors.size() >= 12, controller.id + " must distinguish steel, iron and accent");
            }
        }
    }

    private static JsonObject assertJavaBlockModel(String id) throws IOException {
        JsonObject model = resource("assets/alcoholic/models/block/" + id + ".json");
        assertEquals("minecraft:block/block", model.get("parent").getAsString(), id);
        assertFalse(model.has("format_version"), id);
        assertFalse(model.has("texture_size"), id);
        assertFalse(model.has("loader"), id);
        assertFalse(model.has("bones"), id);
        assertFalse(model.has("animation"), id);
        assertFalse(model.has("animations"), id);

        JsonObject textures = model.getAsJsonObject("textures");
        assertEquals("alcoholic:block/" + id, textures.get("0").getAsString(), id);
        assertEquals("alcoholic:block/" + id, textures.get("particle").getAsString(), id);

        JsonArray elements = model.getAsJsonArray("elements");
        for (JsonElement value : elements) {
            JsonObject element = value.getAsJsonObject();
            String name = element.get("name").getAsString();
            JsonArray from = element.getAsJsonArray("from");
            JsonArray to = element.getAsJsonArray("to");
            for (int axis = 0; axis < 3; axis++) {
                assertTrue(to.get(axis).getAsDouble() > from.get(axis).getAsDouble(), id + " " + name);
            }
            if (element.has("rotation")) {
                JsonObject rotation = element.getAsJsonObject("rotation");
                assertTrue(JAVA_ANGLES.contains(rotation.get("angle").getAsDouble()), id + " " + name);
                assertTrue(Set.of("x", "y", "z").contains(rotation.get("axis").getAsString()));
            }
            JsonObject faces = element.getAsJsonObject("faces");
            assertFalse(faces.entrySet().isEmpty(), id + " " + name);
            for (var faceEntry : faces.entrySet()) {
                JsonObject face = faceEntry.getValue().getAsJsonObject();
                assertEquals("#0", face.get("texture").getAsString(), id + " " + name);
                JsonArray uv = face.getAsJsonArray("uv");
                assertEquals(4, uv.size(), id + " " + name);
                for (JsonElement coordinate : uv) {
                    double valueCoordinate = coordinate.getAsDouble();
                    assertTrue(valueCoordinate >= 0.0 && valueCoordinate <= 16.0, id + " " + name);
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
        )), id);
        return model;
    }

    private static JsonObject resource(String path) throws IOException {
        ClassLoader loader = SimpleIndustrialControllerResourceContractTest.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing resource " + path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        }
    }

    private record Case(String id, int cubes, String uniqueA, String uniqueB) {
    }
}

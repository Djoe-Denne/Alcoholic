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

class ArtisanalFermenterResourceContractTest {
    private static final String CLOSED_MODEL_PATH = "assets/alcoholic/models/block/artisanal_fermenter.json";
    private static final String OPEN_MODEL_PATH = "assets/alcoholic/models/block/artisanal_fermenter_open.json";
    private static final String TEXTURE_PATH = "assets/alcoholic/textures/block/artisanal_fermenter.png";
    private static final Set<Double> JAVA_ANGLES = Set.of(-45.0, -22.5, 0.0, 22.5, 45.0);

    @Test
    void blockstateSwitchesClosedAndOpenHatchModels() throws IOException {
        JsonObject variants = resource("assets/alcoholic/blockstates/artisanal_fermenter.json")
                .getAsJsonObject("variants");
        assertEquals(8, variants.size());
        assertEquals(
                "alcoholic:block/artisanal_fermenter",
                variants.getAsJsonObject("facing=north,open=false").get("model").getAsString()
        );
        assertEquals(
                "alcoholic:block/artisanal_fermenter_open",
                variants.getAsJsonObject("facing=north,open=true").get("model").getAsString()
        );
        assertEquals(180, variants.getAsJsonObject("facing=south,open=false").get("y").getAsInt());
        assertEquals(180, variants.getAsJsonObject("facing=south,open=true").get("y").getAsInt());
        assertEquals(270, variants.getAsJsonObject("facing=west,open=false").get("y").getAsInt());
        assertEquals(90, variants.getAsJsonObject("facing=east,open=true").get("y").getAsInt());

        JsonObject item = resource("assets/alcoholic/models/item/artisanal_fermenter.json");
        assertEquals("alcoholic:block/artisanal_fermenter", item.get("parent").getAsString());
    }

    @Test
    void closedModelKeepsTheHatchFlushOnNegativeZ() throws IOException {
        JsonObject model = assertJavaBlockModel(CLOSED_MODEL_PATH);
        assertEquals(50, model.getAsJsonArray("elements").size());
        assertFalse(anyHatchHasXRotation(model), "Closed hatch must stay flush on the staves");
        assertModelHasFermenterParts(model);
    }

    @Test
    void openModelKeepsTheApprovedAjarHatch() throws IOException {
        JsonObject model = assertJavaBlockModel(OPEN_MODEL_PATH);
        assertEquals(50, model.getAsJsonArray("elements").size());
        assertTrue(anyHatchHasXRotation(model), "Open hatch must stay ajar on a Java-legal X rotation");
        assertModelHasFermenterParts(model);
    }

    @Test
    void textureIsThe64DefaultDownsampledFromThe512Master() throws IOException {
        ClassLoader loader = ArtisanalFermenterResourceContractTest.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(TEXTURE_PATH)) {
            assertNotNull(stream, "Missing fermenter texture");
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, "Invalid fermenter PNG");
            assertEquals(64, image.getWidth());
            assertEquals(64, image.getHeight());

            Set<Integer> colors = new HashSet<>();
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    colors.add(image.getRGB(x, y));
                }
            }
            assertTrue(colors.size() >= 12, "Texture must distinguish oak, iron and copper");
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
        assertEquals("alcoholic:block/artisanal_fermenter", textures.get("0").getAsString());
        assertEquals("alcoholic:block/artisanal_fermenter", textures.get("particle").getAsString());

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
                if (name.startsWith("hatch_")) {
                    assertEquals(22.5, rotation.get("angle").getAsDouble(), name);
                    assertEquals("x", rotation.get("axis").getAsString(), name);
                }
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

    private static void assertModelHasFermenterParts(JsonObject model) {
        Set<String> names = new HashSet<>();
        boolean hatchFacesNegativeZ = false;
        boolean tapFacesPositiveX = false;
        for (JsonElement value : model.getAsJsonArray("elements")) {
            JsonObject element = value.getAsJsonObject();
            String name = element.get("name").getAsString();
            names.add(name);
            JsonArray from = element.getAsJsonArray("from");
            if (name.startsWith("hatch_") && from.get(2).getAsDouble() < 1.0) {
                hatchFacesNegativeZ = true;
            }
            if (name.startsWith("copper_") && from.get(0).getAsDouble() > 14.0) {
                tapFacesPositiveX = true;
            }
        }
        assertTrue(names.containsAll(Set.of(
                "oak_body_ew",
                "oak_lid_ew",
                "oak_foot_fl",
                "iron_hoop_bot_ew",
                "iron_hoop_mid_ew",
                "iron_hoop_top_ew",
                "hatch_disk",
                "airlock_glass",
                "copper_pipe_out"
        )));
        assertTrue(hatchFacesNegativeZ, "Hatch must stay on the -Z face");
        assertTrue(tapFacesPositiveX, "Tap must stay on the +X side");
    }

    private static boolean anyHatchHasXRotation(JsonObject model) {
        for (JsonElement value : model.getAsJsonArray("elements")) {
            JsonObject element = value.getAsJsonObject();
            if (!element.get("name").getAsString().startsWith("hatch_") || !element.has("rotation")) {
                continue;
            }
            JsonObject rotation = element.getAsJsonObject("rotation");
            if (rotation.get("angle").getAsDouble() == 22.5 && "x".equals(rotation.get("axis").getAsString())) {
                return true;
            }
        }
        return false;
    }

    private static JsonObject resource(String path) throws IOException {
        ClassLoader loader = ArtisanalFermenterResourceContractTest.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing resource " + path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        }
    }
}

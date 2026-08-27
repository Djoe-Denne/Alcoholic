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

class MaltMillResourceContractTest {
    private static final String MODEL_PATH = "assets/alcoholic/models/block/malt_mill.json";
    private static final String COMPLETE_MODEL_PATH = "assets/alcoholic/models/block/malt_mill_complete.json";
    private static final String ROLLER_FRONT_MODEL_PATH =
            "assets/alcoholic/models/block/malt_mill_roller_front.json";
    private static final String ROLLER_REAR_MODEL_PATH =
            "assets/alcoholic/models/block/malt_mill_roller_rear.json";
    private static final String DRIVE_AXLE_MODEL_PATH =
            "assets/alcoholic/models/block/malt_mill_drive_axle.json";
    private static final String TEXTURE_PATH = "assets/alcoholic/textures/block/malt_mill.png";
    private static final Set<Double> JAVA_ANGLES = Set.of(-45.0, -22.5, 0.0, 22.5, 45.0);

    @Test
    void blockstatePreservesTheFourExistingFacingVariants() throws IOException {
        JsonObject variants = resource("assets/alcoholic/blockstates/malt_mill.json")
                .getAsJsonObject("variants");
        assertEquals(4, variants.size());
        assertEquals(
                "alcoholic:block/malt_mill",
                variants.getAsJsonObject("facing=north").get("model").getAsString()
        );
        assertEquals(180, variants.getAsJsonObject("facing=south").get("y").getAsInt());
        assertEquals(270, variants.getAsJsonObject("facing=west").get("y").getAsInt());
        assertEquals(90, variants.getAsJsonObject("facing=east").get("y").getAsInt());

        JsonObject item = resource("assets/alcoholic/models/item/malt_mill.json");
        assertEquals("alcoholic:block/malt_mill_complete", item.get("parent").getAsString());
    }

    @Test
    void modelIsPartitionedIntoStaticAndMovingJavaGeometry() throws IOException {
        JsonObject staticModel = resource(MODEL_PATH);
        JsonObject completeModel = resource(COMPLETE_MODEL_PATH);
        JsonObject rollerFrontModel = resource(ROLLER_FRONT_MODEL_PATH);
        JsonObject rollerRearModel = resource(ROLLER_REAR_MODEL_PATH);
        JsonObject driveAxleModel = resource(DRIVE_AXLE_MODEL_PATH);

        for (JsonObject model : Set.of(
                staticModel,
                completeModel,
                rollerFrontModel,
                rollerRearModel,
                driveAxleModel
        )) {
            assertJavaModel(model);
        }

        assertEquals(55, staticModel.getAsJsonArray("elements").size());
        assertEquals(67, completeModel.getAsJsonArray("elements").size());
        assertEquals(5, rollerFrontModel.getAsJsonArray("elements").size());
        assertEquals(5, rollerRearModel.getAsJsonArray("elements").size());
        assertEquals(2, driveAxleModel.getAsJsonArray("elements").size());
        assertFalse(staticModel.has("display"));
        assertFalse(rollerFrontModel.has("display"));
        assertFalse(rollerRearModel.has("display"));
        assertFalse(driveAxleModel.has("display"));

        Set<String> staticNames = elementNames(staticModel);
        Set<String> rollerFrontNames = elementNames(rollerFrontModel);
        Set<String> rollerRearNames = elementNames(rollerRearModel);
        Set<String> driveAxleNames = elementNames(driveAxleModel);
        Set<String> completeNames = elementNames(completeModel);
        Set<String> partitionNames = new HashSet<>(staticNames);
        partitionNames.addAll(rollerFrontNames);
        partitionNames.addAll(rollerRearNames);
        partitionNames.addAll(driveAxleNames);

        assertEquals(completeNames, partitionNames);
        assertEquals(
                completeNames.size(),
                staticNames.size() + rollerFrontNames.size() + rollerRearNames.size() + driveAxleNames.size(),
                "A cube belongs to more than one Malt Mill partition"
        );
        assertTrue(staticNames.containsAll(Set.of(
                "oak_hopper_wall_front",
                "oak_chute_floor",
                "dark_grist_throat"
        )));
        assertTrue(rollerFrontNames.contains("roller_front_core"));
        assertTrue(rollerRearNames.contains("roller_rear_core"));
        assertTrue(driveAxleNames.contains("iron_axle_shaft"));
        assertFalse(completeNames.stream().anyMatch(name -> name.contains("crank") || name.contains("handle")));

        for (JsonElement value : staticModel.getAsJsonArray("elements")) {
            JsonObject element = value.getAsJsonObject();
            String name = element.get("name").getAsString();
            if (name.startsWith("oak_chute_") || name.equals("dark_grist_throat")) {
                JsonArray from = element.getAsJsonArray("from");
                JsonArray to = element.getAsJsonArray("to");
                assertTrue(from.get(0).getAsDouble() >= 0.0, name);
                assertTrue(to.get(0).getAsDouble() <= 16.0, name);
                assertTrue(from.get(2).getAsDouble() >= 0.0, name);
                assertTrue(to.get(2).getAsDouble() <= 16.0, name);
            }
        }

        JsonObject display = completeModel.getAsJsonObject("display");
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

    private static void assertJavaModel(JsonObject model) {
        assertEquals("minecraft:block/block", model.get("parent").getAsString());
        assertFalse(model.has("format_version"), "Java 1.19.2 models must not use Bedrock metadata");
        assertFalse(model.has("texture_size"), "Java 1.19.2 models use normalized UV coordinates");
        assertFalse(model.has("loader"));
        assertFalse(model.has("bones"));
        assertFalse(model.has("animation"));
        assertFalse(model.has("animations"));

        JsonObject textures = model.getAsJsonObject("textures");
        assertEquals("alcoholic:block/malt_mill", textures.get("0").getAsString());
        assertEquals("alcoholic:block/malt_mill", textures.get("particle").getAsString());

        JsonArray elements = model.getAsJsonArray("elements");
        for (JsonElement value : elements) {
            JsonObject element = value.getAsJsonObject();
            String name = element.get("name").getAsString();

            JsonArray from = element.getAsJsonArray("from");
            JsonArray to = element.getAsJsonArray("to");
            for (int axis = 0; axis < 3; axis++) {
                assertTrue(to.get(axis).getAsDouble() > from.get(axis).getAsDouble(), name);
                if (axis == 1) {
                    assertTrue(from.get(axis).getAsDouble() >= 0.0, name);
                    assertTrue(to.get(axis).getAsDouble() <= 32.0, name);
                }
            }

            if (element.has("rotation")) {
                JsonObject rotation = element.getAsJsonObject("rotation");
                assertTrue(JAVA_ANGLES.contains(rotation.get("angle").getAsDouble()), name);
                assertTrue(Set.of("x", "y", "z").contains(rotation.get("axis").getAsString()), name);
            }

            JsonObject faces = element.getAsJsonObject("faces");
            assertFalse(faces.entrySet().isEmpty(), name);
            for (var faceEntry : faces.entrySet()) {
                JsonObject face = faceEntry.getValue().getAsJsonObject();
                assertEquals("#0", face.get("texture").getAsString(), name);
                JsonArray uv = face.getAsJsonArray("uv");
                assertEquals(4, uv.size(), name);
                assertFalse(
                        uv.get(0).getAsDouble() == uv.get(2).getAsDouble()
                                || uv.get(1).getAsDouble() == uv.get(3).getAsDouble(),
                        name
                );
                for (JsonElement coordinate : uv) {
                    double valueCoordinate = coordinate.getAsDouble();
                    assertTrue(valueCoordinate >= 0.0 && valueCoordinate <= 16.0, name);
                }
            }
        }
    }

    private static Set<String> elementNames(JsonObject model) {
        Set<String> names = new HashSet<>();
        for (JsonElement value : model.getAsJsonArray("elements")) {
            assertTrue(names.add(value.getAsJsonObject().get("name").getAsString()), "Duplicate element name");
        }
        return names;
    }

    @Test
    void defaultTextureIsTheReadableThirtyTwoPixelAtlas() throws IOException {
        ClassLoader loader = MaltMillResourceContractTest.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(TEXTURE_PATH)) {
            assertNotNull(stream, "Missing malt mill texture");
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, "Invalid malt mill PNG");
            assertEquals(32, image.getWidth());
            assertEquals(32, image.getHeight());

            Set<Integer> colors = new HashSet<>();
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    colors.add(image.getRGB(x, y));
                }
            }
            assertTrue(colors.size() >= 12, "Texture must distinguish oak, iron, rollers and copper");
        }
    }

    private static JsonObject resource(String path) throws IOException {
        ClassLoader loader = MaltMillResourceContractTest.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing resource " + path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        }
    }
}

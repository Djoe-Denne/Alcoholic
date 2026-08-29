package com.djden.alcoholic.forge;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.multiblock.FormedArtSize;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormedMeshResourceContractTest {
    @Test
    void everyArtMachineHasABerJsonWithoutANewBlockId() throws IOException {
        assertEquals(13, FormedArtSize.all().size());
        for (ResourceId id : FormedArtSize.all().keySet()) {
            JsonObject model = resource("assets/alcoholic/models/block/formed/" + id.path() + ".json");
            assertEquals("minecraft:block/block", model.get("parent").getAsString());
            assertFalse(model.has("format_version"));
            assertFalse(model.has("texture_size"));
            JsonArray elements = model.getAsJsonArray("elements");
            int minCubes = id.path().startsWith("craft_") ? 70 : 100;
            assertTrue(elements.size() >= minCubes, id.toString());
            for (JsonElement value : elements) {
                JsonObject element = value.getAsJsonObject();
                JsonArray from = element.getAsJsonArray("from");
                JsonArray to = element.getAsJsonArray("to");
                for (int axis = 0; axis < 3; axis++) {
                    assertTrue(to.get(axis).getAsDouble() > from.get(axis).getAsDouble(), id.toString());
                    assertTrue(from.get(axis).getAsDouble() > -8.0, id.toString());
                    assertTrue(to.get(axis).getAsDouble() < 24.0, id.toString());
                }
            }
        }
    }

    @Test
    void brewingKettleFormedAtlasIs64() throws IOException {
        try (InputStream stream = FormedMeshResourceContractTest.class.getClassLoader()
                .getResourceAsStream("assets/alcoholic/textures/block/formed/industrial_brewing_kettle.png")) {
            assertNotNull(stream);
            BufferedImage image = ImageIO.read(stream);
            assertEquals(64, image.getWidth());
            assertEquals(64, image.getHeight());
        }
    }

    private static JsonObject resource(String path) throws IOException {
        ClassLoader loader = FormedMeshResourceContractTest.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing " + path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        }
    }
}

package com.djden.alcoholic.minecraft.viticulture;

import com.djden.alcoholic.application.viticulture.VineVarieties;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ViticultureDataReloadListenerTest {
    @Test
    void buildsOneCompleteSnapshotFromGlobalAndVarietyFiles() {
        Map<ResourceLocation, JsonElement> resources = Map.of(
                id("settings"),
                json("""
                        {
                          "type": "settings",
                          "max_wire_distance": 24,
                          "untrained": { "yield": 0.6, "quality": 0.8 }
                        }
                        """),
                id("red_grape"),
                json("""
                        {
                          "variety": "alcoholic:red_grape",
                          "growth": {
                            "base_growth_chance": 0.9,
                            "climate": {
                              "temperature_celsius": 27.0,
                              "humidity": 0.45
                            }
                          }
                        }
                        """)
        );

        ViticultureSettings parsed =
                ViticultureDataReloadListener.parseSnapshot(resources);

        assertEquals(24, parsed.maxWireDistance());
        assertEquals(0.6, parsed.untrained().yield());
        assertEquals(0.8, parsed.untrained().quality());
        assertEquals(
                0.9,
                parsed.forVariety(VineVarieties.RED_GRAPE)
                        .growth()
                        .baseGrowthChance()
        );
        assertEquals(
                27.0,
                parsed.forVariety(VineVarieties.RED_GRAPE)
                        .growth()
                        .climateProfile()
                        .idealEnvironment()
                        .temperatureCelsius()
        );
    }

    @Test
    void rejectsInvalidSnapshotInsteadOfPartiallyApplyingIt() {
        Map<ResourceLocation, JsonElement> resources = Map.of(
                id("settings"),
                json("{\"type\":\"settings\",\"max_wire_distance\":1}")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ViticultureDataReloadListener.parseSnapshot(resources)
        );
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("alcoholic", path);
    }

    private static JsonElement json(String source) {
        return JsonParser.parseString(source);
    }
}

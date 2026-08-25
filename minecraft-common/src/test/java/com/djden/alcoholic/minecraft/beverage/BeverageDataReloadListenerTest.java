package com.djden.alcoholic.minecraft.beverage;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeverageDataReloadListenerTest {
    @Test
    void buildsOneCompleteSnapshotFromSplitDataFiles() {
        AlcoholicApi api = AlcoholicApi.create();
        BuiltinRegistrations.install(api);
        Map<ResourceLocation, JsonElement> resources = Map.of(
                id("testpack", "ingredients/apple"),
                json("""
                        { "id": "testpack:apple", "tags": ["alcoholic:fruits/apple"] }
                        """),
                id("testpack", "processes/press_fruit"),
                json("""
                        {
                          "id": "testpack:press_fruit",
                          "process": "alcoholic:press",
                          "inputs": { "source": { "tag": "alcoholic:fruits/apple" } },
                          "outputs": ["must"]
                        }
                        """),
                id("testpack", "liquids/apple_must"),
                json("""
                        { "id": "testpack:apple_must", "defaults": { "alcoholic:sugar": 0.5 } }
                        """),
                id("testpack", "liquids/young_cider"),
                json("""
                        { "id": "testpack:young_cider", "defaults": { "alcoholic:ethanol": 0.05 } }
                        """),
                id("testpack", "beverages/cider"),
                json("""
                        {
                          "id": "testpack:cider",
                          "category": "cider",
                          "graph": {
                            "nodes": [
                              { "id": "press", "definition": "testpack:press_fruit" },
                              {
                                "id": "ferment",
                                "process": "alcoholic:ferment",
                                "inputs": { "must": { "node": "press", "port": "must" } },
                                "outputs": ["cider"]
                              }
                            ],
                            "outputs": { "result": { "node": "ferment", "port": "cider" } }
                          }
                        }
                        """)
        );

        BeverageCatalog parsed = BeverageDataReloadListener.parseSnapshot(resources, api);

        assertEquals(1, parsed.beverages().size());
        assertTrue(parsed.beverage(ResourceId.parse("testpack:cider")).isPresent());
        assertEquals(
                ResourceId.parse("alcoholic:press"),
                parsed.beverage(ResourceId.parse("testpack:cider"))
                        .orElseThrow()
                        .graph()
                        .node("press")
                        .orElseThrow()
                        .processType()
                        .orElseThrow()
        );
    }

    @Test
    void rejectsInvalidSnapshotInsteadOfPartiallyApplyingIt() {
        AlcoholicApi api = AlcoholicApi.create();
        BuiltinRegistrations.install(api);
        Map<ResourceLocation, JsonElement> resources = Map.of(
                id("testpack", "beverages/broken"),
                json("""
                        {
                          "id": "testpack:broken",
                          "graph": {
                            "nodes": [
                              {
                                "id": "loop",
                                "process": "alcoholic:press",
                                "inputs": { "in": { "node": "loop", "port": "out" } },
                                "outputs": ["out"]
                              }
                            ]
                          }
                        }
                        """)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> BeverageDataReloadListener.parseSnapshot(resources, api)
        );
    }

    private static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    private static JsonElement json(String source) {
        return JsonParser.parseString(source);
    }
}

package com.djden.alcoholic.application.beverage;

import com.djden.alcoholic.addon.test.TestAddonBootstrap;
import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.data.JsonDataParser;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.quality.ShippedQualityGraphs;
import com.djden.alcoholic.domain.beverage.InputReference;
import com.djden.alcoholic.domain.beverage.ProcessNode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadBeverageCatalogUseCaseTest {
    private final LoadBeverageCatalogUseCase loader = new LoadBeverageCatalogUseCase();

    @Test
    void loadsAcceptanceFixturesWithoutHardcodedFamilyLogic() {
        AlcoholicApi api = api();
        BeverageCatalog catalog = loader.load(
                FixtureCatalogs.ingredients(),
                FixtureCatalogs.processes(),
                FixtureCatalogs.acceptanceBeverages(),
                FixtureCatalogs.liquids(),
                FixtureCatalogs.quality(),
                api
        );

        assertEquals(9, catalog.beverages().size());
        assertEquals(
                ResourceId.parse("alcoholic:wine"),
                catalog.beverage(ResourceId.parse("testpack:red_wine")).orElseThrow().quality().orElseThrow()
        );
        assertTrue(catalog.quality(ResourceId.parse("alcoholic:wine")).isPresent());
        assertTrue(catalog.beverage(ResourceId.parse("testpack:cider")).isPresent());
        assertTrue(catalog.beverage(ResourceId.parse("testpack:fruit_liqueur")).isPresent());

        ProcessNode mash = catalog.beverage(ResourceId.parse("testpack:beer"))
                .orElseThrow()
                .graph()
                .node("mash")
                .orElseThrow();
        assertEquals(2, mash.inputs().size());
        assertTrue(mash.inputs().get("water") instanceof InputReference.ItemInput);

        ProcessNode wheatMash = catalog.beverage(ResourceId.parse("testpack:wheat_beer"))
                .orElseThrow()
                .graph()
                .node("mash")
                .orElseThrow();
        assertEquals(3, wheatMash.inputs().size());
        assertTrue(catalog.beverage(ResourceId.parse("testpack:grain_mash")).isPresent());
        assertTrue(catalog.beverage(ResourceId.parse("testpack:whisky"))
                .orElseThrow()
                .graph()
                .node("distill")
                .orElseThrow()
                .processType()
                .filter(id -> id.equals(ResourceId.parse("alcoholic:distill")))
                .isPresent());

        ProcessNode infuse = catalog.beverage(ResourceId.parse("testpack:fruit_liqueur"))
                .orElseThrow()
                .graph()
                .node("infuse")
                .orElseThrow();
        assertTrue(infuse.inputs().get("spirit") instanceof InputReference.BeverageInput);
        assertFalse(catalog.beverage(ResourceId.parse("testpack:fruit_liqueur"))
                .orElseThrow()
                .graph()
                .nodes()
                .stream()
                .anyMatch(node -> node.processType()
                        .filter(id -> id.equals(ResourceId.parse("alcoholic:ferment")))
                        .isPresent()));

        ProcessNode press = catalog.beverage(ResourceId.parse("testpack:cider"))
                .orElseThrow()
                .graph()
                .node("press")
                .orElseThrow();
        assertEquals(ResourceId.parse("alcoholic:press"), press.processType().orElseThrow());
        assertEquals(1, press.outputs().size());
    }

    @Test
    void acceptsJavaAddonProcessWithoutChangingCoreClasses() {
        AlcoholicApi api = api();
        TestAddonBootstrap.install(api);
        BeverageCatalog catalog = loader.load(
                Map.of(),
                Map.of(),
                FixtureCatalogs.addonBeverages(),
                api
        );

        assertTrue(catalog.beverage(ResourceId.parse("testaddon:polished_rice_wash")).isPresent());
        assertEquals(
                ResourceId.parse("testaddon:rice_polishing"),
                catalog.beverage(ResourceId.parse("testaddon:polished_rice_wash"))
                        .orElseThrow()
                        .graph()
                        .node("polish")
                        .orElseThrow()
                        .processType()
                        .orElseThrow()
        );
    }

    @Test
    void rejectsCycleUnknownProcessDuplicateAndBrokenReferences() {
        AlcoholicApi api = api();

        assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(),
                Map.of(),
                Map.of(
                        ResourceId.parse("testpack:cycle"),
                        JsonDataParser.parse("""
                                {
                                  "id": "testpack:cycle",
                                  "graph": {
                                    "nodes": [
                                      {
                                        "id": "a",
                                        "process": "alcoholic:press",
                                        "inputs": { "in": { "node": "b", "port": "out" } },
                                        "outputs": ["out"]
                                      },
                                      {
                                        "id": "b",
                                        "process": "alcoholic:press",
                                        "inputs": { "in": { "node": "a", "port": "out" } },
                                        "outputs": ["out"]
                                      }
                                    ]
                                  }
                                }
                                """)
                ),
                api
        ));

        assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(),
                Map.of(),
                Map.of(
                        ResourceId.parse("testpack:unknown_process"),
                        JsonDataParser.parse("""
                                {
                                  "id": "testpack:unknown_process",
                                  "graph": {
                                    "nodes": [
                                      {
                                        "id": "custom",
                                        "process": "missing:process",
                                        "outputs": ["out"]
                                      }
                                    ]
                                  }
                                }
                                """)
                ),
                api
        ));

        assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(),
                Map.of(),
                Map.of(
                        ResourceId.parse("testpack:missing_port"),
                        JsonDataParser.parse("""
                                {
                                  "id": "testpack:missing_port",
                                  "graph": {
                                    "nodes": [
                                      {
                                        "id": "press",
                                        "process": "alcoholic:press",
                                        "outputs": ["must"]
                                      },
                                      {
                                        "id": "next",
                                        "process": "alcoholic:ferment",
                                        "inputs": { "in": { "node": "press", "port": "missing" } },
                                        "outputs": ["out"]
                                      }
                                    ]
                                  }
                                }
                                """)
                ),
                api
        ));

        assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(),
                Map.of(),
                Map.of(
                        ResourceId.parse("testpack:one"),
                        JsonDataParser.parse("{\"id\":\"testpack:dup\",\"graph\":{\"nodes\":[]}}"),
                        ResourceId.parse("testpack:two"),
                        JsonDataParser.parse("{\"id\":\"testpack:dup\",\"graph\":{\"nodes\":[]}}")
                ),
                api
        ));

        assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(),
                Map.of(),
                Map.of(
                        ResourceId.parse("testpack:broken_ref"),
                        JsonDataParser.parse("""
                                {
                                  "id": "testpack:broken_ref",
                                  "graph": {
                                    "nodes": [
                                      {
                                        "id": "infuse",
                                        "process": "alcoholic:infuse",
                                        "inputs": { "base": { "beverage": "testpack:missing" } },
                                        "outputs": ["out"]
                                      }
                                    ]
                                  }
                                }
                                """)
                ),
                api
        ));
    }

    @Test
    void rejectsUnknownPropertyAndInvalidAddonConfig() {
        AlcoholicApi api = api();
        TestAddonBootstrap.install(api);

        assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(),
                Map.of(),
                Map.of(
                        ResourceId.parse("testpack:bad_property"),
                        JsonDataParser.parse("""
                                {
                                  "id": "testpack:bad_property",
                                  "properties": ["missing:property"],
                                  "graph": {
                                    "nodes": [
                                      { "id": "press", "process": "alcoholic:press", "outputs": ["out"] }
                                    ]
                                  }
                                }
                                """)
                ),
                api
        ));

        assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(),
                Map.of(),
                Map.of(
                        ResourceId.parse("testaddon:bad_config"),
                        JsonDataParser.parse("""
                                {
                                  "id": "testaddon:bad_config",
                                  "graph": {
                                    "nodes": [
                                      {
                                        "id": "polish",
                                        "process": "testaddon:rice_polishing",
                                        "config": { "ratio": 4.0 },
                                        "outputs": ["out"]
                                      }
                                    ]
                                  }
                                }
                                """)
                ),
                api
        ));
    }

    @Test
    void keepsPreviousSnapshotWhenReplacementIsInvalid() {
        AlcoholicApi api = api();
        BeverageCatalogStore store = new BeverageCatalogStore();
        BeverageCatalog valid = loader.load(
                FixtureCatalogs.ingredients(),
                FixtureCatalogs.processes(),
                FixtureCatalogs.acceptanceBeverages(),
                FixtureCatalogs.liquids(),
                FixtureCatalogs.quality(),
                api
        );
        store.replace(valid);

        assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(),
                Map.of(),
                Map.of(
                        ResourceId.parse("testpack:cycle"),
                        JsonDataParser.parse("""
                                {
                                  "id": "testpack:cycle",
                                  "graph": {
                                    "nodes": [
                                      {
                                        "id": "a",
                                        "process": "alcoholic:press",
                                        "inputs": { "in": { "node": "a", "port": "out" } },
                                        "outputs": ["out"]
                                      }
                                    ]
                                  }
                                }
                                """)
                ),
                api
        ));
        assertEquals(valid, store.snapshot());
    }

    @Test
    void rejectsUnknownQualityGraph() {
        AlcoholicApi api = api();
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(),
                Map.of(),
                Map.of(
                        ResourceId.parse("testpack:beverages/missing_quality"),
                        JsonDataParser.parse("""
                                {
                                  "id": "testpack:missing_quality",
                                  "quality": "testpack:missing",
                                  "graph": {
                                    "nodes": [
                                      { "id": "press", "process": "alcoholic:press", "outputs": ["must"] }
                                    ]
                                  }
                                }
                                """)
                ),
                Map.of(),
                Map.of(),
                api
        ));
        assertTrue(thrown.getMessage().contains("unknown quality graph"));
    }

    @Test
    void rejectsQualityCatalogMissingGeneric() {
        AlcoholicApi api = api();
        DataNode wine = ShippedQualityGraphs.sources().get(new ResourceId("alcoholic", "quality/wine"));
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(new ResourceId("alcoholic", "quality/wine"), wine),
                api
        ));
        assertTrue(thrown.getMessage().contains("alcoholic:generic"));
    }

    @Test
    void accumulatesQualityAndBeverageIssues() {
        AlcoholicApi api = api();
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(),
                Map.of(),
                Map.of(
                        ResourceId.parse("testpack:beverages/cycle"),
                        JsonDataParser.parse("""
                                {
                                  "id": "testpack:cycle",
                                  "graph": {
                                    "nodes": [
                                      {
                                        "id": "a",
                                        "process": "alcoholic:press",
                                        "inputs": { "in": { "node": "a", "port": "out" } },
                                        "outputs": ["out"]
                                      }
                                    ]
                                  }
                                }
                                """)
                ),
                Map.of(),
                Map.of(
                        ResourceId.parse("testpack:quality/loop"),
                        JsonDataParser.parse("""
                                {
                                  "id": "testpack:loop",
                                  "nodes": [
                                    {
                                      "id": "a",
                                      "op": "alcoholic:harvest_complexity",
                                      "inputs": { "in": "b" }
                                    },
                                    {
                                      "id": "b",
                                      "op": "alcoholic:harvest_complexity",
                                      "inputs": { "in": "a" }
                                    }
                                  ],
                                  "outputs": { "profile": { "node": "a", "port": "value" } }
                                }
                                """)
                ),
                api
        ));
        assertTrue(thrown.getMessage().contains("cycle"));
    }

    private static AlcoholicApi api() {
        AlcoholicApi api = AlcoholicApi.create();
        BuiltinRegistrations.install(api);
        return api;
    }
}

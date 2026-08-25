package com.djden.alcoholic.application.beverage;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.JsonDataParser;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessNotMandatoryCatalogTest {
    private final LoadBeverageCatalogUseCase loader = new LoadBeverageCatalogUseCase();

    @Test
    void validatesGraphsThatOmitPressOrFerment() {
        AlcoholicApi api = api();
        BeverageCatalog catalog = loader.load(
                FixtureCatalogs.ingredients(),
                FixtureCatalogs.processes(),
                FixtureCatalogs.acceptanceBeverages(),
                FixtureCatalogs.liquids(),
                api
        );

        assertFalse(hasProcess(catalog, "testpack:fruit_liqueur", "alcoholic:press"));
        assertFalse(hasProcess(catalog, "testpack:fruit_liqueur", "alcoholic:ferment"));
        assertFalse(hasProcess(catalog, "testpack:spirit", "alcoholic:press"));
        assertFalse(hasProcess(catalog, "testpack:spirit", "alcoholic:ferment"));
        assertTrue(hasProcess(catalog, "testpack:rum", "alcoholic:ferment"));
        assertFalse(hasProcess(catalog, "testpack:rum", "alcoholic:press"));
        assertTrue(hasProcess(catalog, "testpack:beer", "alcoholic:mash"));
        assertTrue(hasProcess(catalog, "testpack:whisky", "alcoholic:distill"));
        assertTrue(hasProcess(catalog, "testpack:whisky", "alcoholic:age"));
        assertTrue(hasProcess(catalog, "testpack:beer", "alcoholic:age"));
        assertTrue(hasProcess(catalog, "testpack:cider", "alcoholic:press"));
        assertTrue(hasProcess(catalog, "testpack:cider", "alcoholic:age"));
        assertTrue(hasProcess(catalog, "testpack:rum", "alcoholic:age"));
    }

    @Test
    void acceptsANodeThatFeedsTwoDownstreamProcesses() {
        AlcoholicApi api = api();
        BeverageCatalog catalog = loader.load(
                Map.of(),
                Map.of(),
                Map.of(
                        ResourceId.parse("testpack:split"),
                        JsonDataParser.parse("""
                                {
                                  "id": "testpack:split",
                                  "graph": {
                                    "nodes": [
                                      {
                                        "id": "press",
                                        "process": "alcoholic:press",
                                        "outputs": ["must"]
                                      },
                                      {
                                        "id": "ferment",
                                        "process": "alcoholic:ferment",
                                        "inputs": { "must": { "node": "press", "port": "must" } },
                                        "outputs": ["young"]
                                      },
                                      {
                                        "id": "infuse",
                                        "process": "alcoholic:infuse",
                                        "inputs": { "must": { "node": "press", "port": "must" } },
                                        "outputs": ["flavoured"]
                                      }
                                    ]
                                  }
                                }
                                """)
                ),
                api
        );
        assertTrue(hasProcess(catalog, "testpack:split", "alcoholic:press"));
        assertTrue(hasProcess(catalog, "testpack:split", "alcoholic:ferment"));
        assertTrue(hasProcess(catalog, "testpack:split", "alcoholic:infuse"));
    }

    @Test
    void loadRejectsCyclesWithNodeIds() {
        AlcoholicApi api = api();
        try {
            loader.load(
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
                                            "process": "alcoholic:infuse",
                                            "inputs": { "in": { "node": "c", "port": "out" } },
                                            "outputs": ["out"]
                                          },
                                          {
                                            "id": "b",
                                            "process": "alcoholic:infuse",
                                            "inputs": { "in": { "node": "a", "port": "out" } },
                                            "outputs": ["out"]
                                          },
                                          {
                                            "id": "c",
                                            "process": "alcoholic:infuse",
                                            "inputs": { "in": { "node": "b", "port": "out" } },
                                            "outputs": ["out"]
                                          }
                                        ]
                                      }
                                    }
                                    """)
                    ),
                    api
            );
            throw new AssertionError("cycle must be rejected");
        } catch (IllegalArgumentException exception) {
            assertTrue(exception.getMessage().contains("cycle"));
            assertTrue(exception.getMessage().contains("testpack:cycle"));
        }
    }

    private static boolean hasProcess(BeverageCatalog catalog, String beverage, String process) {
        ResourceId type = ResourceId.parse(process);
        return catalog.beverage(ResourceId.parse(beverage))
                .orElseThrow()
                .graph()
                .nodes()
                .stream()
                .anyMatch(node -> node.processType().filter(type::equals).isPresent());
    }

    private static AlcoholicApi api() {
        AlcoholicApi api = AlcoholicApi.create();
        BuiltinRegistrations.install(api);
        return api;
    }
}

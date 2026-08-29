package com.djden.alcoholic.application.quality;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.JsonDataParser;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.domain.quality.QualityGraphIds;
import com.djden.alcoholic.domain.quality.QualityGraphValidator;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadQualityCatalogUseCaseTest {
    private final LoadQualityCatalogUseCase loader = new LoadQualityCatalogUseCase();

    @Test
    void emptyDatapackLoadsNoGraphs() {
        assertTrue(loader.load(Map.of(), api()).isEmpty());
    }

    @Test
    void loadsShippedGraphsIncludingSumAndObjectInputs() {
        var graphs = ShippedQualityGraphs.load(api());
        assertEquals(4, graphs.size());
        assertTrue(graphs.containsKey(QualityGraphIds.WINE));
        assertTrue(graphs.containsKey(QualityGraphIds.BEER));
        assertTrue(graphs.containsKey(QualityGraphIds.GENERIC));
        assertTrue(graphs.containsKey(QualityGraphIds.SPIRIT));
        assertTrue(graphs.get(QualityGraphIds.WINE).nodes().size() > 2);
    }

    @Test
    void rejectsUnknownOperators() {
        AlcoholicApi api = api();
        assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(
                        ResourceId.parse("testpack:quality/broken"),
                        JsonDataParser.parse("""
                                {
                                  "id": "testpack:broken",
                                  "nodes": [
                                    { "id": "x", "op": "testpack:missing" }
                                  ],
                                  "outputs": { "profile": { "node": "x", "port": "value" } }
                                }
                                """)
                ),
                api
        ));
    }

    @Test
    void rejectsCycles() {
        AlcoholicApi api = api();
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> loader.load(
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

    @Test
    void rejectsDuplicateDatapackIds() {
        AlcoholicApi api = api();
        String graph = """
                {
                  "id": "testpack:dup",
                  "nodes": [
                    { "id": "x", "op": "alcoholic:harvest_complexity" }
                  ],
                  "outputs": { "profile": { "node": "x", "port": "value" } }
                }
                """;
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(
                        ResourceId.parse("testpack:quality/one"),
                        JsonDataParser.parse(graph),
                        ResourceId.parse("testpack:quality/two"),
                        JsonDataParser.parse(graph)
                ),
                api
        ));
        assertTrue(thrown.getMessage().contains("duplicate id"));
    }

    @Test
    void rejectsOversizedGraph() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(
                        ResourceId.parse("testpack:quality/huge"),
                        JsonDataParser.parse(sizedGraph(QualityGraphValidator.MAX_NODES + 1))
                ),
                api()
        ));
        assertTrue(thrown.getMessage().contains("exceeds"));
    }

    @Test
    void acceptsGraphAtNodeLimitViaLoader() {
        var graphs = loader.load(
                Map.of(
                        ResourceId.parse("testpack:quality/limit"),
                        JsonDataParser.parse(sizedGraph(QualityGraphValidator.MAX_NODES))
                ),
                api()
        );
        assertEquals(1, graphs.size());
    }

    @Test
    void rejectsMissingProfileOutput() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(
                        ResourceId.parse("testpack:quality/noprofile"),
                        JsonDataParser.parse("""
                                {
                                  "id": "testpack:noprofile",
                                  "nodes": [
                                    { "id": "x", "op": "alcoholic:harvest_complexity" }
                                  ]
                                }
                                """)
                ),
                api()
        ));
        assertTrue(thrown.getMessage().contains("outputs.profile"));
    }

    @Test
    void foldWithoutOutputsInheritsDefaultOutputs() {
        var graphs = loader.load(
                Map.of(
                        ResourceId.parse("testpack:quality/fold"),
                        JsonDataParser.parse("""
                                {
                                  "id": "testpack:fold",
                                  "nodes": [
                                    { "id": "harvest", "op": "alcoholic:harvest_complexity" },
                                    {
                                      "id": "fold",
                                      "op": "alcoholic:fold_summary",
                                      "inputs": { "complexity": "harvest" }
                                    }
                                  ],
                                  "outputs": { "profile": { "node": "fold", "port": "summary" } }
                                }
                                """)
                ),
                api()
        );
        assertTrue(graphs.get(ResourceId.parse("testpack:fold")).node("fold").orElseThrow()
                .hasOutput("summary"));
    }

    @Test
    void rejectsInvalidReadConfig() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(
                        ResourceId.parse("testpack:quality/read"),
                        JsonDataParser.parse("""
                                {
                                  "id": "testpack:read",
                                  "nodes": [
                                    { "id": "x", "op": "alcoholic:read" }
                                  ],
                                  "outputs": { "profile": { "node": "x", "port": "value" } }
                                }
                                """)
                ),
                api()
        ));
        assertTrue(thrown.getMessage().contains("property"));
    }

    @Test
    void rejectsInvalidDistanceBalanceConfig() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(
                        ResourceId.parse("testpack:quality/balance"),
                        JsonDataParser.parse("""
                                {
                                  "id": "testpack:balance",
                                  "nodes": [
                                    { "id": "x", "op": "alcoholic:distance_balance", "config": {} }
                                  ],
                                  "outputs": { "profile": { "node": "x", "port": "value" } }
                                }
                                """)
                ),
                api()
        ));
        assertTrue(thrown.getMessage().contains("groups"));
    }

    @Test
    void rejectsEthanolAsReadInput() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(
                        ResourceId.parse("testpack:quality/abv"),
                        JsonDataParser.parse("""
                                {
                                  "id": "testpack:abv",
                                  "nodes": [
                                    {
                                      "id": "x",
                                      "op": "alcoholic:read",
                                      "config": { "property": "alcoholic:ethanol" }
                                    }
                                  ],
                                  "outputs": { "profile": { "node": "x", "port": "value" } }
                                }
                                """)
                ),
                api()
        ));
        assertTrue(thrown.getMessage().contains("ethanol"));
    }

    private static String sizedGraph(int nodes) {
        StringBuilder json = new StringBuilder();
        json.append("{\"id\":\"testpack:sized\",\"nodes\":[");
        for (int index = 0; index < nodes; index++) {
            if (index > 0) {
                json.append(',');
            }
            json.append("{\"id\":\"n").append(index).append("\",\"op\":\"alcoholic:harvest_complexity\"}");
        }
        json.append("],\"outputs\":{\"profile\":{\"node\":\"n0\",\"port\":\"value\"}}}");
        return json.toString();
    }

    private static AlcoholicApi api() {
        AlcoholicApi api = AlcoholicApi.create();
        BuiltinRegistrations.install(api);
        return api;
    }
}

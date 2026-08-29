package com.djden.alcoholic.application.quality;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.JsonDataParser;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.domain.quality.BuiltinQualityGraphs;
import com.djden.alcoholic.domain.quality.QualityGraphValidator;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadQualityCatalogUseCaseTest {
    private final LoadQualityCatalogUseCase loader = new LoadQualityCatalogUseCase();

    @Test
    void seedsBuiltinGraphsWhenDatapackIsEmpty() {
        AlcoholicApi api = api();
        var graphs = loader.load(Map.of(), api);
        assertTrue(graphs.containsKey(BuiltinQualityGraphs.WINE));
        assertTrue(graphs.containsKey(BuiltinQualityGraphs.BEER));
        assertTrue(graphs.containsKey(BuiltinQualityGraphs.GENERIC));
        assertEquals(4, graphs.size());
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
    void datapackOverridesBuiltinId() {
        AlcoholicApi api = api();
        var graphs = loader.load(
                Map.of(
                        ResourceId.parse("alcoholic:quality/wine"),
                        JsonDataParser.parse("""
                                {
                                  "id": "alcoholic:wine",
                                  "nodes": [
                                    { "id": "harvest", "op": "alcoholic:harvest_complexity" },
                                    {
                                      "id": "fold",
                                      "op": "alcoholic:fold_summary",
                                      "inputs": { "complexity": "harvest" },
                                      "outputs": ["purity", "complexity", "maturity", "balance", "defects", "summary"]
                                    }
                                  ],
                                  "outputs": { "profile": { "node": "fold", "port": "summary" } }
                                }
                                """)
                ),
                api
        );
        assertEquals(2, graphs.get(BuiltinQualityGraphs.WINE).nodes().size());
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
        StringBuilder json = new StringBuilder();
        json.append("{\"id\":\"testpack:huge\",\"nodes\":[");
        for (int index = 0; index <= QualityGraphValidator.MAX_NODES; index++) {
            if (index > 0) {
                json.append(',');
            }
            json.append("{\"id\":\"n").append(index).append("\",\"op\":\"alcoholic:harvest_complexity\"}");
        }
        json.append("],\"outputs\":{\"profile\":{\"node\":\"n0\",\"port\":\"value\"}}}");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> loader.load(
                Map.of(
                        ResourceId.parse("testpack:quality/huge"),
                        JsonDataParser.parse(json.toString())
                ),
                api()
        ));
        assertTrue(thrown.getMessage().contains("exceeds"));
    }

    private static AlcoholicApi api() {
        AlcoholicApi api = AlcoholicApi.create();
        BuiltinRegistrations.install(api);
        return api;
    }
}

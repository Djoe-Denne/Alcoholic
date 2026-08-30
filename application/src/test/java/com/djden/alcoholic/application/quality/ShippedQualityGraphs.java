package com.djden.alcoholic.application.quality;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.data.JsonDataParser;
import com.djden.alcoholic.domain.quality.QualityGraph;
import com.djden.alcoholic.domain.quality.QualityGraphIds;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ShippedQualityGraphs {
    private ShippedQualityGraphs() {
    }

    public static Map<ResourceId, DataNode> sources() {
        Map<ResourceId, DataNode> sources = new LinkedHashMap<>();
        for (String name : List.of("wine", "beer", "generic", "spirit")) {
            sources.put(new ResourceId("alcoholic", "quality/" + name), read(name));
        }
        return sources;
    }

    static Map<ResourceId, QualityGraph> load(AlcoholicApi api) {
        return new LoadQualityCatalogUseCase().load(sources(), api);
    }

    static QualityGraph graph(AlcoholicApi api, ResourceId id) {
        QualityGraph graph = load(api).get(id);
        if (graph == null) {
            throw new IllegalStateException("missing shipped graph " + id);
        }
        return graph;
    }

    static QualityGraph generic(AlcoholicApi api) {
        return graph(api, QualityGraphIds.GENERIC);
    }

    private static DataNode read(String name) {
        String classpath = "data/alcoholic/alcoholic/quality/" + name + ".json";
        try (InputStream input = ShippedQualityGraphs.class.getClassLoader().getResourceAsStream(classpath)) {
            if (input == null) {
                throw new IllegalStateException("Missing shipped quality graph " + classpath);
            }
            return JsonDataParser.parse(new String(input.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}

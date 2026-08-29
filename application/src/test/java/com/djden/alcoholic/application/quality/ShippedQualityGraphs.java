package com.djden.alcoholic.application.quality;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.data.JsonDataParser;
import com.djden.alcoholic.domain.quality.QualityGraph;
import com.djden.alcoholic.domain.quality.QualityGraphIds;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ShippedQualityGraphs {
    private ShippedQualityGraphs() {
    }

    private static Path directory() {
        Path fromModule = Path.of(
                "..",
                "minecraft-common",
                "src",
                "main",
                "resources",
                "data",
                "alcoholic",
                "alcoholic",
                "quality"
        );
        if (Files.isDirectory(fromModule)) {
            return fromModule;
        }
        Path fromRoot = Path.of(
                "minecraft-common",
                "src",
                "main",
                "resources",
                "data",
                "alcoholic",
                "alcoholic",
                "quality"
        );
        if (Files.isDirectory(fromRoot)) {
            return fromRoot;
        }
        throw new IllegalStateException("Cannot find shipped quality graphs under " + fromModule.toAbsolutePath());
    }

    public static Map<ResourceId, DataNode> sources() {
        Path dir = directory();
        Map<ResourceId, DataNode> sources = new LinkedHashMap<>();
        for (String name : List.of("wine", "beer", "generic", "spirit")) {
            Path file = dir.resolve(name + ".json");
            try {
                sources.put(
                        new ResourceId("alcoholic", "quality/" + name),
                        JsonDataParser.parse(Files.readString(file))
                );
            } catch (IOException exception) {
                throw new UncheckedIOException("Missing shipped quality graph " + file.toAbsolutePath(), exception);
            }
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
}

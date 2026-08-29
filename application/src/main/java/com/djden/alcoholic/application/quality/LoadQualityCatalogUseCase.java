package com.djden.alcoholic.application.quality;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.quality.QualityOperator;
import com.djden.alcoholic.application.beverage.ValidationIssue;
import com.djden.alcoholic.application.beverage.ValidationResult;
import com.djden.alcoholic.application.beverage.codec.ProcessDefinitionCodec;
import com.djden.alcoholic.application.beverage.codec.QualityGraphCodec;
import com.djden.alcoholic.domain.beverage.GraphIssue;
import com.djden.alcoholic.domain.quality.BuiltinQualityGraphs;
import com.djden.alcoholic.domain.quality.QualityGraph;
import com.djden.alcoholic.domain.quality.QualityGraphValidator;
import com.djden.alcoholic.domain.quality.QualityNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class LoadQualityCatalogUseCase {
    public Map<ResourceId, QualityGraph> load(Map<ResourceId, DataNode> sources, AlcoholicApi api) {
        Objects.requireNonNull(api, "api");
        List<ValidationIssue> issues = new ArrayList<>();
        Map<ResourceId, QualityGraph> graphs = new LinkedHashMap<>(BuiltinQualityGraphs.all());
        Set<ResourceId> datapackIds = new HashSet<>();
        Objects.requireNonNull(sources, "quality").forEach((source, node) -> {
            String path = "quality/" + source;
            try {
                QualityGraph graph = QualityGraphCodec.INSTANCE.decode(
                        node,
                        path,
                        ProcessDefinitionCodec.fallbackId(source)
                );
                if (!datapackIds.add(graph.id())) {
                    issues.add(new ValidationIssue(path, "duplicate id " + graph.id()));
                    return;
                }
                graphs.put(graph.id(), graph);
            } catch (RuntimeException exception) {
                issues.add(new ValidationIssue(path, exception.getMessage()));
            }
        });
        graphs.forEach((id, graph) -> validate(graph, "quality/" + id, api, issues));
        new ValidationResult(issues).throwIfInvalid();
        return Map.copyOf(graphs);
    }

    private static void validate(
            QualityGraph graph,
            String path,
            AlcoholicApi api,
            List<ValidationIssue> issues
    ) {
        for (GraphIssue issue : QualityGraphValidator.validate(graph, path)) {
            issues.add(new ValidationIssue(issue.path(), issue.message()));
        }
        for (QualityNode node : graph.nodes()) {
            String nodePath = path + "/nodes/" + node.id();
            Optional<QualityOperator<?>> operator = api.qualityOperators().get(node.operator());
            if (operator.isEmpty()) {
                issues.add(new ValidationIssue(nodePath + "/op", "unknown quality operator " + node.operator()));
                continue;
            }
            try {
                operator.get().configCodec().decode(node.config(), nodePath + "/config");
            } catch (RuntimeException exception) {
                issues.add(new ValidationIssue(nodePath + "/config", exception.getMessage()));
            }
        }
    }
}

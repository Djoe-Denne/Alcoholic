package com.djden.alcoholic.application.quality;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.quality.QualityOperator;
import com.djden.alcoholic.api.quality.QualityPropertyRefs;
import com.djden.alcoholic.application.beverage.ValidationIssue;
import com.djden.alcoholic.application.beverage.ValidationResult;
import com.djden.alcoholic.application.beverage.codec.ProcessDefinitionCodec;
import com.djden.alcoholic.application.beverage.codec.QualityGraphCodec;
import com.djden.alcoholic.domain.beverage.GraphIssue;
import com.djden.alcoholic.domain.process.QualityProfile;
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
        List<ValidationIssue> issues = new ArrayList<>();
        Map<ResourceId, QualityGraph> graphs = load(sources, api, issues);
        new ValidationResult(issues).throwIfInvalid();
        return graphs;
    }

    public Map<ResourceId, QualityGraph> load(
            Map<ResourceId, DataNode> sources,
            AlcoholicApi api,
            List<ValidationIssue> issues
    ) {
        Objects.requireNonNull(api, "api");
        Objects.requireNonNull(issues, "issues");
        Map<ResourceId, QualityGraph> graphs = new LinkedHashMap<>();
        Set<ResourceId> datapackIds = new HashSet<>();
        Objects.requireNonNull(sources, "quality").forEach((source, node) -> {
            String path = "quality/" + source;
            try {
                QualityGraph graph = applyDefaultOutputs(
                        QualityGraphCodec.INSTANCE.decode(
                                node,
                                path,
                                ProcessDefinitionCodec.fallbackId(source)
                        ),
                        api,
                        path,
                        issues
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
        return Map.copyOf(graphs);
    }

    private static QualityGraph applyDefaultOutputs(
            QualityGraph graph,
            AlcoholicApi api,
            String path,
            List<ValidationIssue> issues
    ) {
        List<QualityNode> nodes = new ArrayList<>();
        for (QualityNode node : graph.nodes()) {
            List<String> outputs = node.outputs();
            if (outputs.isEmpty()) {
                Optional<QualityOperator<?>> operator = api.qualityOperators().get(node.operator());
                if (operator.isPresent()) {
                    outputs = operator.get().defaultOutputs();
                }
                if (outputs.isEmpty()) {
                    issues.add(new ValidationIssue(
                            path + "/nodes/" + node.id() + "/outputs",
                            "node must declare outputs"
                    ));
                }
            }
            nodes.add(new QualityNode(node.id(), node.operator(), node.config(), node.inputs(), outputs));
        }
        return new QualityGraph(graph.id(), nodes, graph.outputs());
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
                Object config = operator.get().configCodec().decode(node.config(), nodePath + "/config");
                rejectEthanolInputs(config, nodePath + "/config", issues);
            } catch (RuntimeException exception) {
                issues.add(new ValidationIssue(nodePath + "/config", exception.getMessage()));
            }
        }
    }

    private static void rejectEthanolInputs(Object config, String path, List<ValidationIssue> issues) {
        if (config instanceof QualityPropertyRefs refs) {
            refs.propertyIds().forEach(id -> rejectEthanol(id, path, issues));
        }
    }

    private static void rejectEthanol(ResourceId id, String path, List<ValidationIssue> issues) {
        if (QualityProfile.ETHANOL.equals(id)) {
            issues.add(new ValidationIssue(path, "ethanol is never a quality input"));
        }
    }
}

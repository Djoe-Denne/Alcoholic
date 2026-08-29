package com.djden.alcoholic.domain.quality;

import com.djden.alcoholic.domain.beverage.GraphIssue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Structural DAG checks that do not consult the operator registry.
 */
public final class QualityGraphValidator {
    public static final int MAX_NODES = 256;

    private QualityGraphValidator() {
    }

    public static List<GraphIssue> validate(QualityGraph graph, String path) {
        List<GraphIssue> issues = new ArrayList<>();
        if (graph.nodes().size() > MAX_NODES) {
            issues.add(new GraphIssue(path + "/nodes", "graph exceeds " + MAX_NODES + " nodes"));
            return List.copyOf(issues);
        }
        Map<String, QualityNode> nodes = new LinkedHashMap<>();
        for (int index = 0; index < graph.nodes().size(); index++) {
            QualityNode node = graph.nodes().get(index);
            String nodePath = path + "/nodes[" + index + "]";
            if (nodes.putIfAbsent(node.id(), node) != null) {
                issues.add(new GraphIssue(nodePath + "/id", "duplicate node " + node.id()));
            }
        }

        for (QualityNode node : graph.nodes()) {
            String nodePath = path + "/nodes/" + node.id();
            node.inputs().forEach((port, input) -> validateInput(
                    input,
                    nodes,
                    nodePath + "/inputs/" + port,
                    issues
            ));
        }

        graph.outputs().forEach((name, output) -> {
            String outputPath = path + "/outputs/" + name;
            QualityNode source = nodes.get(output.nodeId());
            if (source == null) {
                issues.add(new GraphIssue(outputPath, "unknown node " + output.nodeId()));
                return;
            }
            if (!source.hasOutput(output.port())) {
                issues.add(new GraphIssue(
                        outputPath,
                        "unknown port " + output.port() + " on node " + output.nodeId()
                ));
            }
        });

        issues.addAll(detectCycles(graph, nodes, path));
        return List.copyOf(issues);
    }

    private static void validateInput(
            QualityInput input,
            Map<String, QualityNode> nodes,
            String path,
            List<GraphIssue> issues
    ) {
        if (input instanceof QualityInput.NodePort reference) {
            validatePort(reference, nodes, path, issues);
            return;
        }
        if (input instanceof QualityInput.Sum sum) {
            for (int index = 0; index < sum.sources().size(); index++) {
                validatePort(
                        sum.sources().get(index),
                        nodes,
                        path + "[" + index + "]",
                        issues
                );
            }
        }
    }

    private static void validatePort(
            QualityInput.NodePort reference,
            Map<String, QualityNode> nodes,
            String path,
            List<GraphIssue> issues
    ) {
        QualityNode source = nodes.get(reference.nodeId());
        if (source == null) {
            issues.add(new GraphIssue(path, "unknown node " + reference.nodeId()));
            return;
        }
        if (!source.hasOutput(reference.port()) && !"value".equals(reference.port())) {
            issues.add(new GraphIssue(
                    path,
                    "unknown port " + reference.port() + " on node " + reference.nodeId()
            ));
        }
    }

    private static List<GraphIssue> detectCycles(
            QualityGraph graph,
            Map<String, QualityNode> nodes,
            String path
    ) {
        Map<String, Set<String>> outgoing = new HashMap<>();
        Map<String, Integer> incoming = new HashMap<>();
        nodes.keySet().forEach(id -> {
            outgoing.put(id, new HashSet<>());
            incoming.put(id, 0);
        });
        for (QualityNode node : graph.nodes()) {
            for (QualityInput input : node.inputs().values()) {
                for (QualityInput.NodePort reference : sources(input)) {
                    if (nodes.containsKey(reference.nodeId())
                            && outgoing.get(reference.nodeId()).add(node.id())) {
                        incoming.merge(node.id(), 1, Integer::sum);
                    }
                }
            }
        }

        Deque<String> ready = new ArrayDeque<>();
        incoming.forEach((id, count) -> {
            if (count == 0) {
                ready.add(id);
            }
        });
        int visited = 0;
        while (!ready.isEmpty()) {
            String current = ready.removeFirst();
            visited++;
            for (String next : outgoing.get(current)) {
                int remaining = incoming.merge(next, -1, Integer::sum);
                if (remaining == 0) {
                    ready.add(next);
                }
            }
        }
        if (visited == nodes.size()) {
            return List.of();
        }
        List<String> cyclic = new ArrayList<>();
        incoming.forEach((id, count) -> {
            if (count > 0) {
                cyclic.add(id);
            }
        });
        cyclic.sort(String::compareTo);
        return List.of(new GraphIssue(
                path,
                "graph contains a cycle involving nodes " + String.join(", ", cyclic)
        ));
    }

    private static List<QualityInput.NodePort> sources(QualityInput input) {
        if (input instanceof QualityInput.NodePort port) {
            return List.of(port);
        }
        if (input instanceof QualityInput.Sum sum) {
            return sum.sources();
        }
        return List.of();
    }
}

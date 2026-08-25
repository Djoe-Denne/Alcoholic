package com.djden.alcoholic.domain.beverage;

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
 * Structural DAG checks that do not consult process or property registries.
 */
public final class ProcessGraphValidator {
    private ProcessGraphValidator() {
    }

    public static List<GraphIssue> validate(ProcessGraph graph, String path) {
        List<GraphIssue> issues = new ArrayList<>();
        Map<String, ProcessNode> nodes = new LinkedHashMap<>();
        for (int index = 0; index < graph.nodes().size(); index++) {
            ProcessNode node = graph.nodes().get(index);
            String nodePath = path + "/nodes[" + index + "]";
            if (nodes.putIfAbsent(node.id(), node) != null) {
                issues.add(new GraphIssue(nodePath + "/id", "duplicate node " + node.id()));
            }
            Set<String> ports = new HashSet<>();
            for (String output : node.outputs()) {
                if (!ports.add(output)) {
                    issues.add(new GraphIssue(nodePath + "/outputs", "duplicate port " + output));
                }
            }
        }

        for (ProcessNode node : graph.nodes()) {
            String nodePath = path + "/nodes/" + node.id();
            node.inputs().forEach((port, input) -> {
                if (!(input instanceof InputReference.NodeOutputInput reference)) {
                    return;
                }
                String inputPath = nodePath + "/inputs/" + port;
                ProcessNode source = nodes.get(reference.nodeId());
                if (source == null) {
                    issues.add(new GraphIssue(inputPath, "unknown node " + reference.nodeId()));
                    return;
                }
                if (!source.hasOutput(reference.port())) {
                    issues.add(new GraphIssue(
                            inputPath,
                            "unknown port " + reference.port() + " on node " + reference.nodeId()
                    ));
                }
            });
        }

        graph.outputs().forEach((name, output) -> {
            String outputPath = path + "/outputs/" + name;
            ProcessNode source = nodes.get(output.nodeId());
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

    private static List<GraphIssue> detectCycles(
            ProcessGraph graph,
            Map<String, ProcessNode> nodes,
            String path
    ) {
        Map<String, Set<String>> outgoing = new HashMap<>();
        Map<String, Integer> incoming = new HashMap<>();
        nodes.keySet().forEach(id -> {
            outgoing.put(id, new HashSet<>());
            incoming.put(id, 0);
        });
        for (ProcessNode node : graph.nodes()) {
            for (InputReference input : node.inputs().values()) {
                if (input instanceof InputReference.NodeOutputInput reference
                        && nodes.containsKey(reference.nodeId())) {
                    if (outgoing.get(reference.nodeId()).add(node.id())) {
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
        java.util.List<String> cyclic = new java.util.ArrayList<>();
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
}

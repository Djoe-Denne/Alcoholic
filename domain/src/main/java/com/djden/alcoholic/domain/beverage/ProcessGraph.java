package com.djden.alcoholic.domain.beverage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record ProcessGraph(
        List<ProcessNode> nodes,
        Map<String, OutputReference> outputs
) {
    public ProcessGraph {
        nodes = List.copyOf(new ArrayList<>(Objects.requireNonNull(nodes, "nodes")));
        outputs = copyOutputs(outputs);
    }

    public Optional<ProcessNode> node(String id) {
        return nodes.stream().filter(node -> node.id().equals(id)).findFirst();
    }

    private static Map<String, OutputReference> copyOutputs(Map<String, OutputReference> outputs) {
        Objects.requireNonNull(outputs, "outputs");
        Map<String, OutputReference> copy = new LinkedHashMap<>();
        outputs.forEach((key, value) -> copy.put(
                Objects.requireNonNull(key, "output"),
                Objects.requireNonNull(value, "reference")
        ));
        return Map.copyOf(copy);
    }
}

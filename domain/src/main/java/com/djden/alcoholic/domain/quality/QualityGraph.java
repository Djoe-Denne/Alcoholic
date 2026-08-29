package com.djden.alcoholic.domain.quality;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.beverage.OutputReference;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record QualityGraph(
        ResourceId id,
        List<QualityNode> nodes,
        Map<String, OutputReference> outputs
) {
    public QualityGraph {
        Objects.requireNonNull(id, "id");
        nodes = List.copyOf(new ArrayList<>(Objects.requireNonNull(nodes, "nodes")));
        outputs = copyOutputs(outputs);
    }

    public Optional<QualityNode> node(String nodeId) {
        return nodes.stream().filter(node -> node.id().equals(nodeId)).findFirst();
    }

    private static Map<String, OutputReference> copyOutputs(Map<String, OutputReference> outputs) {
        Map<String, OutputReference> copy = new LinkedHashMap<>();
        Objects.requireNonNull(outputs, "outputs").forEach((key, value) -> copy.put(
                Objects.requireNonNull(key, "output"),
                Objects.requireNonNull(value, "reference")
        ));
        return Map.copyOf(copy);
    }
}

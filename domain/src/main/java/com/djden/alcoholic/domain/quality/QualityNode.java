package com.djden.alcoholic.domain.quality;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record QualityNode(
        String id,
        ResourceId operator,
        DataNode config,
        Map<String, QualityInput> inputs,
        List<String> outputs
) {
    public QualityNode {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id is blank");
        }
        Objects.requireNonNull(operator, "operator");
        config = config == null ? DataNode.object(Map.of()) : config;
        inputs = copyInputs(inputs);
        outputs = outputs == null || outputs.isEmpty()
                ? List.of("value")
                : List.copyOf(new ArrayList<>(outputs));
    }

    public boolean hasOutput(String port) {
        return outputs.contains(port);
    }

    private static Map<String, QualityInput> copyInputs(Map<String, QualityInput> inputs) {
        Map<String, QualityInput> copy = new LinkedHashMap<>();
        Objects.requireNonNull(inputs, "inputs").forEach((key, value) -> copy.put(
                Objects.requireNonNull(key, "port"),
                Objects.requireNonNull(value, "input")
        ));
        return Map.copyOf(copy);
    }
}

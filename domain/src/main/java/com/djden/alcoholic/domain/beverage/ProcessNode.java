package com.djden.alcoholic.domain.beverage;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record ProcessNode(
        String id,
        Optional<ResourceId> processType,
        Optional<ResourceId> processDefinition,
        DataNode config,
        Map<String, InputReference> inputs,
        List<String> outputs
) {
    public ProcessNode {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id is blank");
        }
        processType = Objects.requireNonNull(processType, "processType");
        processDefinition = Objects.requireNonNull(processDefinition, "processDefinition");
        config = config == null ? DataNode.object(Map.of()) : config;
        inputs = copyInputs(inputs);
        outputs = List.copyOf(new ArrayList<>(Objects.requireNonNull(outputs, "outputs")));
        if (processType.isEmpty() && processDefinition.isEmpty()) {
            throw new IllegalArgumentException("node " + id + " must declare a process type or definition");
        }
    }

    public boolean hasOutput(String port) {
        return outputs.contains(port);
    }

    private static Map<String, InputReference> copyInputs(Map<String, InputReference> inputs) {
        Objects.requireNonNull(inputs, "inputs");
        Map<String, InputReference> copy = new LinkedHashMap<>();
        inputs.forEach((key, value) -> copy.put(
                Objects.requireNonNull(key, "port"),
                Objects.requireNonNull(value, "input")
        ));
        return Map.copyOf(copy);
    }
}

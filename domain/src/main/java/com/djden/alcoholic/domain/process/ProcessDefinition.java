package com.djden.alcoholic.domain.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.domain.beverage.InputReference;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ProcessDefinition(
        ResourceId id,
        ResourceId processType,
        DataNode config,
        Map<String, InputReference> inputs,
        List<String> outputs
) {
    public ProcessDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(processType, "processType");
        config = config == null ? DataNode.object(Map.of()) : config;
        inputs = copyInputs(inputs);
        outputs = List.copyOf(new ArrayList<>(Objects.requireNonNull(outputs, "outputs")));
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

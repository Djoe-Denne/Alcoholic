package com.djden.alcoholic.application.beverage.codec;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.domain.process.ProcessDefinition;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ProcessDefinitionCodec implements DataCodec<ProcessDefinition> {
    public static final ProcessDefinitionCodec INSTANCE = new ProcessDefinitionCodec();

    private ProcessDefinitionCodec() {
    }

    public ProcessDefinition decode(DataNode node, String path, ResourceId fallbackId) {
        DataNode.ObjectNode object = node.asObject(path);
        ResourceId id = object.get("id")
                .map(value -> DataCodecs.RESOURCE_ID.decode(value, DataDecodeException.child(path, "id")))
                .orElse(fallbackId);
        ResourceId processType = DataCodecs.RESOURCE_ID.decode(
                object.require("process", path),
                DataDecodeException.child(path, "process")
        );
        DataNode config = object.get("config").orElseGet(() -> DataNode.object(Map.of()));
        return new ProcessDefinition(
                id,
                processType,
                config,
                InputReferenceCodec.INSTANCE.decodeMap(
                        object.get("inputs").orElseGet(() -> DataNode.object(Map.of())),
                        DataDecodeException.child(path, "inputs")
                ),
                object.get("outputs")
                        .map(value -> DataCodecs.STRING.listOf().decode(
                                value,
                                DataDecodeException.child(path, "outputs")
                        ))
                        .orElse(List.of())
        );
    }

    @Override
    public ProcessDefinition decode(DataNode node, String path) {
        return decode(node, path, null);
    }

    @Override
    public DataNode encode(ProcessDefinition value) {
        DataNode.ObjectBuilder builder = DataNode.objectBuilder()
                .put("id", DataNode.string(value.id().toString()))
                .put("process", DataNode.string(value.processType().toString()))
                .put("config", value.config());
        DataNode.ObjectBuilder inputs = DataNode.objectBuilder();
        value.inputs().forEach((name, input) -> inputs.put(name, InputReferenceCodec.INSTANCE.encode(input)));
        builder.put("inputs", inputs.build());
        builder.put("outputs", DataCodecs.STRING.listOf().encode(value.outputs()));
        return builder.build();
    }

    public static ResourceId fallbackId(ResourceId source) {
        String path = source.path();
        int separator = path.lastIndexOf('/');
        return new ResourceId(source.namespace(), separator >= 0 ? path.substring(separator + 1) : path);
    }

    public static Optional<ResourceId> declaredId(DataNode node, String path) {
        return node.asObject(path).get("id")
                .map(value -> DataCodecs.RESOURCE_ID.decode(value, DataDecodeException.child(path, "id")));
    }
}

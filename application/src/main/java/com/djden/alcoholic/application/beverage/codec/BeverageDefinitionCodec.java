package com.djden.alcoholic.application.beverage.codec;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.domain.beverage.BeverageDefinition;
import com.djden.alcoholic.domain.beverage.OutputReference;
import com.djden.alcoholic.domain.beverage.ProcessGraph;
import com.djden.alcoholic.domain.beverage.ProcessNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BeverageDefinitionCodec implements DataCodec<BeverageDefinition> {
    public static final BeverageDefinitionCodec INSTANCE = new BeverageDefinitionCodec();

    private BeverageDefinitionCodec() {
    }

    public BeverageDefinition decode(DataNode node, String path, ResourceId fallbackId) {
        DataNode.ObjectNode object = node.asObject(path);
        ResourceId id = object.get("id")
                .map(value -> DataCodecs.RESOURCE_ID.decode(value, DataDecodeException.child(path, "id")))
                .orElse(fallbackId);
        Optional<ResourceId> category = object.get("category").map(value -> parseCategory(value, path));
        DataNode graphNode = object.get("graph").orElseGet(() -> DataNode.object(Map.of()));
        List<ResourceId> properties = object.get("properties")
                .map(value -> DataCodecs.RESOURCE_ID.listOf().decode(
                        value,
                        DataDecodeException.child(path, "properties")
                ))
                .orElse(List.of());
        return new BeverageDefinition(
                id,
                category,
                decodeGraph(graphNode, DataDecodeException.child(path, "graph")),
                properties
        );
    }

    @Override
    public BeverageDefinition decode(DataNode node, String path) {
        return decode(node, path, null);
    }

    @Override
    public DataNode encode(BeverageDefinition value) {
        DataNode.ObjectBuilder builder = DataNode.objectBuilder()
                .put("id", DataNode.string(value.id().toString()));
        value.category().ifPresent(category -> builder.put("category", DataNode.string(category.toString())));
        builder.put("graph", encodeGraph(value.graph()));
        builder.put("properties", DataCodecs.RESOURCE_ID.listOf().encode(value.properties()));
        return builder.build();
    }

    private static ResourceId parseCategory(DataNode node, String path) {
        String raw = node.asString(DataDecodeException.child(path, "category"));
        if (raw.indexOf(':') >= 0) {
            return ResourceId.parse(raw);
        }
        return new ResourceId("alcoholic", raw);
    }

    private static ProcessGraph decodeGraph(DataNode node, String path) {
        DataNode.ObjectNode object = node.asObject(path);
        List<ProcessNode> nodes = new ArrayList<>();
        DataNode nodesNode = object.get("nodes").orElseGet(() -> DataNode.list(List.of()));
        DataNode.ListNode list = nodesNode.asList(DataDecodeException.child(path, "nodes"));
        for (int index = 0; index < list.size(); index++) {
            nodes.add(decodeNode(list.get(index), DataDecodeException.index(path + "/nodes", index)));
        }
        Map<String, OutputReference> outputs = new LinkedHashMap<>();
        object.get("outputs").ifPresent(value -> {
            value.asObject(DataDecodeException.child(path, "outputs")).fields()
                    .forEach((name, child) -> outputs.put(
                            name,
                            decodeOutput(child, DataDecodeException.child(path + "/outputs", name))
                    ));
        });
        return new ProcessGraph(nodes, outputs);
    }

    private static ProcessNode decodeNode(DataNode node, String path) {
        DataNode.ObjectNode object = node.asObject(path);
        String id = object.require("id", path).asString(DataDecodeException.child(path, "id"));
        Optional<ResourceId> processType = object.get("process")
                .map(value -> DataCodecs.RESOURCE_ID.decode(value, DataDecodeException.child(path, "process")));
        Optional<ResourceId> definition = object.get("definition")
                .map(value -> DataCodecs.RESOURCE_ID.decode(value, DataDecodeException.child(path, "definition")));
        DataNode config = object.get("config").orElseGet(() -> DataNode.object(Map.of()));
        return new ProcessNode(
                id,
                processType,
                definition,
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

    private static OutputReference decodeOutput(DataNode node, String path) {
        DataNode.ObjectNode object = node.asObject(path);
        return new OutputReference(
                object.require("node", path).asString(DataDecodeException.child(path, "node")),
                object.require("port", path).asString(DataDecodeException.child(path, "port"))
        );
    }

    private static DataNode encodeGraph(ProcessGraph graph) {
        List<DataNode> nodes = new ArrayList<>();
        for (ProcessNode node : graph.nodes()) {
            DataNode.ObjectBuilder builder = DataNode.objectBuilder()
                    .put("id", DataNode.string(node.id()));
            node.processType().ifPresent(id -> builder.put("process", DataNode.string(id.toString())));
            node.processDefinition().ifPresent(id -> builder.put("definition", DataNode.string(id.toString())));
            builder.put("config", node.config());
            DataNode.ObjectBuilder inputs = DataNode.objectBuilder();
            node.inputs().forEach((name, input) -> inputs.put(name, InputReferenceCodec.INSTANCE.encode(input)));
            builder.put("inputs", inputs.build());
            builder.put("outputs", DataCodecs.STRING.listOf().encode(node.outputs()));
            nodes.add(builder.build());
        }
        DataNode.ObjectBuilder outputs = DataNode.objectBuilder();
        graph.outputs().forEach((name, output) -> outputs.put(
                name,
                DataNode.objectBuilder()
                        .put("node", DataNode.string(output.nodeId()))
                        .put("port", DataNode.string(output.port()))
                        .build()
        ));
        return DataNode.objectBuilder()
                .put("nodes", DataNode.list(nodes))
                .put("outputs", outputs.build())
                .build();
    }
}

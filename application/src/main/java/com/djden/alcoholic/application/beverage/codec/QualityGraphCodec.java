package com.djden.alcoholic.application.beverage.codec;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.domain.beverage.OutputReference;
import com.djden.alcoholic.domain.quality.QualityGraph;
import com.djden.alcoholic.domain.quality.QualityInput;
import com.djden.alcoholic.domain.quality.QualityNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class QualityGraphCodec implements DataCodec<QualityGraph> {
    public static final QualityGraphCodec INSTANCE = new QualityGraphCodec();

    private QualityGraphCodec() {
    }

    public QualityGraph decode(DataNode node, String path, ResourceId fallbackId) {
        DataNode.ObjectNode object = node.asObject(path);
        ResourceId id = object.get("id")
                .map(value -> DataCodecs.RESOURCE_ID.decode(value, DataDecodeException.child(path, "id")))
                .orElse(fallbackId);
        DataNode graphNode = object.get("nodes").isPresent() ? node : object.get("graph").orElse(node);
        return new QualityGraph(id, decodeNodes(graphNode, path), decodeOutputs(graphNode, path));
    }

    @Override
    public QualityGraph decode(DataNode node, String path) {
        return decode(node, path, null);
    }

    @Override
    public DataNode encode(QualityGraph value) {
        List<DataNode> nodes = new ArrayList<>();
        for (QualityNode node : value.nodes()) {
            DataNode.ObjectBuilder builder = DataNode.objectBuilder()
                    .put("id", DataNode.string(node.id()))
                    .put("op", DataNode.string(node.operator().toString()))
                    .put("config", node.config());
            DataNode.ObjectBuilder inputs = DataNode.objectBuilder();
            node.inputs().forEach((name, input) -> inputs.put(name, encodeInput(input)));
            builder.put("inputs", inputs.build());
            builder.put("outputs", DataCodecs.STRING.listOf().encode(node.outputs()));
            nodes.add(builder.build());
        }
        DataNode.ObjectBuilder outputs = DataNode.objectBuilder();
        value.outputs().forEach((name, output) -> outputs.put(
                name,
                DataNode.objectBuilder()
                        .put("node", DataNode.string(output.nodeId()))
                        .put("port", DataNode.string(output.port()))
                        .build()
        ));
        return DataNode.objectBuilder()
                .put("id", DataNode.string(value.id().toString()))
                .put("nodes", DataNode.list(nodes))
                .put("outputs", outputs.build())
                .build();
    }

    private static List<QualityNode> decodeNodes(DataNode node, String path) {
        DataNode.ObjectNode object = node.asObject(path);
        DataNode.ListNode list = object.get("nodes").orElseGet(() -> DataNode.list(List.of()))
                .asList(DataDecodeException.child(path, "nodes"));
        List<QualityNode> nodes = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            nodes.add(decodeNode(list.get(index), DataDecodeException.index(path + "/nodes", index)));
        }
        return nodes;
    }

    private static Map<String, OutputReference> decodeOutputs(DataNode node, String path) {
        DataNode.ObjectNode object = node.asObject(path);
        Map<String, OutputReference> outputs = new LinkedHashMap<>();
        object.get("outputs").ifPresent(value -> value.asObject(DataDecodeException.child(path, "outputs"))
                .fields()
                .forEach((name, child) -> outputs.put(
                        name,
                        decodeOutput(child, DataDecodeException.child(path + "/outputs", name))
                )));
        return outputs;
    }

    private static QualityNode decodeNode(DataNode node, String path) {
        DataNode.ObjectNode object = node.asObject(path);
        String id = object.require("id", path).asString(DataDecodeException.child(path, "id"));
        ResourceId operator = DataCodecs.RESOURCE_ID.decode(
                object.get("op").orElseGet(() -> object.require("operator", path)),
                DataDecodeException.child(path, "op")
        );
        return new QualityNode(
                id,
                operator,
                object.get("config").orElseGet(() -> DataNode.object(Map.of())),
                decodeInputs(object.get("inputs").orElseGet(() -> DataNode.object(Map.of())),
                        DataDecodeException.child(path, "inputs")),
                object.get("outputs")
                        .map(value -> DataCodecs.STRING.listOf().decode(
                                value,
                                DataDecodeException.child(path, "outputs")
                        ))
                        .orElse(List.of())
        );
    }

    private static Map<String, QualityInput> decodeInputs(DataNode node, String path) {
        Map<String, QualityInput> inputs = new LinkedHashMap<>();
        node.asObject(path).fields().forEach((name, child) ->
                inputs.put(name, decodeInput(child, DataDecodeException.child(path, name))));
        return inputs;
    }

    private static QualityInput decodeInput(DataNode node, String path) {
        if (node instanceof DataNode.StringNode string) {
            return new QualityInput.NodePort(string.value());
        }
        if (node instanceof DataNode.ListNode list) {
            List<QualityInput.NodePort> sources = new ArrayList<>();
            for (int index = 0; index < list.size(); index++) {
                QualityInput decoded = decodeInput(list.get(index), DataDecodeException.index(path, index));
                if (decoded instanceof QualityInput.NodePort port) {
                    sources.add(port);
                } else if (decoded instanceof QualityInput.Sum sum) {
                    sources.addAll(sum.sources());
                }
            }
            return new QualityInput.Sum(sources);
        }
        DataNode.ObjectNode object = node.asObject(path);
        return new QualityInput.NodePort(
                object.require("node", path).asString(DataDecodeException.child(path, "node")),
                object.get("port").map(value -> value.asString(DataDecodeException.child(path, "port"))).orElse("value")
        );
    }

    private static DataNode encodeInput(QualityInput input) {
        if (input instanceof QualityInput.NodePort port) {
            if ("value".equals(port.port())) {
                return DataNode.string(port.nodeId());
            }
            return DataNode.objectBuilder()
                    .put("node", DataNode.string(port.nodeId()))
                    .put("port", DataNode.string(port.port()))
                    .build();
        }
        if (input instanceof QualityInput.Sum sum) {
            List<DataNode> sources = new ArrayList<>();
            sum.sources().forEach(port -> sources.add(encodeInput(port)));
            return DataNode.list(sources);
        }
        return DataNode.object(Map.of());
    }

    private static OutputReference decodeOutput(DataNode node, String path) {
        DataNode.ObjectNode object = node.asObject(path);
        return new OutputReference(
                object.require("node", path).asString(DataDecodeException.child(path, "node")),
                object.require("port", path).asString(DataDecodeException.child(path, "port"))
        );
    }

    public static Optional<ResourceId> declaredId(DataNode node, String path) {
        return node.asObject(path).get("id")
                .map(value -> DataCodecs.RESOURCE_ID.decode(value, DataDecodeException.child(path, "id")));
    }
}

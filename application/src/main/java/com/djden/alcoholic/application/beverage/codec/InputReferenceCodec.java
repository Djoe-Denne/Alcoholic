package com.djden.alcoholic.application.beverage.codec;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.domain.beverage.InputReference;

import java.util.LinkedHashMap;
import java.util.Map;

public final class InputReferenceCodec implements DataCodec<InputReference> {
    public static final InputReferenceCodec INSTANCE = new InputReferenceCodec();

    private InputReferenceCodec() {
    }

    @Override
    public InputReference decode(DataNode node, String path) {
        DataNode.ObjectNode object = node.asObject(path);
        int kinds = count(object, "item", "tag", "ingredient", "beverage", "node");
        if (kinds != 1) {
            throw new DataDecodeException(
                    path,
                    "input must declare exactly one of item, tag, ingredient, beverage, or node"
            );
        }
        if (object.has("item")) {
            return new InputReference.ItemInput(id(object, "item", path));
        }
        if (object.has("tag")) {
            return new InputReference.TagInput(id(object, "tag", path));
        }
        if (object.has("ingredient")) {
            return new InputReference.IngredientInput(id(object, "ingredient", path));
        }
        if (object.has("beverage")) {
            return new InputReference.BeverageInput(id(object, "beverage", path));
        }
        if (!object.has("port")) {
            throw new DataDecodeException(DataDecodeException.child(path, "port"), "missing field");
        }
        return new InputReference.NodeOutputInput(
                object.require("node", path).asString(DataDecodeException.child(path, "node")),
                object.require("port", path).asString(DataDecodeException.child(path, "port"))
        );
    }

    @Override
    public DataNode encode(InputReference value) {
        if (value instanceof InputReference.ItemInput input) {
            return field("item", input.item());
        }
        if (value instanceof InputReference.TagInput input) {
            return field("tag", input.tag());
        }
        if (value instanceof InputReference.IngredientInput input) {
            return field("ingredient", input.ingredient());
        }
        if (value instanceof InputReference.BeverageInput input) {
            return field("beverage", input.beverage());
        }
        InputReference.NodeOutputInput input = (InputReference.NodeOutputInput) value;
        return DataNode.objectBuilder()
                .put("node", DataNode.string(input.nodeId()))
                .put("port", DataNode.string(input.port()))
                .build();
    }

    public Map<String, InputReference> decodeMap(DataNode node, String path) {
        if (node == null || node.isNull()) {
            return Map.of();
        }
        Map<String, InputReference> inputs = new LinkedHashMap<>();
        node.asObject(path).fields().forEach((name, child) ->
                inputs.put(name, decode(child, DataDecodeException.child(path, name)))
        );
        return Map.copyOf(inputs);
    }

    private static int count(DataNode.ObjectNode object, String... keys) {
        int total = 0;
        for (String key : keys) {
            if (object.has(key)) {
                total++;
            }
        }
        return total;
    }

    private static ResourceId id(DataNode.ObjectNode object, String field, String path) {
        return DataCodecs.RESOURCE_ID.decode(
                object.require(field, path),
                DataDecodeException.child(path, field)
        );
    }

    private static DataNode field(String name, ResourceId id) {
        return DataNode.objectBuilder().put(name, DataNode.string(id.toString())).build();
    }
}

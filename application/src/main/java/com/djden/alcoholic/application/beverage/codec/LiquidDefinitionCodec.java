package com.djden.alcoholic.application.beverage.codec;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.domain.liquid.LiquidDefinition;

import java.util.LinkedHashMap;
import java.util.Map;

public final class LiquidDefinitionCodec implements DataCodec<LiquidDefinition> {
    public static final LiquidDefinitionCodec INSTANCE = new LiquidDefinitionCodec();

    private LiquidDefinitionCodec() {
    }

    public LiquidDefinition decode(DataNode node, String path, ResourceId fallbackId) {
        DataNode.ObjectNode object = node.asObject(path);
        ResourceId id = object.get("id")
                .map(value -> DataCodecs.RESOURCE_ID.decode(value, DataDecodeException.child(path, "id")))
                .orElse(fallbackId);
        Map<ResourceId, Object> defaults = new LinkedHashMap<>();
        object.get("defaults").ifPresent(value -> {
            DataNode.ObjectNode fields = value.asObject(DataDecodeException.child(path, "defaults"));
            fields.fields().forEach((name, child) -> defaults.put(
                    ResourceId.parse(name),
                    decodeValue(child, DataDecodeException.child(path, "defaults/" + name))
            ));
        });
        return new LiquidDefinition(id, defaults);
    }

    @Override
    public LiquidDefinition decode(DataNode node, String path) {
        return decode(node, path, null);
    }

    @Override
    public DataNode encode(LiquidDefinition value) {
        DataNode.ObjectBuilder defaults = DataNode.objectBuilder();
        value.defaults().forEach((id, property) -> defaults.put(id.toString(), encodeValue(property)));
        return DataNode.objectBuilder()
                .put("id", DataNode.string(value.id().toString()))
                .put("defaults", defaults.build())
                .build();
    }

    private static Object decodeValue(DataNode node, String path) {
        if (node instanceof DataNode.NumberNode number) {
            return number.value().doubleValue();
        }
        if (node instanceof DataNode.StringNode string) {
            return string.value();
        }
        if (node instanceof DataNode.BoolNode bool) {
            return bool.value();
        }
        throw new DataDecodeException(path, "unsupported default property value");
    }

    private static DataNode encodeValue(Object value) {
        if (value instanceof Number number) {
            return DataNode.number(number);
        }
        if (value instanceof Boolean bool) {
            return DataNode.bool(bool);
        }
        return DataNode.string(String.valueOf(value));
    }
}

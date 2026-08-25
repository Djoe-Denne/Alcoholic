package com.djden.alcoholic.application.beverage.codec;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.domain.ingredient.IngredientDefinition;

import java.util.LinkedHashSet;
import java.util.List;

public final class IngredientDefinitionCodec implements DataCodec<IngredientDefinition> {
    public static final IngredientDefinitionCodec INSTANCE = new IngredientDefinitionCodec();

    private IngredientDefinitionCodec() {
    }

    public IngredientDefinition decode(DataNode node, String path, ResourceId fallbackId) {
        DataNode.ObjectNode object = node.asObject(path);
        ResourceId id = object.get("id")
                .map(value -> DataCodecs.RESOURCE_ID.decode(value, DataDecodeException.child(path, "id")))
                .orElse(fallbackId);
        List<ResourceId> tags = object.get("tags")
                .map(value -> DataCodecs.RESOURCE_ID.listOf().decode(
                        value,
                        DataDecodeException.child(path, "tags")
                ))
                .orElse(List.of());
        return new IngredientDefinition(id, new LinkedHashSet<>(tags));
    }

    @Override
    public IngredientDefinition decode(DataNode node, String path) {
        return decode(node, path, null);
    }

    @Override
    public DataNode encode(IngredientDefinition value) {
        return DataNode.objectBuilder()
                .put("id", DataNode.string(value.id().toString()))
                .put("tags", DataCodecs.RESOURCE_ID.listOf().encode(List.copyOf(value.tags())))
                .build();
    }
}

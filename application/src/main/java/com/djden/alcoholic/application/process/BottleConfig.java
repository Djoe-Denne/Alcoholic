package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.ingredient.IngredientSelector;
import com.djden.alcoholic.api.process.ProcessDisplaySpec;
import com.djden.alcoholic.api.process.ProcessDisplaying;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;

public record BottleConfig(int volumeMillibuckets, ResourceId bottleItem) implements ProcessDisplaying {
    public BottleConfig {
        if (volumeMillibuckets < 1) {
            volumeMillibuckets = 250;
        }
        bottleItem = bottleItem == null ? ResourceId.parse("alcoholic:beverage_bottle") : bottleItem;
    }

    @Override
    public ProcessDisplaySpec display() {
        return ProcessDisplaySpec.builder()
                .itemIn(
                        new IngredientSelector.Item(ResourceId.parse("alcoholic:empty_bottle")),
                        1,
                        volumeMillibuckets + " mB"
                )
                .itemOut(bottleItem, 1)
                .build();
    }

    public static final DataCodec<BottleConfig> CODEC = new DataCodec<>() {
        @Override
        public BottleConfig decode(DataNode node, String path) {
            if (node == null || node.isNull()) {
                return incomplete();
            }
            DataNode.ObjectNode object = node.asObject(path);
            int volume = object.get("volume")
                    .map(value -> value.asNumber(DataDecodeException.child(path, "volume")).intValue())
                    .orElse(250);
            ResourceId item = object.get("item")
                    .map(value -> DataCodecs.RESOURCE_ID.decode(value, DataDecodeException.child(path, "item")))
                    .orElse(ResourceId.parse("alcoholic:beverage_bottle"));
            return new BottleConfig(volume, item);
        }

        @Override
        public DataNode encode(BottleConfig value) {
            return DataNode.objectBuilder()
                    .put("volume", DataNode.number(value.volumeMillibuckets()))
                    .put("item", DataNode.string(value.bottleItem().toString()))
                    .build();
        }
    };

    public static BottleConfig incomplete() {
        return new BottleConfig(250, ResourceId.parse("alcoholic:beverage_bottle"));
    }
}

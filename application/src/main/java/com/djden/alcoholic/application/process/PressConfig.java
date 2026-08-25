package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.ingredient.IngredientSelector;
import com.djden.alcoholic.api.process.ItemOutput;

import java.util.Optional;

public record PressConfig(
        Optional<IngredientSelector> inputSelector,
        int inputAmount,
        Optional<ResourceId> outputLiquid,
        double outputVolume,
        Optional<ItemOutput> byproduct,
        int processingTicks,
        double yield,
        boolean createCompatible
) implements ReferencedLiquids {
    public PressConfig {
        inputSelector = inputSelector == null ? Optional.empty() : inputSelector;
        if (inputAmount < 1) {
            inputAmount = 1;
        }
        outputLiquid = outputLiquid == null ? Optional.empty() : outputLiquid;
        if (outputVolume < 0.0) {
            outputVolume = 0.0;
        }
        byproduct = byproduct == null ? Optional.empty() : byproduct;
        if (processingTicks < 1) {
            processingTicks = 1;
        }
        if (!Double.isFinite(yield) || yield < 0.0) {
            yield = 1.0;
        }
    }

    public static final DataCodec<PressConfig> CODEC = new DataCodec<>() {
        @Override
        public PressConfig decode(DataNode node, String path) {
            if (node == null || node.isNull()) {
                return incomplete();
            }
            DataNode.ObjectNode object = node.asObject(path);
            Optional<IngredientSelector> selector = Optional.empty();
            int amount = 1;
            if (object.has("input")) {
                DataNode.ObjectNode input = object.require("input", path)
                        .asObject(DataDecodeException.child(path, "input"));
                selector = Optional.of(selector(input, DataDecodeException.child(path, "input")));
                amount = input.get("amount")
                        .map(value -> value.asNumber(DataDecodeException.child(path, "input/amount")).intValue())
                        .orElse(1);
            }
            Optional<ResourceId> liquid = Optional.empty();
            double volume = 0.0;
            if (object.has("output")) {
                DataNode.ObjectNode output = object.require("output", path)
                        .asObject(DataDecodeException.child(path, "output"));
                if (output.has("liquid")) {
                    liquid = Optional.of(DataCodecs.RESOURCE_ID.decode(
                            output.require("liquid", DataDecodeException.child(path, "output")),
                            DataDecodeException.child(path, "output/liquid")
                    ));
                }
                volume = output.get("volume")
                        .map(value -> value.asNumber(DataDecodeException.child(path, "output/volume")).doubleValue())
                        .orElse(0.0);
            }
            Optional<ItemOutput> byproduct = Optional.empty();
            if (object.has("byproduct")) {
                DataNode.ObjectNode byproductNode = object.require("byproduct", path)
                        .asObject(DataDecodeException.child(path, "byproduct"));
                byproduct = Optional.of(new ItemOutput(
                        DataCodecs.RESOURCE_ID.decode(
                                byproductNode.require("item", DataDecodeException.child(path, "byproduct")),
                                DataDecodeException.child(path, "byproduct/item")
                        ),
                        byproductNode.get("amount")
                                .map(value -> value.asNumber(
                                        DataDecodeException.child(path, "byproduct/amount")
                                ).intValue())
                                .orElse(1)
                ));
            }
            int ticks = object.get("processing_time")
                    .or(() -> object.get("processingTime"))
                    .map(value -> value.asNumber(DataDecodeException.child(path, "processing_time")).intValue())
                    .orElse(200);
            double yield = object.get("yield")
                    .map(value -> {
                        DataNode yieldNode = value;
                        if (yieldNode instanceof DataNode.ObjectNode yieldObject) {
                            return yieldObject.get("base")
                                    .orElse(yieldNode)
                                    .asNumber(DataDecodeException.child(path, "yield")).doubleValue();
                        }
                        return yieldNode.asNumber(DataDecodeException.child(path, "yield")).doubleValue();
                    })
                    .orElse(1.0);
            boolean createCompatible = object.get("create_compatible")
                    .or(() -> object.get("createCompatible"))
                    .map(value -> value.asBoolean(DataDecodeException.child(path, "create_compatible")))
                    .orElse(false);
            return new PressConfig(selector, amount, liquid, volume, byproduct, ticks, yield, createCompatible);
        }

        @Override
        public DataNode encode(PressConfig value) {
            DataNode.ObjectBuilder builder = DataNode.objectBuilder();
            value.inputSelector().ifPresent(selector -> {
                DataNode.ObjectBuilder input = DataNode.objectBuilder();
                encodeSelector(input, selector);
                input.put("amount", DataNode.number(value.inputAmount()));
                builder.put("input", input.build());
            });
            value.outputLiquid().ifPresent(liquid -> builder.put(
                    "output",
                    DataNode.objectBuilder()
                            .put("liquid", DataNode.string(liquid.toString()))
                            .put("volume", DataNode.number(value.outputVolume()))
                            .build()
            ));
            value.byproduct().ifPresent(item -> builder.put(
                    "byproduct",
                    DataNode.objectBuilder()
                            .put("item", DataNode.string(item.item().toString()))
                            .put("amount", DataNode.number(item.amount()))
                            .build()
            ));
            builder.put("processing_time", DataNode.number(value.processingTicks()));
            builder.put("yield", DataNode.number(value.yield()));
            builder.put("create_compatible", DataNode.bool(value.createCompatible()));
            return builder.build();
        }
    };

    public static PressConfig incomplete() {
        return new PressConfig(Optional.empty(), 1, Optional.empty(), 0.0, Optional.empty(), 200, 1.0, false);
    }

    public boolean executable() {
        return outputLiquid.isPresent() && outputVolume > 0.0;
    }

    @Override
    public java.util.Collection<ResourceId> liquidIds() {
        return outputLiquid.stream().toList();
    }

    static IngredientSelector selector(DataNode.ObjectNode input, String path) {
        if (input.has("tag")) {
            return new IngredientSelector.Tag(id(input, "tag", path));
        }
        if (input.has("item")) {
            return new IngredientSelector.Item(id(input, "item", path));
        }
        if (input.has("ingredient")) {
            String raw = input.require("ingredient", path)
                    .asString(DataDecodeException.child(path, "ingredient"));
            if (raw.startsWith("#")) {
                return new IngredientSelector.Tag(ResourceId.parse(raw.substring(1)));
            }
            return new IngredientSelector.DefinedIngredient(ResourceId.parse(raw));
        }
        throw new DataDecodeException(path, "input must declare tag, item, or ingredient");
    }

    private static ResourceId id(DataNode.ObjectNode object, String field, String path) {
        String raw = object.require(field, path).asString(DataDecodeException.child(path, field));
        return raw.startsWith("#") ? ResourceId.parse(raw.substring(1)) : ResourceId.parse(raw);
    }

    private static void encodeSelector(DataNode.ObjectBuilder builder, IngredientSelector selector) {
        if (selector instanceof IngredientSelector.Tag tag) {
            builder.put("tag", DataNode.string(tag.id().toString()));
            return;
        }
        if (selector instanceof IngredientSelector.Item item) {
            builder.put("item", DataNode.string(item.id().toString()));
            return;
        }
        if (selector instanceof IngredientSelector.DefinedIngredient ingredient) {
            builder.put("ingredient", DataNode.string(ingredient.id().toString()));
        }
    }
}

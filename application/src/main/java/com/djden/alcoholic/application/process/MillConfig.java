package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.ingredient.IngredientSelector;
import com.djden.alcoholic.api.process.ProcessDisplaySpec;
import com.djden.alcoholic.api.process.ProcessDisplaying;
import com.djden.alcoholic.api.process.SolidAccepting;

import java.util.Optional;

public record MillConfig(
        Optional<IngredientSelector> inputSelector,
        int inputAmount,
        Optional<ResourceId> outputItem,
        int outputAmount,
        int processingTicks,
        boolean createCompatible
) implements SolidAccepting, ProcessDisplaying {
    public MillConfig {
        inputSelector = inputSelector == null ? Optional.empty() : inputSelector;
        if (inputAmount < 1) {
            inputAmount = 1;
        }
        outputItem = outputItem == null ? Optional.empty() : outputItem;
        if (outputAmount < 1) {
            outputAmount = 1;
        }
        if (processingTicks < 1) {
            processingTicks = 1;
        }
    }

    @Override
    public ProcessDisplaySpec display() {
        ProcessDisplaySpec.Builder builder = ProcessDisplaySpec.builder();
        inputSelector.ifPresent(selector -> builder.itemIn(selector, inputAmount));
        outputItem.ifPresent(item -> builder.itemOut(item, outputAmount));
        return builder.duration(processingTicks).build();
    }

    public static final DataCodec<MillConfig> CODEC = new DataCodec<>() {
        @Override
        public MillConfig decode(DataNode node, String path) {
            if (node == null || node.isNull()) {
                return incomplete();
            }
            DataNode.ObjectNode object = node.asObject(path);
            Optional<IngredientSelector> selector = Optional.empty();
            int amount = 1;
            if (object.has("input")) {
                DataNode.ObjectNode input = object.require("input", path)
                        .asObject(DataDecodeException.child(path, "input"));
                selector = Optional.of(PressConfig.selector(input, DataDecodeException.child(path, "input")));
                amount = input.get("amount")
                        .map(value -> value.asNumber(DataDecodeException.child(path, "input/amount")).intValue())
                        .orElse(1);
            }
            Optional<ResourceId> output = Optional.empty();
            int outputAmount = 1;
            if (object.has("output")) {
                DataNode.ObjectNode outputNode = object.require("output", path)
                        .asObject(DataDecodeException.child(path, "output"));
                if (outputNode.has("item")) {
                    output = Optional.of(DataCodecs.RESOURCE_ID.decode(
                            outputNode.require("item", DataDecodeException.child(path, "output")),
                            DataDecodeException.child(path, "output/item")
                    ));
                }
                outputAmount = outputNode.get("amount")
                        .map(value -> value.asNumber(
                                DataDecodeException.child(path, "output/amount")
                        ).intValue())
                        .orElse(1);
            }
            int ticks = object.get("processing_time")
                    .map(value -> value.asNumber(DataDecodeException.child(path, "processing_time")).intValue())
                    .orElse(100);
            boolean createCompatible = object.get("create_compatible")
                    .or(() -> object.get("createCompatible"))
                    .map(value -> value.asBoolean(DataDecodeException.child(path, "create_compatible")))
                    .orElse(false);
            return new MillConfig(selector, amount, output, outputAmount, ticks, createCompatible);
        }

        @Override
        public DataNode encode(MillConfig value) {
            DataNode.ObjectBuilder builder = DataNode.objectBuilder();
            value.inputSelector().ifPresent(selector -> {
                DataNode.ObjectBuilder input = DataNode.objectBuilder();
                PressConfig.encodeSelector(input, selector);
                input.put("amount", DataNode.number(value.inputAmount()));
                builder.put("input", input.build());
            });
            value.outputItem().ifPresent(item -> builder.put(
                    "output",
                    DataNode.objectBuilder()
                            .put("item", DataNode.string(item.toString()))
                            .put("amount", DataNode.number(value.outputAmount()))
                            .build()
            ));
            builder.put("processing_time", DataNode.number(value.processingTicks()));
            builder.put("create_compatible", DataNode.bool(value.createCompatible()));
            return builder.build();
        }
    };

    public static MillConfig incomplete() {
        return new MillConfig(Optional.empty(), 1, Optional.empty(), 1, 100, false);
    }

    public boolean executable() {
        return inputSelector.isPresent() && outputItem.isPresent();
    }

    @Override
    public Optional<IngredientSelector> inputSelector() {
        return inputSelector;
    }
}

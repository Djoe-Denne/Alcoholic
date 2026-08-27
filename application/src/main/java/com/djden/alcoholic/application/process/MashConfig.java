package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.ingredient.IngredientSelector;
import com.djden.alcoholic.api.process.ItemOutput;
import com.djden.alcoholic.api.process.LiquidAccepting;
import com.djden.alcoholic.api.process.ProcessDisplaySpec;
import com.djden.alcoholic.api.process.ProcessDisplaying;
import com.djden.alcoholic.api.process.SolidAccepting;
import com.djden.alcoholic.domain.process.TemperatureProfile;

import java.util.List;
import java.util.Optional;

public record MashConfig(
        Optional<IngredientSelector> inputSelector,
        int inputAmount,
        Optional<ResourceId> inputLiquid,
        double inputLiquidVolume,
        Optional<ResourceId> outputLiquid,
        double outputVolume,
        Optional<ItemOutput> byproduct,
        int processingTicks,
        TemperatureProfile temperature,
        ResourceId sugarProperty,
        ResourceId colorProperty,
        ResourceId temperatureProperty
) implements SolidAccepting, LiquidAccepting, ReferencedLiquids, ProcessDisplaying {
    public MashConfig {
        inputSelector = inputSelector == null ? Optional.empty() : inputSelector;
        if (inputAmount < 1) {
            inputAmount = 1;
        }
        inputLiquid = inputLiquid == null ? Optional.empty() : inputLiquid;
        if (inputLiquidVolume < 0.0) {
            inputLiquidVolume = 0.0;
        }
        outputLiquid = outputLiquid == null ? Optional.empty() : outputLiquid;
        if (outputVolume < 0.0) {
            outputVolume = 0.0;
        }
        byproduct = byproduct == null ? Optional.empty() : byproduct;
        if (processingTicks < 1) {
            processingTicks = 1;
        }
        temperature = temperature == null ? TemperatureProfiles.mashDefault() : temperature;
        sugarProperty = sugarProperty == null ? ResourceId.parse("alcoholic:sugar") : sugarProperty;
        colorProperty = colorProperty == null ? ResourceId.parse("alcoholic:color") : colorProperty;
        temperatureProperty = temperatureProperty == null
                ? ResourceId.parse("alcoholic:temperature")
                : temperatureProperty;
    }

    @Override
    public ProcessDisplaySpec display() {
        ProcessDisplaySpec.Builder builder = ProcessDisplaySpec.builder();
        inputSelector.ifPresent(selector -> builder.itemIn(selector, inputAmount));
        inputLiquid.ifPresent(fluid -> builder.fluidIn(fluid, ProcessDisplaySpec.millibuckets(inputLiquidVolume)));
        byproduct.filter(item -> item.amount() >= 1)
                .ifPresent(item -> builder.itemOut(item.item(), item.amount()));
        outputLiquid.ifPresent(fluid -> builder.fluidOut(fluid, ProcessDisplaySpec.millibuckets(outputVolume)));
        return ProcessDisplays.preferred(builder.duration(processingTicks), temperature).build();
    }

    public static final DataCodec<MashConfig> CODEC = new DataCodec<>() {
        @Override
        public MashConfig decode(DataNode node, String path) {
            if (node == null || node.isNull()) {
                return incomplete();
            }
            DataNode.ObjectNode object = node.asObject(path);
            Optional<IngredientSelector> selector = Optional.empty();
            int amount = 1;
            DataNode.ObjectNode solid = object.get("solid")
                    .or(() -> object.get("input"))
                    .map(value -> value.asObject(DataDecodeException.child(path, "solid")))
                    .orElse(null);
            if (solid != null) {
                selector = Optional.of(PressConfig.selector(solid, DataDecodeException.child(path, "solid")));
                amount = solid.get("amount")
                        .map(value -> value.asNumber(DataDecodeException.child(path, "solid/amount")).intValue())
                        .orElse(1);
            }
            Optional<ResourceId> liquid = Optional.empty();
            double liquidVolume = 1000.0;
            if (object.has("liquid")) {
                DataNode.ObjectNode liquidNode = object.require("liquid", path)
                        .asObject(DataDecodeException.child(path, "liquid"));
                if (liquidNode.has("fluid")) {
                    liquid = Optional.of(DataCodecs.RESOURCE_ID.decode(
                            liquidNode.require("fluid", DataDecodeException.child(path, "liquid")),
                            DataDecodeException.child(path, "liquid/fluid")
                    ));
                } else if (liquidNode.has("liquid")) {
                    liquid = Optional.of(DataCodecs.RESOURCE_ID.decode(
                            liquidNode.require("liquid", DataDecodeException.child(path, "liquid")),
                            DataDecodeException.child(path, "liquid/liquid")
                    ));
                }
                liquidVolume = liquidNode.get("volume")
                        .map(value -> value.asNumber(
                                DataDecodeException.child(path, "liquid/volume")
                        ).doubleValue())
                        .orElse(1000.0);
            }
            Optional<ResourceId> output = Optional.empty();
            double volume = 0.0;
            if (object.has("output")) {
                DataNode.ObjectNode outputNode = object.require("output", path)
                        .asObject(DataDecodeException.child(path, "output"));
                if (outputNode.has("liquid")) {
                    output = Optional.of(DataCodecs.RESOURCE_ID.decode(
                            outputNode.require("liquid", DataDecodeException.child(path, "output")),
                            DataDecodeException.child(path, "output/liquid")
                    ));
                }
                volume = outputNode.get("volume")
                        .map(value -> value.asNumber(
                                DataDecodeException.child(path, "output/volume")
                        ).doubleValue())
                        .orElse(liquidVolume);
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
                    .map(value -> value.asNumber(DataDecodeException.child(path, "processing_time")).intValue())
                    .orElse(200);
            ResourceId sugar = object.get("sugar_property")
                    .map(value -> DataCodecs.RESOURCE_ID.decode(
                            value,
                            DataDecodeException.child(path, "sugar_property")
                    ))
                    .orElse(ResourceId.parse("alcoholic:sugar"));
            ResourceId color = object.get("color_property")
                    .map(value -> DataCodecs.RESOURCE_ID.decode(
                            value,
                            DataDecodeException.child(path, "color_property")
                    ))
                    .orElse(ResourceId.parse("alcoholic:color"));
            return new MashConfig(
                    selector,
                    amount,
                    liquid,
                    liquidVolume,
                    output,
                    volume,
                    byproduct,
                    ticks,
                    TemperatureProfiles.decode(object, path, TemperatureProfiles.mashDefault()),
                    sugar,
                    color,
                    ResourceId.parse("alcoholic:temperature")
            );
        }

        @Override
        public DataNode encode(MashConfig value) {
            DataNode.ObjectBuilder builder = DataNode.objectBuilder();
            value.inputSelector().ifPresent(selector -> {
                DataNode.ObjectBuilder solid = DataNode.objectBuilder();
                PressConfig.encodeSelector(solid, selector);
                solid.put("amount", DataNode.number(value.inputAmount()));
                builder.put("solid", solid.build());
            });
            if (value.inputLiquid().isPresent() || value.inputLiquidVolume() > 0.0) {
                DataNode.ObjectBuilder liquid = DataNode.objectBuilder();
                value.inputLiquid().ifPresent(id -> liquid.put("fluid", DataNode.string(id.toString())));
                liquid.put("volume", DataNode.number(value.inputLiquidVolume()));
                builder.put("liquid", liquid.build());
            }
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
            builder.put("sugar_property", DataNode.string(value.sugarProperty().toString()));
            builder.put("color_property", DataNode.string(value.colorProperty().toString()));
            TemperatureProfiles.encode(builder, value.temperature());
            return builder.build();
        }
    };

    public static MashConfig incomplete() {
        return new MashConfig(
                Optional.empty(),
                1,
                Optional.empty(),
                0.0,
                Optional.empty(),
                0.0,
                Optional.empty(),
                200,
                TemperatureProfiles.mashDefault(),
                ResourceId.parse("alcoholic:sugar"),
                ResourceId.parse("alcoholic:color"),
                ResourceId.parse("alcoholic:temperature")
        );
    }

    public boolean executable() {
        return outputLiquid.isPresent() && outputVolume > 0.0 && inputLiquidVolume > 0.0;
    }

    @Override
    public Optional<IngredientSelector> inputSelector() {
        return inputSelector;
    }

    @Override
    public boolean acceptsLiquid(ResourceId liquid) {
        return inputLiquid.isPresent() && inputLiquid.filter(liquid::equals).isPresent();
    }

    @Override
    public java.util.Collection<ResourceId> liquidIds() {
        return outputLiquid.stream().toList();
    }
}

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
import com.djden.alcoholic.domain.process.KilnProfile;
import com.djden.alcoholic.domain.process.TemperatureProfile;

import java.util.Optional;

public record MaltConfig(
        Optional<IngredientSelector> inputSelector,
        int inputAmount,
        Optional<ResourceId> outputItem,
        int outputAmount,
        int processingTicks,
        double moistureRequirement,
        TemperatureProfile temperature,
        KilnProfile kiln
) implements SolidAccepting, ProcessDisplaying {
    public MaltConfig {
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
        if (!Double.isFinite(moistureRequirement) || moistureRequirement < 0.0) {
            moistureRequirement = 0.0;
        }
        temperature = temperature == null ? TemperatureProfiles.maltDefault() : temperature;
        kiln = kiln == null ? KilnProfile.pale() : kiln;
    }

    @Override
    public ProcessDisplaySpec display() {
        ProcessDisplaySpec.Builder builder = ProcessDisplaySpec.builder();
        inputSelector.ifPresent(selector -> builder.itemIn(selector, inputAmount));
        outputItem.ifPresent(item -> builder.itemOut(item, outputAmount));
        return ProcessDisplays.preferred(builder.duration(processingTicks), temperature).build();
    }

    public static final DataCodec<MaltConfig> CODEC = new DataCodec<>() {
        @Override
        public MaltConfig decode(DataNode node, String path) {
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
                    .or(() -> object.get("duration"))
                    .map(value -> value.asNumber(DataDecodeException.child(path, "processing_time")).intValue())
                    .orElse(200);
            double moisture = object.get("moisture_requirement")
                    .or(() -> object.get("moistureRequirement"))
                    .map(value -> value.asNumber(
                            DataDecodeException.child(path, "moisture_requirement")
                    ).doubleValue())
                    .orElse(0.0);
            return new MaltConfig(
                    selector,
                    amount,
                    output,
                    outputAmount,
                    ticks,
                    moisture,
                    TemperatureProfiles.decode(object, path, TemperatureProfiles.maltDefault()),
                    kiln(object, path)
            );
        }

        @Override
        public DataNode encode(MaltConfig value) {
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
            builder.put("moisture_requirement", DataNode.number(value.moistureRequirement()));
            TemperatureProfiles.encode(builder, value.temperature());
            builder.put(
                    "kiln_profile",
                    DataNode.objectBuilder()
                            .put("id", DataNode.string(value.kiln().id().toString()))
                            .put("color_potential", DataNode.number(value.kiln().colorPotential()))
                            .put("fermentable_potential", DataNode.number(value.kiln().fermentablePotential()))
                            .put("roast_intensity", DataNode.number(value.kiln().roastIntensity()))
                            .build()
            );
            return builder.build();
        }
    };

    public static MaltConfig incomplete() {
        return new MaltConfig(
                Optional.empty(),
                1,
                Optional.empty(),
                1,
                200,
                0.0,
                TemperatureProfiles.maltDefault(),
                KilnProfile.pale()
        );
    }

    public boolean executable() {
        return inputSelector.isPresent() && outputItem.isPresent();
    }

    @Override
    public Optional<IngredientSelector> inputSelector() {
        return inputSelector;
    }

    private static KilnProfile kiln(DataNode.ObjectNode object, String path) {
        DataNode.ObjectNode profile = object.get("kiln_profile")
                .or(() -> object.get("kilnProfile"))
                .map(node -> node.asObject(DataDecodeException.child(path, "kiln_profile")))
                .orElse(null);
        if (profile == null) {
            return KilnProfile.pale();
        }
        ResourceId id = profile.get("id")
                .map(value -> DataCodecs.RESOURCE_ID.decode(value, DataDecodeException.child(path, "kiln_profile/id")))
                .orElse(ResourceId.parse("alcoholic:pale"));
        return new KilnProfile(
                id,
                profile.get("color_potential")
                        .map(value -> value.asNumber(
                                DataDecodeException.child(path, "kiln_profile/color_potential")
                        ).doubleValue())
                        .orElse(0.12),
                profile.get("fermentable_potential")
                        .map(value -> value.asNumber(
                                DataDecodeException.child(path, "kiln_profile/fermentable_potential")
                        ).doubleValue())
                        .orElse(0.85),
                profile.get("roast_intensity")
                        .map(value -> value.asNumber(
                                DataDecodeException.child(path, "kiln_profile/roast_intensity")
                        ).doubleValue())
                        .orElse(0.15)
        );
    }
}

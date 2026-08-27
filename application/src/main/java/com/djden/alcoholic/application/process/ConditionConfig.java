package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.process.LiquidAccepting;
import com.djden.alcoholic.api.process.ProcessDisplaySpec;
import com.djden.alcoholic.api.process.ProcessDisplaying;
import com.djden.alcoholic.domain.process.ConditionKinetics;
import com.djden.alcoholic.domain.process.TemperatureProfile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public record ConditionConfig(
        Optional<ResourceId> inputLiquid,
        Optional<ResourceId> outputLiquid,
        int processingTicks,
        TemperatureProfile temperature,
        ConditionKinetics kinetics,
        ResourceId maturityProperty,
        ResourceId sugarProperty,
        ResourceId carbonationProperty
) implements LiquidAccepting, ReferencedLiquids, ProcessDisplaying {
    public ConditionConfig {
        inputLiquid = inputLiquid == null ? Optional.empty() : inputLiquid;
        outputLiquid = outputLiquid == null ? Optional.empty() : outputLiquid;
        if (processingTicks < 1) {
            processingTicks = 1;
        }
        temperature = temperature == null ? TemperatureProfiles.conditionDefault() : temperature;
        kinetics = kinetics == null ? ConditionKinetics.simplified() : kinetics;
        maturityProperty = maturityProperty == null ? ResourceId.parse("alcoholic:maturity") : maturityProperty;
        sugarProperty = sugarProperty == null ? ResourceId.parse("alcoholic:sugar") : sugarProperty;
        carbonationProperty = carbonationProperty == null
                ? ResourceId.parse("alcoholic:carbonation")
                : carbonationProperty;
    }

    @Override
    public ProcessDisplaySpec display() {
        ProcessDisplaySpec.Builder builder = ProcessDisplaySpec.builder();
        inputLiquid.ifPresent(fluid -> builder.fluidIn(fluid, java.util.OptionalInt.empty()));
        outputLiquid.ifPresent(fluid -> builder.fluidOut(fluid, java.util.OptionalInt.empty()));
        return ProcessDisplays.preferred(builder.duration(processingTicks), temperature).build();
    }

    public static final DataCodec<ConditionConfig> CODEC = new DataCodec<>() {
        @Override
        public ConditionConfig decode(DataNode node, String path) {
            if (node == null || node.isNull()) {
                return incomplete();
            }
            DataNode.ObjectNode object = node.asObject(path);
            Optional<ResourceId> input = object.get("input_liquid")
                    .or(() -> object.get("inputLiquid"))
                    .map(value -> DataCodecs.RESOURCE_ID.decode(value, DataDecodeException.child(path, "input_liquid")));
            Optional<ResourceId> output = Optional.empty();
            if (object.has("output")) {
                DataNode.ObjectNode outputNode = object.require("output", path)
                        .asObject(DataDecodeException.child(path, "output"));
                output = outputNode.get("liquid")
                        .map(value -> DataCodecs.RESOURCE_ID.decode(
                                value,
                                DataDecodeException.child(path, "output/liquid")
                        ));
            } else {
                output = object.get("output_liquid")
                        .or(() -> object.get("outputLiquid"))
                        .map(value -> DataCodecs.RESOURCE_ID.decode(
                                value,
                                DataDecodeException.child(path, "output_liquid")
                        ));
            }
            int ticks = object.get("processing_time")
                    .or(() -> object.get("ticks_to_complete"))
                    .map(value -> value.asNumber(DataDecodeException.child(path, "processing_time")).intValue())
                    .orElse(200);
            return new ConditionConfig(
                    input,
                    output,
                    ticks,
                    TemperatureProfiles.decode(object, path, TemperatureProfiles.conditionDefault()),
                    kinetics(object, path, ticks),
                    object.get("maturity_property")
                            .map(value -> DataCodecs.RESOURCE_ID.decode(
                                    value,
                                    DataDecodeException.child(path, "maturity_property")
                            ))
                            .orElse(ResourceId.parse("alcoholic:maturity")),
                    object.get("sugar_property")
                            .map(value -> DataCodecs.RESOURCE_ID.decode(
                                    value,
                                    DataDecodeException.child(path, "sugar_property")
                            ))
                            .orElse(ResourceId.parse("alcoholic:sugar")),
                    object.get("carbonation_property")
                            .map(value -> DataCodecs.RESOURCE_ID.decode(
                                    value,
                                    DataDecodeException.child(path, "carbonation_property")
                            ))
                            .orElse(ResourceId.parse("alcoholic:carbonation"))
            );
        }

        @Override
        public DataNode encode(ConditionConfig value) {
            DataNode.ObjectBuilder builder = DataNode.objectBuilder();
            value.inputLiquid().ifPresent(id -> builder.put("input_liquid", DataNode.string(id.toString())));
            value.outputLiquid().ifPresent(id -> builder.put(
                    "output",
                    DataNode.objectBuilder().put("liquid", DataNode.string(id.toString())).build()
            ));
            builder.put("processing_time", DataNode.number(value.processingTicks()));
            builder.put(
                    "kinetics",
                    DataNode.objectBuilder()
                            .put("maturity_rate", DataNode.number(value.kinetics().maturityPerTick()))
                            .put("carbonation_from_sugar", DataNode.number(value.kinetics().carbonationFromSugar()))
                            .put("completion_threshold", DataNode.number(value.kinetics().completionThreshold()))
                            .build()
            );
            builder.put("maturity_property", DataNode.string(value.maturityProperty().toString()));
            builder.put("sugar_property", DataNode.string(value.sugarProperty().toString()));
            builder.put("carbonation_property", DataNode.string(value.carbonationProperty().toString()));
            TemperatureProfiles.encode(builder, value.temperature());
            return builder.build();
        }
    };

    public boolean executable() {
        return inputLiquid.isPresent() && outputLiquid.isPresent();
    }

    @Override
    public boolean acceptsLiquid(ResourceId liquid) {
        return inputLiquid.isPresent() && inputLiquid.filter(liquid::equals).isPresent();
    }

    @Override
    public Collection<ResourceId> liquidIds() {
        ArrayList<ResourceId> ids = new ArrayList<>();
        inputLiquid.ifPresent(ids::add);
        outputLiquid.ifPresent(ids::add);
        return ids;
    }

    public static ConditionConfig incomplete() {
        return new ConditionConfig(
                Optional.empty(),
                Optional.empty(),
                200,
                TemperatureProfiles.conditionDefault(),
                ConditionKinetics.simplified(),
                ResourceId.parse("alcoholic:maturity"),
                ResourceId.parse("alcoholic:sugar"),
                ResourceId.parse("alcoholic:carbonation")
        );
    }

    private static ConditionKinetics kinetics(DataNode.ObjectNode object, String path, int ticks) {
        double maturity = 1.0 / Math.max(1, ticks);
        double carbonation = 0.35;
        double threshold = 0.85;
        DataNode.ObjectNode node = object.get("kinetics")
                .map(value -> value.asObject(DataDecodeException.child(path, "kinetics")))
                .orElse(null);
        if (node != null) {
            maturity = node.get("maturity_rate")
                    .map(value -> value.asNumber(
                            DataDecodeException.child(path, "kinetics/maturity_rate")
                    ).doubleValue())
                    .orElse(maturity);
            carbonation = node.get("carbonation_from_sugar")
                    .map(value -> value.asNumber(
                            DataDecodeException.child(path, "kinetics/carbonation_from_sugar")
                    ).doubleValue())
                    .orElse(carbonation);
            threshold = node.get("completion_threshold")
                    .map(value -> value.asNumber(
                            DataDecodeException.child(path, "kinetics/completion_threshold")
                    ).doubleValue())
                    .orElse(threshold);
        } else {
            carbonation = object.get("carbonation_from_residual_sugar")
                    .map(value -> value.asNumber(
                            DataDecodeException.child(path, "carbonation_from_residual_sugar")
                    ).doubleValue())
                    .orElse(carbonation);
            threshold = object.get("completion_maturity")
                    .map(value -> value.asNumber(
                            DataDecodeException.child(path, "completion_maturity")
                    ).doubleValue())
                    .orElse(threshold);
        }
        return new ConditionKinetics(maturity, carbonation, threshold);
    }
}

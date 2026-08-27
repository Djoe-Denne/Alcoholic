package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.process.LiquidAccepting;
import com.djden.alcoholic.api.process.ProcessDisplaySpec;
import com.djden.alcoholic.api.process.ProcessDisplaying;
import com.djden.alcoholic.domain.process.AgingKinetics;
import com.djden.alcoholic.domain.process.TemperatureBand;
import com.djden.alcoholic.domain.process.TemperatureProfile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public record AgingConfig(
        Optional<ResourceId> inputLiquid,
        Optional<ResourceId> outputLiquid,
        TemperatureProfile temperature,
        AgingKinetics kinetics,
        ResourceId maturityProperty,
        ResourceId woodProperty,
        ResourceId oxidationProperty
) implements LiquidAccepting, ReferencedLiquids, ProcessDisplaying {
    public AgingConfig {
        inputLiquid = inputLiquid == null ? Optional.empty() : inputLiquid;
        outputLiquid = outputLiquid == null ? Optional.empty() : outputLiquid;
        temperature = temperature == null ? TemperatureProfile.fermentationDefault() : temperature;
        kinetics = kinetics == null ? AgingKinetics.simplified() : kinetics;
        maturityProperty = maturityProperty == null ? ResourceId.parse("alcoholic:maturity") : maturityProperty;
        woodProperty = woodProperty == null ? ResourceId.parse("alcoholic:wood_exposure") : woodProperty;
        oxidationProperty = oxidationProperty == null
                ? ResourceId.parse("alcoholic:oxidation_exposure")
                : oxidationProperty;
    }

    @Override
    public ProcessDisplaySpec display() {
        ProcessDisplaySpec.Builder builder = ProcessDisplaySpec.builder();
        inputLiquid.ifPresent(fluid -> builder.fluidIn(fluid, java.util.OptionalInt.empty()));
        outputLiquid.ifPresent(fluid -> builder.fluidOut(fluid, java.util.OptionalInt.empty()));
        return ProcessDisplays.preferred(builder, temperature).build();
    }

    public static final DataCodec<AgingConfig> CODEC = new DataCodec<>() {
        @Override
        public AgingConfig decode(DataNode node, String path) {
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
            return new AgingConfig(
                    input,
                    output,
                    temperature(object, path),
                    kinetics(object, path),
                    object.get("maturity_property")
                            .map(value -> DataCodecs.RESOURCE_ID.decode(
                                    value,
                                    DataDecodeException.child(path, "maturity_property")
                            ))
                            .orElse(ResourceId.parse("alcoholic:maturity")),
                    object.get("wood_property")
                            .map(value -> DataCodecs.RESOURCE_ID.decode(
                                    value,
                                    DataDecodeException.child(path, "wood_property")
                            ))
                            .orElse(ResourceId.parse("alcoholic:wood_exposure")),
                    object.get("oxidation_property")
                            .map(value -> DataCodecs.RESOURCE_ID.decode(
                                    value,
                                    DataDecodeException.child(path, "oxidation_property")
                            ))
                            .orElse(ResourceId.parse("alcoholic:oxidation_exposure"))
            );
        }

        @Override
        public DataNode encode(AgingConfig value) {
            DataNode.ObjectBuilder builder = DataNode.objectBuilder();
            value.inputLiquid().ifPresent(id -> builder.put("input_liquid", DataNode.string(id.toString())));
            value.outputLiquid().ifPresent(id -> builder.put(
                    "output",
                    DataNode.objectBuilder().put("liquid", DataNode.string(id.toString())).build()
            ));
            return builder.build();
        }
    };

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

    public static AgingConfig incomplete() {
        return new AgingConfig(
                Optional.empty(),
                Optional.empty(),
                TemperatureProfile.fermentationDefault(),
                AgingKinetics.simplified(),
                ResourceId.parse("alcoholic:maturity"),
                ResourceId.parse("alcoholic:wood_exposure"),
                ResourceId.parse("alcoholic:oxidation_exposure")
        );
    }

    private static TemperatureProfile temperature(DataNode.ObjectNode object, String path) {
        return new TemperatureProfile(
                band(object, path, "preferred_temperature", 10.0, 16.0),
                band(object, path, "operating_temperature", 4.0, 22.0),
                band(object, path, "damaging_temperature", -20.0, 40.0)
        );
    }

    private static TemperatureBand band(
            DataNode.ObjectNode object,
            String path,
            String field,
            double defaultMin,
            double defaultMax
    ) {
        return object.get(field)
                .map(node -> {
                    DataNode.ObjectNode band = node.asObject(DataDecodeException.child(path, field));
                    return new TemperatureBand(
                            band.get("min").map(value -> value.asNumber(
                                    DataDecodeException.child(path, field + "/min")
                            ).doubleValue()).orElse(defaultMin),
                            band.get("max").map(value -> value.asNumber(
                                    DataDecodeException.child(path, field + "/max")
                            ).doubleValue()).orElse(defaultMax)
                    );
                })
                .orElse(new TemperatureBand(defaultMin, defaultMax));
    }

    private static AgingKinetics kinetics(DataNode.ObjectNode object, String path) {
        double ticks = object.get("ticks_to_complete")
                .map(value -> value.asNumber(DataDecodeException.child(path, "ticks_to_complete")).doubleValue())
                .orElse(12_000.0);
        if (ticks < 1.0) {
            ticks = 1.0;
        }
        double maturityRate = 1.0 / ticks;
        double woodRate = maturityRate;
        double oxidationRate = maturityRate * 0.15;
        double threshold = 1.0;
        DataNode.ObjectNode kinetics = object.get("kinetics")
                .map(node -> node.asObject(DataDecodeException.child(path, "kinetics")))
                .orElse(null);
        if (kinetics != null) {
            maturityRate = kinetics.get("maturity_rate")
                    .map(value -> value.asNumber(
                            DataDecodeException.child(path, "kinetics/maturity_rate")
                    ).doubleValue())
                    .orElse(maturityRate);
            woodRate = kinetics.get("wood_rate")
                    .map(value -> value.asNumber(
                            DataDecodeException.child(path, "kinetics/wood_rate")
                    ).doubleValue())
                    .orElse(woodRate);
            oxidationRate = kinetics.get("oxidation_rate")
                    .map(value -> value.asNumber(
                            DataDecodeException.child(path, "kinetics/oxidation_rate")
                    ).doubleValue())
                    .orElse(oxidationRate);
            threshold = kinetics.get("completion_threshold")
                    .map(value -> value.asNumber(
                            DataDecodeException.child(path, "kinetics/completion_threshold")
                    ).doubleValue())
                    .orElse(threshold);
        }
        return new AgingKinetics(maturityRate, woodRate, oxidationRate, threshold);
    }
}

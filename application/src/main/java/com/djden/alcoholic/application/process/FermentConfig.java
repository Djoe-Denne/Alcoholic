package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.ingredient.IngredientSelector;
import com.djden.alcoholic.api.process.LiquidAccepting;
import com.djden.alcoholic.api.process.ProcessDisplaySpec;
import com.djden.alcoholic.api.process.ProcessDisplaying;
import com.djden.alcoholic.domain.process.FermentationKinetics;
import com.djden.alcoholic.domain.process.TemperatureBand;
import com.djden.alcoholic.domain.process.TemperatureProfile;

import java.util.Optional;

public record FermentConfig(
        Optional<ResourceId> inputLiquid,
        Optional<ResourceId> outputLiquid,
        Optional<IngredientSelector> yeast,
        boolean requireYeast,
        TemperatureProfile temperature,
        FermentationKinetics kinetics,
        ResourceId sugarProperty,
        ResourceId ethanolProperty,
        ResourceId stressProperty
) implements LiquidAccepting, ReferencedLiquids, ProcessDisplaying {
    public FermentConfig {
        inputLiquid = inputLiquid == null ? Optional.empty() : inputLiquid;
        outputLiquid = outputLiquid == null ? Optional.empty() : outputLiquid;
        yeast = yeast == null ? Optional.empty() : yeast;
        temperature = temperature == null ? TemperatureProfile.fermentationDefault() : temperature;
        kinetics = kinetics == null ? FermentationKinetics.simplified() : kinetics;
        sugarProperty = sugarProperty == null ? ResourceId.parse("alcoholic:sugar") : sugarProperty;
        ethanolProperty = ethanolProperty == null ? ResourceId.parse("alcoholic:ethanol") : ethanolProperty;
        stressProperty = stressProperty == null ? ResourceId.parse("alcoholic:fermentation_stress") : stressProperty;
    }

    @Override
    public ProcessDisplaySpec display() {
        ProcessDisplaySpec.Builder builder = ProcessDisplaySpec.builder();
        yeast.ifPresent(selector -> builder.itemIn(selector, 1));
        inputLiquid.ifPresent(fluid -> builder.fluidIn(fluid, java.util.OptionalInt.empty()));
        outputLiquid.ifPresent(fluid -> builder.fluidOut(fluid, java.util.OptionalInt.empty()));
        return ProcessDisplays.preferred(builder, temperature).build();
    }

    public static final DataCodec<FermentConfig> CODEC = new DataCodec<>() {
        @Override
        public FermentConfig decode(DataNode node, String path) {
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
            Optional<IngredientSelector> yeast = Optional.empty();
            if (object.has("yeast")) {
                yeast = Optional.of(PressConfig.selector(
                        object.require("yeast", path).asObject(DataDecodeException.child(path, "yeast")),
                        DataDecodeException.child(path, "yeast")
                ));
            }
            boolean requireYeast = object.get("require_yeast")
                    .or(() -> object.get("requireYeast"))
                    .map(value -> value.asBoolean(DataDecodeException.child(path, "require_yeast")))
                    .orElse(yeast.isPresent());
            TemperatureProfile profile = temperature(object, path);
            FermentationKinetics kinetics = kinetics(object, path);
            ResourceId sugar = object.get("sugar_property")
                    .map(value -> DataCodecs.RESOURCE_ID.decode(value, DataDecodeException.child(path, "sugar_property")))
                    .orElse(ResourceId.parse("alcoholic:sugar"));
            ResourceId ethanol = object.get("ethanol_property")
                    .map(value -> DataCodecs.RESOURCE_ID.decode(value, DataDecodeException.child(path, "ethanol_property")))
                    .orElse(ResourceId.parse("alcoholic:ethanol"));
            ResourceId stress = object.get("stress_property")
                    .map(value -> DataCodecs.RESOURCE_ID.decode(value, DataDecodeException.child(path, "stress_property")))
                    .orElse(ResourceId.parse("alcoholic:fermentation_stress"));
            return new FermentConfig(input, output, yeast, requireYeast, profile, kinetics, sugar, ethanol, stress);
        }

        @Override
        public DataNode encode(FermentConfig value) {
            DataNode.ObjectBuilder builder = DataNode.objectBuilder();
            value.inputLiquid().ifPresent(id -> builder.put("input_liquid", DataNode.string(id.toString())));
            value.outputLiquid().ifPresent(id -> builder.put(
                    "output",
                    DataNode.objectBuilder().put("liquid", DataNode.string(id.toString())).build()
            ));
            builder.put("require_yeast", DataNode.bool(value.requireYeast()));
            builder.put("sugar_property", DataNode.string(value.sugarProperty().toString()));
            builder.put("ethanol_property", DataNode.string(value.ethanolProperty().toString()));
            return builder.build();
        }
    };

    @Override
    public boolean acceptsLiquid(ResourceId liquid) {
        return inputLiquid.isPresent() && inputLiquid.filter(liquid::equals).isPresent();
    }

    @Override
    public java.util.Collection<ResourceId> liquidIds() {
        java.util.ArrayList<ResourceId> ids = new java.util.ArrayList<>();
        inputLiquid.ifPresent(ids::add);
        outputLiquid.ifPresent(ids::add);
        return ids;
    }

    public static FermentConfig incomplete() {
        return new FermentConfig(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                false,
                TemperatureProfile.fermentationDefault(),
                FermentationKinetics.simplified(),
                ResourceId.parse("alcoholic:sugar"),
                ResourceId.parse("alcoholic:ethanol"),
                ResourceId.parse("alcoholic:fermentation_stress")
        );
    }

    private static TemperatureProfile temperature(DataNode.ObjectNode object, String path) {
        TemperatureBand preferred = band(
                object,
                path,
                "preferred_temperature",
                18.0,
                24.0
        );
        TemperatureBand operating = band(
                object,
                path,
                "operating_temperature",
                10.0,
                30.0
        );
        TemperatureBand damaging = band(
                object,
                path,
                "damaging_temperature",
                -20.0,
                45.0
        );
        return new TemperatureProfile(preferred, operating, damaging);
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

    private static FermentationKinetics kinetics(DataNode.ObjectNode object, String path) {
        DataNode.ObjectNode kinetics = object.get("kinetics")
                .map(node -> node.asObject(DataDecodeException.child(path, "kinetics")))
                .orElse(null);
        double ticks = object.get("ticks_to_complete")
                .map(value -> value.asNumber(DataDecodeException.child(path, "ticks_to_complete")).doubleValue())
                .orElse(12_000.0);
        if (ticks < 1.0) {
            ticks = 1.0;
        }
        double conversion = 0.47;
        double threshold = 0.02;
        double co2 = 0.45;
        double rate = 1.0 / ticks;
        if (kinetics != null) {
            conversion = kinetics.get("sugar_to_ethanol")
                    .map(value -> value.asNumber(
                            DataDecodeException.child(path, "kinetics/sugar_to_ethanol")
                    ).doubleValue())
                    .orElse(conversion);
            rate = kinetics.get("base_rate")
                    .map(value -> value.asNumber(
                            DataDecodeException.child(path, "kinetics/base_rate")
                    ).doubleValue())
                    .orElse(rate);
            threshold = kinetics.get("completion_threshold")
                    .map(value -> value.asNumber(
                            DataDecodeException.child(path, "kinetics/completion_threshold")
                    ).doubleValue())
                    .orElse(threshold);
            co2 = kinetics.get("co2_per_sugar")
                    .map(value -> value.asNumber(
                            DataDecodeException.child(path, "kinetics/co2_per_sugar")
                    ).doubleValue())
                    .orElse(co2);
        }
        return new FermentationKinetics(conversion, rate, threshold, co2);
    }
}

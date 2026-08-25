package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.ingredient.IngredientSelector;
import com.djden.alcoholic.api.process.LiquidAccepting;
import com.djden.alcoholic.api.process.SolidAccepting;
import com.djden.alcoholic.domain.process.HopProfile;
import com.djden.alcoholic.domain.process.TemperatureProfile;

import java.util.List;
import java.util.Optional;

public record BoilConfig(
        Optional<ResourceId> inputLiquid,
        Optional<ResourceId> outputLiquid,
        Optional<IngredientSelector> additionSelector,
        int additionAmount,
        int processingTicks,
        TemperatureProfile temperature,
        HopProfile hopProfile,
        ResourceId bitternessProperty,
        ResourceId aromaProperty,
        List<BoilAddition> additions
) implements SolidAccepting, LiquidAccepting, ReferencedLiquids {
    public BoilConfig {
        inputLiquid = inputLiquid == null ? Optional.empty() : inputLiquid;
        outputLiquid = outputLiquid == null ? Optional.empty() : outputLiquid;
        additionSelector = additionSelector == null ? Optional.empty() : additionSelector;
        if (additionAmount < 1) {
            additionAmount = 1;
        }
        if (processingTicks < 1) {
            processingTicks = 1;
        }
        temperature = temperature == null ? TemperatureProfiles.boilDefault() : temperature;
        hopProfile = hopProfile == null ? HopProfile.generic() : hopProfile;
        bitternessProperty = bitternessProperty == null
                ? ResourceId.parse("alcoholic:bitterness")
                : bitternessProperty;
        aromaProperty = aromaProperty == null
                ? ResourceId.parse("alcoholic:aroma")
                : aromaProperty;
        additions = additions == null ? List.of() : List.copyOf(additions);
    }

    /**
     * Ordered addition at a progress fraction in {@code [0, 1]}. Role is a
     * lightweight extraction hint ({@code bittering}, {@code aroma}, {@code dual}),
     * not a scripting engine.
     */
    public record BoilAddition(IngredientSelector selector, double atProgress, String role) {
        public BoilAddition {
            selector = selector == null ? new IngredientSelector.Tag(ResourceId.parse("alcoholic:hops")) : selector;
            if (!Double.isFinite(atProgress)) {
                atProgress = 0.0;
            }
            atProgress = Math.max(0.0, Math.min(1.0, atProgress));
            role = normalizeRole(role);
        }

        public BoilAddition(IngredientSelector selector, double atProgress) {
            this(selector, atProgress, "dual");
        }

        public static String normalizeRole(String role) {
            if (role == null || role.isBlank()) {
                return "dual";
            }
            String normalized = role.trim().toLowerCase();
            if ("bittering".equals(normalized) || "aroma".equals(normalized) || "dual".equals(normalized)) {
                return normalized;
            }
            return "dual";
        }
    }

    public static final DataCodec<BoilConfig> CODEC = new DataCodec<>() {
        @Override
        public BoilConfig decode(DataNode node, String path) {
            if (node == null || node.isNull()) {
                return incomplete();
            }
            DataNode.ObjectNode object = node.asObject(path);
            Optional<ResourceId> input = object.get("input_liquid")
                    .or(() -> object.get("inputLiquid"))
                    .map(value -> DataCodecs.RESOURCE_ID.decode(
                            value,
                            DataDecodeException.child(path, "input_liquid")
                    ));
            Optional<ResourceId> output = Optional.empty();
            if (object.has("output")) {
                DataNode.ObjectNode outputNode = object.require("output", path)
                        .asObject(DataDecodeException.child(path, "output"));
                output = outputNode.get("liquid")
                        .map(value -> DataCodecs.RESOURCE_ID.decode(
                                value,
                                DataDecodeException.child(path, "output/liquid")
                        ));
            }
            Optional<IngredientSelector> addition = Optional.empty();
            int amount = 1;
            DataNode.ObjectNode additionNode = object.get("addition")
                    .or(() -> object.get("hops"))
                    .map(value -> value.asObject(DataDecodeException.child(path, "addition")))
                    .orElse(null);
            if (additionNode != null) {
                addition = Optional.of(PressConfig.selector(
                        additionNode,
                        DataDecodeException.child(path, "addition")
                ));
                amount = additionNode.get("amount")
                        .map(value -> value.asNumber(
                                DataDecodeException.child(path, "addition/amount")
                        ).intValue())
                        .orElse(1);
            }
            int ticks = object.get("processing_time")
                    .map(value -> value.asNumber(DataDecodeException.child(path, "processing_time")).intValue())
                    .orElse(200);
            HopProfile profile = hopProfile(object, path);
            ResourceId bitterness = object.get("bitterness_property")
                    .map(value -> DataCodecs.RESOURCE_ID.decode(
                            value,
                            DataDecodeException.child(path, "bitterness_property")
                    ))
                    .orElse(ResourceId.parse("alcoholic:bitterness"));
            ResourceId aroma = object.get("aroma_property")
                    .map(value -> DataCodecs.RESOURCE_ID.decode(
                            value,
                            DataDecodeException.child(path, "aroma_property")
                    ))
                    .orElse(ResourceId.parse("alcoholic:aroma"));
            List<BoilAddition> additions = new java.util.ArrayList<>();
            if (object.has("additions")) {
                DataNode.ListNode list = object.require("additions", path)
                        .asList(DataDecodeException.child(path, "additions"));
                for (int index = 0; index < list.size(); index++) {
                    DataNode.ObjectNode row = list.get(index).asObject(
                            DataDecodeException.index(DataDecodeException.child(path, "additions"), index)
                    );
                    additions.add(new BoilAddition(
                            PressConfig.selector(row, DataDecodeException.child(path, "additions")),
                            row.get("at_progress")
                                    .map(value -> value.asNumber(
                                            DataDecodeException.child(path, "additions/at_progress")
                                    ).doubleValue())
                                    .orElse(0.0),
                            row.get("role")
                                    .map(value -> value.asString(
                                            DataDecodeException.child(path, "additions/role")
                                    ))
                                    .orElse("dual")
                    ));
                }
            } else if (addition.isPresent()) {
                additions.add(new BoilAddition(addition.orElseThrow(), 0.0));
            }
            return new BoilConfig(
                    input,
                    output,
                    addition,
                    amount,
                    ticks,
                    TemperatureProfiles.decode(object, path, TemperatureProfiles.boilDefault()),
                    profile,
                    bitterness,
                    aroma,
                    additions
            );
        }

        @Override
        public DataNode encode(BoilConfig value) {
            DataNode.ObjectBuilder builder = DataNode.objectBuilder();
            value.inputLiquid().ifPresent(liquid -> builder.put("input_liquid", DataNode.string(liquid.toString())));
            value.outputLiquid().ifPresent(liquid -> builder.put(
                    "output",
                    DataNode.objectBuilder()
                            .put("liquid", DataNode.string(liquid.toString()))
                            .build()
            ));
            value.additionSelector().ifPresent(selector -> {
                DataNode.ObjectBuilder addition = DataNode.objectBuilder();
                PressConfig.encodeSelector(addition, selector);
                addition.put("amount", DataNode.number(value.additionAmount()));
                builder.put("addition", addition.build());
            });
            if (!value.additions().isEmpty()) {
                List<DataNode> rows = new java.util.ArrayList<>();
                for (BoilAddition addition : value.additions()) {
                    DataNode.ObjectBuilder row = DataNode.objectBuilder();
                    PressConfig.encodeSelector(row, addition.selector());
                    row.put("at_progress", DataNode.number(addition.atProgress()));
                    row.put("role", DataNode.string(addition.role()));
                    rows.add(row.build());
                }
                builder.put("additions", DataNode.list(rows));
            }
            builder.put("processing_time", DataNode.number(value.processingTicks()));
            builder.put(
                    "hop_profile",
                    DataNode.objectBuilder()
                            .put("bitterness_potential", DataNode.number(value.hopProfile().bitternessPotential()))
                            .put("aroma_potential", DataNode.number(value.hopProfile().aromaPotential()))
                            .build()
            );
            builder.put("bitterness_property", DataNode.string(value.bitternessProperty().toString()));
            builder.put("aroma_property", DataNode.string(value.aromaProperty().toString()));
            TemperatureProfiles.encode(builder, value.temperature());
            return builder.build();
        }
    };

    public static BoilConfig incomplete() {
        return new BoilConfig(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                1,
                200,
                TemperatureProfiles.boilDefault(),
                HopProfile.generic(),
                ResourceId.parse("alcoholic:bitterness"),
                ResourceId.parse("alcoholic:aroma"),
                List.of()
        );
    }

    public boolean executable() {
        return inputLiquid.isPresent() && outputLiquid.isPresent();
    }

    public int requiredAdditionItems() {
        int additionsRequired = additions.isEmpty() ? (additionSelector.isPresent() ? 1 : 0) : additions.size();
        long required = (long) additionAmount * additionsRequired;
        return required >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) required;
    }

    @Override
    public Optional<IngredientSelector> inputSelector() {
        return additionSelector.or(() -> additions.isEmpty()
                ? Optional.empty()
                : Optional.of(additions.get(0).selector()));
    }

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

    private static HopProfile hopProfile(DataNode.ObjectNode object, String path) {
        DataNode.ObjectNode profile = object.get("hop_profile")
                .or(() -> object.get("hopProfile"))
                .map(node -> node.asObject(DataDecodeException.child(path, "hop_profile")))
                .orElse(null);
        if (profile == null) {
            return HopProfile.generic();
        }
        return new HopProfile(
                profile.get("bitterness_potential")
                        .map(value -> value.asNumber(
                                DataDecodeException.child(path, "hop_profile/bitterness_potential")
                        ).doubleValue())
                        .orElse(0.55),
                profile.get("aroma_potential")
                        .map(value -> value.asNumber(
                                DataDecodeException.child(path, "hop_profile/aroma_potential")
                        ).doubleValue())
                        .orElse(0.40)
        );
    }
}

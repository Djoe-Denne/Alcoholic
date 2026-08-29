package com.djden.alcoholic.domain.quality;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.liquid.LiquidBatchView;
import com.djden.alcoholic.api.process.ExecutorModifiers;
import com.djden.alcoholic.api.quality.QualityEvaluationContext;
import com.djden.alcoholic.api.quality.QualityOperator;
import com.djden.alcoholic.api.quality.QualitySignal;
import com.djden.alcoholic.api.registry.RegistrationException;
import com.djden.alcoholic.domain.process.OxygenCurve;
import com.djden.alcoholic.domain.process.QualityProfile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class BuiltinQualityOperators {
    public static final ResourceId READ = ResourceId.parse("alcoholic:read");
    public static final ResourceId HARVEST_COMPLEXITY = ResourceId.parse("alcoholic:harvest_complexity");
    public static final ResourceId DISTANCE_BALANCE = ResourceId.parse("alcoholic:distance_balance");
    public static final ResourceId WEIGHTED_PRESENT = ResourceId.parse("alcoholic:weighted_present");
    public static final ResourceId OXYGEN_CURVE = ResourceId.parse("alcoholic:oxygen_curve");
    public static final ResourceId WOOD_SWEET_SPOT = ResourceId.parse("alcoholic:wood_sweet_spot");
    public static final ResourceId AGING_MATURITY = ResourceId.parse("alcoholic:aging_maturity");
    public static final ResourceId STRESS = ResourceId.parse("alcoholic:stress");
    public static final ResourceId CAP_FLOOR = ResourceId.parse("alcoholic:cap_floor");
    public static final ResourceId FOLD_SUMMARY = ResourceId.parse("alcoholic:fold_summary");

    private static final double MATURITY_AGING_TICKS = 72_000.0;

    private BuiltinQualityOperators() {
    }

    public static List<QualityOperator<?>> all() {
        return List.of(
                QualityOperator.of(READ, ReadConfig.CODEC, BuiltinQualityOperators::read),
                QualityOperator.of(HARVEST_COMPLEXITY, DataCodecs.UNIT, BuiltinQualityOperators::harvest),
                QualityOperator.of(DISTANCE_BALANCE, DistanceBalanceConfig.CODEC, BuiltinQualityOperators::balance),
                QualityOperator.of(WEIGHTED_PRESENT, WeightedPresentConfig.CODEC, BuiltinQualityOperators::weighted),
                QualityOperator.of(
                        OXYGEN_CURVE,
                        DataCodecs.UNIT,
                        BuiltinQualityOperators::oxygen,
                        List.of("complexity", "defects")
                ),
                QualityOperator.of(WOOD_SWEET_SPOT, WoodSweetSpotConfig.CODEC, BuiltinQualityOperators::wood),
                QualityOperator.of(AGING_MATURITY, DataCodecs.UNIT, BuiltinQualityOperators::maturity),
                QualityOperator.of(STRESS, DataCodecs.UNIT, BuiltinQualityOperators::stress),
                QualityOperator.of(
                        CAP_FLOOR,
                        DataCodecs.UNIT,
                        BuiltinQualityOperators::caps,
                        List.of("cap", "floor")
                ),
                QualityOperator.of(
                        FOLD_SUMMARY,
                        DataCodecs.UNIT,
                        BuiltinQualityOperators::fold,
                        List.of("purity", "complexity", "maturity", "balance", "defects", "summary")
                )
        );
    }

    public static Map<ResourceId, QualityOperator<?>> map() {
        Map<ResourceId, QualityOperator<?>> operators = new LinkedHashMap<>();
        all().forEach(operator -> operators.put(operator.id(), operator));
        return Map.copyOf(operators);
    }

    public static void install(AlcoholicApi api) {
        Objects.requireNonNull(api, "api");
        for (QualityOperator<?> operator : all()) {
            if (api.qualityOperators().contains(operator.id())) {
                continue;
            }
            try {
                api.qualityOperators().register(operator);
            } catch (RegistrationException exception) {
                if (!api.qualityOperators().contains(operator.id())) {
                    throw exception;
                }
            }
        }
    }

    private static QualitySignal read(QualityEvaluationContext context, ReadConfig config) {
        return QualitySignal.value(number(context.batch(), config.property()));
    }

    private static QualitySignal harvest(QualityEvaluationContext context, Void config) {
        double harvest = number(context.batch(), QualityProfile.HARVEST_QUALITY);
        return QualitySignal.value(harvest);
    }

    private static QualitySignal balance(QualityEvaluationContext context, DistanceBalanceConfig config) {
        LiquidBatchView batch = context.batch();
        double sum = 0.0;
        int count = 0;
        for (DistanceBalanceConfig.Group group : config.groups()) {
            boolean present = group.present().isEmpty()
                    ? group.targets().keySet().stream().anyMatch(id -> number(batch, id) > 0.0)
                    : group.present().stream().anyMatch(id -> number(batch, id) > 0.0);
            if (!present) {
                continue;
            }
            double distance = 0.0;
            int targets = 0;
            for (Map.Entry<ResourceId, Double> target : group.targets().entrySet()) {
                distance += Math.abs(number(batch, target.getKey()) - target.getValue());
                targets++;
            }
            if (targets == 0) {
                continue;
            }
            sum += clamp01(1.0 - distance / targets);
            count++;
        }
        return QualitySignal.value(count == 0 ? 0.5 : clamp01(sum / count));
    }

    private static QualitySignal weighted(QualityEvaluationContext context, WeightedPresentConfig config) {
        LiquidBatchView batch = context.batch();
        if ("closeness".equals(config.mode())) {
            ResourceId left = config.left().orElse(QualityProfile.ACIDITY);
            ResourceId right = config.right().orElse(QualityProfile.SUGAR);
            double leftValue = number(batch, left);
            double rightValue = number(batch, right);
            if (leftValue <= 0.0 && rightValue <= 0.0) {
                return QualitySignal.value(0.0);
            }
            return QualitySignal.value(clamp01((1.0 - Math.abs(leftValue - rightValue)) * config.weight()));
        }
        boolean present = config.weights().keySet().stream().anyMatch(id -> number(batch, id) > 0.0);
        if (!present) {
            return QualitySignal.value(0.0);
        }
        double sum = 0.0;
        for (Map.Entry<ResourceId, Double> weight : config.weights().entrySet()) {
            sum += number(batch, weight.getKey()) * weight.getValue();
        }
        return QualitySignal.value(clamp01(sum));
    }

    private static QualitySignal oxygen(QualityEvaluationContext context, Void config) {
        LiquidBatchView batch = context.batch();
        double oxidation = first(batch, QualityProfile.OXIDATION, QualityProfile.OXIDATION_ALT);
        OxygenCurve.Evaluation evaluation = OxygenCurve.evaluate(
                oxidation,
                batch.provenance().totalAgingTime()
        );
        return QualitySignal.empty()
                .with("complexity", evaluation.complexityBonus())
                .with("defects", evaluation.defects());
    }

    private static QualitySignal wood(QualityEvaluationContext context, WoodSweetSpotConfig config) {
        LiquidBatchView batch = context.batch();
        double value = config.property()
                .map(id -> number(batch, id))
                .filter(number -> number > 0.0)
                .orElseGet(() -> first(
                        batch,
                        config.fallback().orElse(QualityProfile.WOOD),
                        QualityProfile.WOOD_ALT
                ));
        if (config.property().isPresent() && number(batch, config.property().orElseThrow()) <= 0.0) {
            return QualitySignal.value(0.0);
        }
        if (value <= 0.0) {
            return QualitySignal.value(0.0);
        }
        return QualitySignal.value(clamp01(OxygenCurve.woodSweetSpot(value) * config.weight()));
    }

    private static QualitySignal maturity(QualityEvaluationContext context, Void config) {
        double maturity = number(context.batch(), QualityProfile.MATURITY);
        double agingTime = context.batch().provenance().totalAgingTime();
        double agingFactor = agingTime > 0.0
                ? Math.min(0.25, agingTime / MATURITY_AGING_TICKS * 0.25)
                : 0.0;
        return QualitySignal.value(clamp01(maturity + agingFactor));
    }

    private static QualitySignal stress(QualityEvaluationContext context, Void config) {
        LiquidBatchView batch = context.batch();
        double value = Math.max(
                batch.provenance().fermentationStress(),
                Math.max(number(batch, QualityProfile.STRESS), number(batch, QualityProfile.FERMENTATION_STRESS))
        );
        return QualitySignal.value(value);
    }

    private static QualitySignal caps(QualityEvaluationContext context, Void config) {
        ExecutorModifiers scale = context.modifiers();
        double cap = Math.min(scale.complexityCap(), number(context.batch(), QualityProfile.COMPLEXITY_CAP, 1.0));
        double floor = Math.max(scale.purityFloor(), number(context.batch(), QualityProfile.PURITY_FLOOR, 0.0));
        return QualitySignal.empty().with("cap", cap).with("floor", floor);
    }

    private static QualitySignal fold(QualityEvaluationContext context, Void config) {
        double complexitySum = context.input("complexity", 0.0);
        double balance = context.input("balance", 0.5);
        double maturity = context.input("maturity", 0.0);
        double defects = context.input("defects", 0.0);
        double cap = context.inputSignal("caps").get("cap", context.input("caps", 1.0));
        if (!context.inputSignal("caps").ports().containsKey("cap") && context.input("caps", -1.0) < 0.0) {
            cap = Math.min(context.modifiers().complexityCap(), number(context.batch(), QualityProfile.COMPLEXITY_CAP, 1.0));
        }
        defects = clamp01(defects);
        double purity = clamp01(1.0 - defects);
        double complexity = Math.min(cap, clamp01(complexitySum));
        double summary = clamp01(((purity + complexity + maturity + balance) / 4.0) * (1.0 - defects));
        summary = Math.min(cap, summary);
        return QualitySignal.empty()
                .with("purity", purity)
                .with("complexity", complexity)
                .with("maturity", maturity)
                .with("balance", balance)
                .with("defects", defects)
                .with("summary", summary);
    }

    static double first(LiquidBatchView batch, ResourceId primary, ResourceId secondary) {
        double value = number(batch, primary);
        return value > 0.0 ? value : number(batch, secondary);
    }

    static double number(LiquidBatchView batch, ResourceId id) {
        return number(batch, id, 0.0);
    }

    static double number(LiquidBatchView batch, ResourceId id, double fallback) {
        Object value = batch.property(id).orElse(null);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return fallback;
    }

    static double clamp01(double value) {
        if (!Double.isFinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }

    public record ReadConfig(ResourceId property) {
        public static final DataCodec<ReadConfig> CODEC = new DataCodec<>() {
            @Override
            public ReadConfig decode(DataNode node, String path) {
                DataNode.ObjectNode object = node.asObject(path);
                return new ReadConfig(DataCodecs.RESOURCE_ID.decode(
                        object.require("property", path),
                        DataDecodeException.child(path, "property")
                ));
            }

            @Override
            public DataNode encode(ReadConfig value) {
                return DataNode.objectBuilder()
                        .put("property", DataNode.string(value.property().toString()))
                        .build();
            }
        };
    }

    public record DistanceBalanceConfig(List<Group> groups) {
        public DistanceBalanceConfig {
            groups = List.copyOf(new ArrayList<>(Objects.requireNonNull(groups, "groups")));
        }

        public record Group(List<ResourceId> present, Map<ResourceId, Double> targets) {
            public Group {
                present = List.copyOf(new ArrayList<>(Objects.requireNonNull(present, "present")));
                targets = Map.copyOf(new LinkedHashMap<>(Objects.requireNonNull(targets, "targets")));
            }
        }

        public static final DataCodec<DistanceBalanceConfig> CODEC = new DataCodec<>() {
            @Override
            public DistanceBalanceConfig decode(DataNode node, String path) {
                DataNode.ObjectNode object = node.asObject(path);
                DataNode.ListNode list = object.require("groups", path)
                        .asList(DataDecodeException.child(path, "groups"));
                List<Group> groups = new ArrayList<>();
                for (int index = 0; index < list.size(); index++) {
                    String groupPath = DataDecodeException.index(path + "/groups", index);
                    DataNode.ObjectNode group = list.get(index).asObject(groupPath);
                    List<ResourceId> present = group.get("present")
                            .map(value -> DataCodecs.RESOURCE_ID.listOf().decode(
                                    value,
                                    DataDecodeException.child(groupPath, "present")
                            ))
                            .orElse(List.of());
                    groups.add(new Group(present, decodeTargets(
                            group.get("targets").orElseGet(() -> DataNode.object(Map.of())),
                            DataDecodeException.child(groupPath, "targets")
                    )));
                }
                return new DistanceBalanceConfig(groups);
            }

            @Override
            public DataNode encode(DistanceBalanceConfig value) {
                List<DataNode> groups = new ArrayList<>();
                for (Group group : value.groups()) {
                    DataNode.ObjectBuilder targets = DataNode.objectBuilder();
                    group.targets().forEach((id, target) ->
                            targets.put(id.toString(), DataNode.number(target)));
                    groups.add(DataNode.objectBuilder()
                            .put("present", DataCodecs.RESOURCE_ID.listOf().encode(group.present()))
                            .put("targets", targets.build())
                            .build());
                }
                return DataNode.objectBuilder().put("groups", DataNode.list(groups)).build();
            }
        };
    }

    public record WeightedPresentConfig(
            String mode,
            Map<ResourceId, Double> weights,
            Optional<ResourceId> left,
            Optional<ResourceId> right,
            double weight
    ) {
        public WeightedPresentConfig {
            mode = mode == null || mode.isBlank() ? "sum" : mode;
            weights = Map.copyOf(new LinkedHashMap<>(Objects.requireNonNullElse(weights, Map.of())));
            left = left == null ? Optional.empty() : left;
            right = right == null ? Optional.empty() : right;
            if (!Double.isFinite(weight)) {
                weight = 0.15;
            }
        }

        public static final DataCodec<WeightedPresentConfig> CODEC = new DataCodec<>() {
            @Override
            public WeightedPresentConfig decode(DataNode node, String path) {
                DataNode.ObjectNode object = node.asObject(path);
                return new WeightedPresentConfig(
                        object.get("mode").map(value -> value.asString(DataDecodeException.child(path, "mode")))
                                .orElse("sum"),
                        decodeTargets(
                                object.get("weights").orElseGet(() -> DataNode.object(Map.of())),
                                DataDecodeException.child(path, "weights")
                        ),
                        object.get("left").map(value -> DataCodecs.RESOURCE_ID.decode(
                                value,
                                DataDecodeException.child(path, "left")
                        )),
                        object.get("right").map(value -> DataCodecs.RESOURCE_ID.decode(
                                value,
                                DataDecodeException.child(path, "right")
                        )),
                        object.get("weight")
                                .map(value -> value.asNumber(DataDecodeException.child(path, "weight")).doubleValue())
                                .orElse(0.15)
                );
            }

            @Override
            public DataNode encode(WeightedPresentConfig value) {
                DataNode.ObjectBuilder weights = DataNode.objectBuilder();
                value.weights().forEach((id, weight) -> weights.put(id.toString(), DataNode.number(weight)));
                DataNode.ObjectBuilder builder = DataNode.objectBuilder()
                        .put("mode", DataNode.string(value.mode()))
                        .put("weights", weights.build())
                        .put("weight", DataNode.number(value.weight()));
                value.left().ifPresent(id -> builder.put("left", DataNode.string(id.toString())));
                value.right().ifPresent(id -> builder.put("right", DataNode.string(id.toString())));
                return builder.build();
            }
        };
    }

    public record WoodSweetSpotConfig(Optional<ResourceId> property, Optional<ResourceId> fallback, double weight) {
        public WoodSweetSpotConfig {
            property = property == null ? Optional.empty() : property;
            fallback = fallback == null ? Optional.empty() : fallback;
            if (!Double.isFinite(weight) || weight <= 0.0) {
                weight = 1.0;
            }
        }

        public static final DataCodec<WoodSweetSpotConfig> CODEC = new DataCodec<>() {
            @Override
            public WoodSweetSpotConfig decode(DataNode node, String path) {
                if (node == null || node.isNull() || (node instanceof DataNode.ObjectNode object && object.fields().isEmpty())) {
                    return new WoodSweetSpotConfig(Optional.empty(), Optional.empty(), 1.0);
                }
                DataNode.ObjectNode object = node.asObject(path);
                return new WoodSweetSpotConfig(
                        object.get("property").map(value -> DataCodecs.RESOURCE_ID.decode(
                                value,
                                DataDecodeException.child(path, "property")
                        )),
                        object.get("fallback").map(value -> DataCodecs.RESOURCE_ID.decode(
                                value,
                                DataDecodeException.child(path, "fallback")
                        )),
                        object.get("weight")
                                .map(value -> value.asNumber(DataDecodeException.child(path, "weight")).doubleValue())
                                .orElse(1.0)
                );
            }

            @Override
            public DataNode encode(WoodSweetSpotConfig value) {
                DataNode.ObjectBuilder builder = DataNode.objectBuilder()
                        .put("weight", DataNode.number(value.weight()));
                value.property().ifPresent(id -> builder.put("property", DataNode.string(id.toString())));
                value.fallback().ifPresent(id -> builder.put("fallback", DataNode.string(id.toString())));
                return builder.build();
            }
        };
    }

    private static Map<ResourceId, Double> decodeTargets(DataNode node, String path) {
        Map<ResourceId, Double> targets = new LinkedHashMap<>();
        node.asObject(path).fields().forEach((key, value) -> targets.put(
                ResourceId.parse(key),
                value.asNumber(DataDecodeException.child(path, key)).doubleValue()
        ));
        return targets;
    }
}

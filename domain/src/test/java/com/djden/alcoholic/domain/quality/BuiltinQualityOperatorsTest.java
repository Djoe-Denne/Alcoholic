package com.djden.alcoholic.domain.quality;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.liquid.LiquidBatchView;
import com.djden.alcoholic.api.process.ExecutorModifiers;
import com.djden.alcoholic.api.quality.QualityEvaluationContext;
import com.djden.alcoholic.api.quality.QualityOperator;
import com.djden.alcoholic.api.quality.QualitySignal;
import com.djden.alcoholic.domain.liquid.BatchProvenance;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.domain.process.QualityProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuiltinQualityOperatorsTest {
    private static final ResourceId MUST = ResourceId.parse("alcoholic:red_grape_must");

    @Test
    void readReturnsPropertyOrZero() {
        QualityOperator<BuiltinQualityOperators.ReadConfig> read = operator(BuiltinQualityOperators.READ);
        Context context = new Context(batch(PropertyBag.empty().with(QualityProfile.SUGAR, 0.42)));
        assertEquals(
                0.42,
                read.evaluate(context, new BuiltinQualityOperators.ReadConfig(QualityProfile.SUGAR)).get("value", 0.0),
                1e-9
        );
        assertEquals(
                0.0,
                read.evaluate(context, new BuiltinQualityOperators.ReadConfig(QualityProfile.COLOR)).get("value", 1.0),
                1e-9
        );
    }

    @Test
    void harvestReadsHarvestQuality() {
        QualitySignal signal = operator(BuiltinQualityOperators.HARVEST_COMPLEXITY)
                .evaluate(new Context(batch(PropertyBag.empty().with(QualityProfile.HARVEST_QUALITY, 0.60))), null);
        assertEquals(0.60, signal.get("value", 0.0), 1e-9);
    }

    @Test
    void distanceBalanceAveragesPresentTargets() {
        QualityOperator<BuiltinQualityOperators.DistanceBalanceConfig> balance =
                operator(BuiltinQualityOperators.DISTANCE_BALANCE);
        QualitySignal signal = balance.evaluate(
                new Context(batch(PropertyBag.empty().with(QualityProfile.TANNIN, 0.50))),
                new BuiltinQualityOperators.DistanceBalanceConfig(List.of(
                        new BuiltinQualityOperators.DistanceBalanceConfig.Group(
                                List.of(QualityProfile.TANNIN),
                                Map.of(QualityProfile.TANNIN, 0.50)
                        )
                ))
        );
        assertEquals(1.0, signal.get("value", 0.0), 1e-9);
    }

    @Test
    void weightedPresentSumsWeightedProperties() {
        QualityOperator<BuiltinQualityOperators.WeightedPresentConfig> weighted =
                operator(BuiltinQualityOperators.WEIGHTED_PRESENT);
        QualitySignal signal = weighted.evaluate(
                new Context(batch(PropertyBag.empty().with(QualityProfile.TANNIN, 0.50))),
                new BuiltinQualityOperators.WeightedPresentConfig(
                        "sum",
                        Map.of(QualityProfile.TANNIN, 0.20),
                        Optional.empty(),
                        Optional.empty(),
                        1.0
                )
        );
        assertEquals(0.10, signal.get("value", 0.0), 1e-9);
    }

    @Test
    void oxygenCurveWritesComplexityAndDefects() {
        LiquidBatch aged = new LiquidBatch(
                Optional.empty(),
                Optional.of(MUST),
                1000,
                PropertyBag.empty().with(QualityProfile.OXIDATION, 0.18),
                BatchProvenance.empty().withSummaries(0.0, 8_000, 0.0, 0.18)
        );
        QualitySignal signal = operator(BuiltinQualityOperators.OXYGEN_CURVE).evaluate(new Context(aged), null);
        assertTrue(signal.get("complexity", 0.0) > 0.0);
        assertEquals(0.0, signal.get("defects", 1.0), 1e-9);
    }

    @Test
    void woodSweetSpotReadsWoodExposure() {
        QualityOperator<BuiltinQualityOperators.WoodSweetSpotConfig> wood =
                operator(BuiltinQualityOperators.WOOD_SWEET_SPOT);
        QualitySignal signal = wood.evaluate(
                new Context(batch(PropertyBag.empty().with(QualityProfile.WOOD, 0.25))),
                new BuiltinQualityOperators.WoodSweetSpotConfig(Optional.empty(), Optional.empty(), 1.0)
        );
        assertTrue(signal.get("value", 0.0) > 0.0);
    }

    @Test
    void agingMaturityAddsAgingFactor() {
        LiquidBatch aged = new LiquidBatch(
                Optional.empty(),
                Optional.of(MUST),
                1000,
                PropertyBag.empty().with(QualityProfile.MATURITY, 0.40),
                BatchProvenance.empty().withSummaries(0.0, 36_000, 0.0, 0.0)
        );
        QualitySignal signal = operator(BuiltinQualityOperators.AGING_MATURITY).evaluate(new Context(aged), null);
        assertTrue(signal.get("value", 0.0) > 0.40);
    }

    @Test
    void stressReadsFermentationStress() {
        QualitySignal signal = operator(BuiltinQualityOperators.STRESS).evaluate(
                new Context(batch(PropertyBag.empty().with(QualityProfile.FERMENTATION_STRESS, 0.30))),
                null
        );
        assertEquals(0.30, signal.get("value", 0.0), 1e-9);
    }

    @Test
    void capFloorReadsModifiers() {
        QualitySignal signal = operator(BuiltinQualityOperators.CAP_FLOOR).evaluate(
                new Context(batch(PropertyBag.empty()), ExecutorModifiers.industrialVat()),
                null
        );
        assertEquals(ExecutorModifiers.industrialVat().complexityCap(), signal.get("cap", 0.0), 1e-9);
        assertEquals(ExecutorModifiers.industrialVat().purityFloor(), signal.get("floor", 0.0), 1e-9);
    }

    @Test
    void foldSummaryBuildsProfileBag() {
        QualitySignal signal = operator(BuiltinQualityOperators.FOLD_SUMMARY).evaluate(
                new Context(
                        batch(PropertyBag.empty()),
                        ExecutorModifiers.identity(),
                        Map.of(
                                "complexity", QualitySignal.value(0.40),
                                "balance", QualitySignal.value(0.50),
                                "maturity", QualitySignal.value(0.20),
                                "defects", QualitySignal.value(0.10)
                        )
                ),
                null
        );
        assertEquals(0.90, signal.get("purity", 0.0), 1e-9);
        assertEquals(0.40, signal.get("complexity", 0.0), 1e-9);
        assertEquals(0.20, signal.get("maturity", 0.0), 1e-9);
        assertEquals(0.50, signal.get("balance", 0.0), 1e-9);
        assertEquals(0.10, signal.get("defects", 0.0), 1e-9);
        assertTrue(signal.get("summary", 0.0) > 0.0);
    }

    @SuppressWarnings("unchecked")
    private static <C> QualityOperator<C> operator(ResourceId id) {
        return (QualityOperator<C>) BuiltinQualityOperators.map().get(id);
    }

    private static LiquidBatch batch(PropertyBag properties) {
        return LiquidBatch.of(MUST, 1000, properties);
    }

    private record Context(
            LiquidBatchView batch,
            ExecutorModifiers modifiers,
            Map<String, QualitySignal> inputs
    ) implements QualityEvaluationContext {
        Context(LiquidBatchView batch) {
            this(batch, ExecutorModifiers.identity(), Map.of());
        }

        Context(LiquidBatchView batch, ExecutorModifiers modifiers) {
            this(batch, modifiers, Map.of());
        }

        @Override
        public QualitySignal inputSignal(String port) {
            return inputs.getOrDefault(port, QualitySignal.empty());
        }
    }
}

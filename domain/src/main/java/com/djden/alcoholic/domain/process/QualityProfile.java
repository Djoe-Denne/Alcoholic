package com.djden.alcoholic.domain.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.liquid.LiquidBatchView;
import com.djden.alcoholic.api.process.ExecutorModifiers;
import com.djden.alcoholic.api.quality.QualityOperator;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.domain.quality.QualityEvaluator;
import com.djden.alcoholic.domain.quality.QualityGraph;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Derived drink profile. Ethanol is never an input. Axes are not persisted
 * under these names; {@link #summary()} is a UI fold that still respects
 * the tightest complexity cap and highest purity floor stamped on the batch.
 *
 * <p>Evaluate a catalog graph with {@link #evaluate(QualityGraph, LiquidBatchView, ExecutorModifiers)}.
 * Runtime resolution (identity / {@code baseLiquid} / generic) lives in application
 * {@code QualityProfiles}.</p>
 */
public record QualityProfile(
        double purity,
        double complexity,
        double maturity,
        double balance,
        double defects,
        double summary
) {
    public static final ResourceId HARVEST_QUALITY = ResourceId.parse("alcoholic:quality");
    public static final ResourceId SUGAR = ResourceId.parse("alcoholic:sugar");
    public static final ResourceId ACIDITY = ResourceId.parse("alcoholic:acidity");
    public static final ResourceId TANNIN = ResourceId.parse("alcoholic:tannin");
    public static final ResourceId COLOR = ResourceId.parse("alcoholic:color");
    public static final ResourceId MATURITY = ResourceId.parse("alcoholic:maturity");
    public static final ResourceId WOOD = ResourceId.parse("alcoholic:wood_exposure");
    public static final ResourceId WOOD_ALT = ResourceId.parse("alcoholic:wood");
    public static final ResourceId OXIDATION = ResourceId.parse("alcoholic:oxidation_exposure");
    public static final ResourceId OXIDATION_ALT = ResourceId.parse("alcoholic:oxidation");
    public static final ResourceId AROMA = ResourceId.parse("alcoholic:aroma");
    public static final ResourceId BITTERNESS = ResourceId.parse("alcoholic:bitterness");
    public static final ResourceId CARBONATION = ResourceId.parse("alcoholic:carbonation");
    public static final ResourceId STRESS = ResourceId.parse("alcoholic:stress");
    public static final ResourceId FERMENTATION_STRESS = ResourceId.parse("alcoholic:fermentation_stress");
    public static final ResourceId COMPLEXITY_CAP = ResourceId.parse("alcoholic:complexity_cap");
    public static final ResourceId PURITY_FLOOR = ResourceId.parse("alcoholic:purity_floor");
    public static final ResourceId ETHANOL = ResourceId.parse("alcoholic:ethanol");

    public QualityProfile {
        purity = clamp01(purity);
        complexity = clamp01(complexity);
        maturity = clamp01(maturity);
        balance = clamp01(balance);
        defects = clamp01(defects);
        summary = clamp01(summary);
    }

    public static QualityProfile evaluate(
            QualityGraph graph,
            LiquidBatchView batch,
            ExecutorModifiers modifiers
    ) {
        return QualityEvaluator.evaluate(graph, batch, modifiers);
    }

    public static QualityProfile evaluate(
            QualityGraph graph,
            Map<ResourceId, ? extends QualityOperator<?>> operators,
            LiquidBatchView batch,
            ExecutorModifiers modifiers
    ) {
        return QualityEvaluator.evaluate(graph, operators, batch, modifiers);
    }

    public static LiquidBatch stampCap(LiquidBatch batch, ExecutorModifiers modifiers) {
        Objects.requireNonNull(batch, "batch");
        ExecutorModifiers scale = modifiers == null ? ExecutorModifiers.identity() : modifiers;
        double existingCap = batch.number(COMPLEXITY_CAP, 1.0);
        double existingFloor = batch.number(PURITY_FLOOR, 0.0);
        return batch
                .withProperty(COMPLEXITY_CAP, Math.min(existingCap, scale.complexityCap()))
                .withProperty(PURITY_FLOOR, Math.max(existingFloor, scale.purityFloor()));
    }

    public static Map<ResourceId, Object> stampCap(Map<ResourceId, Object> properties, ExecutorModifiers modifiers) {
        Map<ResourceId, Object> next = new LinkedHashMap<>(properties == null ? Map.of() : properties);
        ExecutorModifiers scale = modifiers == null ? ExecutorModifiers.identity() : modifiers;
        next.put(COMPLEXITY_CAP, Math.min(numeric(next.get(COMPLEXITY_CAP), 1.0), scale.complexityCap()));
        next.put(PURITY_FLOOR, Math.max(numeric(next.get(PURITY_FLOOR), 0.0), scale.purityFloor()));
        return Map.copyOf(next);
    }

    public static PropertyBag stampCap(PropertyBag bag, ExecutorModifiers modifiers) {
        PropertyBag source = bag == null ? PropertyBag.empty() : bag;
        return new PropertyBag(stampCap(source.asMap(), modifiers));
    }

    private static double numeric(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return fallback;
    }

    private static double clamp01(double value) {
        if (!Double.isFinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}

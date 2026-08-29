package com.djden.alcoholic.domain.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.liquid.BatchProvenanceView;
import com.djden.alcoholic.api.liquid.LiquidBatchView;
import com.djden.alcoholic.api.process.ExecutorModifiers;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Derived drink profile. Ethanol is never an input. Axes are not persisted
 * under these names; {@link #summary()} is a UI fold that still respects
 * the tightest complexity cap and highest purity floor stamped on the batch.
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
    /** One hour at 20 tps. Ages the maturity axis without farming complexity. */
    private static final double MATURITY_AGING_TICKS = 72_000.0;

    public QualityProfile {
        purity = clamp01(purity);
        complexity = clamp01(complexity);
        maturity = clamp01(maturity);
        balance = clamp01(balance);
        defects = clamp01(defects);
        summary = clamp01(summary);
    }

    public static QualityProfile derive(LiquidBatchView batch) {
        return derive(batch, ExecutorModifiers.identity());
    }

    public static QualityProfile derive(LiquidBatchView batch, ExecutorModifiers modifiers) {
        Objects.requireNonNull(batch, "batch");
        ExecutorModifiers scale = modifiers == null ? ExecutorModifiers.identity() : modifiers;
        BatchProvenanceView provenance = batch.provenance();
        double harvest = number(batch, HARVEST_QUALITY);
        double sugar = number(batch, SUGAR);
        double acid = number(batch, ACIDITY);
        double tannin = number(batch, TANNIN);
        double color = number(batch, COLOR);
        double maturityValue = number(batch, MATURITY);
        double wood = first(batch, WOOD, WOOD_ALT);
        double oxidation = first(batch, OXIDATION, OXIDATION_ALT);
        double aroma = number(batch, AROMA);
        double bitterness = number(batch, BITTERNESS);
        double carbonation = number(batch, CARBONATION);
        double stress = Math.max(
                provenance.fermentationStress(),
                Math.max(number(batch, STRESS), number(batch, FERMENTATION_STRESS))
        );
        OxygenCurve.Evaluation oxygen = OxygenCurve.evaluate(oxidation, provenance.totalAgingTime());
        double floor = Math.max(scale.purityFloor(), number(batch, PURITY_FLOOR, 0.0));
        double defects = clamp01(stress + oxygen.defects() + floor);
        double purity = clamp01(1.0 - defects);
        double nuance = (acid > 0.0 || sugar > 0.0)
                ? clamp01((1.0 - Math.abs(acid - sugar)) * 0.15)
                : 0.0;
        double hop = (aroma > 0.0 || bitterness > 0.0)
                ? clamp01(aroma * 0.35 + bitterness * 0.20)
                : 0.0;
        double tanninComplexity = tannin > 0.0 ? OxygenCurve.woodSweetSpot(tannin) * 0.45 : 0.0;
        double colorComplexity = color > 0.0 ? clamp01(color * 0.10) : 0.0;
        double rawComplexity = harvest * scale.processFidelity()
                + nuance
                + hop
                + tanninComplexity
                + colorComplexity
                + OxygenCurve.woodSweetSpot(wood)
                + oxygen.complexityBonus();
        double cap = Math.min(scale.complexityCap(), number(batch, COMPLEXITY_CAP, 1.0));
        double complexity = Math.min(cap, clamp01(rawComplexity));
        double agingFactor = provenance.totalAgingTime() > 0.0
                ? Math.min(0.25, provenance.totalAgingTime() / MATURITY_AGING_TICKS * 0.25)
                : 0.0;
        double maturityAxis = clamp01(maturityValue + agingFactor);
        double balanceSum = 0.0;
        int balanceCount = 0;
        if (acid > 0.0 || sugar > 0.0) {
            balanceSum += clamp01(1.0 - (Math.abs(sugar - 0.35) + Math.abs(acid - 0.45)) / 2.0);
            balanceCount++;
        }
        if (bitterness > 0.0 || aroma > 0.0) {
            balanceSum += clamp01(1.0 - Math.abs(bitterness - 0.40));
            balanceCount++;
        }
        if (carbonation > 0.0) {
            balanceSum += clamp01(1.0 - Math.abs(carbonation - 0.35));
            balanceCount++;
        }
        if (tannin > 0.0) {
            balanceSum += clamp01(1.0 - Math.abs(tannin - 0.45));
            balanceCount++;
        }
        double balance = balanceCount == 0 ? 0.5 : clamp01(balanceSum / balanceCount);
        double summary = clamp01(((purity + complexity + maturityAxis + balance) / 4.0) * (1.0 - defects));
        summary = Math.min(cap, summary);
        return new QualityProfile(purity, complexity, maturityAxis, balance, defects, summary);
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

    private static double first(LiquidBatchView batch, ResourceId primary, ResourceId secondary) {
        double value = number(batch, primary);
        return value > 0.0 ? value : number(batch, secondary);
    }

    private static double number(LiquidBatchView batch, ResourceId id) {
        return number(batch, id, 0.0);
    }

    private static double number(LiquidBatchView batch, ResourceId id, double fallback) {
        return numeric(batch.property(id).orElse(null), fallback);
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

package com.djden.alcoholic.domain.vessel;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Taste-axis snapshot stored on a barrel and leaked into the next fill.
 * Beverage identity is never consulted.
 */
public final class CaskImprint {
    public static final double FADE = 0.55;
    public static final double DEFAULT_TRANSFER = 0.20;

    public static final ResourceId ACIDITY = ResourceId.parse("alcoholic:acidity");
    public static final ResourceId SUGAR = ResourceId.parse("alcoholic:sugar");
    public static final ResourceId TANNIN = ResourceId.parse("alcoholic:tannin");
    public static final ResourceId AROMA = ResourceId.parse("alcoholic:aroma");
    public static final ResourceId ROAST = ResourceId.parse("alcoholic:roast_intensity");

    private static final Set<ResourceId> DEFAULT_PROPERTIES = Set.of(ACIDITY, SUGAR, TANNIN, AROMA, ROAST);

    private CaskImprint() {
    }

    public static Set<ResourceId> defaultProperties() {
        return DEFAULT_PROPERTIES;
    }

    public static PropertyBag snapshot(PropertyBag source, Set<ResourceId> axes) {
        if (source == null || axes == null || axes.isEmpty()) {
            return PropertyBag.empty();
        }
        Map<ResourceId, Object> kept = new LinkedHashMap<>();
        for (ResourceId id : axes) {
            if (id == null) {
                continue;
            }
            Object value = source.get(id).orElse(null);
            if (value instanceof Number number) {
                double amount = number.doubleValue();
                if (Double.isFinite(amount) && amount > 0.0) {
                    kept.put(id, amount);
                }
            }
        }
        return kept.isEmpty() ? PropertyBag.empty() : new PropertyBag(kept);
    }

    public static double volumeWeight(int volumeMillibuckets, int capacityMillibuckets) {
        if (capacityMillibuckets < 1 || volumeMillibuckets < 1) {
            return 0.0;
        }
        return Math.min(1.0, volumeMillibuckets / (double) capacityMillibuckets);
    }

    public static PropertyBag fade(PropertyBag imprint, double factor) {
        double scale = Double.isFinite(factor) && factor > 0.0 ? factor : 0.0;
        if (imprint == null || imprint.asMap().isEmpty() || scale <= 0.0) {
            return PropertyBag.empty();
        }
        Map<ResourceId, Object> faded = new LinkedHashMap<>();
        imprint.asMap().forEach((id, value) -> {
            if (value instanceof Number number) {
                double amount = number.doubleValue() * scale;
                if (amount > 0.0) {
                    faded.put(id, amount);
                }
            }
        });
        return faded.isEmpty() ? PropertyBag.empty() : new PropertyBag(faded);
    }

    public static PropertyBag mergeEmptying(PropertyBag existing, PropertyBag snapshot) {
        PropertyBag incoming = snapshot == null ? PropertyBag.empty() : snapshot;
        if (existing == null || existing.asMap().isEmpty()) {
            return incoming;
        }
        PropertyBag faded = fade(existing, FADE);
        Set<ResourceId> ids = new LinkedHashSet<>();
        ids.addAll(faded.ids());
        ids.addAll(incoming.ids());
        if (ids.isEmpty()) {
            return PropertyBag.empty();
        }
        Map<ResourceId, Object> merged = new LinkedHashMap<>();
        for (ResourceId id : ids) {
            double left = number(faded, id);
            double right = number(incoming, id);
            double next;
            if (left <= 0.0) {
                next = right;
            } else if (right <= 0.0) {
                next = left;
            } else {
                next = (left + right) * 0.5;
            }
            if (next > 0.0) {
                merged.put(id, next);
            }
        }
        return merged.isEmpty() ? PropertyBag.empty() : new PropertyBag(merged);
    }

    public static PropertyBag fromMap(Map<ResourceId, Double> values) {
        if (values == null || values.isEmpty()) {
            return PropertyBag.empty();
        }
        Map<ResourceId, Object> copy = new LinkedHashMap<>();
        values.forEach((id, amount) -> {
            if (id != null && amount != null && Double.isFinite(amount) && amount > 0.0) {
                copy.put(id, amount);
            }
        });
        return copy.isEmpty() ? PropertyBag.empty() : new PropertyBag(copy);
    }

    public static Map<ResourceId, Double> toMap(PropertyBag imprint) {
        Map<ResourceId, Double> values = new LinkedHashMap<>();
        if (imprint == null) {
            return Map.of();
        }
        imprint.asMap().forEach((id, value) -> {
            if (value instanceof Number number) {
                double amount = number.doubleValue();
                if (Double.isFinite(amount) && amount > 0.0) {
                    values.put(id, amount);
                }
            }
        });
        return Map.copyOf(values);
    }

    /**
     * Linear upward leak: {@code current += imprint * transfer * (maturityStep / threshold)},
     * only when imprint exceeds current, capped at the imprint value.
     */
    public static LiquidBatch leak(
            LiquidBatch batch,
            PropertyBag imprint,
            double transfer,
            double maturityStep,
            double threshold,
            Set<ResourceId> axes
    ) {
        if (batch == null || imprint == null || imprint.asMap().isEmpty()) {
            return batch;
        }
        if (!Double.isFinite(transfer) || transfer <= 0.0 || !Double.isFinite(maturityStep) || maturityStep <= 0.0) {
            return batch;
        }
        if (!Double.isFinite(threshold) || threshold <= 0.0) {
            return batch;
        }
        double fraction = transfer * (maturityStep / threshold);
        if (fraction <= 0.0) {
            return batch;
        }
        Set<ResourceId> allowed = axes == null || axes.isEmpty() ? DEFAULT_PROPERTIES : axes;
        LiquidBatch next = batch;
        for (ResourceId id : allowed) {
            double target = number(imprint, id);
            if (target <= 0.0) {
                continue;
            }
            double current = batch.number(id, 0.0);
            if (target <= current) {
                continue;
            }
            next = next.withProperty(id, Math.min(target, current + target * fraction));
        }
        return next;
    }

    private static double number(PropertyBag bag, ResourceId id) {
        Object value = bag.get(id).orElse(null);
        if (value instanceof Number number) {
            double amount = number.doubleValue();
            return Double.isFinite(amount) && amount > 0.0 ? amount : 0.0;
        }
        return 0.0;
    }
}

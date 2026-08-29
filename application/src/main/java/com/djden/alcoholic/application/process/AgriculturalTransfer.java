package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.ingredient.SolidInputView;
import com.djden.alcoholic.domain.liquid.PropertyBag;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copies agricultural lot properties onto a produced liquid without drink-family logic.
 */
public final class AgriculturalTransfer {
    private static final Set<ResourceId> CHARACTER = Set.of(
            ResourceId.parse("alcoholic:quality"),
            ResourceId.parse("alcoholic:acidity"),
            ResourceId.parse("alcoholic:aroma"),
            ResourceId.parse("alcoholic:bitterness"),
            ResourceId.parse("alcoholic:tannin"),
            ResourceId.parse("alcoholic:color")
    );

    private AgriculturalTransfer() {
    }

    public static PropertyBag combine(List<? extends SolidInputView> lots) {
        if (lots == null || lots.isEmpty()) {
            return PropertyBag.empty();
        }
        int total = 0;
        Map<ResourceId, Double> numeric = new LinkedHashMap<>();
        Map<ResourceId, Object> other = new LinkedHashMap<>();
        for (SolidInputView lot : lots) {
            int count = Math.max(0, lot.count());
            if (count == 0) {
                continue;
            }
            total += count;
            for (ResourceId id : lot.propertyIds()) {
                Object value = lot.property(id).orElse(null);
                if (value instanceof Number number) {
                    numeric.merge(id, number.doubleValue() * count, Double::sum);
                } else if (value != null) {
                    other.merge(id, value, (left, right) -> left.equals(right) ? left : "blended");
                }
            }
        }
        if (total == 0) {
            return PropertyBag.empty();
        }
        Map<ResourceId, Object> merged = new LinkedHashMap<>(other);
        int weight = total;
        numeric.forEach((id, sum) -> merged.put(id, sum / weight));
        return new PropertyBag(merged);
    }

    public static PropertyBag scaleCharacter(PropertyBag bag, double fidelity) {
        PropertyBag source = bag == null ? PropertyBag.empty() : bag;
        if (!Double.isFinite(fidelity) || fidelity >= 1.0 - 1e-12) {
            return source;
        }
        double factor = Math.max(0.0, fidelity);
        PropertyBag next = source;
        for (ResourceId id : CHARACTER) {
            Object value = source.get(id).orElse(null);
            if (value instanceof Number number) {
                next = next.with(id, number.doubleValue() * factor);
            }
        }
        return next;
    }
}

package com.djden.alcoholic.api.property;

import com.djden.alcoholic.api.PublicApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Combines two property values. The property definition owns this behaviour;
 * callers must not switch on property IDs.
 */
@PublicApi
@FunctionalInterface
public interface PropertyAggregator {
    Optional<Object> merge(Object left, double leftVolume, Object right, double rightVolume);

    static PropertyAggregator forStrategy(PropertyMerge merge) {
        PropertyMerge strategy = merge == null ? PropertyMerge.WEIGHTED_AVERAGE : merge;
        return (left, leftVolume, right, rightVolume) -> mergeValues(
                left,
                leftVolume,
                right,
                rightVolume,
                strategy
        );
    }

    @SuppressWarnings("unchecked")
    private static Optional<Object> mergeValues(
            Object left,
            double leftVolume,
            Object right,
            double rightVolume,
            PropertyMerge strategy
    ) {
        if (strategy == PropertyMerge.IDENTICAL_OR_REJECT) {
            return Objects.equals(left, right) ? Optional.of(left) : Optional.empty();
        }
        if (strategy == PropertyMerge.COMBINE_SET) {
            return Optional.of(combineSet(left, right));
        }
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            double total = leftVolume + rightVolume;
            double a = leftNumber.doubleValue();
            double b = rightNumber.doubleValue();
            double value = switch (strategy) {
                case MAX -> Math.max(a, b);
                case MIN -> Math.min(a, b);
                case FIRST -> a;
                case SUM -> a + b;
                case MATCH_OR_BLENDED, WEIGHTED_AVERAGE, CUSTOM ->
                        total <= 0.0 ? 0.0 : (a * leftVolume + b * rightVolume) / total;
                default -> total <= 0.0 ? 0.0 : (a * leftVolume + b * rightVolume) / total;
            };
            if (left instanceof Integer && right instanceof Integer) {
                return Optional.of((int) Math.round(value));
            }
            return Optional.of(value);
        }
        if (strategy == PropertyMerge.FIRST) {
            return Optional.of(left);
        }
        if (Objects.equals(left, right)) {
            return Optional.of(left);
        }
        if (strategy == PropertyMerge.MATCH_OR_BLENDED) {
            return Optional.of("blended");
        }
        return Optional.of(left);
    }

    private static Object combineSet(Object left, Object right) {
        if (left instanceof Map<?, ?> leftMap && right instanceof Map<?, ?> rightMap) {
            Map<Object, Object> merged = new LinkedHashMap<>(leftMap);
            rightMap.forEach((key, value) -> merged.merge(key, value, (existing, incoming) -> {
                if (existing instanceof Number first && incoming instanceof Number second) {
                    return first.doubleValue() + second.doubleValue();
                }
                return existing;
            }));
            return Map.copyOf(merged);
        }
        Set<Object> values = new LinkedHashSet<>();
        addCombined(values, left);
        addCombined(values, right);
        return new ArrayList<>(values);
    }

    private static void addCombined(Collection<Object> values, Object value) {
        if (value instanceof Collection<?> collection) {
            values.addAll(collection);
            return;
        }
        values.add(value);
    }
}

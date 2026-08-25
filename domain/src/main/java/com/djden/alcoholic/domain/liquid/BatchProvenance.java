package com.djden.alcoholic.domain.liquid;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.liquid.BatchProvenanceView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Flattened batch history. Merge and blend renormalize contribution maps.
 */
public final class BatchProvenance implements BatchProvenanceView {
    public static final int SCHEMA_VERSION = 1;
    public static final int MAX_MAP_ENTRIES = 16;
    public static final double MIN_FRACTION = 0.005;

    private final Map<ResourceId, Double> originComposition;
    private final Map<ResourceId, Double> blendComposition;
    private final double fermentationStress;
    private final double totalAgingTime;
    private final double woodExposure;
    private final double oxidationExposure;

    public BatchProvenance(
            Map<ResourceId, Double> originComposition,
            Map<ResourceId, Double> blendComposition,
            double fermentationStress,
            double totalAgingTime,
            double woodExposure,
            double oxidationExposure
    ) {
        this.originComposition = normalize(originComposition);
        this.blendComposition = normalize(blendComposition);
        this.fermentationStress = finiteNonNegative(fermentationStress, "fermentationStress");
        this.totalAgingTime = finiteNonNegative(totalAgingTime, "totalAgingTime");
        this.woodExposure = finiteNonNegative(woodExposure, "woodExposure");
        this.oxidationExposure = finiteNonNegative(oxidationExposure, "oxidationExposure");
    }

    public static BatchProvenance empty() {
        return new BatchProvenance(Map.of(), Map.of(), 0.0, 0.0, 0.0, 0.0);
    }

    public static BatchProvenance ofOrigin(ResourceId origin, double fraction) {
        return new BatchProvenance(Map.of(origin, fraction), Map.of(), 0.0, 0.0, 0.0, 0.0);
    }

    public BatchProvenance copy() {
        return new BatchProvenance(
                originComposition,
                blendComposition,
                fermentationStress,
                totalAgingTime,
                woodExposure,
                oxidationExposure
        );
    }

    public BatchProvenance withSummaries(
            double fermentationStress,
            double totalAgingTime,
            double woodExposure,
            double oxidationExposure
    ) {
        return new BatchProvenance(
                originComposition,
                blendComposition,
                fermentationStress,
                totalAgingTime,
                woodExposure,
                oxidationExposure
        );
    }

    public BatchProvenance merge(BatchProvenance other, double thisVolume, double otherVolume) {
        Objects.requireNonNull(other, "other");
        double total = thisVolume + otherVolume;
        if (total <= 0.0) {
            return empty();
        }
        return new BatchProvenance(
                weightedMaps(originComposition, thisVolume, other.originComposition, otherVolume),
                weightedMaps(blendComposition, thisVolume, other.blendComposition, otherVolume),
                weighted(fermentationStress, thisVolume, other.fermentationStress, otherVolume, total),
                weighted(totalAgingTime, thisVolume, other.totalAgingTime, otherVolume, total),
                weighted(woodExposure, thisVolume, other.woodExposure, otherVolume, total),
                weighted(oxidationExposure, thisVolume, other.oxidationExposure, otherVolume, total)
        );
    }

    public BatchProvenance blendWith(
            ResourceId thisDefinition,
            ResourceId otherDefinition,
            BatchProvenance other,
            double thisVolume,
            double otherVolume
    ) {
        BatchProvenance merged = merge(other, thisVolume, otherVolume);
        Map<ResourceId, Double> blends = new LinkedHashMap<>();
        addWeighted(blends, thisDefinition, thisVolume);
        addWeighted(blends, otherDefinition, otherVolume);
        merged.blendComposition.forEach((id, fraction) ->
                addWeighted(blends, id, fraction * (thisVolume + otherVolume))
        );
        return new BatchProvenance(
                merged.originComposition,
                blends,
                merged.fermentationStress,
                merged.totalAgingTime,
                merged.woodExposure,
                merged.oxidationExposure
        );
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public Map<ResourceId, Double> originComposition() {
        return originComposition;
    }

    @Override
    public Map<ResourceId, Double> blendComposition() {
        return blendComposition;
    }

    @Override
    public double fermentationStress() {
        return fermentationStress;
    }

    @Override
    public double totalAgingTime() {
        return totalAgingTime;
    }

    @Override
    public double woodExposure() {
        return woodExposure;
    }

    @Override
    public double oxidationExposure() {
        return oxidationExposure;
    }

    public int serializedEntryCount() {
        return originComposition.size() + blendComposition.size();
    }

    private static Map<ResourceId, Double> weightedMaps(
            Map<ResourceId, Double> left,
            double leftVolume,
            Map<ResourceId, Double> right,
            double rightVolume
    ) {
        Map<ResourceId, Double> merged = new LinkedHashMap<>();
        left.forEach((id, fraction) -> addWeighted(merged, id, fraction * leftVolume));
        right.forEach((id, fraction) -> addWeighted(merged, id, fraction * rightVolume));
        return merged;
    }

    private static void addWeighted(Map<ResourceId, Double> map, ResourceId id, double weight) {
        if (id == null || weight <= 0.0) {
            return;
        }
        map.merge(id, weight, Double::sum);
    }

    private static Map<ResourceId, Double> normalize(Map<ResourceId, Double> raw) {
        Map<ResourceId, Double> copy = new LinkedHashMap<>();
        if (raw != null) {
            raw.forEach((id, value) -> {
                if (id != null && value != null && Double.isFinite(value) && value > 0.0) {
                    copy.put(id, value);
                }
            });
        }
        double total = copy.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total <= 0.0) {
            return Map.of();
        }
        List<Map.Entry<ResourceId, Double>> ranked = new ArrayList<>();
        copy.forEach((id, value) -> {
            double fraction = value / total;
            if (fraction >= MIN_FRACTION) {
                ranked.add(Map.entry(id, fraction));
            }
        });
        ranked.sort(Comparator.<Map.Entry<ResourceId, Double>>comparingDouble(Map.Entry::getValue).reversed()
                .thenComparing(entry -> entry.getKey().toString()));
        List<Map.Entry<ResourceId, Double>> keptEntries = ranked.size() > MAX_MAP_ENTRIES
                ? new ArrayList<>(ranked.subList(0, MAX_MAP_ENTRIES))
                : ranked;
        double kept = keptEntries.stream().mapToDouble(Map.Entry::getValue).sum();
        Map<ResourceId, Double> normalized = new LinkedHashMap<>();
        if (kept <= 0.0) {
            return Map.of();
        }
        for (Map.Entry<ResourceId, Double> entry : keptEntries) {
            normalized.put(entry.getKey(), entry.getValue() / kept);
        }
        return Map.copyOf(normalized);
    }

    private static double weighted(double left, double leftVolume, double right, double rightVolume, double total) {
        return (left * leftVolume + right * rightVolume) / total;
    }

    private static double finiteNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be >= 0");
        }
        return value;
    }
}

package com.djden.alcoholic.domain.vessel;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.vessel.BarrelHistoryView;
import com.djden.alcoholic.api.vessel.VesselProfileView;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class VesselProfile implements VesselProfileView {
    private final ResourceId id;
    private final ResourceId material;
    private final int capacityMillibuckets;
    private final Set<ResourceId> processCapabilities;
    private final double permeability;
    private final double woodExtractionMultiplier;
    private final double oxidationMultiplier;
    private final Optional<BarrelHistoryView> history;

    public VesselProfile(
            ResourceId id,
            ResourceId material,
            int capacityMillibuckets,
            Set<ResourceId> processCapabilities,
            double permeability,
            double woodExtractionMultiplier,
            double oxidationMultiplier,
            Optional<BarrelHistoryView> history
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.material = Objects.requireNonNull(material, "material");
        if (capacityMillibuckets < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        this.capacityMillibuckets = capacityMillibuckets;
        this.processCapabilities = Set.copyOf(new LinkedHashSet<>(
                Objects.requireNonNull(processCapabilities, "processCapabilities")
        ));
        this.permeability = finiteNonNegative(permeability, "permeability");
        this.woodExtractionMultiplier = finiteNonNegative(woodExtractionMultiplier, "woodExtractionMultiplier");
        this.oxidationMultiplier = finiteNonNegative(oxidationMultiplier, "oxidationMultiplier");
        this.history = history == null ? Optional.empty() : history;
    }

    public static VesselProfile oakBarrel() {
        return new VesselProfile(
                ResourceId.parse("alcoholic:oak_barrel"),
                ResourceId.parse("alcoholic:oak"),
                4_000,
                Set.of(ResourceId.parse("alcoholic:age")),
                0.35,
                1.0,
                0.25,
                Optional.empty()
        );
    }

    public static VesselProfile industrialAgingVessel(int capacityMillibuckets) {
        return new VesselProfile(
                ResourceId.parse("alcoholic:industrial_aging_vessel"),
                ResourceId.parse("alcoholic:oak"),
                Math.max(1, capacityMillibuckets),
                Set.of(ResourceId.parse("alcoholic:age")),
                0.35,
                1.0,
                0.25,
                Optional.empty()
        );
    }

    @Override
    public ResourceId id() {
        return id;
    }

    @Override
    public ResourceId material() {
        return material;
    }

    @Override
    public int capacityMillibuckets() {
        return capacityMillibuckets;
    }

    @Override
    public Set<ResourceId> processCapabilities() {
        return processCapabilities;
    }

    @Override
    public double permeability() {
        return permeability;
    }

    @Override
    public double woodExtractionMultiplier() {
        return woodExtractionMultiplier;
    }

    @Override
    public double oxidationMultiplier() {
        return oxidationMultiplier;
    }

    @Override
    public Optional<BarrelHistoryView> history() {
        return history;
    }

    @Override
    public VesselProfile withHistory(BarrelHistoryView updated) {
        return new VesselProfile(
                id,
                material,
                capacityMillibuckets,
                processCapabilities,
                permeability,
                woodExtractionMultiplier,
                oxidationMultiplier,
                Optional.of(updated)
        );
    }

    public double seasoningMultiplier() {
        return history.filter(value -> value.usageCount() > 0 || !value.previousContents().isEmpty())
                .map(value -> 1.15)
                .orElse(1.0);
    }

    private static double finiteNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be >= 0");
        }
        return value;
    }
}

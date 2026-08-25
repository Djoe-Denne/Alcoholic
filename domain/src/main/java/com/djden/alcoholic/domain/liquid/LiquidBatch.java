package com.djden.alcoholic.domain.liquid;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.beverage.BeverageIdentity;
import com.djden.alcoholic.api.liquid.BatchProvenanceView;
import com.djden.alcoholic.api.liquid.LiquidBatchView;
import com.djden.alcoholic.api.property.LiquidProperty;
import com.djden.alcoholic.api.property.PropertyAggregator;
import com.djden.alcoholic.api.property.PropertyMerge;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public final class LiquidBatch implements LiquidBatchView {
    private final Optional<BeverageIdentity> identity;
    private final Optional<ResourceId> baseLiquid;
    private final double volume;
    private final PropertyBag properties;
    private final BatchProvenance provenance;

    public LiquidBatch(
            Optional<BeverageIdentity> identity,
            Optional<ResourceId> baseLiquid,
            double volume,
            PropertyBag properties
    ) {
        this(identity, baseLiquid, volume, properties, BatchProvenance.empty());
    }

    public LiquidBatch(
            Optional<BeverageIdentity> identity,
            Optional<ResourceId> baseLiquid,
            double volume,
            PropertyBag properties,
            BatchProvenance provenance
    ) {
        this.identity = Objects.requireNonNull(identity, "identity");
        this.baseLiquid = Objects.requireNonNull(baseLiquid, "baseLiquid");
        if (volume < 0.0) {
            throw new IllegalArgumentException("volume must be >= 0");
        }
        this.volume = volume;
        this.properties = Objects.requireNonNull(properties, "properties").copy();
        this.provenance = Objects.requireNonNull(provenance, "provenance").copy();
    }

    public static LiquidBatch of(double volume) {
        return new LiquidBatch(Optional.empty(), Optional.empty(), volume, PropertyBag.empty());
    }

    public static LiquidBatch of(ResourceId definition, double volume, PropertyBag properties) {
        return new LiquidBatch(
                Optional.empty(),
                Optional.of(definition),
                volume,
                properties
        );
    }

    public static LiquidBatch of(
            ResourceId definition,
            double volume,
            PropertyBag properties,
            BatchProvenance provenance
    ) {
        return new LiquidBatch(
                Optional.empty(),
                Optional.of(definition),
                volume,
                properties,
                provenance
        );
    }

    @Override
    public Optional<BeverageIdentity> identity() {
        return identity;
    }

    @Override
    public Optional<ResourceId> baseLiquid() {
        return baseLiquid;
    }

    public Optional<ResourceId> definition() {
        return baseLiquid;
    }

    @Override
    public double volume() {
        return volume;
    }

    public int volumeMillibuckets() {
        return (int) Math.round(volume);
    }

    @Override
    public Set<ResourceId> propertyIds() {
        return properties.ids();
    }

    @Override
    public Optional<Object> property(ResourceId id) {
        return properties.get(id);
    }

    @Override
    public <T> Optional<T> property(LiquidProperty<T> property) {
        return properties.get(property);
    }

    @Override
    public BatchProvenanceView provenance() {
        return provenance;
    }

    public BatchProvenance batchProvenance() {
        return provenance;
    }

    public double number(ResourceId id, double fallback) {
        return properties.get(id)
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::doubleValue)
                .orElse(fallback);
    }

    public PropertyBag properties() {
        return properties;
    }

    public LiquidBatch withProperty(ResourceId id, Object value) {
        return new LiquidBatch(identity, baseLiquid, volume, properties.with(id, value), provenance);
    }

    public LiquidBatch withVolume(double updated) {
        return new LiquidBatch(identity, baseLiquid, updated, properties.copy(), provenance.copy());
    }

    public LiquidBatch withBaseLiquid(ResourceId definition) {
        return new LiquidBatch(identity, Optional.of(definition), volume, properties, provenance);
    }

    public LiquidBatch withIdentity(BeverageIdentity updated) {
        return new LiquidBatch(Optional.of(updated), baseLiquid, volume, properties, provenance);
    }

    public LiquidBatch withProvenance(BatchProvenance updated) {
        return new LiquidBatch(identity, baseLiquid, volume, properties, updated);
    }

    public BatchSplitResult split(double requestedVolume) {
        double requested = Math.max(0.0, requestedVolume);
        if (requested <= 0.0) {
            return new BatchSplitResult(emptyLike(0.0), this);
        }
        if (requested >= volume) {
            return new BatchSplitResult(this, emptyLike(0.0));
        }
        return new BatchSplitResult(
                new LiquidBatch(identity, baseLiquid, requested, properties.copy(), provenance.copy()),
                new LiquidBatch(identity, baseLiquid, volume - requested, properties.copy(), provenance.copy())
        );
    }

    public Optional<LiquidBatch> merge(
            LiquidBatch other,
            Function<ResourceId, PropertyMerge> strategies
    ) {
        return merge(other, strategies, id -> PropertyAggregator.forStrategy(
                strategies == null ? PropertyMerge.WEIGHTED_AVERAGE : strategies.apply(id)
        ));
    }

    public Optional<LiquidBatch> merge(
            LiquidBatch other,
            Function<ResourceId, PropertyMerge> strategies,
            Function<ResourceId, PropertyAggregator> aggregators
    ) {
        Objects.requireNonNull(other, "other");
        if (volume == 0.0) {
            return Optional.of(other);
        }
        if (other.volume == 0.0) {
            return Optional.of(this);
        }
        if (!baseLiquid.equals(other.baseLiquid)) {
            return Optional.empty();
        }
        Optional<PropertyBag> merged = properties.merge(
                other.properties,
                volume,
                other.volume,
                strategies,
                aggregators
        );
        if (merged.isEmpty()) {
            return Optional.empty();
        }
        Optional<BeverageIdentity> mergedIdentity = identity.equals(other.identity) ? identity : Optional.empty();
        return Optional.of(new LiquidBatch(
                mergedIdentity,
                baseLiquid,
                volume + other.volume,
                merged.get(),
                provenance.merge(other.provenance, volume, other.volume)
        ));
    }

    public Optional<LiquidBatch> blend(
            LiquidBatch other,
            ResourceId output,
            Function<ResourceId, PropertyMerge> strategies,
            Function<ResourceId, PropertyAggregator> aggregators
    ) {
        Objects.requireNonNull(other, "other");
        Objects.requireNonNull(output, "output");
        if (volume <= 0.0 || other.volume <= 0.0) {
            return Optional.empty();
        }
        Optional<PropertyBag> merged = properties.merge(
                other.properties,
                volume,
                other.volume,
                strategies,
                aggregators
        );
        if (merged.isEmpty()) {
            return Optional.empty();
        }
        BatchProvenance blended = provenance.blendWith(
                baseLiquid.orElse(output),
                other.baseLiquid.orElse(output),
                other.provenance,
                volume,
                other.volume
        );
        return Optional.of(new LiquidBatch(
                Optional.empty(),
                Optional.of(output),
                volume + other.volume,
                merged.get(),
                blended
        ));
    }

    private LiquidBatch emptyLike(double emptyVolume) {
        return new LiquidBatch(
                identity,
                baseLiquid,
                emptyVolume,
                properties.copy(),
                provenance.copy()
        );
    }
}

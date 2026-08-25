package com.djden.alcoholic.minecraft.fluid;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.property.PropertyAggregator;
import com.djden.alcoholic.api.property.PropertyMerge;
import com.djden.alcoholic.domain.liquid.BatchSplitResult;
import com.djden.alcoholic.domain.liquid.LiquidBatch;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class LiquidTank {
    private int capacity;
    private final Function<ResourceId, PropertyMerge> merge;
    private final Function<ResourceId, PropertyAggregator> aggregators;
    private LiquidBatch stored;

    public LiquidTank(int capacity, Function<ResourceId, PropertyMerge> merge) {
        this(capacity, merge, id -> PropertyAggregator.forStrategy(
                merge == null ? PropertyMerge.WEIGHTED_AVERAGE : merge.apply(id)
        ));
    }

    public LiquidTank(
            int capacity,
            Function<ResourceId, PropertyMerge> merge,
            Function<ResourceId, PropertyAggregator> aggregators
    ) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be >= 0");
        }
        this.capacity = capacity;
        this.merge = Objects.requireNonNull(merge, "merge");
        this.aggregators = Objects.requireNonNull(aggregators, "aggregators");
        this.stored = LiquidBatch.of(0);
    }

    /**
     * A tank that never accepts or yields liquid. Used for vessels that are
     * not currently bound to a real owner.
     */
    public static LiquidTank sealed() {
        return new SealedTank();
    }

    public int capacity() {
        return capacity;
    }

    /**
     * Changes capacity without copying the stored batch. Refuses if contents
     * would no longer fit.
     */
    public boolean tryResize(int newCapacity) {
        if (newCapacity < 1) {
            return false;
        }
        if (stored.volumeMillibuckets() > newCapacity) {
            return false;
        }
        this.capacity = newCapacity;
        return true;
    }

    public Optional<LiquidBatch> contents() {
        return stored.volume() > 0.0 ? Optional.of(stored) : Optional.empty();
    }

    public int fill(LiquidBatch incoming, boolean simulate) {
        Objects.requireNonNull(incoming, "incoming");
        if (incoming.volume() <= 0.0) {
            return 0;
        }
        int space = capacity - stored.volumeMillibuckets();
        if (space <= 0) {
            return 0;
        }
        int accepted = Math.min(space, incoming.volumeMillibuckets());
        LiquidBatch slice = incoming.split(accepted).extracted();
        LiquidBatch next;
        if (stored.volume() <= 0.0) {
            next = slice;
        } else {
            Optional<LiquidBatch> merged = stored.merge(slice, merge, aggregators);
            if (merged.isEmpty()) {
                return 0;
            }
            next = merged.get();
        }
        if (!simulate) {
            stored = next;
        }
        return accepted;
    }

    public LiquidBatch drain(int millibuckets, boolean simulate) {
        BatchSplitResult split = stored.split(Math.max(0, millibuckets));
        if (!simulate) {
            stored = split.remaining().volume() <= 0.0 ? LiquidBatch.of(0) : split.remaining();
        }
        return split.extracted();
    }

    public boolean trySet(LiquidBatch batch) {
        Objects.requireNonNull(batch, "batch");
        if (batch.volume() <= 0.0) {
            stored = LiquidBatch.of(0);
            return true;
        }
        if (batch.volumeMillibuckets() > capacity) {
            return false;
        }
        stored = batch;
        return true;
    }

    public void set(LiquidBatch batch) {
        Objects.requireNonNull(batch, "batch");
        if (batch.volume() <= 0.0) {
            stored = LiquidBatch.of(0);
            return;
        }
        int needed = Math.max(1, batch.volumeMillibuckets());
        if (needed > capacity) {
            capacity = needed;
        }
        stored = batch;
    }

    public void clear() {
        stored = LiquidBatch.of(0);
    }

    private static final class SealedTank extends LiquidTank {
        private SealedTank() {
            super(0, id -> PropertyMerge.WEIGHTED_AVERAGE);
        }

        @Override
        public int fill(LiquidBatch incoming, boolean simulate) {
            return 0;
        }

        @Override
        public LiquidBatch drain(int millibuckets, boolean simulate) {
            return LiquidBatch.of(0);
        }

        @Override
        public boolean tryResize(int newCapacity) {
            return false;
        }

        @Override
        public boolean trySet(LiquidBatch batch) {
            return false;
        }

        @Override
        public void set(LiquidBatch batch) {
            // Sealed tanks never retain liquid.
        }
    }
}

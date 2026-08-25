package com.djden.alcoholic.domain.liquid;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.property.PropertyMerge;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiquidBatchMergeTest {
    private static final ResourceId SUGAR = ResourceId.parse("alcoholic:sugar");
    private static final ResourceId MUST = ResourceId.parse("test:must");

    @Test
    void mergesCompatibleBatchesWithVolumeWeightedSugar() {
        LiquidBatch first = LiquidBatch.of(MUST, 10_000, PropertyBag.empty().with(SUGAR, 180.0));
        LiquidBatch second = LiquidBatch.of(MUST, 5_000, PropertyBag.empty().with(SUGAR, 150.0));

        Optional<LiquidBatch> merged = first.merge(second, id -> PropertyMerge.WEIGHTED_AVERAGE);

        assertTrue(merged.isPresent());
        assertEquals(15_000, merged.orElseThrow().volume());
        assertEquals(170.0, merged.orElseThrow().number(SUGAR, 0.0), 1e-9);
    }

    @Test
    void rejectsDifferentLiquidDefinitions() {
        LiquidBatch must = LiquidBatch.of(MUST, 1000, PropertyBag.empty());
        LiquidBatch other = LiquidBatch.of(ResourceId.parse("test:wash"), 1000, PropertyBag.empty());
        assertTrue(must.merge(other, id -> PropertyMerge.WEIGHTED_AVERAGE).isEmpty());
    }

    @Test
    void splitsTwentyIntoFiveAndFifteenWithIndependentBags() {
        LiquidBatch original = LiquidBatch.of(MUST, 20, PropertyBag.empty().with(SUGAR, 10.0));
        BatchSplitResult split = original.split(5);
        assertEquals(5.0, split.extracted().volume(), 1e-9);
        assertEquals(15.0, split.remaining().volume(), 1e-9);
        assertEquals(20.0, split.extracted().volume() + split.remaining().volume(), 1e-9);
        LiquidBatch mutated = split.extracted().withProperty(SUGAR, 99.0);
        assertEquals(10.0, split.remaining().number(SUGAR, 0.0), 1e-9);
        assertEquals(99.0, mutated.number(SUGAR, 0.0), 1e-9);
    }

    @Test
    void averagesEqualVolumesTenAndTwentyPercentToFifteen() {
        LiquidBatch first = LiquidBatch.of(MUST, 1000, PropertyBag.empty().with(SUGAR, 0.10));
        LiquidBatch second = LiquidBatch.of(MUST, 1000, PropertyBag.empty().with(SUGAR, 0.20));
        LiquidBatch merged = first.merge(second, id -> PropertyMerge.WEIGHTED_AVERAGE).orElseThrow();
        assertEquals(0.15, merged.number(SUGAR, 0.0), 1e-9);
    }

    @Test
    void averagesUnequalVolumesTenAtHundredAndFiveAtForty() {
        LiquidBatch first = LiquidBatch.of(MUST, 10_000, PropertyBag.empty().with(SUGAR, 100.0));
        LiquidBatch second = LiquidBatch.of(MUST, 5_000, PropertyBag.empty().with(SUGAR, 40.0));
        LiquidBatch merged = first.merge(second, id -> PropertyMerge.WEIGHTED_AVERAGE).orElseThrow();
        assertEquals(80.0, merged.number(SUGAR, 0.0), 1e-9);
    }

    @Test
    void rejectsWhenIdenticalOrRejectPropertyDiverges() {
        ResourceId marker = ResourceId.parse("test:lot");
        LiquidBatch first = LiquidBatch.of(MUST, 1000, PropertyBag.empty().with(marker, "a"));
        LiquidBatch second = LiquidBatch.of(MUST, 1000, PropertyBag.empty().with(marker, "b"));
        assertTrue(first.merge(second, id -> PropertyMerge.IDENTICAL_OR_REJECT).isEmpty());
    }
}

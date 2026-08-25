package com.djden.alcoholic.minecraft.fluid;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.property.PropertyMerge;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiquidTankSplitTest {
    private static final ResourceId MUST = ResourceId.parse("test:must");
    private static final ResourceId SUGAR = ResourceId.parse("alcoholic:sugar");

    @Test
    void drainSplitsWithoutDuplicatingVolume() {
        LiquidTank tank = new LiquidTank(8_000, id -> PropertyMerge.WEIGHTED_AVERAGE);
        tank.fill(LiquidBatch.of(MUST, 20, PropertyBag.empty().with(SUGAR, 0.5)), false);
        LiquidBatch extracted = tank.drain(5, false);
        assertEquals(5.0, extracted.volume(), 1e-9);
        assertEquals(15.0, tank.contents().orElseThrow().volume(), 1e-9);
        assertEquals(0.5, extracted.number(SUGAR, 0.0), 1e-9);
        assertEquals(0.5, tank.contents().orElseThrow().number(SUGAR, 0.0), 1e-9);
    }

    @Test
    void fillRejectsADifferentDefinition() {
        LiquidTank tank = new LiquidTank(8_000, id -> PropertyMerge.WEIGHTED_AVERAGE);
        tank.fill(LiquidBatch.of(MUST, 1000, PropertyBag.empty()), false);
        int accepted = tank.fill(LiquidBatch.of(ResourceId.parse("test:other"), 500, PropertyBag.empty()), false);
        assertEquals(0, accepted);
        assertTrue(tank.contents().orElseThrow().baseLiquid().filter(MUST::equals).isPresent());
    }

    @Test
    void sealedTankRejectsFillAndDrain() {
        LiquidTank tank = LiquidTank.sealed();
        assertEquals(0, tank.fill(LiquidBatch.of(MUST, 500, PropertyBag.empty()), false));
        assertTrue(tank.contents().isEmpty());
        assertEquals(0.0, tank.drain(100, false).volume(), 1e-9);
        assertTrue(!tank.trySet(LiquidBatch.of(MUST, 100, PropertyBag.empty())));
    }

    @Test
    void trySetRefusesMoreThanCapacity() {
        LiquidTank tank = new LiquidTank(100, id -> PropertyMerge.WEIGHTED_AVERAGE);
        assertTrue(!tank.trySet(LiquidBatch.of(MUST, 500, PropertyBag.empty())));
        assertTrue(tank.contents().isEmpty());
        tank.set(LiquidBatch.of(MUST, 500, PropertyBag.empty()));
        assertEquals(500, tank.capacity());
        assertEquals(500.0, tank.contents().orElseThrow().volume(), 1e-9);
    }
}

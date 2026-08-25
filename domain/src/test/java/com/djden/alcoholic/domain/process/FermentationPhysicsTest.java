package com.djden.alcoholic.domain.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FermentationPhysicsTest {
    private static final ResourceId SUGAR = ResourceId.parse("alcoholic:sugar");
    private static final ResourceId ETHANOL = ResourceId.parse("alcoholic:ethanol");
    private static final ResourceId STRESS = ResourceId.parse("alcoholic:fermentation_stress");
    private static final ResourceId MUST = ResourceId.parse("test:must");
    private static final ResourceId YOUNG = ResourceId.parse("test:young");

    @Test
    void convertsSugarToEthanolUntilComplete() {
        LiquidBatch start = LiquidBatch.of(
                MUST,
                1000,
                PropertyBag.empty().with(SUGAR, 1.0).with(ETHANOL, 0.0)
        );
        FermentationState state = new FermentationState(start, true, false, 0.0, 0.0);
        FermentationKinetics kinetics = new FermentationKinetics(0.5, 0.1, 0.02, 0.4);
        TemperatureProfile profile = TemperatureProfile.fermentationDefault();

        FermentationState mid = FermentationPhysics.step(
                state, kinetics, profile, SUGAR, ETHANOL, STRESS, YOUNG, 20.0, 1.0
        );
        assertTrue(mid.batch().number(SUGAR, 1.0) < 1.0);
        assertTrue(mid.batch().number(ETHANOL, 0.0) > 0.0);
        assertFalse(mid.complete());

        FermentationState done = mid;
        for (int tick = 0; tick < 40; tick++) {
            done = FermentationPhysics.step(
                    done, kinetics, profile, SUGAR, ETHANOL, STRESS, YOUNG, 20.0, 1.0
            );
        }
        assertTrue(done.complete());
        assertEquals(0.0, done.batch().number(SUGAR, 1.0), 1e-9);
        assertEquals(YOUNG, done.batch().baseLiquid().orElseThrow());
    }

    @Test
    void temperatureOutsidePreferredRangeSlowsConversion() {
        LiquidBatch start = LiquidBatch.of(
                MUST,
                1000,
                PropertyBag.empty().with(SUGAR, 1.0).with(ETHANOL, 0.0)
        );
        FermentationState state = new FermentationState(start, true, false, 0.0, 0.0);
        FermentationKinetics kinetics = new FermentationKinetics(0.5, 0.1, 0.02, 0.4);
        TemperatureProfile profile = TemperatureProfile.fermentationDefault();

        FermentationState preferred = FermentationPhysics.step(
                state, kinetics, profile, SUGAR, ETHANOL, STRESS, YOUNG, 20.0, 1.0
        );
        FermentationState cool = FermentationPhysics.step(
                state, kinetics, profile, SUGAR, ETHANOL, STRESS, YOUNG, 12.0, 1.0
        );
        FermentationState stalled = FermentationPhysics.step(
                state, kinetics, profile, SUGAR, ETHANOL, STRESS, YOUNG, 4.0, 1.0
        );

        assertTrue(preferred.batch().number(SUGAR, 1.0) < cool.batch().number(SUGAR, 1.0));
        assertEquals(1.0, stalled.batch().number(SUGAR, 1.0), 1e-9);
        assertTrue(cool.stress() > preferred.stress());
    }
}

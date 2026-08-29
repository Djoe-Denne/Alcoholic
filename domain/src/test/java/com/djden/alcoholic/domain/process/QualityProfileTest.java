package com.djden.alcoholic.domain.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ExecutorModifiers;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QualityProfileTest {
    private static final ResourceId MUST = ResourceId.parse("alcoholic:red_grape_must");

    @Test
    void stampCapWritesComplexityCapAndPurityFloor() {
        LiquidBatch stamped = QualityProfile.stampCap(
                batch(0.90, 0.20, 0.12, 0.0),
                ExecutorModifiers.industrialPress()
        );
        assertEquals(0.55, stamped.number(QualityProfile.COMPLEXITY_CAP, 1.0), 1e-9);
        assertEquals(0.15, stamped.number(QualityProfile.PURITY_FLOOR, 0.0), 1e-9);
    }

    @Test
    void stampCapTightensExistingCap() {
        LiquidBatch alreadyCapped = batch(0.90, 0.20, 0.12, 0.0)
                .withProperty(QualityProfile.COMPLEXITY_CAP, 0.40);
        LiquidBatch stamped = QualityProfile.stampCap(alreadyCapped, ExecutorModifiers.industrialPress());
        assertEquals(0.40, stamped.number(QualityProfile.COMPLEXITY_CAP, 1.0), 1e-9);
        assertTrue(stamped.number(QualityProfile.PURITY_FLOOR, 0.0) >= 0.15 - 1e-9);
    }

    private static LiquidBatch batch(double harvest, double acid, double ethanol, double maturity) {
        return LiquidBatch.of(
                MUST,
                1000,
                PropertyBag.empty()
                        .with(QualityProfile.HARVEST_QUALITY, harvest)
                        .with(QualityProfile.ACIDITY, acid)
                        .with(QualityProfile.SUGAR, 0.50)
                        .with(QualityProfile.ETHANOL, ethanol)
                        .with(QualityProfile.MATURITY, maturity)
        );
    }
}

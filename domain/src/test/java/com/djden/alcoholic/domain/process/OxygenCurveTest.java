package com.djden.alcoholic.domain.process;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OxygenCurveTest {
    @Test
    void noAgingHasNoOxygenEffect() {
        OxygenCurve.Evaluation evaluation = OxygenCurve.evaluate(0.0, 0.0);
        assertEquals(0.0, evaluation.defects(), 1e-9);
        assertEquals(0.0, evaluation.complexityBonus(), 1e-9);
    }

    @Test
    void veryLowExposureWhileAgingIsReductive() {
        OxygenCurve.Evaluation evaluation = OxygenCurve.evaluate(0.01, 1_000.0);
        assertTrue(evaluation.defects() > 0.0);
        assertEquals(0.0, evaluation.complexityBonus(), 1e-9);
    }

    @Test
    void midBandIsMicroOxygenation() {
        OxygenCurve.Evaluation evaluation = OxygenCurve.evaluate(0.18, 1_000.0);
        assertEquals(0.0, evaluation.defects(), 1e-9);
        assertTrue(evaluation.complexityBonus() > 0.05);
    }

    @Test
    void highExposureIsOxidative() {
        OxygenCurve.Evaluation evaluation = OxygenCurve.evaluate(0.80, 1_000.0);
        assertTrue(evaluation.defects() > 0.2);
        assertEquals(0.0, evaluation.complexityBonus(), 1e-9);
    }

    @Test
    void woodSweetSpotPeaksThenFalls() {
        assertTrue(OxygenCurve.woodSweetSpot(0.40) > OxygenCurve.woodSweetSpot(0.10));
        assertTrue(OxygenCurve.woodSweetSpot(0.40) > OxygenCurve.woodSweetSpot(0.95));
    }
}

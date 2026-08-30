package com.djden.alcoholic.api.quality;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QualitySignalTest {
    @Test
    void clampsNaNAndInfinityToZero() {
        QualitySignal signal = new QualitySignal(Map.of(
                "nan", Double.NaN,
                "pos", Double.POSITIVE_INFINITY,
                "neg", Double.NEGATIVE_INFINITY,
                "ok", 0.4
        ));
        assertEquals(0.0, signal.get("nan", 1.0), 1e-9);
        assertEquals(0.0, signal.get("pos", 1.0), 1e-9);
        assertEquals(0.0, signal.get("neg", 1.0), 1e-9);
        assertEquals(0.4, signal.get("ok", 0.0), 1e-9);
    }

    @Test
    void missingPortReturnsFallback() {
        assertEquals(0.25, QualitySignal.empty().get("value", 0.25), 1e-9);
    }

    @Test
    void getPortOrValueFallsBackToValue() {
        QualitySignal signal = QualitySignal.value(0.40);
        assertEquals(0.40, signal.getPortOrValue("complexity", 0.0), 1e-9);
        assertEquals(0.12, QualitySignal.empty().getPortOrValue("complexity", 0.12), 1e-9);
        assertEquals(0.80, QualitySignal.of("complexity", 0.80).getPortOrValue("complexity", 0.0), 1e-9);
    }
}

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
}

package com.djden.alcoholic.api.quality;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.liquid.LiquidBatchView;
import com.djden.alcoholic.api.process.ExecutorModifiers;

/**
 * Batch chemistry plus already-resolved upstream signals for one operator.
 */
@PublicApi
public interface QualityEvaluationContext {
    LiquidBatchView batch();

    ExecutorModifiers modifiers();

    QualitySignal inputSignal(String port);

    default double input(String port, double fallback) {
        QualitySignal signal = inputSignal(port);
        if (signal.ports().containsKey(port)) {
            return signal.get(port, fallback);
        }
        if (signal.ports().containsKey("value")) {
            return signal.get("value", fallback);
        }
        if (signal.ports().containsKey("complexity")) {
            return signal.get("complexity", fallback);
        }
        return fallback;
    }
}

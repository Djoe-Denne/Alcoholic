package com.djden.alcoholic.domain.process;

/**
 * Elapsed-time maturation without wood. Useful after FERMENT when AGE
 * would over-specify vessel extractives.
 */
public record ConditionKinetics(
        double maturityPerTick,
        double carbonationFromSugar,
        double completionThreshold
) {
    public ConditionKinetics {
        if (!Double.isFinite(maturityPerTick) || maturityPerTick <= 0.0) {
            maturityPerTick = 1.0 / 200.0;
        }
        if (!Double.isFinite(carbonationFromSugar) || carbonationFromSugar < 0.0) {
            carbonationFromSugar = 0.0;
        }
        if (!Double.isFinite(completionThreshold) || completionThreshold <= 0.0) {
            completionThreshold = 1.0;
        }
        completionThreshold = Math.min(1.0, completionThreshold);
    }

    public static ConditionKinetics simplified() {
        return new ConditionKinetics(1.0 / 200.0, 0.35, 0.85);
    }
}

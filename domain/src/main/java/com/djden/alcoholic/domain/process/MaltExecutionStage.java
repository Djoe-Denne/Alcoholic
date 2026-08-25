package com.djden.alcoholic.domain.process;

/**
 * Internal malting phases. These are executor stages, not public process types.
 */
public enum MaltExecutionStage {
    STEEPING(0.25),
    GERMINATION(0.45),
    KILNING(0.30);

    private final double weight;

    MaltExecutionStage(double weight) {
        this.weight = weight;
    }

    public double weight() {
        return weight;
    }

    public boolean requiresMoisture() {
        return this != KILNING;
    }

    public boolean requiresKilnHeat() {
        return this == KILNING;
    }

    public static MaltExecutionStage at(double progress) {
        if (!Double.isFinite(progress) || progress < 0.0) {
            return STEEPING;
        }
        double remaining = Math.min(1.0, progress);
        for (MaltExecutionStage stage : values()) {
            if (remaining < stage.weight - 1e-9) {
                return stage;
            }
            remaining -= stage.weight;
        }
        return KILNING;
    }
}

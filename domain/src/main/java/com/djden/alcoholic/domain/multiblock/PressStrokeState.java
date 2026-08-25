package com.djden.alcoholic.domain.multiblock;

/**
 * Visual / mechanical stroke. Lethal occupancy is only checked while compressing.
 */
public enum PressStrokeState {
    IDLE,
    LOADING,
    COMPRESSING,
    HOLDING,
    RETRACTING;

    public boolean crushActive() {
        return this == COMPRESSING;
    }

    public static PressStrokeState fromProgress(boolean working, double cycle) {
        if (!working) {
            return IDLE;
        }
        double clamped = Math.min(1.0, Math.max(0.0, cycle));
        if (clamped < 0.15) {
            return LOADING;
        }
        if (clamped < 0.55) {
            return COMPRESSING;
        }
        if (clamped < 0.70) {
            return HOLDING;
        }
        return RETRACTING;
    }
}

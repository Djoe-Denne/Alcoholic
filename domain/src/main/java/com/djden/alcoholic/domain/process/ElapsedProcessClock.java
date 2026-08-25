package com.djden.alcoholic.domain.process;

/**
 * Converts persisted timestamps into a bounded simulation delta.
 */
public final class ElapsedProcessClock {
    public static final long DEFAULT_MAX_DELTA_TICKS = 24_000L;

    private ElapsedProcessClock() {
    }

    public static double deltaTicks(long lastProcessedGameTime, long currentGameTime) {
        return deltaTicks(lastProcessedGameTime, currentGameTime, DEFAULT_MAX_DELTA_TICKS);
    }

    public static double deltaTicks(long lastProcessedGameTime, long currentGameTime, long maxDelta) {
        if (lastProcessedGameTime <= 0L || currentGameTime <= lastProcessedGameTime) {
            return 0.0;
        }
        long raw = currentGameTime - lastProcessedGameTime;
        return (double) Math.min(raw, Math.max(1L, maxDelta));
    }
}

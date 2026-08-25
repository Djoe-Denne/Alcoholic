package com.djden.alcoholic.application.machine;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cheap instrumentation for validation and controller ticks. Not a formal benchmark.
 */
public final class MultiblockProfiler {
    public static final MultiblockProfiler SHARED = new MultiblockProfiler();

    private final AtomicInteger validations = new AtomicInteger();
    private final AtomicLong validationNanos = new AtomicLong();
    private final AtomicInteger ticks = new AtomicInteger();
    private final AtomicLong tickNanos = new AtomicLong();

    public void recordValidation(long nanos) {
        validations.incrementAndGet();
        validationNanos.addAndGet(Math.max(0L, nanos));
    }

    public void recordTick(long nanos) {
        ticks.incrementAndGet();
        tickNanos.addAndGet(Math.max(0L, nanos));
    }

    public int validations() {
        return validations.get();
    }

    public int ticks() {
        return ticks.get();
    }

    public double averageValidationMicros() {
        int count = validations.get();
        return count == 0 ? 0.0 : (validationNanos.get() / 1_000.0) / count;
    }

    public double averageTickMicros() {
        int count = ticks.get();
        return count == 0 ? 0.0 : (tickNanos.get() / 1_000.0) / count;
    }

    public String summarize() {
        return "validations=" + validations()
                + " avgValidationUs=" + String.format(java.util.Locale.ROOT, "%.1f", averageValidationMicros())
                + " ticks=" + ticks()
                + " avgTickUs=" + String.format(java.util.Locale.ROOT, "%.1f", averageTickMicros());
    }

    public void reset() {
        validations.set(0);
        validationNanos.set(0);
        ticks.set(0);
        tickNanos.set(0);
    }
}

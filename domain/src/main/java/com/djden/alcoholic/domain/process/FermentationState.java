package com.djden.alcoholic.domain.process;

import com.djden.alcoholic.domain.liquid.LiquidBatch;

import java.util.Objects;

public record FermentationState(
        LiquidBatch batch,
        boolean yeastPitched,
        boolean complete,
        double co2Accumulated,
        double stress
) {
    public FermentationState {
        Objects.requireNonNull(batch, "batch");
        if (!Double.isFinite(co2Accumulated) || co2Accumulated < 0.0) {
            throw new IllegalArgumentException("co2Accumulated must be >= 0");
        }
        if (!Double.isFinite(stress) || stress < 0.0) {
            throw new IllegalArgumentException("stress must be >= 0");
        }
    }

    public FermentationState ventCo2() {
        return new FermentationState(batch, yeastPitched, complete, 0.0, stress);
    }

    public FermentationState withBatch(LiquidBatch updated) {
        return new FermentationState(updated, yeastPitched, complete, co2Accumulated, stress);
    }
}

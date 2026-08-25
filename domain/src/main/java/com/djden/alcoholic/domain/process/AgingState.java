package com.djden.alcoholic.domain.process;

import com.djden.alcoholic.domain.liquid.LiquidBatch;

import java.util.Objects;

public record AgingState(LiquidBatch batch, boolean complete) {
    public AgingState {
        Objects.requireNonNull(batch, "batch");
    }
}

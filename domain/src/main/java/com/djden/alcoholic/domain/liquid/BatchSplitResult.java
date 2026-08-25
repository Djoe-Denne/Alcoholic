package com.djden.alcoholic.domain.liquid;

import java.util.Objects;

public record BatchSplitResult(LiquidBatch extracted, LiquidBatch remaining) {
    public BatchSplitResult {
        Objects.requireNonNull(extracted, "extracted");
        Objects.requireNonNull(remaining, "remaining");
    }
}

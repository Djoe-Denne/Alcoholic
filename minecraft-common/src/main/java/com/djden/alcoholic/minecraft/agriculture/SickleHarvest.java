package com.djden.alcoholic.minecraft.agriculture;

import java.util.function.IntUnaryOperator;

/**
 * Shared sickle yield math. Vanilla uniform Fortune: {@code base + 0..fortune}.
 */
public final class SickleHarvest {
    private SickleHarvest() {
    }

    public static int fortuneAdjustedCount(int base, int fortune, IntUnaryOperator nextIntExclusive) {
        int count = Math.max(1, base);
        if (fortune <= 0) {
            return count;
        }
        return count + nextIntExclusive.applyAsInt(fortune + 1);
    }
}

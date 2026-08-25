package com.djden.alcoholic.domain.multiblock;

/**
 * Non-destructive resize rule: a smaller interior may not silently delete liquid.
 */
public final class ResizePolicy {
    private ResizePolicy() {
    }

    public static boolean accepts(int storedMillibuckets, int newCapacityMillibuckets) {
        return storedMillibuckets <= Math.max(0, newCapacityMillibuckets);
    }
}

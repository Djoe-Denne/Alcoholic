package com.djden.alcoholic.domain.multiblock;

/**
 * Capacity is interior cells times a datapack density. Shell blocks never count.
 */
public final class CapacityCalculator {
    private CapacityCalculator() {
    }

    public static int millibuckets(int interiorVolumeBlocks, int capacityPerInternalBlock) {
        if (interiorVolumeBlocks < 0 || capacityPerInternalBlock < 1) {
            return 0;
        }
        long product = (long) interiorVolumeBlocks * (long) capacityPerInternalBlock;
        return product > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) product;
    }
}

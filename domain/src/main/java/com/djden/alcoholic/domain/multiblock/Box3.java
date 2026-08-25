package com.djden.alcoholic.domain.multiblock;

/**
 * Continuous AABB used for crush occupancy. Not a Minecraft type.
 */
public record Box3(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
    public Box3 {
        if (maxX < minX || maxY < minY || maxZ < minZ) {
            throw new IllegalArgumentException("box max must be >= min");
        }
    }

    public boolean contains(double x, double y, double z) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public double volume() {
        return Math.max(0.0, maxX - minX) * Math.max(0.0, maxY - minY) * Math.max(0.0, maxZ - minZ);
    }

    public Box3 intersection(Box3 other) {
        double ix0 = Math.max(minX, other.minX);
        double iy0 = Math.max(minY, other.minY);
        double iz0 = Math.max(minZ, other.minZ);
        double ix1 = Math.min(maxX, other.maxX);
        double iy1 = Math.min(maxY, other.maxY);
        double iz1 = Math.min(maxZ, other.maxZ);
        if (ix1 <= ix0 || iy1 <= iy0 || iz1 <= iz0) {
            return new Box3(0, 0, 0, 0, 0, 0);
        }
        return new Box3(ix0, iy0, iz0, ix1, iy1, iz1);
    }

    public static Box3 fromInterior(AxisBox interiorCells, double inset) {
        return new Box3(
                interiorCells.minX() + inset,
                interiorCells.minY() + inset,
                interiorCells.minZ() + inset,
                interiorCells.maxX() + 1.0 - inset,
                interiorCells.maxY() + 1.0 - inset,
                interiorCells.maxZ() + 1.0 - inset
        );
    }
}

package com.djden.alcoholic.domain.multiblock;

import java.util.Objects;

/**
 * Inclusive integer bounding box in cell space.
 */
public record AxisBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    public AxisBox {
        if (maxX < minX || maxY < minY || maxZ < minZ) {
            throw new IllegalArgumentException("box max must be >= min");
        }
    }

    public static AxisBox of(CellCoord a, CellCoord b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        return new AxisBox(
                Math.min(a.x(), b.x()),
                Math.min(a.y(), b.y()),
                Math.min(a.z(), b.z()),
                Math.max(a.x(), b.x()),
                Math.max(a.y(), b.y()),
                Math.max(a.z(), b.z())
        );
    }

    public int width() {
        return maxX - minX + 1;
    }

    public int height() {
        return maxY - minY + 1;
    }

    public int depth() {
        return maxZ - minZ + 1;
    }

    public int volume() {
        return width() * height() * depth();
    }

    public boolean contains(CellCoord coord) {
        Objects.requireNonNull(coord, "coord");
        return coord.x() >= minX && coord.x() <= maxX
                && coord.y() >= minY && coord.y() <= maxY
                && coord.z() >= minZ && coord.z() <= maxZ;
    }

    public boolean onShell(CellCoord coord) {
        if (!contains(coord)) {
            return false;
        }
        return coord.x() == minX || coord.x() == maxX
                || coord.y() == minY || coord.y() == maxY
                || coord.z() == minZ || coord.z() == maxZ;
    }

    public boolean interior(CellCoord coord) {
        return contains(coord) && !onShell(coord);
    }

    public int interiorVolume() {
        int innerW = Math.max(0, width() - 2);
        int innerH = Math.max(0, height() - 2);
        int innerD = Math.max(0, depth() - 2);
        return innerW * innerH * innerD;
    }
}

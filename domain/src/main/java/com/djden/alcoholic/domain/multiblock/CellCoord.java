package com.djden.alcoholic.domain.multiblock;

/**
 * Integer cell coordinate. This is not a Minecraft {@code BlockPos}.
 */
public record CellCoord(int x, int y, int z) {
    public CellCoord offset(int dx, int dy, int dz) {
        return new CellCoord(x + dx, y + dy, z + dz);
    }

    public CellCoord relativeTo(CellCoord origin) {
        return new CellCoord(x - origin.x(), y - origin.y(), z - origin.z());
    }
}

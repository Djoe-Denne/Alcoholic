package com.djden.alcoholic.domain.multiblock;

/**
 * World sampling port. Implementations may use Minecraft types; callers in
 * domain and application must not.
 */
@FunctionalInterface
public interface StructureQuery {
    StructureCell cell(CellCoord coord);
}

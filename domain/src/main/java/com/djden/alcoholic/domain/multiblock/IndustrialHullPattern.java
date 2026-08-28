package com.djden.alcoholic.domain.multiblock;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Canonical formed hull used by debug placement and recipe-shaped showcases.
 * Useful face is −Z: item port, controller, hatch, fluid port, windows.
 */
public final class IndustrialHullPattern {
    private IndustrialHullPattern() {
    }

    public static CellCoord controller(int width, int height, int depth) {
        requireSize(width, height, depth);
        return new CellCoord(width / 2, 1, 0);
    }

    public static CellCoord kineticPort(int width, int height, int depth) {
        requireSize(width, height, depth);
        return new CellCoord(width - 1, 1, depth / 2);
    }

    public static Map<CellCoord, PartRole> shell(int width, int height, int depth, boolean kinetic) {
        requireSize(width, height, depth);
        Map<CellCoord, PartRole> cells = new LinkedHashMap<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (onShell(x, y, z, width, height, depth)) {
                        cells.put(new CellCoord(x, y, z), PartRole.CASING);
                    }
                }
            }
        }
        int midX = width / 2;
        cells.put(new CellCoord(midX, 0, 0), PartRole.ITEM_PORT);
        cells.put(controller(width, height, depth), PartRole.CONTROLLER);
        placeIfCasing(cells, new CellCoord(midX, Math.min(2, height - 2), 0), PartRole.HATCH);
        placeIfCasing(cells, new CellCoord(width - 1, fluidY(height), 0), PartRole.FLUID_PORT);
        placeIfCasing(cells, new CellCoord(0, height - 1, 0), PartRole.WINDOW);
        placeIfCasing(cells, new CellCoord(width - 1, height - 1, 0), PartRole.WINDOW);
        if (height >= 6) {
            placeIfCasing(cells, new CellCoord(midX, height - 1, 0), PartRole.WINDOW);
        }
        if (kinetic) {
            cells.put(kineticPort(width, height, depth), PartRole.KINETIC_PORT);
        }
        return Map.copyOf(cells);
    }

    public static StructureQuery query(
            int width,
            int height,
            int depth,
            boolean kinetic,
            String casingTag,
            String windowTag,
            String portTag,
            String controllerId
    ) {
        Objects.requireNonNull(casingTag, "casingTag");
        Objects.requireNonNull(windowTag, "windowTag");
        Objects.requireNonNull(portTag, "portTag");
        Objects.requireNonNull(controllerId, "controllerId");
        Map<CellCoord, PartRole> cells = shell(width, height, depth, kinetic);
        return coord -> cell(cells.get(coord), casingTag, windowTag, portTag, controllerId);
    }

    private static StructureCell cell(
            PartRole role,
            String casingTag,
            String windowTag,
            String portTag,
            String controllerId
    ) {
        if (role == null) {
            return StructureCell.air();
        }
        return switch (role) {
            case CONTROLLER -> StructureCell.structure(role, Set.of(casingTag), controllerId);
            case WINDOW -> StructureCell.structure(role, Set.of(windowTag), "alcoholic:machine_window");
            case ITEM_PORT -> StructureCell.structure(role, Set.of(portTag), "alcoholic:item_port");
            case FLUID_PORT -> StructureCell.structure(role, Set.of(portTag), "alcoholic:fluid_port");
            case KINETIC_PORT -> StructureCell.structure(role, Set.of(portTag), "alcoholic:kinetic_port");
            case HATCH -> StructureCell.structure(role, Set.of(casingTag), "alcoholic:access_hatch");
            case CASING -> StructureCell.structure(role, Set.of(casingTag), "alcoholic:industrial_casing");
        };
    }

    private static void placeIfCasing(Map<CellCoord, PartRole> cells, CellCoord coord, PartRole role) {
        if (cells.get(coord) == PartRole.CASING) {
            cells.put(coord, role);
        }
    }

    private static int fluidY(int height) {
        return Math.max(1, Math.min(height / 2, height - 2));
    }

    private static boolean onShell(int x, int y, int z, int width, int height, int depth) {
        return x == 0 || y == 0 || z == 0
                || x == width - 1 || y == height - 1 || z == depth - 1;
    }

    private static void requireSize(int width, int height, int depth) {
        if (width < 3 || height < 3 || depth < 3) {
            throw new IllegalArgumentException("hollow cuboid must be at least 3x3x3");
        }
    }
}

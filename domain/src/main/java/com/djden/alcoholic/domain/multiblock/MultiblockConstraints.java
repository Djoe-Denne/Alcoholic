package com.djden.alcoholic.domain.multiblock;

import java.util.Objects;
import java.util.Set;

/**
 * Data-driven limits for one hollow-cuboid machine family.
 */
public record MultiblockConstraints(
        int minWidth,
        int minHeight,
        int minDepth,
        int maxWidth,
        int maxHeight,
        int maxDepth,
        int requiredControllers,
        Set<String> casingTags,
        Set<String> windowTags,
        Set<String> portTags,
        Set<PartRole> requiredPorts,
        boolean requireHollowInterior
) {
    public MultiblockConstraints {
        if (minWidth < 3 || minHeight < 3 || minDepth < 3) {
            throw new IllegalArgumentException("hollow machines need at least 3 cells on each axis");
        }
        if (maxWidth < minWidth || maxHeight < minHeight || maxDepth < minDepth) {
            throw new IllegalArgumentException("max dimensions must be >= min");
        }
        if (requiredControllers < 1) {
            requiredControllers = 1;
        }
        casingTags = casingTags == null ? Set.of() : Set.copyOf(casingTags);
        windowTags = windowTags == null ? Set.of() : Set.copyOf(windowTags);
        portTags = portTags == null ? Set.of() : Set.copyOf(portTags);
        requiredPorts = requiredPorts == null ? Set.of() : Set.copyOf(requiredPorts);
    }

    public int maxCellBudget() {
        return maxWidth * maxHeight * maxDepth;
    }

    public boolean dimensionsAllowed(int width, int height, int depth) {
        return width >= minWidth && width <= maxWidth
                && height >= minHeight && height <= maxHeight
                && depth >= minDepth && depth <= maxDepth;
    }

    public boolean acceptsCasing(StructureCell cell) {
        Objects.requireNonNull(cell, "cell");
        return cell.role().orElse(null) == PartRole.CASING && intersects(cell.tags(), casingTags)
                || cell.role().orElse(null) == PartRole.HATCH && intersects(cell.tags(), casingTags);
    }

    public boolean acceptsWindow(StructureCell cell) {
        return cell.role().orElse(null) == PartRole.WINDOW && intersects(cell.tags(), windowTags);
    }

    public boolean acceptsPort(StructureCell cell) {
        return cell.role().filter(PartRole::isPort).isPresent() && intersects(cell.tags(), portTags);
    }

    public boolean acceptsShell(StructureCell cell) {
        if (cell.presence() != CellPresence.STRUCTURE) {
            return false;
        }
        PartRole role = cell.role().orElse(null);
        if (role == PartRole.CONTROLLER) {
            return true;
        }
        if (role == PartRole.WINDOW) {
            return acceptsWindow(cell);
        }
        if (role != null && role.isPort()) {
            return acceptsPort(cell);
        }
        return acceptsCasing(cell);
    }

    private static boolean intersects(Set<String> left, Set<String> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }
        for (String tag : left) {
            if (right.contains(tag)) {
                return true;
            }
        }
        return false;
    }
}

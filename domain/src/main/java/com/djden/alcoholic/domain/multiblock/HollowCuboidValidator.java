package com.djden.alcoholic.domain.multiblock;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Validates one connected hollow cuboid. Interior volume is the empty cells
 * strictly inside the bounding box, never the shell.
 */
public final class HollowCuboidValidator {
    private static final int[][] OFFSETS = {
            {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}
    };

    private HollowCuboidValidator() {
    }

    public static ValidationResult validate(
            MultiblockDefinition definition,
            CellCoord controller,
            StructureQuery query,
            int storedMillibuckets
    ) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(controller, "controller");
        Objects.requireNonNull(query, "query");
        Map<CellCoord, StructureCell> visited = new HashMap<>();
        ArrayDeque<CellCoord> queue = new ArrayDeque<>();
        StructureCell origin = query.cell(controller);
        if (origin.presence() == CellPresence.UNLOADED) {
            return ValidationResult.incomplete("controller chunk unloaded");
        }
        if (!isController(definition, origin)) {
            return ValidationResult.invalid("origin is not this machine's controller");
        }
        queue.add(controller);
        visited.put(controller, origin);
        int budget = definition.constraints().maxCellBudget();
        while (!queue.isEmpty()) {
            CellCoord current = queue.removeFirst();
            for (int[] offset : OFFSETS) {
                CellCoord next = current.offset(offset[0], offset[1], offset[2]);
                if (visited.containsKey(next)) {
                    continue;
                }
                StructureCell sample = query.cell(next);
                if (sample.presence() == CellPresence.UNLOADED) {
                    return ValidationResult.incomplete("structure spans an unloaded chunk");
                }
                if (sample.presence() != CellPresence.STRUCTURE
                        || !definition.constraints().acceptsShell(sample)) {
                    continue;
                }
                if (visited.size() >= budget) {
                    return ValidationResult.invalid("connected shell exceeds dimension budget");
                }
                visited.put(next, sample);
                queue.add(next);
            }
        }
        AxisBox bounds = boundsOf(visited.keySet());
        if (!definition.constraints().dimensionsAllowed(bounds.width(), bounds.height(), bounds.depth())) {
            return ValidationResult.invalid(
                    "dimensions " + bounds.width() + "x" + bounds.height() + "x" + bounds.depth()
                            + " outside allowed range"
            );
        }
        int controllers = 0;
        List<CellCoord> ports = new ArrayList<>();
        Set<PartRole> foundPorts = new HashSet<>();
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    CellCoord coord = new CellCoord(x, y, z);
                    boolean shell = bounds.onShell(coord);
                    StructureCell sample = visited.get(coord);
                    if (shell) {
                        if (sample == null) {
                            StructureCell world = query.cell(coord);
                            if (world.presence() == CellPresence.UNLOADED) {
                                return ValidationResult.incomplete("shell cell unloaded");
                            }
                            return ValidationResult.invalid("shell is not a closed connected frame");
                        }
                        if (!definition.constraints().acceptsShell(sample)) {
                            return ValidationResult.invalid("unsupported block on shell");
                        }
                        if (sample.role().orElse(null) == PartRole.CONTROLLER) {
                            if (!isController(definition, sample)) {
                                return ValidationResult.invalid("foreign controller on shell");
                            }
                            controllers++;
                        }
                        if (sample.role().filter(PartRole::isPort).isPresent()) {
                            ports.add(coord);
                            foundPorts.add(sample.role().orElseThrow());
                        }
                    } else if (definition.constraints().requireHollowInterior()) {
                        StructureCell interior = query.cell(coord);
                        if (interior.presence() == CellPresence.UNLOADED) {
                            return ValidationResult.incomplete("interior cell unloaded");
                        }
                        if (interior.presence() != CellPresence.AIR) {
                            return ValidationResult.invalid("interior is obstructed");
                        }
                    }
                }
            }
        }
        if (controllers != definition.constraints().requiredControllers()) {
            return ValidationResult.invalid(
                    "controller count " + controllers + " != "
                            + definition.constraints().requiredControllers()
            );
        }
        for (PartRole required : definition.constraints().requiredPorts()) {
            if (!foundPorts.contains(required)) {
                return ValidationResult.invalid("missing required port " + required);
            }
        }
        int interior = definition.constraints().requireHollowInterior()
                ? bounds.interiorVolume()
                : 0;
        int capacity = CapacityCalculator.millibuckets(
                interior,
                definition.capacityPerInternalBlock()
        );
        MultiblockGeometry geometry = new MultiblockGeometry(
                bounds,
                interior,
                capacity,
                ports,
                controller
        );
        if (!ResizePolicy.accepts(storedMillibuckets, capacity)) {
            return ValidationResult.overcapacity(
                    geometry,
                    "stored " + storedMillibuckets + " mB exceeds new capacity " + capacity
            );
        }
        return ValidationResult.formed(geometry);
    }

    private static boolean isController(MultiblockDefinition definition, StructureCell cell) {
        if (cell.role().orElse(null) != PartRole.CONTROLLER) {
            return false;
        }
        String expected = definition.controllerBlockId();
        return expected.isBlank() || cell.blockId().filter(expected::equals).isPresent();
    }

    private static AxisBox boundsOf(Set<CellCoord> cells) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (CellCoord cell : cells) {
            minX = Math.min(minX, cell.x());
            minY = Math.min(minY, cell.y());
            minZ = Math.min(minZ, cell.z());
            maxX = Math.max(maxX, cell.x());
            maxY = Math.max(maxY, cell.y());
            maxZ = Math.max(maxZ, cell.z());
        }
        return new AxisBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
}

package com.djden.alcoholic.application.machine;

import com.djden.alcoholic.domain.multiblock.CellCoord;
import com.djden.alcoholic.domain.multiblock.CellPresence;
import com.djden.alcoholic.domain.multiblock.HollowCuboidValidator;
import com.djden.alcoholic.domain.multiblock.PartRole;
import com.djden.alcoholic.domain.multiblock.StructureCell;
import com.djden.alcoholic.domain.multiblock.StructureQuery;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiblockProfilerTest {
    @Test
    void fiftyPassiveValidationsStayCheap() {
        MultiblockProfiler profiler = new MultiblockProfiler();
        StructureQuery query = cuboid(5, 8, 5);
        for (int index = 0; index < 50; index++) {
            long start = System.nanoTime();
            HollowCuboidValidator.validate(BuiltinMachines.industrialTank(), new CellCoord(0, 0, 0), query, 0);
            profiler.recordValidation(System.nanoTime() - start);
        }
        assertTrue(profiler.validations() == 50);
        assertTrue(
                profiler.averageValidationMicros() < 5_000.0,
                "50 tank validations averaged " + profiler.averageValidationMicros() + " us"
        );
    }

    private static StructureQuery cuboid(int width, int height, int depth) {
        Set<CellCoord> shell = new HashSet<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (x == 0 || y == 0 || z == 0 || x == width - 1 || y == height - 1 || z == depth - 1) {
                        shell.add(new CellCoord(x, y, z));
                    }
                }
            }
        }
        return coord -> {
            if (!shell.contains(coord)) {
                if (coord.x() > 0 && coord.x() < width - 1
                        && coord.y() > 0 && coord.y() < height - 1
                        && coord.z() > 0 && coord.z() < depth - 1) {
                    return StructureCell.air();
                }
                return new StructureCell(CellPresence.AIR, java.util.Optional.empty(), Set.of(), java.util.Optional.empty());
            }
            if (coord.x() == 0 && coord.y() == 0 && coord.z() == 0) {
                return StructureCell.structure(
                        PartRole.CONTROLLER,
                        Set.of(),
                        "alcoholic:industrial_tank_controller"
                );
            }
            return StructureCell.structure(
                    PartRole.CASING,
                    Set.of(BuiltinMachines.TANK_CASING),
                    "alcoholic:industrial_casing"
            );
        };
    }
}

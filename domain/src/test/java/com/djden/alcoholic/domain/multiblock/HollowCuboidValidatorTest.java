package com.djden.alcoholic.domain.multiblock;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ExecutorModifiers;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HollowCuboidValidatorTest {
    private static final String CASING = "alcoholic:industrial_tank_casing";
    private static final String PORTS = "alcoholic:industrial_ports";
    private static final String CONTROLLER = "alcoholic:industrial_tank_controller";

    @Test
    void minimumInteriorIsTwoBlocks() {
        MapQuery world = hollow(3, 4, 3, CASING, CONTROLLER);
        ValidationResult result = HollowCuboidValidator.validate(
                tank(),
                new CellCoord(1, 0, 1),
                world,
                0
        );
        assertTrue(result.formed(), result.reason());
        assertEquals(2, result.geometry().orElseThrow().interiorVolume());
        assertEquals(32_000, result.geometry().orElseThrow().capacityMillibuckets());
    }

    @Test
    void largerStructureScalesInteriorAndCapacity() {
        MapQuery world = hollow(5, 6, 5, CASING, CONTROLLER);
        ValidationResult result = HollowCuboidValidator.validate(
                tank(),
                new CellCoord(2, 0, 2),
                world,
                0
        );
        assertTrue(result.formed(), result.reason());
        assertEquals(36, result.geometry().orElseThrow().interiorVolume());
        assertEquals(36 * 16_000, result.geometry().orElseThrow().capacityMillibuckets());
    }

    @Test
    void invalidCasingRejected() {
        MapQuery world = hollow(3, 4, 3, "other:steel", CONTROLLER);
        ValidationResult result = HollowCuboidValidator.validate(
                tank(),
                new CellCoord(1, 0, 1),
                world,
                0
        );
        assertEquals(ValidationStatus.INVALID, result.status());
    }

    @Test
    void blockedInteriorRejected() {
        MapQuery world = hollow(3, 4, 3, CASING, CONTROLLER);
        world.put(new CellCoord(1, 1, 1), StructureCell.obstruction("minecraft:stone"));
        ValidationResult result = HollowCuboidValidator.validate(
                tank(),
                new CellCoord(1, 0, 1),
                world,
                0
        );
        assertEquals(ValidationStatus.INVALID, result.status());
        assertTrue(result.reason().contains("interior"));
    }

    @Test
    void portOnShellIsAccepted() {
        MapQuery world = hollow(3, 4, 3, CASING, CONTROLLER);
        world.put(
                new CellCoord(0, 1, 1),
                StructureCell.structure(PartRole.FLUID_PORT, Set.of(PORTS), "alcoholic:fluid_port")
        );
        ValidationResult result = HollowCuboidValidator.validate(
                tank(),
                new CellCoord(1, 0, 1),
                world,
                0
        );
        assertTrue(result.formed(), result.reason());
        assertEquals(1, result.geometry().orElseThrow().ports().size());
    }

    @Test
    void resizeDownWhileOverfilledStaysOvercapacity() {
        MapQuery small = hollow(3, 4, 3, CASING, CONTROLLER);
        ValidationResult result = HollowCuboidValidator.validate(tank(), new CellCoord(1, 0, 1), small, 40_000);
        assertEquals(ValidationStatus.OVERCAPACITY, result.status());
        assertFalse(result.formed());
        assertTrue(result.geometry().isPresent());
    }

    @Test
    void unloadedChunkIsIncompleteNotInvalid() {
        MapQuery world = hollow(3, 4, 3, CASING, CONTROLLER);
        world.put(new CellCoord(2, 0, 1), StructureCell.unloaded());
        ValidationResult result = HollowCuboidValidator.validate(tank(), new CellCoord(1, 0, 1), world, 0);
        assertEquals(ValidationStatus.INCOMPLETE, result.status());
    }

    @Test
    void requiredKineticPortMustBePresent() {
        MapQuery world = hollow(3, 4, 3, "alcoholic:pressure_safe_casing", "alcoholic:industrial_press_controller");
        MultiblockDefinition press = new MultiblockDefinition(
                ResourceId.parse("alcoholic:industrial_press"),
                MachineKind.PRESS,
                Optional.of(ResourceId.parse("alcoholic:press")),
                new MultiblockConstraints(
                        3, 4, 3, 7, 8, 7, 1,
                        Set.of("alcoholic:pressure_safe_casing"),
                        Set.of("alcoholic:valid_machine_windows"),
                        Set.of(PORTS),
                        Set.of(PartRole.KINETIC_PORT),
                        true
                ),
                4_000,
                ExecutorModifiers.identity(),
                KineticRequirement.industrialPress(),
                "alcoholic:industrial_press_controller"
        );
        ValidationResult missing = HollowCuboidValidator.validate(press, new CellCoord(1, 0, 1), world, 0);
        assertEquals(ValidationStatus.INVALID, missing.status());
        world.put(
                new CellCoord(0, 2, 1),
                StructureCell.structure(PartRole.KINETIC_PORT, Set.of(PORTS), "alcoholic:kinetic_port")
        );
        ValidationResult formed = HollowCuboidValidator.validate(press, new CellCoord(1, 0, 1), world, 0);
        assertTrue(formed.formed(), formed.reason());
    }

    @Test
    void shellBlocksAreNotCountedAsCapacity() {
        assertEquals(0, CapacityCalculator.millibuckets(0, 16_000));
        assertEquals(16_000, CapacityCalculator.millibuckets(1, 16_000));
        assertEquals(2, new AxisBox(0, 0, 0, 2, 3, 2).interiorVolume());
        assertEquals(27, new AxisBox(0, 0, 0, 4, 4, 4).interiorVolume());
    }

    @Test
    void resizePolicyNeverDeletesLiquid() {
        assertTrue(ResizePolicy.accepts(32_000, 32_000));
        assertFalse(ResizePolicy.accepts(32_001, 32_000));
    }

    private static MultiblockDefinition tank() {
        return new MultiblockDefinition(
                ResourceId.parse("alcoholic:industrial_storage_tank"),
                MachineKind.STORAGE,
                Optional.empty(),
                new MultiblockConstraints(
                        3, 4, 3, 9, 16, 9, 1,
                        Set.of(CASING),
                        Set.of("alcoholic:valid_machine_windows"),
                        Set.of(PORTS),
                        Set.of(),
                        true
                ),
                16_000,
                ExecutorModifiers.identity(),
                KineticRequirement.none(),
                CONTROLLER
        );
    }

    static MapQuery hollow(int width, int height, int depth, String casingTag, String controllerId) {
        MapQuery query = new MapQuery();
        CellCoord controller = new CellCoord(width / 2, 0, depth / 2);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    boolean shell = x == 0 || x == width - 1 || y == 0 || y == height - 1 || z == 0 || z == depth - 1;
                    CellCoord coord = new CellCoord(x, y, z);
                    if (!shell) {
                        query.put(coord, StructureCell.air());
                    } else if (coord.equals(controller)) {
                        query.put(coord, StructureCell.structure(PartRole.CONTROLLER, Set.of(casingTag), controllerId));
                    } else {
                        query.put(coord, StructureCell.structure(
                                PartRole.CASING,
                                Set.of(casingTag),
                                "alcoholic:industrial_casing"
                        ));
                    }
                }
            }
        }
        return query;
    }

    static final class MapQuery implements StructureQuery {
        private final Map<CellCoord, StructureCell> cells = new HashMap<>();

        void put(CellCoord coord, StructureCell cell) {
            cells.put(coord, cell);
        }

        @Override
        public StructureCell cell(CellCoord coord) {
            return cells.getOrDefault(coord, StructureCell.air());
        }
    }
}

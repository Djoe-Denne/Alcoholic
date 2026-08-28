package com.djden.alcoholic.domain.multiblock;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ExecutorModifiers;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndustrialHullPatternTest {
    private static final String CASING = "alcoholic:fermenter_casing";
    private static final String WINDOWS = "alcoholic:valid_machine_windows";
    private static final String PORTS = "alcoholic:industrial_ports";

    @Test
    void controllerSitsOnUsefulFaceNotOriginCorner() {
        CellCoord controller = IndustrialHullPattern.controller(3, 4, 3);
        assertEquals(0, controller.z());
        assertNotEquals(new CellCoord(0, 0, 0), controller);
        assertEquals(new CellCoord(1, 1, 0), controller);
    }

    @Test
    void minHullMatchesRecipeRoles() {
        Map<CellCoord, PartRole> shell = IndustrialHullPattern.shell(3, 4, 3, false);
        assertEquals(PartRole.ITEM_PORT, shell.get(new CellCoord(1, 0, 0)));
        assertEquals(PartRole.CONTROLLER, shell.get(new CellCoord(1, 1, 0)));
        assertEquals(PartRole.HATCH, shell.get(new CellCoord(1, 2, 0)));
        assertEquals(PartRole.FLUID_PORT, shell.get(new CellCoord(2, 2, 0)));
        assertEquals(PartRole.WINDOW, shell.get(new CellCoord(0, 3, 0)));
        assertEquals(PartRole.WINDOW, shell.get(new CellCoord(2, 3, 0)));
        assertEquals(PartRole.CASING, shell.get(new CellCoord(0, 0, 0)));
    }

    @Test
    void kineticPortSitsOnPlusXNotUsefulFace() {
        CellCoord kinetic = IndustrialHullPattern.kineticPort(3, 4, 3);
        assertEquals(2, kinetic.x());
        assertEquals(1, kinetic.z());
        assertEquals(PartRole.KINETIC_PORT, IndustrialHullPattern.shell(3, 4, 3, true).get(kinetic));
    }

    @Test
    void specialsDoNotShareCells() {
        Map<CellCoord, PartRole> shell = IndustrialHullPattern.shell(5, 6, 5, true);
        Set<PartRole> seen = new HashSet<>();
        int specials = 0;
        for (Map.Entry<CellCoord, PartRole> entry : shell.entrySet()) {
            if (entry.getValue() == PartRole.CASING) {
                continue;
            }
            specials++;
            assertTrue(seen.add(entry.getValue()) || entry.getValue() == PartRole.WINDOW, entry.toString());
        }
        assertTrue(specials >= 6);
    }

    @Test
    void recipeHullFormsMinimumTank() {
        ValidationResult result = HollowCuboidValidator.validate(
                tank(),
                IndustrialHullPattern.controller(3, 4, 3),
                IndustrialHullPattern.query(3, 4, 3, false, CASING, WINDOWS, PORTS, tank().controllerBlockId()),
                0
        );
        assertTrue(result.formed(), result.reason());
    }

    @Test
    void recipeHullFormsKineticPress() {
        MultiblockDefinition press = press();
        ValidationResult result = HollowCuboidValidator.validate(
                press,
                IndustrialHullPattern.controller(3, 4, 3),
                IndustrialHullPattern.query(
                        3,
                        4,
                        3,
                        true,
                        "alcoholic:pressure_safe_casing",
                        WINDOWS,
                        PORTS,
                        press.controllerBlockId()
                ),
                0
        );
        assertTrue(result.formed(), result.reason());
    }

    private static MultiblockDefinition tank() {
        return new MultiblockDefinition(
                ResourceId.parse("alcoholic:industrial_storage_tank"),
                MachineKind.STORAGE,
                Optional.empty(),
                new MultiblockConstraints(
                        3, 4, 3, 9, 16, 9, 1,
                        Set.of(CASING),
                        Set.of(WINDOWS),
                        Set.of(PORTS),
                        Set.of(),
                        true
                ),
                16_000,
                ExecutorModifiers.identity(),
                KineticRequirement.none(),
                "alcoholic:industrial_tank_controller"
        );
    }

    private static MultiblockDefinition press() {
        return new MultiblockDefinition(
                ResourceId.parse("alcoholic:industrial_press"),
                MachineKind.PRESS,
                Optional.of(ResourceId.parse("alcoholic:press")),
                new MultiblockConstraints(
                        3, 4, 3, 7, 8, 7, 1,
                        Set.of("alcoholic:pressure_safe_casing"),
                        Set.of(WINDOWS),
                        Set.of(PORTS),
                        Set.of(PartRole.KINETIC_PORT),
                        true
                ),
                4_000,
                ExecutorModifiers.identity(),
                KineticRequirement.industrialPress(),
                "alcoholic:industrial_press_controller"
        );
    }
}

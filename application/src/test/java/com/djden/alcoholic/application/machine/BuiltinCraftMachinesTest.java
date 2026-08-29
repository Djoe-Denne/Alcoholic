package com.djden.alcoholic.application.machine;

import com.djden.alcoholic.domain.multiblock.HollowCuboidValidator;
import com.djden.alcoholic.domain.multiblock.IndustrialHullPattern;
import com.djden.alcoholic.domain.multiblock.MachineKind;
import com.djden.alcoholic.domain.multiblock.MachineScale;
import com.djden.alcoholic.domain.multiblock.MultiblockDefinition;
import com.djden.alcoholic.domain.multiblock.PartRole;
import com.djden.alcoholic.domain.multiblock.ValidationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuiltinCraftMachinesTest {
    @Test
    void beerLineFamiliesStayOnTheSameProcessIds() {
        assertEquals(MachineKind.MALT, BuiltinCraftMachines.craftMaltHouse().kind());
        assertEquals(MachineKind.MILL, BuiltinCraftMachines.craftMill().kind());
        assertEquals(MachineKind.MASH, BuiltinCraftMachines.craftMashTun().kind());
        assertEquals(MachineKind.BOIL, BuiltinCraftMachines.craftBrewingKettle().kind());
        assertEquals(MachineKind.FERMENT, BuiltinCraftMachines.craftVat().kind());
        assertEquals("alcoholic:malt", BuiltinCraftMachines.craftMaltHouse().processType().orElseThrow().toString());
        assertEquals("alcoholic:mill", BuiltinCraftMachines.craftMill().processType().orElseThrow().toString());
        assertEquals("alcoholic:mash", BuiltinCraftMachines.craftMashTun().processType().orElseThrow().toString());
        assertEquals("alcoholic:boil", BuiltinCraftMachines.craftBrewingKettle().processType().orElseThrow().toString());
        assertEquals("alcoholic:ferment", BuiltinCraftMachines.craftVat().processType().orElseThrow().toString());
        assertEquals(MachineScale.CRAFT, BuiltinCraftMachines.craftMashTun().scale());
        assertTrue(BuiltinCraftMachines.craftMill().kinetic().required());
        assertFalse(BuiltinCraftMachines.craftMashTun().kinetic().required());
    }

    @Test
    void minAndMaxHollowHullsForm() {
        assertTrue(forms(BuiltinCraftMachines.craftMashTun(), 3, 3, 3).formed());
        assertTrue(forms(BuiltinCraftMachines.craftMashTun(), 5, 5, 5).formed());
        assertTrue(forms(BuiltinCraftMachines.craftMill(), 3, 3, 3).formed());
        assertFalse(forms(BuiltinCraftMachines.craftMashTun(), 3, 6, 3).formed());
    }

    private static ValidationResult forms(MultiblockDefinition definition, int width, int height, int depth) {
        boolean kinetic = definition.constraints().requiredPorts().contains(PartRole.KINETIC_PORT);
        return HollowCuboidValidator.validate(
                definition,
                IndustrialHullPattern.controller(width, height, depth),
                IndustrialHullPattern.query(
                        width,
                        height,
                        depth,
                        kinetic,
                        BuiltinCraftMachines.CRAFT_CASING,
                        BuiltinMachines.WINDOWS,
                        BuiltinMachines.PORTS,
                        definition.controllerBlockId()
                ),
                0
        );
    }
}

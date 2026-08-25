package com.djden.alcoholic.application.machine;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ExecutorModifiers;
import com.djden.alcoholic.domain.multiblock.KineticRequirement;
import com.djden.alcoholic.domain.multiblock.MachineKind;
import com.djden.alcoholic.domain.multiblock.MultiblockConstraints;
import com.djden.alcoholic.domain.multiblock.MultiblockDefinition;
import com.djden.alcoholic.domain.multiblock.PartRole;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Shipped hollow-cuboid families. Parameters stay data-shaped so a datapack
 * can replace them without a Java fork.
 */
public final class BuiltinMachines {
    public static final ResourceId INDUSTRIAL_PRESS = ResourceId.parse("alcoholic:industrial_press");
    public static final ResourceId INDUSTRIAL_VAT = ResourceId.parse("alcoholic:industrial_fermentation_vat");
    public static final ResourceId INDUSTRIAL_TANK = ResourceId.parse("alcoholic:industrial_storage_tank");

    public static final String TANK_CASING = "alcoholic:industrial_tank_casing";
    public static final String FERMENTER_CASING = "alcoholic:fermenter_casing";
    public static final String PRESSURE_CASING = "alcoholic:pressure_safe_casing";
    public static final String WINDOWS = "alcoholic:valid_machine_windows";
    public static final String PORTS = "alcoholic:industrial_ports";

    private BuiltinMachines() {
    }

    public static Map<ResourceId, MultiblockDefinition> all() {
        Map<ResourceId, MultiblockDefinition> machines = new LinkedHashMap<>();
        machines.put(INDUSTRIAL_PRESS, industrialPress());
        machines.put(INDUSTRIAL_VAT, industrialVat());
        machines.put(INDUSTRIAL_TANK, industrialTank());
        return Map.copyOf(machines);
    }

    public static MultiblockDefinition industrialPress() {
        return new MultiblockDefinition(
                INDUSTRIAL_PRESS,
                MachineKind.PRESS,
                Optional.of(ResourceId.parse("alcoholic:press")),
                new MultiblockConstraints(
                        3, 4, 3,
                        7, 8, 7,
                        1,
                        Set.of(PRESSURE_CASING),
                        Set.of(WINDOWS),
                        Set.of(PORTS),
                        Set.of(PartRole.KINETIC_PORT),
                        true
                ),
                4_000,
                ExecutorModifiers.industrialPress(),
                KineticRequirement.industrialPress(),
                "alcoholic:industrial_press_controller"
        );
    }

    public static MultiblockDefinition industrialVat() {
        return new MultiblockDefinition(
                INDUSTRIAL_VAT,
                MachineKind.FERMENT,
                Optional.of(ResourceId.parse("alcoholic:ferment")),
                new MultiblockConstraints(
                        3, 4, 3,
                        9, 16, 9,
                        1,
                        Set.of(FERMENTER_CASING),
                        Set.of(WINDOWS),
                        Set.of(PORTS),
                        Set.of(),
                        true
                ),
                8_000,
                ExecutorModifiers.industrialVat(),
                KineticRequirement.none(),
                "alcoholic:industrial_vat_controller"
        );
    }

    public static MultiblockDefinition industrialTank() {
        return new MultiblockDefinition(
                INDUSTRIAL_TANK,
                MachineKind.STORAGE,
                Optional.empty(),
                new MultiblockConstraints(
                        3, 4, 3,
                        9, 16, 9,
                        1,
                        Set.of(TANK_CASING),
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
}

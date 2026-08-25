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
    public static final ResourceId INDUSTRIAL_MALT_HOUSE = ResourceId.parse("alcoholic:industrial_malt_house");
    public static final ResourceId INDUSTRIAL_ROLLER_MILL = ResourceId.parse("alcoholic:industrial_roller_mill");
    public static final ResourceId INDUSTRIAL_MASH_TUN = ResourceId.parse("alcoholic:industrial_mash_tun");
    public static final ResourceId INDUSTRIAL_BREWING_KETTLE = ResourceId.parse("alcoholic:industrial_brewing_kettle");
    public static final ResourceId INDUSTRIAL_CONDITIONING_VESSEL =
            ResourceId.parse("alcoholic:industrial_conditioning_vessel");

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
        machines.put(INDUSTRIAL_MALT_HOUSE, industrialMaltHouse());
        machines.put(INDUSTRIAL_ROLLER_MILL, industrialRollerMill());
        machines.put(INDUSTRIAL_MASH_TUN, industrialMashTun());
        machines.put(INDUSTRIAL_BREWING_KETTLE, industrialBrewingKettle());
        machines.put(INDUSTRIAL_CONDITIONING_VESSEL, industrialConditioningVessel());
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

    public static MultiblockDefinition industrialMaltHouse() {
        return new MultiblockDefinition(
                INDUSTRIAL_MALT_HOUSE,
                MachineKind.MALT,
                Optional.of(ResourceId.parse("alcoholic:malt")),
                new MultiblockConstraints(
                        3, 4, 3,
                        7, 8, 7,
                        1,
                        Set.of(FERMENTER_CASING),
                        Set.of(WINDOWS),
                        Set.of(PORTS),
                        Set.of(),
                        true
                ),
                2_000,
                ExecutorModifiers.industrialMaltHouse(),
                KineticRequirement.none(),
                "alcoholic:industrial_malt_house_controller"
        );
    }

    public static MultiblockDefinition industrialRollerMill() {
        return new MultiblockDefinition(
                INDUSTRIAL_ROLLER_MILL,
                MachineKind.MILL,
                Optional.of(ResourceId.parse("alcoholic:mill")),
                new MultiblockConstraints(
                        3, 4, 3,
                        5, 6, 5,
                        1,
                        Set.of(PRESSURE_CASING),
                        Set.of(WINDOWS),
                        Set.of(PORTS),
                        Set.of(PartRole.KINETIC_PORT),
                        true
                ),
                1_000,
                ExecutorModifiers.industrialRollerMill(),
                KineticRequirement.industrialRollerMill(),
                "alcoholic:industrial_roller_mill_controller"
        );
    }

    public static MultiblockDefinition industrialMashTun() {
        return new MultiblockDefinition(
                INDUSTRIAL_MASH_TUN,
                MachineKind.MASH,
                Optional.of(ResourceId.parse("alcoholic:mash")),
                new MultiblockConstraints(
                        3, 4, 3,
                        9, 12, 9,
                        1,
                        Set.of(FERMENTER_CASING),
                        Set.of(WINDOWS),
                        Set.of(PORTS),
                        Set.of(),
                        true
                ),
                8_000,
                ExecutorModifiers.industrialMashTun(),
                KineticRequirement.none(),
                "alcoholic:industrial_mash_tun_controller"
        );
    }

    public static MultiblockDefinition industrialBrewingKettle() {
        return new MultiblockDefinition(
                INDUSTRIAL_BREWING_KETTLE,
                MachineKind.BOIL,
                Optional.of(ResourceId.parse("alcoholic:boil")),
                new MultiblockConstraints(
                        3, 4, 3,
                        7, 8, 7,
                        1,
                        Set.of(PRESSURE_CASING),
                        Set.of(WINDOWS),
                        Set.of(PORTS),
                        Set.of(),
                        true
                ),
                6_000,
                ExecutorModifiers.industrialBrewingKettle(),
                KineticRequirement.none(),
                "alcoholic:industrial_brewing_kettle_controller"
        );
    }

    public static MultiblockDefinition industrialConditioningVessel() {
        return new MultiblockDefinition(
                INDUSTRIAL_CONDITIONING_VESSEL,
                MachineKind.CONDITION,
                Optional.of(ResourceId.parse("alcoholic:condition")),
                new MultiblockConstraints(
                        3, 4, 3,
                        7, 10, 7,
                        1,
                        Set.of(FERMENTER_CASING),
                        Set.of(WINDOWS),
                        Set.of(PORTS),
                        Set.of(),
                        true
                ),
                8_000,
                ExecutorModifiers.industrialConditioningVessel(),
                KineticRequirement.none(),
                "alcoholic:industrial_conditioning_vessel_controller"
        );
    }
}

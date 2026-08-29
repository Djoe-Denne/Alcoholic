package com.djden.alcoholic.application.machine;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ExecutorModifiers;
import com.djden.alcoholic.domain.multiblock.KineticRequirement;
import com.djden.alcoholic.domain.multiblock.MachineKind;
import com.djden.alcoholic.domain.multiblock.MachineScale;
import com.djden.alcoholic.domain.multiblock.MultiblockConstraints;
import com.djden.alcoholic.domain.multiblock.MultiblockDefinition;
import com.djden.alcoholic.domain.multiblock.PartRole;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Small hollow-cuboid beer-line families. Kept out of {@link BuiltinMachines}
 * so industrial FORMED progression coverage stays industrial-only.
 */
public final class BuiltinCraftMachines {
    public static final ResourceId CRAFT_MALT_HOUSE = ResourceId.parse("alcoholic:craft_malt_house");
    public static final ResourceId CRAFT_MILL = ResourceId.parse("alcoholic:craft_mill");
    public static final ResourceId CRAFT_MASH_TUN = ResourceId.parse("alcoholic:craft_mash_tun");
    public static final ResourceId CRAFT_BREWING_KETTLE = ResourceId.parse("alcoholic:craft_brewing_kettle");
    public static final ResourceId CRAFT_VAT = ResourceId.parse("alcoholic:craft_vat");

    public static final String CRAFT_CASING = "alcoholic:craft_casing";

    private BuiltinCraftMachines() {
    }

    public static Map<ResourceId, MultiblockDefinition> all() {
        Map<ResourceId, MultiblockDefinition> machines = new LinkedHashMap<>();
        machines.put(CRAFT_MALT_HOUSE, craftMaltHouse());
        machines.put(CRAFT_MILL, craftMill());
        machines.put(CRAFT_MASH_TUN, craftMashTun());
        machines.put(CRAFT_BREWING_KETTLE, craftBrewingKettle());
        machines.put(CRAFT_VAT, craftVat());
        return Map.copyOf(machines);
    }

    public static MultiblockDefinition craftMaltHouse() {
        return definition(
                CRAFT_MALT_HOUSE,
                MachineKind.MALT,
                Optional.of(ResourceId.parse("alcoholic:malt")),
                Set.of(),
                1_000,
                ExecutorModifiers.craftMaltHouse(),
                KineticRequirement.none(),
                "alcoholic:craft_malt_house_controller"
        );
    }

    public static MultiblockDefinition craftMill() {
        return definition(
                CRAFT_MILL,
                MachineKind.MILL,
                Optional.of(ResourceId.parse("alcoholic:mill")),
                Set.of(PartRole.KINETIC_PORT),
                1_000,
                ExecutorModifiers.craftMill(),
                KineticRequirement.craftMill(),
                "alcoholic:craft_mill_controller"
        );
    }

    public static MultiblockDefinition craftMashTun() {
        return definition(
                CRAFT_MASH_TUN,
                MachineKind.MASH,
                Optional.of(ResourceId.parse("alcoholic:mash")),
                Set.of(),
                2_000,
                ExecutorModifiers.craftMashTun(),
                KineticRequirement.none(),
                "alcoholic:craft_mash_tun_controller"
        );
    }

    public static MultiblockDefinition craftBrewingKettle() {
        return definition(
                CRAFT_BREWING_KETTLE,
                MachineKind.BOIL,
                Optional.of(ResourceId.parse("alcoholic:boil")),
                Set.of(),
                2_000,
                ExecutorModifiers.craftBrewingKettle(),
                KineticRequirement.none(),
                "alcoholic:craft_brewing_kettle_controller"
        );
    }

    public static MultiblockDefinition craftVat() {
        return definition(
                CRAFT_VAT,
                MachineKind.FERMENT,
                Optional.of(ResourceId.parse("alcoholic:ferment")),
                Set.of(),
                2_000,
                ExecutorModifiers.craftVat(),
                KineticRequirement.none(),
                "alcoholic:craft_vat_controller"
        );
    }

    private static MultiblockDefinition definition(
            ResourceId id,
            MachineKind kind,
            Optional<ResourceId> process,
            Set<PartRole> requiredPorts,
            int capacity,
            ExecutorModifiers modifiers,
            KineticRequirement kinetic,
            String controller
    ) {
        return new MultiblockDefinition(
                id,
                kind,
                process,
                new MultiblockConstraints(
                        3, 3, 3,
                        5, 5, 5,
                        1,
                        Set.of(CRAFT_CASING),
                        Set.of(BuiltinMachines.WINDOWS),
                        Set.of(BuiltinMachines.PORTS),
                        requiredPorts,
                        true
                ),
                capacity,
                modifiers,
                kinetic,
                controller,
                MachineScale.CRAFT
        );
    }
}

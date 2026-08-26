package com.djden.alcoholic.minecraft.menu;

import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.content.ContentRegistrationPorts;
import com.djden.alcoholic.platform.api.registry.RegistryRef;
import net.minecraft.world.inventory.MenuType;

import java.util.Objects;

public final class MachineMenuRegistrar {
    private MachineMenuRegistrar() {
    }

    public static MachineMenuContent register(ContentRegistrationPorts ports) {
        Objects.requireNonNull(ports, "ports");
        RegistryRef<MenuType<?>> twoSlots = ports.menus().register(
                AlcoholicIds.TWO_SLOTS_MENU,
                () -> new MenuType<>(MachineMenu::twoSlots)
        );
        RegistryRef<MenuType<?>> twoSlotsOneTank = ports.menus().register(
                AlcoholicIds.TWO_SLOTS_ONE_TANK_MENU,
                () -> new MenuType<>(MachineMenu::twoSlotsOneTank)
        );
        RegistryRef<MenuType<?>> twoSlotsTwoTanks = ports.menus().register(
                AlcoholicIds.TWO_SLOTS_TWO_TANKS_MENU,
                () -> new MenuType<>(MachineMenu::twoSlotsTwoTanks)
        );
        RegistryRef<MenuType<?>> oneSlotOneTank = ports.menus().register(
                AlcoholicIds.ONE_SLOT_ONE_TANK_MENU,
                () -> new MenuType<>(MachineMenu::oneSlotOneTank)
        );
        RegistryRef<MenuType<?>> oneTank = ports.menus().register(
                AlcoholicIds.ONE_TANK_MENU,
                () -> new MenuType<>(MachineMenu::oneTank)
        );
        RegistryRef<MenuType<?>> twoTanks = ports.menus().register(
                AlcoholicIds.TWO_TANKS_MENU,
                () -> new MenuType<>(MachineMenu::twoTanks)
        );
        RegistryRef<MenuType<?>> fuel = ports.menus().register(
                AlcoholicIds.FUEL_MENU,
                () -> new MenuType<>(MachineMenu::fuel)
        );
        RegistryRef<MenuType<?>> energy = ports.menus().register(
                AlcoholicIds.ENERGY_MENU,
                () -> new MenuType<>(MachineMenu::energy)
        );
        MachineMenuContent content = new MachineMenuContent(
                twoSlots,
                twoSlotsOneTank,
                twoSlotsTwoTanks,
                oneSlotOneTank,
                oneTank,
                twoTanks,
                fuel,
                energy
        );
        MachineMenus.bind(content);
        return content;
    }
}

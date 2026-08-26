package com.djden.alcoholic.minecraft.menu;

import com.djden.alcoholic.platform.api.registry.RegistryRef;
import net.minecraft.world.inventory.MenuType;

public record MachineMenuContent(
        RegistryRef<MenuType<?>> twoSlots,
        RegistryRef<MenuType<?>> twoSlotsOneTank,
        RegistryRef<MenuType<?>> twoSlotsTwoTanks,
        RegistryRef<MenuType<?>> oneSlotOneTank,
        RegistryRef<MenuType<?>> oneTank,
        RegistryRef<MenuType<?>> twoTanks,
        RegistryRef<MenuType<?>> fuel,
        RegistryRef<MenuType<?>> energy
) {
}

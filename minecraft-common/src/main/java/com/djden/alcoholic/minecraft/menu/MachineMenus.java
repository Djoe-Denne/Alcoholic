package com.djden.alcoholic.minecraft.menu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public final class MachineMenus {
    private static MachineMenuContent content;

    private MachineMenus() {
    }

    public static void bind(MachineMenuContent registered) {
        content = registered;
    }

    public static MachineMenu create(MachineAccess access, int id, Inventory inventory) {
        MachineLayout layout = access.layout();
        return new MachineMenu(
                type(layout),
                id,
                inventory,
                access.items(),
                new MachineContainerData(access),
                layout,
                access
        );
    }

    public static boolean tryOpen(Player player, MachineAccess access) {
        if (!(player instanceof ServerPlayer server)) {
            return false;
        }
        server.openMenu(access);
        return true;
    }

    @SuppressWarnings("unchecked")
    public static MenuType<MachineMenu> type(MachineLayout layout) {
        if (content == null) {
            throw new IllegalStateException("Machine menus are not registered");
        }
        return (MenuType<MachineMenu>) switch (layout) {
            case TWO_SLOTS -> content.twoSlots().get();
            case TWO_SLOTS_ONE_TANK -> content.twoSlotsOneTank().get();
            case TWO_SLOTS_TWO_TANKS -> content.twoSlotsTwoTanks().get();
            case ONE_SLOT_ONE_TANK -> content.oneSlotOneTank().get();
            case ONE_TANK -> content.oneTank().get();
            case TWO_TANKS -> content.twoTanks().get();
            case FUEL -> content.fuel().get();
            case ENERGY -> content.energy().get();
        };
    }
}

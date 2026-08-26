package com.djden.alcoholic.minecraft.content;

import com.djden.alcoholic.platform.api.registry.RegistryPort;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public record ContentRegistrationPorts(
        RegistryPort<Block> blocks,
        RegistryPort<Item> items,
        RegistryPort<BlockEntityType<?>> blockEntities,
        RegistryPort<MenuType<?>> menus
) {
}

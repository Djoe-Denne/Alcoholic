package com.djden.alcoholic.minecraft.content;

import com.djden.alcoholic.platform.api.registry.RegistryRef;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public record CraftContent(
        RegistryRef<Block> craftCasing,
        RegistryRef<Item> craftCasingItem,
        RegistryRef<Block> maltHouseController,
        RegistryRef<Item> maltHouseControllerItem,
        RegistryRef<BlockEntityType<?>> maltHouseControllerEntity,
        RegistryRef<Block> millController,
        RegistryRef<Item> millControllerItem,
        RegistryRef<BlockEntityType<?>> millControllerEntity,
        RegistryRef<Block> mashTunController,
        RegistryRef<Item> mashTunControllerItem,
        RegistryRef<BlockEntityType<?>> mashTunControllerEntity,
        RegistryRef<Block> brewingKettleController,
        RegistryRef<Item> brewingKettleControllerItem,
        RegistryRef<BlockEntityType<?>> brewingKettleControllerEntity,
        RegistryRef<Block> vatController,
        RegistryRef<Item> vatControllerItem,
        RegistryRef<BlockEntityType<?>> vatControllerEntity
) {
}

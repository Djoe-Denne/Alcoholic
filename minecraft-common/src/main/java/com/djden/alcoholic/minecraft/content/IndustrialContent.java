package com.djden.alcoholic.minecraft.content;

import com.djden.alcoholic.platform.api.registry.RegistryRef;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public record IndustrialContent(
        RegistryRef<Block> industrialCasing,
        RegistryRef<Item> industrialCasingItem,
        RegistryRef<Block> machineWindow,
        RegistryRef<Item> machineWindowItem,
        RegistryRef<Block> accessHatch,
        RegistryRef<Item> accessHatchItem,
        RegistryRef<Block> fluidPort,
        RegistryRef<Item> fluidPortItem,
        RegistryRef<BlockEntityType<?>> fluidPortEntity,
        RegistryRef<Block> itemPort,
        RegistryRef<Item> itemPortItem,
        RegistryRef<BlockEntityType<?>> itemPortEntity,
        RegistryRef<Block> kineticPort,
        RegistryRef<Item> kineticPortItem,
        RegistryRef<BlockEntityType<?>> kineticPortEntity,
        RegistryRef<Block> pressController,
        RegistryRef<Item> pressControllerItem,
        RegistryRef<BlockEntityType<?>> pressControllerEntity,
        RegistryRef<Block> vatController,
        RegistryRef<Item> vatControllerItem,
        RegistryRef<BlockEntityType<?>> vatControllerEntity,
        RegistryRef<Block> tankController,
        RegistryRef<Item> tankControllerItem,
        RegistryRef<BlockEntityType<?>> tankControllerEntity
) {
}

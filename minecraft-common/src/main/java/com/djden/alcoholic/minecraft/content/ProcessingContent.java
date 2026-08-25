package com.djden.alcoholic.minecraft.content;

import com.djden.alcoholic.platform.api.registry.RegistryRef;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public record ProcessingContent(
        RegistryRef<Block> artisanalPress,
        RegistryRef<Item> artisanalPressItem,
        RegistryRef<BlockEntityType<?>> artisanalPressEntity,
        RegistryRef<Block> artisanalFermenter,
        RegistryRef<Item> artisanalFermenterItem,
        RegistryRef<BlockEntityType<?>> artisanalFermenterEntity,
        RegistryRef<Block> oakBarrel,
        RegistryRef<Item> oakBarrelItem,
        RegistryRef<BlockEntityType<?>> oakBarrelEntity,
        RegistryRef<Block> blendingCrock,
        RegistryRef<Item> blendingCrockItem,
        RegistryRef<BlockEntityType<?>> blendingCrockEntity,
        RegistryRef<Item> yeast,
        RegistryRef<Item> grapePomace,
        RegistryRef<Item> emptyBottle,
        RegistryRef<Item> beverageBottle
) {
}

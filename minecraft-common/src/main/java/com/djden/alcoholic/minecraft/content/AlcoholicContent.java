package com.djden.alcoholic.minecraft.content;

import com.djden.alcoholic.platform.api.registry.RegistryRef;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public record AlcoholicContent(
        RegistryRef<Block> redGrapevine,
        RegistryRef<Block> whiteGrapevine,
        RegistryRef<Item> redGrapes,
        RegistryRef<Item> whiteGrapes,
        RegistryRef<Item> redGrapeCutting,
        RegistryRef<Item> whiteGrapeCutting,
        RegistryRef<Block> vineyardPost,
        RegistryRef<Block> endPost,
        RegistryRef<Block> trellisWire,
        RegistryRef<Item> vineyardPostItem,
        RegistryRef<Item> endPostItem,
        RegistryRef<Item> trellisSpool,
        RegistryRef<Item> pruningShears,
        RegistryRef<BlockEntityType<?>> vineBlockEntity
) {
}

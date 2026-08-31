package com.djden.alcoholic.minecraft.content;

import com.djden.alcoholic.platform.api.registry.RegistryRef;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public record GrainContent(
        RegistryRef<Block> barleyCrop,
        RegistryRef<Block> hopBine,
        RegistryRef<Block> hopBineStem,
        RegistryRef<Block> hopBineCanopy,
        RegistryRef<Block> wildHops,
        RegistryRef<Item> barley,
        RegistryRef<Item> barleySeeds,
        RegistryRef<Item> hops,
        RegistryRef<Item> hopRhizome,
        RegistryRef<Item> maltedBarley,
        RegistryRef<Item> grist
) {
}

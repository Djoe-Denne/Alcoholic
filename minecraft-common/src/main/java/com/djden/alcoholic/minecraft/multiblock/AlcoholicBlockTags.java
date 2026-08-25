package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class AlcoholicBlockTags {
    public static final TagKey<Block> INDUSTRIAL_TANK_CASING = tag("industrial_tank_casing");
    public static final TagKey<Block> FERMENTER_CASING = tag("fermenter_casing");
    public static final TagKey<Block> PRESSURE_SAFE_CASING = tag("pressure_safe_casing");
    public static final TagKey<Block> VALID_MACHINE_WINDOWS = tag("valid_machine_windows");
    public static final TagKey<Block> INDUSTRIAL_PORTS = tag("industrial_ports");

    private AlcoholicBlockTags() {
    }

    private static TagKey<Block> tag(String path) {
        return TagKey.create(
                Registry.BLOCK_REGISTRY,
                ResourceLocation.fromNamespaceAndPath(AlcoholicIds.MOD_ID, path)
        );
    }
}

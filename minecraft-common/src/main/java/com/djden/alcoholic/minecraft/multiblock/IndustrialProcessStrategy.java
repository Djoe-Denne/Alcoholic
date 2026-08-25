package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.domain.multiblock.MultiblockDefinition;

/**
 * World tick for one industrial process capability. Register on
 * {@link IndustrialRuntime} instead of extending {@code MachineKind}.
 */
@FunctionalInterface
public interface IndustrialProcessStrategy {
    void tick(MultiblockControllerBlockEntity machine, MultiblockDefinition definition, long gameTime);
}

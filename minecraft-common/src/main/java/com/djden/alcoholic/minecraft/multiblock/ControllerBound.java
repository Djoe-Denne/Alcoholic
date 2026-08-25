package com.djden.alcoholic.minecraft.multiblock;

import net.minecraft.core.BlockPos;

public interface ControllerBound {
    BlockPos cachedController();

    void bindController(BlockPos controller);

    void clearController();

    MultiblockControllerBlockEntity controller();
}

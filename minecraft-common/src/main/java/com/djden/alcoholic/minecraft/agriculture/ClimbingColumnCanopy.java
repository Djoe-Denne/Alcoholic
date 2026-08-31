package com.djden.alcoholic.minecraft.agriculture;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Top segment that occupies the trellis wire. Clicks route to the root.
 */
public interface ClimbingColumnCanopy {
    boolean belongsTo(ClimbingColumnRoot root);

    BlockState restoredWire(BlockState canopyState);

    Direction.Axis axis(BlockState canopyState);
}

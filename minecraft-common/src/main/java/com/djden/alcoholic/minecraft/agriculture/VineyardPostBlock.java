package com.djden.alcoholic.minecraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A structural trellis post. End posts use a distinct block ID but share this
 * runtime type so trellis validation can treat both as valid anchors.
 */
public class VineyardPostBlock extends Block implements CropSupportPost {
    private static final VoxelShape POST_SHAPE =
            Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);

    public VineyardPostBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return POST_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return POST_SHAPE;
    }
}

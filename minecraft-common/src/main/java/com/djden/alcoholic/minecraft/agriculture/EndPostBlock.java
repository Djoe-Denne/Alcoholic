package com.djden.alcoholic.minecraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class EndPostBlock extends VineyardPostBlock {
    private static final VoxelShape END_POST_SHAPE = Shapes.or(
            Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0),
            Block.box(3.0, 14.0, 3.0, 13.0, 16.0, 13.0)
    );

    public EndPostBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return END_POST_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return END_POST_SHAPE;
    }
}

package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.minecraft.menu.MachineMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public final class ArtisanalFermenterBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    private static final VoxelShape BODY = Block.box(1, 0, 1, 15, 14, 15);
    private static final VoxelShape AIRLOCK = Block.box(6.7, 13.2, 6.7, 9.3, 15.25, 9.3);
    private static final VoxelShape HATCH = Block.box(5.1, 3.8, 0.25, 10.9, 8.6, 1.5);
    private static final VoxelShape HATCH_OPEN = Block.box(5.1, 3.8, -1.3, 10.9, 9.2, 1.5);
    private static final VoxelShape TAP = Block.box(14.5, 1.2, 6.1, 17.4, 5.9, 9.7);
    private static final VoxelShape CLOSED_NORTH = Shapes.or(BODY, AIRLOCK, HATCH, TAP);
    private static final VoxelShape OPEN_NORTH = Shapes.or(BODY, AIRLOCK, HATCH_OPEN, TAP);

    private final Supplier<? extends BlockEntityType<?>> blockEntityType;

    public ArtisanalFermenterBlock(
            Properties properties,
            Supplier<? extends BlockEntityType<?>> blockEntityType
    ) {
        super(properties);
        this.blockEntityType = blockEntityType;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, false));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new ArtisanalFermenterBlockEntity(blockEntityType.get(), position, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return level.isClientSide ? null : createTickerHelper(
                type,
                expectedType(),
                ArtisanalFermenterBlockEntity::serverTick
        );
    }

    @SuppressWarnings("unchecked")
    private BlockEntityType<ArtisanalFermenterBlockEntity> expectedType() {
        return (BlockEntityType<ArtisanalFermenterBlockEntity>) blockEntityType.get();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape north = state.getValue(OPEN) ? OPEN_NORTH : CLOSED_NORTH;
        return rotateHorizontal(north, state.getValue(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos position, RandomSource random) {
        if (!(level.getBlockEntity(position) instanceof ArtisanalFermenterBlockEntity entity)
                || !entity.visualVenting()) {
            return;
        }
        double x = position.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.08;
        double y = position.getY() + 0.94 + random.nextDouble() * 0.04;
        double z = position.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.08;
        if (random.nextBoolean()) {
            level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0.0, 0.02, 0.0);
        }
        if (random.nextFloat() < 0.45F) {
            level.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0, 0.02, 0.0);
        }
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (!(level.getBlockEntity(position) instanceof ArtisanalFermenterBlockEntity entity)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (com.djden.alcoholic.minecraft.bottle.Bottling.bottle(player, held, entity.tank())) {
            return InteractionResult.CONSUME;
        }
        if (!held.isEmpty() && entity.insertYeast(held)) {
            return InteractionResult.CONSUME;
        }
        if (held.isEmpty() && MachineMenus.tryOpen(player, entity)) {
            return InteractionResult.CONSUME;
        }
        player.displayClientMessage(entity.status(), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(
            BlockState state,
            Level level,
            BlockPos position,
            BlockState newState,
            boolean moved
    ) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(position) instanceof ArtisanalFermenterBlockEntity entity) {
            Containers.dropContents(level, position, entity);
        }
        super.onRemove(state, level, position, newState, moved);
    }

    static VoxelShape rotateHorizontal(VoxelShape north, Direction facing) {
        if (facing == Direction.NORTH) {
            return north;
        }
        int turns = switch (facing) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
        VoxelShape rotated = north;
        for (int turn = 0; turn < turns; turn++) {
            VoxelShape[] next = {Shapes.empty()};
            rotated.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    next[0] = Shapes.or(next[0], Shapes.box(1.0 - maxZ, minY, minX, 1.0 - minZ, maxY, maxX)));
            rotated = next[0];
        }
        return rotated;
    }
}

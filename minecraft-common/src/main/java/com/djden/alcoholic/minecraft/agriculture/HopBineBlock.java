package com.djden.alcoholic.minecraft.agriculture;

import com.djden.alcoholic.minecraft.advancement.AdvancementHooks;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * Vertical supported bine. Grows toward an overhead trellis run rather than
 * as a reskinned cereal crop.
 */
public class HopBineBlock extends BushBlock implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    public static final EnumProperty<Segment> SEGMENT = EnumProperty.create("segment", Segment.class);
    public static final int MAX_HEIGHT = 4;
    private static final VoxelShape[] SHAPES = {
            Block.box(4.0, 0.0, 4.0, 12.0, 6.0, 12.0),
            Block.box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0),
            Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0)
    };

    private final Supplier<ItemStack> harvest;

    public HopBineBlock(Properties properties, Supplier<ItemStack> harvest) {
        super(properties);
        this.harvest = harvest;
        registerDefaultState(stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(SEGMENT, Segment.SINGLE));
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.FARMLAND)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.getBlock() instanceof HopBineBlock
                || state.getBlock() instanceof CropSupportPost;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return mayPlaceOn(below, level, pos.below())
                && TrellisDetector.shared().hasOverheadRun(level, pos, MAX_HEIGHT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[Math.min(state.getValue(AGE), SHAPES.length - 1)];
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 0) {
            return;
        }
        grow(level, pos, state);
    }

    private void grow(ServerLevel level, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < 2) {
            level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
            return;
        }
        BlockPos above = pos.above();
        if (level.isEmptyBlock(above)
                && above.getY() - ground(level, pos) < MAX_HEIGHT
                && TrellisDetector.shared().hasOverheadRun(level, above, MAX_HEIGHT)) {
            level.setBlock(
                    above,
                    segmentStateAt(level, above, defaultBlockState()),
                    Block.UPDATE_ALL
            );
            syncSegment(level, pos, state);
        }
    }

    private static int ground(LevelReader level, BlockPos pos) {
        BlockPos cursor = pos;
        while (level.getBlockState(cursor.below()).getBlock() instanceof HopBineBlock) {
            cursor = cursor.below();
        }
        return cursor.getY();
    }

    public ItemStack harvestItem() {
        return harvest.get();
    }

    public static boolean isMature(BlockState state) {
        return state.hasProperty(AGE) && state.getValue(AGE) >= 2;
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter level, BlockPos pos, BlockState state, boolean client) {
        return state.getValue(AGE) < 2 || level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        grow(level, pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return segmentStateAt(context.getLevel(), context.getClickedPos(), defaultBlockState());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, SEGMENT);
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighbor,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (direction == Direction.UP || direction == Direction.DOWN) {
            return stateForSegment(
                    state,
                    isHopBine(level.getBlockState(pos.below())),
                    isHopBine(level.getBlockState(pos.above()))
            );
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (harvestBy(player, level, pos, state)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block block,
            BlockPos fromPos,
            boolean moving
    ) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        syncSegment(level, pos, state);
    }

    public static BlockState segmentStateAt(LevelReader level, BlockPos pos, BlockState state) {
        return stateForSegment(
                state,
                isHopBine(level.getBlockState(pos.below())),
                isHopBine(level.getBlockState(pos.above()))
        );
    }

    static BlockState stateForSegment(
            BlockState state,
            boolean hasHopBineBelow,
            boolean hasHopBineAbove
    ) {
        return state.setValue(SEGMENT, Segment.fromNeighbors(hasHopBineBelow, hasHopBineAbove));
    }

    private static boolean isHopBine(BlockState state) {
        return state.getBlock() instanceof HopBineBlock;
    }

    private static void syncSegment(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return;
        }
        BlockState next = stateForSegment(
                state,
                isHopBine(level.getBlockState(pos.below())),
                isHopBine(level.getBlockState(pos.above()))
        );
        if (!next.equals(state)) {
            level.setBlock(pos, next, Block.UPDATE_CLIENTS);
        }
    }

    public boolean harvestBy(Player player, Level level, BlockPos pos, BlockState state) {
        if (!isMature(state) || level.isClientSide) {
            return false;
        }
        ItemStack drop = harvest.get();
        if (!player.getInventory().add(drop)) {
            player.drop(drop, false);
        }
        AdvancementHooks.harvest(player, AdvancementHooks.location(AlcoholicIds.HOPS));
        level.setBlock(pos, state.setValue(AGE, 0), Block.UPDATE_CLIENTS);
        return true;
    }

    public enum Segment implements StringRepresentable {
        SINGLE("single"),
        BOTTOM("bottom"),
        MIDDLE("middle"),
        TOP("top");

        private final String serializedName;

        Segment(String serializedName) {
            this.serializedName = serializedName;
        }

        public static Segment fromNeighbors(boolean hasHopBineBelow, boolean hasHopBineAbove) {
            if (hasHopBineBelow) {
                return hasHopBineAbove ? MIDDLE : TOP;
            }
            return hasHopBineAbove ? BOTTOM : SINGLE;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}

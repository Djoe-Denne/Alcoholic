package com.djden.alcoholic.minecraft.agriculture;

import com.djden.alcoholic.minecraft.advancement.AdvancementHooks;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Hop cep. Column geometry is {@link ClimbingColumn}; age is the only
 * hop-specific growth variable. No pruning, no harvest lot.
 */
public class HopBineBlock extends BushBlock
        implements BonemealableBlock, ClimbingColumnRoot, ColumnHarvest {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    public static final BooleanProperty TRAINED = BooleanProperty.create("trained");
    public static final BooleanProperty EXTENDED = BooleanProperty.create("extended");
    private static final VoxelShape[] SHAPES = {
            Block.box(4.0, 0.0, 4.0, 12.0, 6.0, 12.0),
            Block.box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0),
            Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0)
    };

    private final Supplier<ItemStack> harvest;
    private final Supplier<? extends Block> stemBlock;
    private final Supplier<? extends Block> canopyBlock;

    public HopBineBlock(
            Properties properties,
            Supplier<ItemStack> harvest,
            Supplier<? extends Block> stemBlock,
            Supplier<? extends Block> canopyBlock
    ) {
        super(properties);
        this.harvest = Objects.requireNonNull(harvest, "harvest");
        this.stemBlock = Objects.requireNonNull(stemBlock, "stemBlock");
        this.canopyBlock = Objects.requireNonNull(canopyBlock, "canopyBlock");
        registerDefaultState(stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(TRAINED, false)
                .setValue(EXTENDED, false));
    }

    @Override
    public Block stemBlock() {
        return stemBlock.get();
    }

    @Override
    public Block canopyBlock() {
        return canopyBlock.get();
    }

    public ItemStack harvestItem() {
        return harvest.get();
    }

    public static int boundAge(int age) {
        return Mth.clamp(age, 0, 2);
    }

    public static boolean isMature(BlockState state) {
        return state.hasProperty(AGE) && state.getValue(AGE) >= 2;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.FARMLAND)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.GRASS_BLOCK);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return mayPlaceOn(below, level, pos.below())
                && TrellisDetector.shared().hasOverheadRun(level, pos, HopColumn.MAX_WIRE_OFFSET);
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
            refresh(level, pos, state.setValue(AGE, age + 1), true);
            return;
        }
        refresh(level, pos, state, true);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter level, BlockPos pos, BlockState state, boolean client) {
        if (state.getValue(AGE) < 2) {
            return true;
        }
        return level instanceof LevelReader reader
                && TrellisDetector.shared().boundedWireHeightAbove(reader, pos) == HopColumn.MAX_WIRE_OFFSET
                && level.getBlockState(pos.above()).isAir();
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
        return defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, TRAINED, EXTENDED);
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
        return state;
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
        ItemStack held = player.getItemInHand(hand);
        if (SickleItem.isSickle(held) && harvestColumn(player, level, pos, state)) {
            if (!level.isClientSide && !player.getAbilities().instabuild) {
                held.hurtAndBreak(1, player, brokenBy -> brokenBy.broadcastBreakEvent(hand));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useAsRoot(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        return use(state, level, position, player, hand, hit);
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
            ClimbingColumn.removeProjection(level, pos, this);
            level.destroyBlock(pos, true);
            return;
        }
        if (!level.isClientSide) {
            refresh(level, pos, state, false);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(
            BlockState state,
            Level level,
            BlockPos position,
            BlockState newState,
            boolean isMoving
    ) {
        if (!state.is(newState.getBlock())) {
            ClimbingColumn.removeProjection(level, position, this);
        }
        super.onRemove(state, level, position, newState, isMoving);
    }

    @Override
    public boolean harvestColumn(
            Player player,
            Level level,
            BlockPos rootPos,
            BlockState rootState
    ) {
        if (!isMature(rootState)) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }
        ItemStack drop = harvest.get();
        drop.setCount(SickleItem.fortuneAdjustedCount(
                HopColumn.harvestCount(level, rootPos, this),
                SickleItem.heldSickle(player),
                level.random
        ));
        if (!player.getInventory().add(drop)) {
            player.drop(drop, false);
        }
        AdvancementHooks.harvest(player, AdvancementHooks.location(AlcoholicIds.HOPS));
        refresh(level, rootPos, rootState.setValue(AGE, 0), false);
        return true;
    }

    private void refresh(Level level, BlockPos pos, BlockState state, boolean allowGrowth) {
        if (level.isClientSide) {
            return;
        }
        if (!state.equals(level.getBlockState(pos))) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            state = level.getBlockState(pos);
        }
        HopColumn.sync(
                level,
                pos,
                this,
                state.getValue(AGE),
                TrellisDetector.shared(),
                allowGrowth
        );
    }
}

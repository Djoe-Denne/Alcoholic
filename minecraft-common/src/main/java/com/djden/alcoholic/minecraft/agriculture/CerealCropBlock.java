package com.djden.alcoholic.minecraft.agriculture;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.minecraft.advancement.AdvancementHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Annual cereal with three visual stages. Not a perennial vine.
 */
public class CerealCropBlock extends CropBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    private static final VoxelShape[] SHAPES = {
            Block.box(2.0, 0.0, 2.0, 14.0, 4.0, 14.0),
            Block.box(2.0, 0.0, 2.0, 14.0, 9.0, 14.0),
            Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0)
    };

    private final Supplier<Item> seeds;
    private final ResourceId harvestCrop;

    public CerealCropBlock(Properties properties, Supplier<Item> seeds, ResourceId harvestCrop) {
        super(properties);
        this.seeds = seeds;
        this.harvestCrop = Objects.requireNonNull(harvestCrop, "harvestCrop");
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return 2;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seeds.get();
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return super.mayPlaceOn(state, level, pos)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[Math.min(state.getValue(AGE), SHAPES.length - 1)];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public void growCrops(Level level, BlockPos pos, BlockState state) {
        int next = Math.min(getAge(state) + 1, getMaxAge());
        level.setBlock(pos, getStateForAge(next), Block.UPDATE_CLIENTS);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos, 0) >= 9 && random.nextInt(3) == 0) {
            growCrops(level, pos, state);
        }
    }

    public static boolean isMature(BlockState state) {
        return state.hasProperty(AGE) && state.getValue(AGE) >= 2;
    }

    @Override
    public void playerDestroy(
            Level level,
            Player player,
            BlockPos pos,
            BlockState state,
            @Nullable BlockEntity blockEntity,
            ItemStack tool
    ) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (isMature(state)) {
            AdvancementHooks.harvest(player, AdvancementHooks.location(harvestCrop));
        }
    }
}

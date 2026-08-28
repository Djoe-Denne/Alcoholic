package com.djden.alcoholic.minecraft.agriculture;

import com.djden.alcoholic.application.viticulture.HarvestVineResult;
import com.djden.alcoholic.domain.viticulture.PruningLevel;
import com.djden.alcoholic.domain.viticulture.Vine;
import com.djden.alcoholic.domain.viticulture.VineEnvironment;
import com.djden.alcoholic.domain.viticulture.VineGrowthStage;
import com.djden.alcoholic.domain.viticulture.VineHealth;
import com.djden.alcoholic.domain.viticulture.VineVariety;
import com.djden.alcoholic.domain.ingredient.GrapeColor;
import com.djden.alcoholic.minecraft.advancement.AdvancementHooks;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.viticulture.HarvestLotNbt;
import com.djden.alcoholic.minecraft.viticulture.MinecraftClimateResolver;
import com.djden.alcoholic.minecraft.viticulture.PruningShearsItem;
import com.djden.alcoholic.minecraft.viticulture.ViticultureRuntime;
import com.djden.alcoholic.api.ResourceId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Persistent perennial vine backed by the domain {@link Vine} model.
 */
public class VineBlock extends BaseEntityBlock {
    public static final int MAX_LEGACY_AGE = 4;
    public static final IntegerProperty AGE =
            IntegerProperty.create("age", 0, MAX_LEGACY_AGE);
    public static final EnumProperty<VineStage> STAGE =
            EnumProperty.create("stage", VineStage.class);
    public static final BooleanProperty TRAINED = BooleanProperty.create("trained");
    public static final BooleanProperty EXTENDED = BooleanProperty.create("extended");

    private static final VoxelShape[] SHAPES = {
            Block.box(5.0, 0.0, 5.0, 11.0, 5.0, 11.0),
            Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0),
            Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0),
            Block.box(1.0, 0.0, 1.0, 15.0, 15.0, 15.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
    };
    private static final VoxelShape EXTENDED_SHAPE =
            Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
    private static final VoxelShape TRAINED_SHAPE =
            Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    private final VineVariety<ResourceId> variety;
    private final ViticultureRuntime runtime;
    private final Supplier<? extends BlockEntityType<?>> blockEntityType;
    private final Supplier<? extends Block> stemBlock;
    private final Supplier<? extends Block> canopyBlock;
    private final MinecraftClimateResolver climateResolver;
    private final TrellisDetector trellisDetector;

    public VineBlock(
            Properties properties,
            VineVariety<ResourceId> variety,
            ViticultureRuntime runtime,
            Supplier<? extends BlockEntityType<?>> blockEntityType
    ) {
        this(properties, variety, runtime, blockEntityType, () -> null);
    }

    public VineBlock(
            Properties properties,
            VineVariety<ResourceId> variety,
            ViticultureRuntime runtime,
            Supplier<? extends BlockEntityType<?>> blockEntityType,
            Supplier<? extends Block> stemBlock
    ) {
        this(properties, variety, runtime, blockEntityType, stemBlock, () -> null);
    }

    public VineBlock(
            Properties properties,
            VineVariety<ResourceId> variety,
            ViticultureRuntime runtime,
            Supplier<? extends BlockEntityType<?>> blockEntityType,
            Supplier<? extends Block> stemBlock,
            Supplier<? extends Block> canopyBlock
    ) {
        super(properties);
        this.variety = Objects.requireNonNull(variety, "variety");
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.blockEntityType = Objects.requireNonNull(blockEntityType, "blockEntityType");
        this.stemBlock = Objects.requireNonNull(stemBlock, "stemBlock");
        this.canopyBlock = Objects.requireNonNull(canopyBlock, "canopyBlock");
        climateResolver = new MinecraftClimateResolver();
        trellisDetector = new TrellisDetector(runtime);
        registerDefaultState(
                stateDefinition.any()
                        .setValue(STAGE, VineStage.PLANTED)
                        .setValue(TRAINED, false)
                        .setValue(EXTENDED, false)
                        .setValue(AGE, 0)
        );
    }

    public Block stemBlock() {
        return stemBlock.get();
    }

    public Block canopyBlock() {
        return canopyBlock.get();
    }

    public VineVariety<ResourceId> variety() {
        return variety;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new VineBlockEntity(blockEntityType.get(), position, state, variety);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void randomTick(
            BlockState state,
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        VineBlockEntity entity = getOrCreateEntity(level, position, state);
        if (entity == null) {
            return;
        }

        boolean trained = trellisDetector.boundedWireHeightAbove(level, position) > 0;
        VineEnvironment environment = climateResolver.resolve(level, position);
        Vine<ResourceId> current = entity.vine();
        Vine<ResourceId> grown = runtime.grow(
                current,
                environment,
                trained,
                random.nextDouble()
        );
        if (!grown.equals(current)) {
            entity.setVine(grown);
        } else {
            BlockState synchronizedState = stateForVine(state, current);
            if (synchronizedState != state) {
                level.setBlock(position, synchronizedState, Block.UPDATE_CLIENTS);
            }
        }
        refreshStructure(level, position, entity.vine().growthStage(), true);
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
        ItemStack held = player.getItemInHand(hand);

        if (held.getItem() instanceof PruningShearsItem shears
                && state.getValue(STAGE).domainStage() == VineGrowthStage.DORMANT) {
            if (!level.isClientSide) {
                VineBlockEntity entity = getOrCreateEntity(level, position, state);
                if (entity != null) {
                    PruningLevel pruning = shears.selectedLevel(held);
                    entity.setVine(runtime.prune(entity.vine(), pruning));
                    refreshStructure(level, position, entity.vine().growthStage(), false);
                    if (!player.getAbilities().instabuild) {
                        held.hurtAndBreak(
                                1,
                                player,
                                brokenBy -> brokenBy.broadcastBreakEvent(hand)
                        );
                    }
                    player.displayClientMessage(
                            Component.translatable(
                                    "message.alcoholic.vine.pruned",
                                    Component.translatable(pruningKey(pruning))
                            ),
                            true
                    );
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (held.isEmpty() && player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                inspect(level, position, state, player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (held.isEmpty() && isHarvestReadyState(state)) {
            if (!level.isClientSide) {
                harvest(level, position, state, player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean canSurvive(
            BlockState state,
            LevelReader level,
            BlockPos position
    ) {
        return mayPlaceOn(level.getBlockState(position.below()));
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighbor,
            LevelAccessor level,
            BlockPos position,
            BlockPos neighborPosition
    ) {
        return direction == Direction.DOWN && !canSurvive(state, level, position)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(
                        state,
                        direction,
                        neighbor,
                        level,
                        position,
                        neighborPosition
                );
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos position,
            Block block,
            BlockPos fromPos,
            boolean moving
    ) {
        if (!state.canSurvive(level, position)) {
            VineColumn.removeProjection(level, position, this);
            level.destroyBlock(position, true);
            return;
        }
        VineBlockEntity entity = getOrCreateEntity(level, position, state);
        if (entity != null) {
            refreshStructure(level, position, entity.vine().growthStage(), false);
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
            VineColumn.removeProjection(level, position, this);
        }
        super.onRemove(state, level, position, newState, isMoving);
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        if (state.getValue(EXTENDED)) {
            return EXTENDED_SHAPE;
        }
        if (state.getValue(TRAINED)) {
            return TRAINED_SHAPE;
        }
        return SHAPES[Mth.clamp(state.getValue(AGE), 0, MAX_LEGACY_AGE)];
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(STAGE, TRAINED, EXTENDED, AGE);
    }

    BlockState stateForVine(BlockState state, Vine<ResourceId> vine) {
        return state
                .setValue(STAGE, VineStage.fromDomain(vine.growthStage()))
                .setValue(AGE, legacyAge(vine.growthStage()));
    }

    private void harvest(
            Level level,
            BlockPos position,
            BlockState state,
            Player player
    ) {
        VineBlockEntity entity = getOrCreateEntity(level, position, state);
        if (entity == null
                || entity.vine().growthStage() != VineGrowthStage.HARVEST_READY) {
            return;
        }

        VineEnvironment environment = climateResolver.resolve(level, position);
        boolean trained = trellisDetector.isTrained(level, position);
        HarvestVineResult result = runtime.harvest(
                entity.vine(),
                environment,
                trained,
                level.getGameTime()
        );
        ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(
                result.harvestItem().namespace(),
                result.harvestItem().path()
        );
        Optional<Item> registeredItem = Registry.ITEM.getOptional(itemId);
        if (registeredItem.isEmpty() || registeredItem.get() == net.minecraft.world.item.Items.AIR) {
            player.displayClientMessage(
                    Component.translatable("message.alcoholic.vine.no_harvest_item"),
                    true
            );
            return;
        }

        Item item = registeredItem.get();
        int count = Mth.clamp(
                (int) Math.round(result.harvest().quantity()),
                1,
                item.getMaxStackSize()
        );
        ItemStack grapes = new ItemStack(item, count);
        HarvestLotNbt.write(
                grapes,
                entity.vine().variety().id(),
                result.harvest().quality(),
                result.harvest().sugar(),
                result.harvest().acidity()
        );

        entity.setVine(result.harvest().vine());
        refreshStructure(level, position, result.harvest().vine().growthStage(), false);
        popResource(level, position, grapes);
        AdvancementHooks.harvest(
                player,
                AdvancementHooks.location(
                        entity.vine().variety().grapeColor() == GrapeColor.RED
                                ? AlcoholicIds.RED_GRAPES
                                : AlcoholicIds.WHITE_GRAPES
                )
        );
        level.playSound(
                null,
                position,
                SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                SoundSource.BLOCKS,
                1.0F,
                0.9F + level.random.nextFloat() * 0.2F
        );
    }

    private void inspect(
            Level level,
            BlockPos position,
            BlockState state,
            Player player
    ) {
        VineBlockEntity entity = getOrCreateEntity(level, position, state);
        if (entity == null) {
            return;
        }
        Vine<ResourceId> vine = entity.vine();
        VineEnvironment environment = climateResolver.resolve(level, position);
        double suitability = runtime.settings()
                .forVariety(vine.variety())
                .growth()
                .climateProfile()
                .suitability(environment);
        player.displayClientMessage(
                Component.translatable(
                        "message.alcoholic.vine.inspect",
                        Component.translatable(climateKey(suitability)),
                        Component.translatable(healthKey(vine.health())),
                        Component.translatable(stageKey(vine.growthStage())),
                        Math.round(vine.growthProgress() * 100.0),
                        Component.translatable(pruningKey(vine.pruningLevel()))
                ),
                false
        );
    }

    private void refreshStructure(
            Level level,
            BlockPos position,
            VineGrowthStage stage,
            boolean allowGrowth
    ) {
        VineColumn.sync(level, position, this, stage, trellisDetector, allowGrowth);
    }

    private VineBlockEntity getOrCreateEntity(
            Level level,
            BlockPos position,
            BlockState state
    ) {
        BlockEntity existing = level.getBlockEntity(position);
        if (existing instanceof VineBlockEntity vineEntity) {
            return vineEntity;
        }
        if (level.isClientSide) {
            return null;
        }
        VineBlockEntity created = (VineBlockEntity) newBlockEntity(position, state);
        created.setVine(
                VineBlockEntity.migrateLegacy(variety, state.getValue(AGE))
        );
        level.setBlockEntity(created);
        return created;
    }

    private static boolean mayPlaceOn(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND);
    }

    private static int legacyAge(VineGrowthStage stage) {
        return switch (stage) {
            case PLANTED -> 0;
            case ESTABLISHING -> 1;
            case VEGETATIVE, FLOWERING, GREEN_FRUIT, DORMANT -> 2;
            case RIPENING -> 3;
            case HARVEST_READY -> 4;
        };
    }

    private static boolean isHarvestReadyState(BlockState state) {
        return state.getValue(STAGE).domainStage() == VineGrowthStage.HARVEST_READY
                || (state.getValue(STAGE) == VineStage.PLANTED
                && state.getValue(AGE) == MAX_LEGACY_AGE);
    }

    private static String climateKey(double suitability) {
        if (suitability >= 0.85) {
            return "message.alcoholic.vine.climate.ideal";
        }
        if (suitability >= 0.65) {
            return "message.alcoholic.vine.climate.good";
        }
        if (suitability >= 0.40) {
            return "message.alcoholic.vine.climate.average";
        }
        return "message.alcoholic.vine.climate.poor";
    }

    private static String stageKey(VineGrowthStage stage) {
        return "message.alcoholic.vine.stage."
                + stage.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static String pruningKey(PruningLevel level) {
        return switch (level) {
            case LIGHT -> "message.alcoholic.vine.pruning.light";
            case BALANCED -> "message.alcoholic.vine.pruning.balanced";
            case SEVERE -> "message.alcoholic.vine.pruning.severe";
        };
    }

    private static String healthKey(VineHealth health) {
        if (health.equals(VineHealth.THRIVING)) {
            return "message.alcoholic.vine.health.thriving";
        }
        if (health.equals(VineHealth.HEALTHY)) {
            return "message.alcoholic.vine.health.healthy";
        }
        if (health.equals(VineHealth.STRESSED)) {
            return "message.alcoholic.vine.health.stressed";
        }
        return "message.alcoholic.vine.health.poor";
    }
}

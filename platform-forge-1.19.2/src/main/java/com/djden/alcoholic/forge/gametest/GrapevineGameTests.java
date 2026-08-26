package com.djden.alcoholic.forge.gametest;

import com.djden.alcoholic.application.viticulture.VineVarieties;
import com.djden.alcoholic.domain.viticulture.PruningLevel;
import com.djden.alcoholic.domain.viticulture.Vine;
import com.djden.alcoholic.domain.viticulture.VineEnvironment;
import com.djden.alcoholic.domain.viticulture.VineGrowthStage;
import com.djden.alcoholic.domain.viticulture.VineHealth;
import com.djden.alcoholic.domain.viticulture.VineVariety;
import com.djden.alcoholic.integration.vinery.VineryIntegration;
import com.djden.alcoholic.minecraft.agriculture.TrellisDetector;
import com.djden.alcoholic.minecraft.agriculture.TrellisWireBlock;
import com.djden.alcoholic.minecraft.agriculture.VineBlock;
import com.djden.alcoholic.minecraft.agriculture.VineBlockEntity;
import com.djden.alcoholic.minecraft.agriculture.VineStage;
import com.djden.alcoholic.minecraft.agriculture.VineCanopyBlock;
import com.djden.alcoholic.minecraft.agriculture.VineStemBlock;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.viticulture.PruningShearsItem;
import com.djden.alcoholic.minecraft.viticulture.ViticultureRuntime;
import com.djden.alcoholic.api.ResourceId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(AlcoholicIds.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GrapevineGameTests {
    private static final BlockPos VINE_POSITION = new BlockPos(1, 1, 1);

    private GrapevineGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void grapeCuttingSurvivesOnIrrigatedFarmland(GameTestHelper helper) {
        BlockPos soil = VINE_POSITION.below();
        helper.setBlock(
                soil,
                Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE)
        );
        VineBlock vine = vine("red_grapevine");
        helper.setBlock(VINE_POSITION, vine.defaultBlockState());
        require(
                helper,
                helper.getBlockState(VINE_POSITION).canSurvive(helper.getLevel(), helper.absolutePos(VINE_POSITION)),
                "Grapevine did not accept irrigated farmland"
        );
        helper.runAtTickTime(2, () -> {
            require(
                    helper,
                    helper.getBlockState(VINE_POSITION).is(vine),
                    "Grapevine broke after being planted on irrigated farmland"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void matureHarvestKeepsPerennialVine(GameTestHelper helper) {
        VineBlock vineBlock = vine("red_grapevine");
        Vine<ResourceId> mature = vineAt(
                VineGrowthStage.HARVEST_READY,
                0,
                true,
                0.0,
                Vine.NO_HARVEST
        );
        VineBlockEntity entity = placeVine(helper, vineBlock, mature);

        helper.useBlock(VINE_POSITION);

        Vine<ResourceId> harvested = entity.vine();
        require(
                helper,
                helper.getBlockState(VINE_POSITION).getBlock() == vineBlock,
                "Harvest removed the perennial vine block"
        );
        require(
                helper,
                harvested.growthStage() == VineGrowthStage.DORMANT,
                "Mature harvest did not enter DORMANT"
        );
        require(helper, harvested.ageCycles() == 1, "Harvest did not increment ageCycles");
        require(
                helper,
                harvested.lastHarvest() != Vine.NO_HARVEST,
                "Harvest did not persist lastHarvest"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void immatureVineDoesNotHarvest(GameTestHelper helper) {
        VineBlock vineBlock = vine("white_grapevine");
        Vine<ResourceId> flowering = vineAt(
                VineVarieties.WHITE_GRAPE,
                VineGrowthStage.FLOWERING,
                0,
                true,
                0.25,
                Vine.NO_HARVEST
        );
        VineBlockEntity entity = placeVine(helper, vineBlock, flowering);

        helper.useBlock(VINE_POSITION);

        require(helper, entity.vine().equals(flowering), "Immature vine was harvested");
        require(
                helper,
                helper.getBlockState(VINE_POSITION).getValue(VineBlock.STAGE)
                        == VineStage.FLOWERING,
                "Immature vine state changed unexpectedly"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void dormantVineResumesAtFlowering(GameTestHelper helper) {
        VineBlock vineBlock = vine("red_grapevine");
        Vine<ResourceId> dormant = vineAt(
                VineGrowthStage.DORMANT,
                1,
                true,
                0.75,
                42L
        );
        VineBlockEntity entity = placeVine(helper, vineBlock, dormant);
        VineEnvironment ideal = ViticultureRuntime.shared()
                .settings()
                .forVariety(VineVarieties.RED_GRAPE)
                .growth()
                .climateProfile()
                .idealEnvironment();

        Vine<ResourceId> regrown = ViticultureRuntime.shared().grow(
                dormant,
                ideal,
                true,
                0.0
        );
        entity.setVine(regrown);

        require(
                helper,
                regrown.growthStage() == VineGrowthStage.FLOWERING,
                "DORMANT vine did not resume at FLOWERING"
        );
        require(
                helper,
                regrown.growthStage() != VineGrowthStage.ESTABLISHING,
                "Established vine regressed to ESTABLISHING"
        );
        require(helper, regrown.hasEstablished(), "Established state was lost");
        require(helper, regrown.ageCycles() == 1, "Age cycle changed during regrowth");
        require(helper, regrown.lastHarvest() == 42L, "Last harvest was not retained");
        require(
                helper,
                helper.getBlockState(VINE_POSITION).getValue(VineBlock.STAGE)
                        == VineStage.FLOWERING,
                "Regrown block state did not synchronize to FLOWERING"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void vineBlockEntityRoundTrips(GameTestHelper helper) {
        VineBlock vineBlock = vine("red_grapevine");
        Vine<ResourceId> expected = vineAt(
                VineGrowthStage.RIPENING,
                2,
                true,
                0.5,
                27L
        );
        VineBlockEntity source = placeVine(helper, vineBlock, expected);
        CompoundTag serialized = source.getUpdateTag();
        VineBlockEntity restored = (VineBlockEntity) vineBlock.newBlockEntity(
                helper.absolutePos(VINE_POSITION),
                helper.getBlockState(VINE_POSITION)
        );
        restored.load(serialized);

        require(helper, restored.vine().equals(expected), "Vine block entity NBT changed");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void boundedWireBetweenPostsIsTrained(GameTestHelper helper) {
        placeTrellis(helper, 2);
        require(
                helper,
                TrellisDetector.shared().isTrained(
                        helper.getLevel(),
                        helper.absolutePos(VINE_POSITION)
                ),
                "Wire segment bounded by posts was not detected as trained"
        );
        require(
                helper,
                TrellisDetector.shared().boundedWireHeightAbove(
                        helper.getLevel(),
                        helper.absolutePos(VINE_POSITION)
                ) == 1,
                "Wire at +1 was not reported as height 1"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void vineExtendsToSecondBlockWhenWireAtPlusTwo(GameTestHelper helper) {
        VineBlock vineBlock = vine("red_grapevine");
        placeVine(helper, vineBlock, vineAt(VineGrowthStage.VEGETATIVE, 0, false, 0.0, Vine.NO_HARVEST));
        placeTrellis(helper, 3);
        growOnce(helper, vineBlock);

        BlockPos stemPos = VINE_POSITION.above();
        require(
                helper,
                helper.getBlockState(stemPos).getBlock() instanceof VineStemBlock,
                "Vegetative vine did not grow a stem toward a wire at +2"
        );
        require(
                helper,
                helper.getBlockState(VINE_POSITION).getValue(VineBlock.EXTENDED),
                "Root was not marked extended after growing a stem"
        );
        require(
                helper,
                helper.getBlockState(VINE_POSITION).getValue(VineBlock.TRAINED)
                        && helper.getBlockState(stemPos).getValue(VineStemBlock.TRAINED),
                "Trained state was not copied onto both segments"
        );
        require(
                helper,
                helper.getBlockEntity(stemPos) == null,
                "Stem created a block entity"
        );
        BlockState canopy = helper.getBlockState(VINE_POSITION.above(2));
        require(
                helper,
                canopy.getBlock() instanceof VineCanopyBlock,
                "High wire was not replaced by a canopy"
        );
        require(
                helper,
                canopy.getValue(VineCanopyBlock.AXIS) == Direction.Axis.X
                        && canopy.getValue(VineCanopyBlock.STAGE)
                        == helper.getBlockState(VINE_POSITION).getValue(VineBlock.STAGE),
                "Canopy did not keep the wire axis or root stage"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void vineStaysSingleBlockWhenWireAtPlusOne(GameTestHelper helper) {
        VineBlock vineBlock = vine("red_grapevine");
        placeVine(helper, vineBlock, vineAt(VineGrowthStage.VEGETATIVE, 0, false, 0.0, Vine.NO_HARVEST));
        placeTrellis(helper, 2);
        growOnce(helper, vineBlock);

        require(
                helper,
                !(helper.getBlockState(VINE_POSITION.above()).getBlock() instanceof VineStemBlock),
                "Wire at +1 grew an extra stem"
        );
        require(
                helper,
                helper.getBlockState(VINE_POSITION).getValue(VineBlock.TRAINED),
                "Wire at +1 did not train the root"
        );
        require(
                helper,
                !helper.getBlockState(VINE_POSITION).getValue(VineBlock.EXTENDED),
                "Wire at +1 marked the root as extended"
        );
        require(
                helper,
                helper.getBlockState(VINE_POSITION.above()).getBlock() instanceof VineCanopyBlock,
                "Wire at +1 was not replaced by a canopy"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void plantedVineDoesNotExtendTowardHighWire(GameTestHelper helper) {
        VineBlock vineBlock = vine("red_grapevine");
        placeVine(helper, vineBlock, vineAt(VineGrowthStage.PLANTED, 0, false, 0.0, Vine.NO_HARVEST));
        placeTrellis(helper, 3);
        growOnce(helper, vineBlock);

        require(
                helper,
                !(helper.getBlockState(VINE_POSITION.above()).getBlock() instanceof VineStemBlock),
                "Planted vine grew a stem before becoming vegetative"
        );
        require(
                helper,
                !helper.getBlockState(VINE_POSITION).getValue(VineBlock.EXTENDED),
                "Planted vine was marked extended"
        );
        require(
                helper,
                helper.getBlockState(new BlockPos(1, 3, 1)).getBlock() instanceof TrellisWireBlock,
                "Planted vine replaced a wire it cannot reach"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void obstructedPathPreventsTrainingAndStem(GameTestHelper helper) {
        VineBlock vineBlock = vine("red_grapevine");
        placeVine(helper, vineBlock, vineAt(VineGrowthStage.VEGETATIVE, 0, false, 0.0, Vine.NO_HARVEST));
        helper.setBlock(VINE_POSITION.above(), Blocks.STONE.defaultBlockState());
        placeTrellis(helper, 3);
        growOnce(helper, vineBlock);

        require(
                helper,
                helper.getBlockState(VINE_POSITION.above()).is(Blocks.STONE),
                "Obstruction was replaced by a stem"
        );
        require(
                helper,
                !helper.getBlockState(VINE_POSITION).getValue(VineBlock.TRAINED),
                "Obstructed high wire still trained the vine"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void wireRemovalShrinksColumnAndUntrains(GameTestHelper helper) {
        VineBlock vineBlock = vine("red_grapevine");
        placeVine(
                helper,
                vineBlock,
                vineAt(VineGrowthStage.VEGETATIVE, 0, false, 0.0, Vine.NO_HARVEST)
        );
        placeTrellis(helper, 3);
        growOnce(helper, vineBlock);
        helper.setBlock(new BlockPos(1, 3, 1), Blocks.AIR.defaultBlockState());

        helper.runAtTickTime(2, () -> {
            require(
                    helper,
                    !(helper.getBlockState(VINE_POSITION.above()).getBlock() instanceof VineStemBlock),
                    "Stem remained after the wire was removed"
            );
            require(
                    helper,
                    !helper.getBlockState(VINE_POSITION).getValue(VineBlock.TRAINED),
                    "Root stayed trained after the wire was removed"
            );
            require(
                    helper,
                    !helper.getBlockState(VINE_POSITION).getValue(VineBlock.EXTENDED),
                    "Root stayed extended after the wire was removed"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void soilRemovalDestroysEntireColumn(GameTestHelper helper) {
        VineBlock vineBlock = vine("red_grapevine");
        placeVine(
                helper,
                vineBlock,
                vineAt(VineGrowthStage.VEGETATIVE, 0, false, 0.0, Vine.NO_HARVEST)
        );
        placeTrellis(helper, 3);
        growOnce(helper, vineBlock);
        helper.setBlock(VINE_POSITION.below(), Blocks.AIR.defaultBlockState());

        helper.runAtTickTime(2, () -> {
            require(
                    helper,
                    helper.getBlockState(VINE_POSITION).isAir(),
                    "Root survived after its soil was removed"
            );
            require(
                    helper,
                    helper.getBlockState(VINE_POSITION.above()).isAir()
                            || helper.getBlockState(VINE_POSITION.above()).is(Blocks.AIR),
                    "Stem survived after the soil was removed"
            );
            require(
                    helper,
                    !(helper.getBlockState(VINE_POSITION.above()).getBlock() instanceof VineStemBlock),
                    "Stem block remained after the soil was removed"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void breakingStemDropsNothingAndLeavesRoot(GameTestHelper helper) {
        VineBlock vineBlock = vine("red_grapevine");
        placeVine(
                helper,
                vineBlock,
                vineAt(VineGrowthStage.VEGETATIVE, 0, false, 0.0, Vine.NO_HARVEST)
        );
        placeTrellis(helper, 3);
        growOnce(helper, vineBlock);
        BlockPos stemPos = VINE_POSITION.above();
        helper.getLevel().destroyBlock(helper.absolutePos(stemPos), true);

        require(
                helper,
                helper.getBlockState(VINE_POSITION).is(vineBlock),
                "Breaking the stem destroyed the root"
        );
        require(
                helper,
                itemCountNear(helper, VINE_POSITION, item("red_grape_cutting")) == 0,
                "Breaking the stem dropped a cutting"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void rootBreakDropsSingleCuttingAndClearsStem(GameTestHelper helper) {
        VineBlock vineBlock = vine("red_grapevine");
        placeVine(
                helper,
                vineBlock,
                vineAt(VineGrowthStage.VEGETATIVE, 0, false, 0.0, Vine.NO_HARVEST)
        );
        placeTrellis(helper, 3);
        growOnce(helper, vineBlock);
        helper.getLevel().destroyBlock(helper.absolutePos(VINE_POSITION), true);

        require(
                helper,
                !(helper.getBlockState(VINE_POSITION.above()).getBlock() instanceof VineStemBlock),
                "Stem remained after the root was broken"
        );
        require(
                helper,
                itemCountNear(helper, VINE_POSITION, item("red_grape_cutting")) == 1,
                "Root break did not drop exactly one cutting"
        );
        require(
                helper,
                helper.getBlockState(VINE_POSITION.above(2)).getBlock() instanceof TrellisWireBlock,
                "Breaking the root did not restore the trellis wire"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void breakingCanopyRestoresWireAndDropsNothing(GameTestHelper helper) {
        VineBlock vineBlock = vine("red_grapevine");
        placeVine(
                helper,
                vineBlock,
                vineAt(VineGrowthStage.VEGETATIVE, 0, false, 0.0, Vine.NO_HARVEST)
        );
        placeTrellis(helper, 2);
        growOnce(helper, vineBlock);
        BlockPos canopyPos = VINE_POSITION.above();
        BlockState canopy = helper.getBlockState(canopyPos);
        require(
                helper,
                canopy.getBlock() instanceof VineCanopyBlock,
                "Expected a canopy before breaking it"
        );
        canopy.getBlock().playerDestroy(
                helper.getLevel(),
                helper.makeMockPlayer(),
                helper.absolutePos(canopyPos),
                canopy,
                null,
                ItemStack.EMPTY
        );
        require(
                helper,
                helper.getBlockState(canopyPos).getBlock() instanceof TrellisWireBlock,
                "Breaking the canopy did not restore the wire"
        );
        require(
                helper,
                helper.getBlockState(canopyPos).getValue(TrellisWireBlock.AXIS)
                        == Direction.Axis.X,
                "Restored wire lost its axis"
        );
        require(
                helper,
                itemCountNear(helper, VINE_POSITION, item("red_grape_cutting")) == 0,
                "Breaking the canopy dropped a cutting"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void harvestFromCanopyDelegatesToRoot(GameTestHelper helper) {
        VineBlock vineBlock = vine("red_grapevine");
        VineBlockEntity entity = placeVine(
                helper,
                vineBlock,
                vineAt(VineGrowthStage.HARVEST_READY, 0, true, 0.0, Vine.NO_HARVEST)
        );
        placeTrellis(helper, 2);
        growOnce(helper, vineBlock);
        helper.useBlock(VINE_POSITION.above());

        require(
                helper,
                helper.getBlockState(VINE_POSITION).is(vineBlock),
                "Harvest from the canopy removed the perennial root"
        );
        require(
                helper,
                entity.vine().growthStage() == VineGrowthStage.DORMANT,
                "Harvest from the canopy did not enter DORMANT"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void harvestFromStemDelegatesToRoot(GameTestHelper helper) {
        VineBlock vineBlock = vine("red_grapevine");
        VineBlockEntity entity = placeVine(
                helper,
                vineBlock,
                vineAt(VineGrowthStage.HARVEST_READY, 0, true, 0.0, Vine.NO_HARVEST)
        );
        placeTrellis(helper, 3);
        attachStem(helper, vineBlock);
        helper.useBlock(VINE_POSITION.above());

        require(
                helper,
                helper.getBlockState(VINE_POSITION).is(vineBlock),
                "Harvest from the stem removed the perennial root"
        );
        require(
                helper,
                entity.vine().growthStage() == VineGrowthStage.DORMANT,
                "Harvest from the stem did not enter DORMANT"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void pruneFromStemDelegatesToRoot(GameTestHelper helper) {
        VineBlock vineBlock = vine("red_grapevine");
        VineBlockEntity entity = placeVine(
                helper,
                vineBlock,
                vineAt(VineGrowthStage.DORMANT, 1, true, 0.75, 42L)
        );
        placeTrellis(helper, 3);
        attachStem(helper, vineBlock);
        Player player = helper.makeMockPlayer();
        ItemStack shears = new ItemStack(item("pruning_shears"));
        if (shears.getItem() instanceof PruningShearsItem pruningShears) {
            pruningShears.setSelectedLevel(shears, PruningLevel.SEVERE);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, shears);
        BlockPos stemPos = VINE_POSITION.above();
        helper.getBlockState(stemPos).use(
                helper.getLevel(),
                player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(helper.absolutePos(stemPos)),
                        Direction.UP,
                        helper.absolutePos(stemPos),
                        false
                )
        );

        require(
                helper,
                entity.vine().pruningLevel() == PruningLevel.SEVERE,
                "Pruning from the stem did not apply the selected pruning level"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void stemStageTracksRootStage(GameTestHelper helper) {
        VineBlock vineBlock = vine("white_grapevine");
        placeVine(
                helper,
                vineBlock,
                vineAt(
                        VineVarieties.WHITE_GRAPE,
                        VineGrowthStage.FLOWERING,
                        0,
                        true,
                        0.25,
                        Vine.NO_HARVEST
                )
        );
        placeTrellis(helper, 3);
        growOnce(helper, vineBlock);
        BlockState stem = helper.getBlockState(VINE_POSITION.above());
        require(
                helper,
                stem.getBlock() instanceof VineStemBlock,
                "Flowering vine did not grow a stem"
        );
        require(
                helper,
                stem.getValue(VineStemBlock.STAGE)
                        == helper.getBlockState(VINE_POSITION).getValue(VineBlock.STAGE),
                "Stem stage did not match the root"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void stemIsNotRandomlyTicking(GameTestHelper helper) {
        VineBlock vineBlock = vine("red_grapevine");
        VineBlockEntity entity = placeVine(
                helper,
                vineBlock,
                vineAt(VineGrowthStage.VEGETATIVE, 0, false, 0.0, Vine.NO_HARVEST)
        );
        placeTrellis(helper, 3);
        growOnce(helper, vineBlock);
        Vine<ResourceId> before = entity.vine();
        BlockState stem = helper.getBlockState(VINE_POSITION.above());
        require(
                helper,
                stem.getBlock() instanceof VineStemBlock,
                "Expected a stem before ticking it"
        );
        require(
                helper,
                !stem.isRandomlyTicking(),
                "Stem is randomly ticking"
        );
        stem.getBlock().randomTick(
                stem,
                helper.getLevel(),
                helper.absolutePos(VINE_POSITION.above()),
                helper.getLevel().random
        );
        require(helper, entity.vine().equals(before), "Stem tick advanced the domain vine");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void grapeProviderMatchesLoadedMods(GameTestHelper helper) {
        Vine<ResourceId> mature = vineAt(
                VineGrowthStage.HARVEST_READY,
                0,
                true,
                0.0,
                Vine.NO_HARVEST
        );
        VineEnvironment ideal = ViticultureRuntime.shared()
                .settings()
                .forVariety(VineVarieties.RED_GRAPE)
                .harvest()
                .climateProfile()
                .idealEnvironment();
        ResourceId resolved = ViticultureRuntime.shared()
                .harvest(mature, ideal, true, 1L)
                .harvestItem();
        ResourceId expected = ModList.get().isLoaded(VineryIntegration.MOD_ID)
                ? VineryIntegration.RED_GRAPE
                : AlcoholicIds.RED_GRAPES;

        require(
                helper,
                resolved.equals(expected),
                "Resolved grape provider " + resolved + " instead of " + expected
        );
        helper.succeed();
    }

    private static void placeTrellis(GameTestHelper helper, int wireY) {
        Block post = block("vineyard_post");
        Block wire = block("trellis_wire");
        helper.setBlock(new BlockPos(0, wireY, 1), post.defaultBlockState());
        helper.setBlock(
                new BlockPos(1, wireY, 1),
                wire.defaultBlockState().setValue(
                        TrellisWireBlock.AXIS,
                        Direction.Axis.X
                )
        );
        helper.setBlock(new BlockPos(2, wireY, 1), post.defaultBlockState());
    }

    private static void growOnce(GameTestHelper helper, VineBlock vineBlock) {
        vineBlock.randomTick(
                helper.getBlockState(VINE_POSITION),
                helper.getLevel(),
                helper.absolutePos(VINE_POSITION),
                helper.getLevel().random
        );
    }

    private static void attachStem(GameTestHelper helper, VineBlock root) {
        Block stem = root.stemBlock();
        if (stem == null) {
            throw new IllegalStateException("Vine root has no stem block");
        }
        VineStage stage = helper.getBlockState(VINE_POSITION).getValue(VineBlock.STAGE);
        helper.setBlock(
                VINE_POSITION.above(),
                stem.defaultBlockState()
                        .setValue(VineStemBlock.STAGE, stage)
                        .setValue(VineStemBlock.TRAINED, true)
        );
        helper.setBlock(
                VINE_POSITION,
                helper.getBlockState(VINE_POSITION)
                        .setValue(VineBlock.EXTENDED, true)
                        .setValue(VineBlock.TRAINED, true)
        );
    }

    private static Item item(String path) {
        Item item = ForgeRegistries.ITEMS.getValue(
                ResourceLocation.fromNamespaceAndPath(AlcoholicIds.MOD_ID, path)
        );
        if (item == null) {
            throw new IllegalStateException("Missing registered item alcoholic:" + path);
        }
        return item;
    }

    private static int itemCountNear(GameTestHelper helper, BlockPos position, Item item) {
        return helper.getLevel()
                .getEntitiesOfClass(
                        ItemEntity.class,
                        new AABB(helper.absolutePos(position)).inflate(3.0)
                )
                .stream()
                .filter(entity -> entity.getItem().is(item))
                .mapToInt(entity -> entity.getItem().getCount())
                .sum();
    }

    private static VineBlockEntity placeVine(
            GameTestHelper helper,
            VineBlock block,
            Vine<ResourceId> vine
    ) {
        helper.setBlock(VINE_POSITION.below(), Blocks.DIRT.defaultBlockState());
        BlockState state = block.defaultBlockState()
                .setValue(VineBlock.STAGE, VineStage.fromDomain(vine.growthStage()))
                .setValue(VineBlock.TRAINED, false)
                .setValue(VineBlock.EXTENDED, false)
                .setValue(VineBlock.AGE, legacyAge(vine.growthStage()));
        helper.setBlock(VINE_POSITION, state);
        if (!(helper.getBlockEntity(VINE_POSITION) instanceof VineBlockEntity entity)) {
            helper.fail("Vine block entity was not created");
            throw new IllegalStateException("Vine block entity was not created");
        }
        entity.setVine(vine);
        return entity;
    }

    private static Vine<ResourceId> vineAt(
            VineGrowthStage stage,
            int ageCycles,
            boolean established,
            double progress,
            long lastHarvest
    ) {
        return vineAt(
                VineVarieties.RED_GRAPE,
                stage,
                ageCycles,
                established,
                progress,
                lastHarvest
        );
    }

    private static Vine<ResourceId> vineAt(
            VineVariety<ResourceId> variety,
            VineGrowthStage stage,
            int ageCycles,
            boolean established,
            double progress,
            long lastHarvest
    ) {
        return new Vine<>(
                variety,
                stage,
                ageCycles,
                established,
                VineHealth.HEALTHY,
                PruningLevel.BALANCED,
                progress,
                lastHarvest
        );
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

    private static VineBlock vine(String path) {
        return (VineBlock) block(path);
    }

    private static Block block(String path) {
        Block block = ForgeRegistries.BLOCKS.getValue(
                ResourceLocation.fromNamespaceAndPath(AlcoholicIds.MOD_ID, path)
        );
        if (block == null) {
            throw new IllegalStateException("Missing registered block alcoholic:" + path);
        }
        return block;
    }

    private static void require(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }
}

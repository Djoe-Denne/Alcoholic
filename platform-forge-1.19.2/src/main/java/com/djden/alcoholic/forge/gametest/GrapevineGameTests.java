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
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.viticulture.ViticultureRuntime;
import com.djden.alcoholic.api.ResourceId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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
        Block post = block("vineyard_post");
        Block wire = block("trellis_wire");
        helper.setBlock(new BlockPos(0, 2, 1), post.defaultBlockState());
        helper.setBlock(
                new BlockPos(1, 2, 1),
                wire.defaultBlockState().setValue(
                        TrellisWireBlock.AXIS,
                        Direction.Axis.X
                )
        );
        helper.setBlock(new BlockPos(2, 2, 1), post.defaultBlockState());

        require(
                helper,
                TrellisDetector.shared().isTrained(
                        helper.getLevel(),
                        helper.absolutePos(VINE_POSITION)
                ),
                "Wire segment bounded by posts was not detected as trained"
        );
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

    private static VineBlockEntity placeVine(
            GameTestHelper helper,
            VineBlock block,
            Vine<ResourceId> vine
    ) {
        BlockState state = block.defaultBlockState()
                .setValue(VineBlock.STAGE, VineStage.fromDomain(vine.growthStage()))
                .setValue(VineBlock.TRAINED, false)
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

package com.djden.alcoholic.forge.gametest;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.multiblock.ConfiguredPortMode;
import com.djden.alcoholic.minecraft.multiblock.HollowCuboidPlacer;
import com.djden.alcoholic.minecraft.multiblock.MultiblockControllerBlockEntity;
import com.djden.alcoholic.minecraft.multiblock.PortBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(AlcoholicIds.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraftGameTests {
    private static final BlockPos ORIGIN = new BlockPos(1, 1, 1);
    private static final BlockPos OTHER = new BlockPos(6, 1, 1);

    private CraftGameTests() {
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void outputItemPortExtractsCraftMaltFromLateralFace(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 3, 3, "craft_malt_house_controller", "craft_casing", null);
        BlockPos portPos = ORIGIN.offset(2, 1, 0);
        helper.setBlock(
                portPos,
                block("item_port").defaultBlockState().setValue(PortBlocks.MODE, ConfiguredPortMode.OUTPUT)
        );
        MultiblockControllerBlockEntity house = revalidate(helper, ORIGIN);
        require(helper, house.formed(), "Craft malt house did not form: " + house.debugDump());
        int output = house.layout().firstOutputSlot();
        house.setItem(output, new ItemStack(item("malted_barley"), 12));
        IItemHandler handler = helper.getBlockEntity(portPos)
                .getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.NORTH)
                .orElseThrow(IllegalStateException::new);
        ItemStack extracted = handler.extractItem(0, 64, false);
        require(
                helper,
                extracted.is(item("malted_barley")) && extracted.getCount() == 12,
                "Output malt-house port did not yield malted barley from NORTH"
        );
        require(helper, house.getItem(output).isEmpty(), "Malt-house output slot still held items");
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void formsMinimumCraftMashTun(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 3, 3, "craft_mash_tun_controller", "craft_casing", null);
        MultiblockControllerBlockEntity mash = revalidate(helper, ORIGIN);
        require(helper, mash.formed(), "Craft mash tun 3x3x3 did not form: " + mash.debugDump());
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void formsMaximumCraftMashTun(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 5, 5, 5, "craft_mash_tun_controller", "craft_casing", null);
        MultiblockControllerBlockEntity mash = revalidate(helper, ORIGIN);
        require(helper, mash.formed(), "Craft mash tun 5x5x5 did not form: " + mash.debugDump());
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void rejectsIndustrialCasingOnCraft(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 3, 3, "craft_mash_tun_controller", "industrial_casing", null);
        MultiblockControllerBlockEntity mash = revalidate(helper, ORIGIN);
        require(helper, !mash.formed(), "Craft mash tun formed on industrial casing");
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 1100)
    public static void craftMashTunProducesWortAndSpentGrain(GameTestHelper helper) {
        helper.setBlock(ORIGIN.below(), Blocks.MAGMA_BLOCK.defaultBlockState());
        buildHollow(helper, ORIGIN, 3, 3, 3, "craft_mash_tun_controller", "craft_casing", null);
        MultiblockControllerBlockEntity mash = revalidate(helper, ORIGIN);
        require(helper, mash.formed(), "Craft mash tun did not form: " + mash.debugDump());
        require(helper, mash.tank().capacity() == 2_000, "Unexpected min craft mash capacity " + mash.tank().capacity());
        mash.insert(new ItemStack(item("grist"), 1));
        mash.tank(MultiblockControllerBlockEntity.INPUT_TANK)
                .fill(LiquidBatch.of(ResourceId.parse("minecraft:water"), 1000, PropertyBag.empty()), false);
        helper.runAtTickTime(980, () -> {
            MultiblockControllerBlockEntity entity = controller(helper, ORIGIN);
            require(helper, entity.tank().contents().isPresent(), "Craft mash produced no liquid");
            require(
                    helper,
                    entity.tank().contents().orElseThrow().baseLiquid().filter(AlcoholicIds.WORT::equals).isPresent(),
                    "Craft mash did not produce wort"
            );
            require(
                    helper,
                    entity.getItem(MultiblockControllerBlockEntity.OUTPUT_SLOT).is(item("spent_grain")),
                    "Spent grain was not extractable"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 1100)
    public static void leftoverWaterStillProducesWort(GameTestHelper helper) {
        helper.setBlock(ORIGIN.below(), Blocks.MAGMA_BLOCK.defaultBlockState());
        buildHollow(helper, ORIGIN, 3, 3, 3, "craft_mash_tun_controller", "craft_casing", null);
        MultiblockControllerBlockEntity mash = revalidate(helper, ORIGIN);
        require(helper, mash.formed(), "Craft mash tun did not form: " + mash.debugDump());
        mash.insert(new ItemStack(item("grist"), 1));
        mash.tank(MultiblockControllerBlockEntity.INPUT_TANK)
                .fill(LiquidBatch.of(ResourceId.parse("minecraft:water"), 1500, PropertyBag.empty()), false);
        helper.runAtTickTime(980, () -> {
            MultiblockControllerBlockEntity entity = controller(helper, ORIGIN);
            require(
                    helper,
                    entity.tank().contents().orElseThrow().baseLiquid().filter(AlcoholicIds.WORT::equals).isPresent(),
                    "Leftover water did not produce wort"
            );
            require(
                    helper,
                    entity.tank(MultiblockControllerBlockEntity.INPUT_TANK).contents()
                            .orElseThrow()
                            .volumeMillibuckets() == 500,
                    "Expected 500 mB leftover water"
            );
            require(
                    helper,
                    entity.getItem(MultiblockControllerBlockEntity.OUTPUT_SLOT).is(item("spent_grain")),
                    "Spent grain was not extractable after leftover mash"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void craftMashTunCapacityScalesWithInterior(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 3, 3, "craft_mash_tun_controller", "craft_casing", null);
        MultiblockControllerBlockEntity small = revalidate(helper, ORIGIN);
        buildHollow(helper, OTHER, 5, 5, 5, "craft_mash_tun_controller", "craft_casing", null);
        MultiblockControllerBlockEntity large = revalidate(helper, OTHER);
        require(helper, small.formed() && large.formed(), "Craft mash tuns did not form");
        require(helper, small.tank().capacity() == 2_000, "Min craft mash capacity " + small.tank().capacity());
        require(helper, large.tank().capacity() == 27 * 2_000, "Max craft mash capacity " + large.tank().capacity());
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void wortMovesFromCraftMashToIndustrialKettle(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 3, 3, "craft_mash_tun_controller", "craft_casing", null);
        buildHollow(helper, OTHER, 3, 4, 3, "industrial_brewing_kettle_controller", "industrial_casing", null);
        helper.setBlock(ORIGIN.offset(2, 1, 0), block("fluid_port").defaultBlockState());
        helper.setBlock(OTHER.offset(0, 1, 0), block("fluid_port").defaultBlockState());
        MultiblockControllerBlockEntity mash = revalidate(helper, ORIGIN);
        MultiblockControllerBlockEntity kettle = revalidate(helper, OTHER);
        require(helper, mash.formed(), "Craft mash did not form: " + mash.debugDump());
        require(helper, kettle.formed(), "Industrial kettle did not form: " + kettle.debugDump());
        mash.tank().fill(LiquidBatch.of(AlcoholicIds.WORT, 1000, PropertyBag.empty()), false);
        IFluidHandler from = helper.getBlockEntity(ORIGIN.offset(2, 1, 0))
                .getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        IFluidHandler to = helper.getBlockEntity(OTHER.offset(0, 1, 0))
                .getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        FluidStack drained = from.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        require(helper, to.fill(drained, IFluidHandler.FluidAction.EXECUTE) == 1000, "Mixed-scale wort transfer failed");
        require(
                helper,
                kettle.tank().contents().flatMap(LiquidBatch::baseLiquid).filter(AlcoholicIds.WORT::equals).isPresent(),
                "Industrial kettle did not accept wort from the craft mash tun"
        );
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 1100)
    public static void craftMashTunStaysFormedAfterFullWaterMash(GameTestHelper helper) {
        helper.setBlock(ORIGIN.below(), Blocks.MAGMA_BLOCK.defaultBlockState());
        buildHollow(helper, ORIGIN, 3, 3, 3, "craft_mash_tun_controller", "craft_casing", null);
        MultiblockControllerBlockEntity mash = revalidate(helper, ORIGIN);
        require(helper, mash.formed(), "Craft mash tun did not form: " + mash.debugDump());
        mash.insert(new ItemStack(item("grist"), 1));
        mash.tank(MultiblockControllerBlockEntity.INPUT_TANK)
                .fill(LiquidBatch.of(ResourceId.parse("minecraft:water"), 2000, PropertyBag.empty()), false);
        helper.runAtTickTime(980, () -> {
            MultiblockControllerBlockEntity entity = controller(helper, ORIGIN);
            require(
                    helper,
                    entity.tank().contents().orElseThrow().baseLiquid().filter(AlcoholicIds.WORT::equals).isPresent(),
                    "Full-water craft mash did not produce wort"
            );
            MultiblockControllerBlockEntity revalidated = revalidate(helper, ORIGIN);
            require(
                    helper,
                    revalidated.formed(),
                    "Craft mash tun unformed after a full-water batch: " + revalidated.debugDump()
            );
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void craftMashTunStaysFormedWhenWaterRefilledWithWortPresent(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 3, 3, "craft_mash_tun_controller", "craft_casing", null);
        MultiblockControllerBlockEntity mash = revalidate(helper, ORIGIN);
        require(helper, mash.formed(), "Craft mash tun did not form: " + mash.debugDump());
        // Simulates post-process leftover + refill: each vessel fits the 2000 mB hull
        // on its own, but the sum (3000 mB) exceeds it. The validator must compare
        // the fullest vessel, not the sum.
        mash.tank().fill(LiquidBatch.of(AlcoholicIds.WORT, 1000, PropertyBag.empty()), false);
        mash.tank(MultiblockControllerBlockEntity.INPUT_TANK)
                .fill(LiquidBatch.of(ResourceId.parse("minecraft:water"), 2000, PropertyBag.empty()), false);
        MultiblockControllerBlockEntity revalidated = revalidate(helper, ORIGIN);
        require(
                helper,
                revalidated.formed(),
                "Craft mash tun unformed with full water over wort: " + revalidated.debugDump()
        );
        helper.succeed();
    }

    private static void buildHollow(
            GameTestHelper helper,
            BlockPos origin,
            int width,
            int height,
            int depth,
            String controller,
            String casing,
            String extraPort
    ) {
        HollowCuboidPlacer.place(
                helper.getLevel(),
                helper.absolutePos(origin),
                width,
                height,
                depth,
                block(controller),
                block(casing),
                extraPort == null ? null : block(extraPort)
        );
    }

    private static MultiblockControllerBlockEntity revalidate(GameTestHelper helper, BlockPos pos) {
        MultiblockControllerBlockEntity entity = controller(helper, pos);
        entity.markStructureDirty();
        MultiblockControllerBlockEntity.tick(entity.getLevel(), entity.getBlockPos(), entity.getBlockState(), entity);
        return entity;
    }

    private static MultiblockControllerBlockEntity controller(GameTestHelper helper, BlockPos pos) {
        return (MultiblockControllerBlockEntity) helper.getBlockEntity(pos);
    }

    private static Block block(String path) {
        Block value = ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath(AlcoholicIds.MOD_ID, path));
        if (value == null) {
            throw new IllegalStateException("Missing block alcoholic:" + path);
        }
        return value;
    }

    private static Item item(String path) {
        Item value = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(AlcoholicIds.MOD_ID, path));
        if (value == null) {
            throw new IllegalStateException("Missing item alcoholic:" + path);
        }
        return value;
    }

    private static void require(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }
}

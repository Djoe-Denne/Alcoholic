package com.djden.alcoholic.forge.gametest;

import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.process.BottleStandBlock;
import com.djden.alcoholic.minecraft.process.BottleStandBlockEntity;
import com.djden.alcoholic.minecraft.process.BottleStandNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

/** Behavioural coverage for the bounded, same-style stand network. */
@GameTestHolder(AlcoholicIds.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BottleStandGameTests {
    private static final BlockPos ORIGIN = new BlockPos(1, 1, 1);

    private BottleStandGameTests() {
    }

    @GameTest(template = "empty")
    public static void standOwnsNineSingleBottleCells(GameTestHelper helper) {
        BottleStandBlockEntity rack = place(helper, ORIGIN, "bottle_rack");
        ItemStack bottle = new ItemStack(item("beverage_bottle"), 12);
        for (int index = 0; index < BottleStandBlockEntity.SLOT_COUNT; index++) {
            require(helper, rack.insertOne(bottle), "stand rejected slot " + index);
        }
        require(helper, !rack.insertOne(bottle), "stand accepted a tenth bottle");
        require(helper, bottle.getCount() == 3, "stand did not insert exactly nine bottles");
        require(helper, !rack.insertOne(new ItemStack(Items.DIRT)), "stand accepted a non-bottle item");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sameStyleSameFacingStandsJoinAcrossWidthAndHeight(GameTestHelper helper) {
        place(helper, ORIGIN, "bottle_rack");
        place(helper, ORIGIN.east(), "bottle_rack");
        place(helper, ORIGIN.above(), "bottle_rack");
        place(helper, ORIGIN.above().east(), "bottle_rack");
        require(helper, network(helper, ORIGIN).rows() == 4, "2x2 rack grid did not expose four chest rows");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void differentStandStyleAndOversizedGridDoNotJoin(GameTestHelper helper) {
        place(helper, ORIGIN, "bottle_rack");
        place(helper, ORIGIN.east(), "bottle_shelf");
        require(helper, network(helper, ORIGIN).rows() == 1, "rack joined a shelf");

        for (int offset = 1; offset <= 3; offset++) {
            place(helper, ORIGIN.relative(net.minecraft.core.Direction.NORTH.getClockWise(), offset), "bottle_rack");
        }
        require(helper, network(helper, ORIGIN).rows() == 1, "four-wide rack grid exceeded the configured maximum");
        helper.succeed();
    }

    private static BottleStandNetwork network(GameTestHelper helper, BlockPos position) {
        Block block = helper.getBlockState(position).getBlock();
        return BottleStandNetwork.find(
                helper.getLevel(),
                helper.absolutePos(position),
                (BottleStandBlock) block
        );
    }

    private static BottleStandBlockEntity place(GameTestHelper helper, BlockPos position, String id) {
        helper.setBlock(position, block(id).defaultBlockState());
        return (BottleStandBlockEntity) helper.getBlockEntity(position);
    }

    private static Block block(String path) {
        Block block = ForgeRegistries.BLOCKS.getValue(new net.minecraft.resources.ResourceLocation(AlcoholicIds.MOD_ID, path));
        if (block == null || block == Blocks.AIR) {
            throw new IllegalStateException("Missing block alcoholic:" + path);
        }
        return block;
    }

    private static Item item(String path) {
        Item item = ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation(AlcoholicIds.MOD_ID, path));
        if (item == null || item == Items.AIR) {
            throw new IllegalStateException("Missing item alcoholic:" + path);
        }
        return item;
    }

    private static void require(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }
}

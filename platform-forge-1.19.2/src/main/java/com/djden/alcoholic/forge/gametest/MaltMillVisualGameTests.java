package com.djden.alcoholic.forge.gametest;

import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.mechanical.PrimitiveCombustionEngineBlockEntity;
import com.djden.alcoholic.minecraft.process.MaltMillBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(AlcoholicIds.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MaltMillVisualGameTests {
    private static final BlockPos MILL = new BlockPos(1, 1, 1);
    private static final BlockPos ENGINE = MILL.east();

    private MaltMillVisualGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void poweredEmptyMillStaysVisuallyIdle(GameTestHelper helper) {
        MaltMillBlockEntity mill = poweredMill(helper);
        helper.runAtTickTime(3, () -> {
            require(helper, !mill.visualRunning(), "Powered empty mill started its rollers");
            require(helper, !mill.visualGrinding(), "Powered empty mill emitted grinding state");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void unpoweredInputMillStaysVisuallyIdle(GameTestHelper helper) {
        MaltMillBlockEntity mill = placeMill(helper);
        mill.insert(new ItemStack(item("malted_barley")));
        helper.runAtTickTime(3, () -> {
            require(helper, !mill.visualRunning(), "Unpowered mill started its rollers");
            require(helper, !mill.visualGrinding(), "Unpowered mill emitted grinding state");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void poweredInvalidInputSpinsWithoutGrinding(GameTestHelper helper) {
        MaltMillBlockEntity mill = poweredMill(helper);
        mill.insert(new ItemStack(Items.DIRT));
        helper.runAtTickTime(3, () -> {
            require(helper, mill.visualRunning(), "Powered mill did not spin with an item inside");
            require(helper, !mill.visualGrinding(), "Invalid input enabled grinding particles");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void poweredMaltPublishesTransientGrindingState(GameTestHelper helper) {
        MaltMillBlockEntity mill = poweredMill(helper);
        mill.insert(new ItemStack(item("malted_barley")));
        helper.runAtTickTime(3, () -> {
            require(helper, mill.visualRunning(), "Powered mill did not spin with malt inside");
            require(helper, mill.visualGrinding(), "Valid malt did not enable grinding particles");
            require(helper, mill.getUpdateTag().getBoolean("VisualRunning"), "Running state missing from update tag");
            require(helper, mill.getUpdateTag().getBoolean("VisualGrinding"), "Grinding state missing from update tag");
            require(
                    helper,
                    !mill.saveWithoutMetadata().contains("VisualRunning")
                            && !mill.saveWithoutMetadata().contains("VisualGrinding"),
                    "Cosmetic visual state leaked into persistent NBT"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void millStopsOneTickAfterPowerIsRemoved(GameTestHelper helper) {
        MaltMillBlockEntity mill = poweredMill(helper);
        mill.insert(new ItemStack(item("malted_barley")));
        helper.runAtTickTime(3, () -> {
            require(helper, mill.visualRunning(), "Mill never entered its running visual state");
            helper.setBlock(ENGINE, Blocks.AIR.defaultBlockState());
        });
        helper.runAtTickTime(4, () -> {
            require(helper, !mill.visualRunning(), "Mill kept spinning after power was removed");
            require(helper, !mill.visualGrinding(), "Mill kept grinding after power was removed");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void millStopsOneTickAfterInputIsRemoved(GameTestHelper helper) {
        MaltMillBlockEntity mill = poweredMill(helper);
        mill.insert(new ItemStack(item("malted_barley")));
        helper.runAtTickTime(3, () -> {
            require(helper, mill.visualRunning(), "Mill never entered its running visual state");
            mill.setItem(MaltMillBlockEntity.INPUT_SLOT, ItemStack.EMPTY);
        });
        helper.runAtTickTime(4, () -> {
            require(helper, !mill.visualRunning(), "Mill kept spinning after its input was removed");
            require(helper, !mill.visualGrinding(), "Mill kept grinding after its input was removed");
            helper.succeed();
        });
    }

    private static MaltMillBlockEntity poweredMill(GameTestHelper helper) {
        MaltMillBlockEntity mill = placeMill(helper);
        helper.setBlock(ENGINE, block("primitive_combustion_engine").defaultBlockState());
        PrimitiveCombustionEngineBlockEntity engine =
                (PrimitiveCombustionEngineBlockEntity) helper.getBlockEntity(ENGINE);
        engine.insertFuel(new ItemStack(Items.COAL));
        PrimitiveCombustionEngineBlockEntity.serverTick(
                helper.getLevel(),
                helper.absolutePos(ENGINE),
                helper.getBlockState(ENGINE),
                engine
        );
        return mill;
    }

    private static MaltMillBlockEntity placeMill(GameTestHelper helper) {
        helper.setBlock(MILL, block("malt_mill").defaultBlockState());
        return (MaltMillBlockEntity) helper.getBlockEntity(MILL);
    }

    private static Block block(String path) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(AlcoholicIds.MOD_ID, path));
        if (block == null || block == Blocks.AIR) {
            throw new IllegalStateException("Missing block alcoholic:" + path);
        }
        return block;
    }

    private static Item item(String path) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(AlcoholicIds.MOD_ID, path));
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

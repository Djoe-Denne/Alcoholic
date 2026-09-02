package com.djden.alcoholic.forge.gametest;

import com.djden.alcoholic.domain.multiblock.CellCoord;
import com.djden.alcoholic.domain.multiblock.IndustrialHullPattern;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.debug.BeerLinePlacer;
import com.djden.alcoholic.minecraft.mechanical.PrimitiveCombustionEngineBlockEntity;
import com.djden.alcoholic.minecraft.multiblock.MultiblockControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(AlcoholicIds.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PlaceGameTests {
    private static final BlockPos ORIGIN = new BlockPos(1, 1, 1);

    private PlaceGameTests() {
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void placesArtisanalBeerLine(GameTestHelper helper) {
        BeerLinePlacer.placeArtisanal(helper.getLevel(), helper.absolutePos(ORIGIN));
        requireBlock(helper, ORIGIN, "malting_floor", "Malting floor missing");
        requireBlock(helper, ORIGIN.offset(2, 0, 0), "malt_mill", "Malt mill missing");
        requireBlock(helper, ORIGIN.offset(3, 0, 0), "primitive_combustion_engine", "Mill engine missing");
        require(
                helper,
                helper.getBlockState(ORIGIN.offset(5, 0, 0)).is(Blocks.MAGMA_BLOCK),
                "Mash heat pad was not magma"
        );
        requireBlock(helper, ORIGIN.offset(5, 1, 0), "mash_tun", "Mash tun missing");
        require(
                helper,
                helper.getBlockState(ORIGIN.offset(7, 0, 0)).is(Blocks.CAMPFIRE)
                        && helper.getBlockState(ORIGIN.offset(7, 0, 0)).getValue(BlockStateProperties.LIT),
                "Kettle heat pad was not a lit campfire"
        );
        requireBlock(helper, ORIGIN.offset(7, 1, 0), "brewing_kettle", "Brewing kettle missing");
        requireBlock(helper, ORIGIN.offset(9, 0, 0), "artisanal_fermenter", "Fermenter missing");
        if (!(helper.getBlockEntity(ORIGIN.offset(3, 0, 0)) instanceof PrimitiveCombustionEngineBlockEntity engine)) {
            helper.fail("Engine block entity missing");
            return;
        }
        require(helper, engine.getItem(0).is(Items.COAL), "Engine was not loaded with coal");
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void placesFormedCraftMashTun(GameTestHelper helper) {
        BeerLinePlacer.PlaceResult result = BeerLinePlacer.placeMachine(
                helper.getLevel(),
                "craft_mash_tun",
                helper.absolutePos(ORIGIN),
                null
        ).orElseThrow();
        require(helper, result.formed(), "Craft mash tun result was unformed: " + result.reason());
        BlockPos controllerPos = relative(helper, result.controller());
        requireBlock(helper, controllerPos, "craft_mash_tun_controller", "Craft mash controller missing");
        requireBlock(helper, ORIGIN.offset(2, 0, 0), "craft_casing", "Craft casing missing");
        if (!(helper.getBlockEntity(controllerPos) instanceof MultiblockControllerBlockEntity controller)) {
            helper.fail("Craft mash controller entity missing");
            return;
        }
        require(helper, controller.formed(), "Craft mash tun did not form: " + controller.debugDump());
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void placesFormedMaltHouse(GameTestHelper helper) {
        BeerLinePlacer.PlaceResult result = BeerLinePlacer.placeMachine(
                helper.getLevel(),
                "malt_house",
                helper.absolutePos(ORIGIN),
                null
        ).orElseThrow();
        require(helper, result.formed(), "Malt house result was unformed: " + result.reason());
        BlockPos controllerPos = relative(helper, result.controller());
        if (!(helper.getBlockEntity(controllerPos) instanceof MultiblockControllerBlockEntity controller)) {
            helper.fail("Malt house controller missing");
            return;
        }
        require(helper, controller.formed(), "Malt house did not form: " + controller.debugDump());
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void placesRollerMillWithPortAndEngine(GameTestHelper helper) {
        BeerLinePlacer.PlaceResult result = BeerLinePlacer.placeMachine(
                helper.getLevel(),
                "roller_mill",
                helper.absolutePos(ORIGIN),
                null
        ).orElseThrow();
        require(helper, result.formed(), "Roller mill result was unformed: " + result.reason());
        BlockPos controllerPos = relative(helper, result.controller());
        CellCoord port = IndustrialHullPattern.kineticPort(3, 4, 3);
        requireBlock(helper, controllerPos, "industrial_roller_mill_controller", "Roller mill controller missing");
        requireBlock(helper, ORIGIN.offset(port.x(), port.y(), port.z()), "kinetic_port", "Kinetic port missing");
        requireBlock(
                helper,
                ORIGIN.offset(port.x() + 1, port.y(), port.z()),
                "primitive_combustion_engine",
                "Roller mill engine missing"
        );
        if (!(helper.getBlockEntity(controllerPos) instanceof MultiblockControllerBlockEntity controller)) {
            helper.fail("Roller mill controller entity missing");
            return;
        }
        require(helper, controller.formed(), "Roller mill did not form: " + controller.debugDump());
        helper.succeed();
    }

    private static BlockPos relative(GameTestHelper helper, BlockPos absolute) {
        return absolute.subtract(helper.absolutePos(BlockPos.ZERO));
    }

    private static void requireBlock(GameTestHelper helper, BlockPos pos, String path, String message) {
        require(helper, helper.getBlockState(pos).is(block(path)), message + ": " + helper.getBlockState(pos));
    }

    private static Block block(String path) {
        Block value = ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath(AlcoholicIds.MOD_ID, path));
        if (value == null) {
            throw new IllegalStateException("Missing block alcoholic:" + path);
        }
        return value;
    }

    private static void require(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }
}

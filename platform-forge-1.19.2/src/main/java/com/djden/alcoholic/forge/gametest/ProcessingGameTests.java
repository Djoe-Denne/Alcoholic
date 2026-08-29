package com.djden.alcoholic.forge.gametest;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.application.viticulture.VineVarieties;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.minecraft.bottle.Bottling;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.process.ArtisanalFermenterBlockEntity;
import com.djden.alcoholic.minecraft.process.ArtisanalPressBlockEntity;
import com.djden.alcoholic.minecraft.process.OakBarrelBlockEntity;
import com.djden.alcoholic.minecraft.viticulture.HarvestLotNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(AlcoholicIds.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ProcessingGameTests {
    private static final BlockPos PRESS = new BlockPos(1, 1, 1);
    private static final BlockPos FERMENTER = new BlockPos(2, 1, 1);

    private ProcessingGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 250)
    public static void grapesPressIntoMust(GameTestHelper helper) {
        helper.setBlock(PRESS, block("artisanal_press").defaultBlockState());
        ArtisanalPressBlockEntity press = (ArtisanalPressBlockEntity) helper.getBlockEntity(PRESS);
        ItemStack grapes = new ItemStack(item("red_grapes"), 8);
        HarvestLotNbt.write(grapes, VineVarieties.RED_GRAPE.id(), 0.7, 0.82, 0.31);
        press.insert(grapes);
        helper.runAtTickTime(210, () -> {
            require(helper, press.tank().contents().isPresent(), "Press did not produce liquid");
            LiquidBatch batch = press.tank().contents().orElseThrow();
            require(
                    helper,
                    batch.baseLiquid().filter(AlcoholicIds.RED_GRAPE_MUST::equals).isPresent(),
                    "Pressed liquid was not red grape must"
            );
            require(
                    helper,
                    batch.number(ResourceId.parse("alcoholic:sugar"), 0.0) > 0.7,
                    "Harvest sugar did not transfer into must"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 250)
    public static void harvestBatchesStayDistinguishableAfterPress(GameTestHelper helper) {
        helper.setBlock(PRESS, block("artisanal_press").defaultBlockState());
        helper.setBlock(FERMENTER, block("artisanal_press").defaultBlockState());
        ArtisanalPressBlockEntity first = (ArtisanalPressBlockEntity) helper.getBlockEntity(PRESS);
        ArtisanalPressBlockEntity second = (ArtisanalPressBlockEntity) helper.getBlockEntity(FERMENTER);
        ItemStack highSugar = new ItemStack(item("red_grapes"), 8);
        ItemStack highAcid = new ItemStack(item("red_grapes"), 8);
        HarvestLotNbt.write(highSugar, VineVarieties.RED_GRAPE.id(), 0.5, 0.90, 0.20);
        HarvestLotNbt.write(highAcid, VineVarieties.RED_GRAPE.id(), 0.5, 0.20, 0.90);
        first.insert(highSugar);
        second.insert(highAcid);
        helper.runAtTickTime(210, () -> {
            double sugarA = first.tank().contents().orElseThrow()
                    .number(ResourceId.parse("alcoholic:sugar"), 0.0);
            double sugarB = second.tank().contents().orElseThrow()
                    .number(ResourceId.parse("alcoholic:sugar"), 0.0);
            require(helper, sugarA > sugarB, "Pressed batches were not distinguishable");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void pressFluidMovesIntoFermenter(GameTestHelper helper) {
        helper.setBlock(PRESS, block("artisanal_press").defaultBlockState());
        helper.setBlock(FERMENTER, block("artisanal_fermenter").defaultBlockState());
        ArtisanalPressBlockEntity press = (ArtisanalPressBlockEntity) helper.getBlockEntity(PRESS);
        ArtisanalFermenterBlockEntity fermenter = (ArtisanalFermenterBlockEntity) helper.getBlockEntity(FERMENTER);
        press.tank().fill(
                LiquidBatch.of(
                        AlcoholicIds.RED_GRAPE_MUST,
                        1000,
                        PropertyBag.empty().with(ResourceId.parse("alcoholic:sugar"), 0.8)
                ),
                false
        );
        IFluidHandler from = press.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        IFluidHandler to = fermenter.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        FluidStack drained = from.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        int filled = to.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        require(helper, filled == 1000, "Fluid did not move from press to fermenter");
        require(
                helper,
                fermenter.tank().contents().isPresent(),
                "Fermenter did not accept transferred must"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void fermenterConvertsMustProgressively(GameTestHelper helper) {
        helper.setBlock(FERMENTER, block("artisanal_fermenter").defaultBlockState());
        ArtisanalFermenterBlockEntity fermenter =
                (ArtisanalFermenterBlockEntity) helper.getBlockEntity(FERMENTER);
        fermenter.tank().fill(
                LiquidBatch.of(
                        AlcoholicIds.RED_GRAPE_MUST,
                        1000,
                        PropertyBag.empty()
                                .with(ResourceId.parse("alcoholic:sugar"), 0.80)
                                .with(ResourceId.parse("alcoholic:ethanol"), 0.0)
                ),
                false
        );
        fermenter.insertYeast(new ItemStack(item("yeast")));
        helper.runAtTickTime(320, () -> {
            LiquidBatch batch = fermenter.tank().contents().orElseThrow();
            require(
                    helper,
                    batch.number(ResourceId.parse("alcoholic:sugar"), 1.0) < 0.80,
                    "Sugar did not decrease"
            );
            require(
                    helper,
                    batch.number(ResourceId.parse("alcoholic:ethanol"), 0.0) > 0.0,
                    "Ethanol did not increase"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void pressFluidMovesIntoCreateTankWhenPresent(GameTestHelper helper) {
        if (!ModList.get().isLoaded("create")) {
            helper.succeed();
            return;
        }
        Block tankBlock = createFluidTank();
        helper.setBlock(PRESS, block("artisanal_press").defaultBlockState());
        helper.setBlock(FERMENTER, tankBlock.defaultBlockState());
        ArtisanalPressBlockEntity press = (ArtisanalPressBlockEntity) helper.getBlockEntity(PRESS);
        press.tank().fill(
                LiquidBatch.of(
                        AlcoholicIds.RED_GRAPE_MUST,
                        1000,
                        PropertyBag.empty().with(ResourceId.parse("alcoholic:sugar"), 0.75)
                ),
                false
        );
        IFluidHandler from = press.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        IFluidHandler tank = helper.getBlockEntity(FERMENTER)
                .getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        FluidStack drained = from.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        int filled = tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        require(helper, filled == 1000, "Create tank did not accept Distillery must");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void createTankFluidMovesIntoFermenterWhenPresent(GameTestHelper helper) {
        if (!ModList.get().isLoaded("create")) {
            helper.succeed();
            return;
        }
        Block tankBlock = createFluidTank();
        helper.setBlock(PRESS, tankBlock.defaultBlockState());
        helper.setBlock(FERMENTER, block("artisanal_fermenter").defaultBlockState());
        IFluidHandler tank = helper.getBlockEntity(PRESS)
                .getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        ArtisanalFermenterBlockEntity fermenter =
                (ArtisanalFermenterBlockEntity) helper.getBlockEntity(FERMENTER);
        IFluidHandler to = fermenter.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        net.minecraft.world.level.material.Fluid mustFluid = ForgeRegistries.FLUIDS.getValue(
                ResourceLocation.fromNamespaceAndPath(AlcoholicIds.MOD_ID, "red_grape_must")
        );
        require(
                helper,
                mustFluid != null && mustFluid != net.minecraft.world.level.material.Fluids.EMPTY,
                "Missing red grape must fluid"
        );
        FluidStack must = new FluidStack(mustFluid, 1000);
        int filledTank = tank.fill(must, IFluidHandler.FluidAction.EXECUTE);
        require(helper, filledTank == 1000, "Create tank did not accept must");
        FluidStack drained = tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        int filled = to.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        require(helper, filled == 1000, "Fermenter did not accept must from Create tank");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void fermenterFluidMovesIntoBarrel(GameTestHelper helper) {
        helper.setBlock(PRESS, block("artisanal_fermenter").defaultBlockState());
        helper.setBlock(FERMENTER, block("oak_barrel").defaultBlockState());
        ArtisanalFermenterBlockEntity fermenter =
                (ArtisanalFermenterBlockEntity) helper.getBlockEntity(PRESS);
        OakBarrelBlockEntity barrel = (OakBarrelBlockEntity) helper.getBlockEntity(FERMENTER);
        fermenter.tank().fill(youngWine(1000, 0.70), false);
        IFluidHandler from = fermenter.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        IFluidHandler to = barrel.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        FluidStack drained = from.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        int filled = to.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        require(helper, filled == 1000, "Fluid did not move from fermenter to barrel");
        require(helper, barrel.tank().contents().isPresent(), "Barrel did not accept young wine");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void oakBarrelAgesYoungRedWineThenBottles(GameTestHelper helper) {
        helper.setBlock(PRESS, block("oak_barrel").defaultBlockState());
        shelterBarrel(helper, PRESS);
        OakBarrelBlockEntity barrel = (OakBarrelBlockEntity) helper.getBlockEntity(PRESS);
        barrel.tank().fill(
                LiquidBatch.of(
                        AlcoholicIds.YOUNG_RED_WINE,
                        1000,
                        PropertyBag.empty()
                                .with(ResourceId.parse("alcoholic:sugar"), 0.12)
                                .with(ResourceId.parse("alcoholic:maturity"), 0.999)
                ),
                false
        );
        helper.runAtTickTime(160, () -> {
            OakBarrelBlockEntity aged = (OakBarrelBlockEntity) helper.getBlockEntity(PRESS);
            LiquidBatch batch = aged.tank().contents().orElseThrow();
            require(
                    helper,
                    batch.baseLiquid().filter(AlcoholicIds.RED_WINE::equals).isPresent(),
                    "Oak barrel did not finish AGE into red wine: "
                            + batch.baseLiquid().map(ResourceId::toString).orElse("-")
                            + " maturity="
                            + batch.number(ResourceId.parse("alcoholic:maturity"), 0.0)
            );
            Player player = helper.makeMockPlayer();
            ItemStack emptyBottle = new ItemStack(item("empty_bottle"));
            require(helper, Bottling.bottle(player, emptyBottle, aged.tank()), "Finished red wine should bottle");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void barrelPartialDrainKeepsRemainder(GameTestHelper helper) {
        helper.setBlock(PRESS, block("oak_barrel").defaultBlockState());
        OakBarrelBlockEntity barrel = (OakBarrelBlockEntity) helper.getBlockEntity(PRESS);
        barrel.tank().fill(youngWine(1000, 0.66), false);
        LiquidBatch extracted = barrel.tank().drain(400, false);
        require(helper, extracted.volumeMillibuckets() == 400, "Partial drain extracted the wrong volume");
        require(
                helper,
                barrel.tank().contents().orElseThrow().volumeMillibuckets() == 600,
                "Barrel remainder was not conserved"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void barrelPersistsBatchAcrossReload(GameTestHelper helper) {
        helper.setBlock(PRESS, block("oak_barrel").defaultBlockState());
        OakBarrelBlockEntity barrel = (OakBarrelBlockEntity) helper.getBlockEntity(PRESS);
        barrel.tank().fill(youngWine(800, 0.55), false);
        CompoundTag tag = barrel.saveWithoutMetadata();
        barrel.load(tag);
        require(helper, barrel.tank().contents().isPresent(), "Barrel lost contents after NBT reload");
        require(
                helper,
                barrel.tank().contents().orElseThrow().baseLiquid()
                        .filter(AlcoholicIds.YOUNG_RED_WINE::equals)
                        .isPresent(),
                "Barrel definition did not survive reload"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void barrelEmptyAndRefillPreservesHistory(GameTestHelper helper) {
        helper.setBlock(PRESS, block("oak_barrel").defaultBlockState());
        OakBarrelBlockEntity barrel = (OakBarrelBlockEntity) helper.getBlockEntity(PRESS);
        barrel.tank().fill(youngWine(500, 0.40), false);
        OakBarrelBlockEntity.serverTick(barrel.getLevel(), barrel.getBlockPos(), barrel.getBlockState(), barrel);
        barrel.tank().drain(500, false);
        OakBarrelBlockEntity.serverTick(barrel.getLevel(), barrel.getBlockPos(), barrel.getBlockState(), barrel);
        require(helper, barrel.history().usageCount() >= 1, "Emptying did not record barrel history");
        barrel.tank().fill(youngWine(500, 0.40), false);
        require(helper, !barrel.history().previousContents().isEmpty(), "Refill cleared previous contents");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void barrelFluidMovesIntoCreateTankWhenPresent(GameTestHelper helper) {
        if (!ModList.get().isLoaded("create")) {
            helper.succeed();
            return;
        }
        Block tankBlock = createFluidTank();
        helper.setBlock(PRESS, block("oak_barrel").defaultBlockState());
        helper.setBlock(FERMENTER, tankBlock.defaultBlockState());
        OakBarrelBlockEntity barrel = (OakBarrelBlockEntity) helper.getBlockEntity(PRESS);
        barrel.tank().fill(youngWine(1000, 0.73), false);
        IFluidHandler from = barrel.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        IFluidHandler tank = helper.getBlockEntity(FERMENTER)
                .getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        FluidStack drained = from.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        int filled = tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        require(helper, filled == 1000, "Create tank did not accept barrel liquid");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void createTankFluidMovesIntoBarrelWhenPresent(GameTestHelper helper) {
        if (!ModList.get().isLoaded("create")) {
            helper.succeed();
            return;
        }
        Block tankBlock = createFluidTank();
        helper.setBlock(PRESS, tankBlock.defaultBlockState());
        helper.setBlock(FERMENTER, block("oak_barrel").defaultBlockState());
        IFluidHandler tank = helper.getBlockEntity(PRESS)
                .getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        OakBarrelBlockEntity barrel = (OakBarrelBlockEntity) helper.getBlockEntity(FERMENTER);
        IFluidHandler to = barrel.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        net.minecraft.world.level.material.Fluid wine = ForgeRegistries.FLUIDS.getValue(
                ResourceLocation.fromNamespaceAndPath(AlcoholicIds.MOD_ID, "young_red_wine")
        );
        require(helper, wine != null && wine != net.minecraft.world.level.material.Fluids.EMPTY, "Missing young red wine fluid");
        FluidStack stack = new FluidStack(wine, 1000);
        require(helper, tank.fill(stack, IFluidHandler.FluidAction.EXECUTE) == 1000, "Create tank did not accept wine");
        FluidStack drained = tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        require(helper, to.fill(drained, IFluidHandler.FluidAction.EXECUTE) == 1000, "Barrel did not accept wine from Create");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void richMetadataRoundTripsThroughCreateWhenPresent(GameTestHelper helper) {
        if (!ModList.get().isLoaded("create")) {
            helper.succeed();
            return;
        }
        Block tankBlock = createFluidTank();
        helper.setBlock(PRESS, block("oak_barrel").defaultBlockState());
        helper.setBlock(FERMENTER, tankBlock.defaultBlockState());
        OakBarrelBlockEntity barrel = (OakBarrelBlockEntity) helper.getBlockEntity(PRESS);
        barrel.tank().fill(youngWine(1000, 0.88), false);
        IFluidHandler from = barrel.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        IFluidHandler tank = helper.getBlockEntity(FERMENTER)
                .getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        FluidStack moved = from.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        tank.fill(moved, IFluidHandler.FluidAction.EXECUTE);
        FluidStack back = tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        barrel.tank().fill(
                com.djden.alcoholic.forge.fluid.ForgeLiquidAdapter.fromStack(back).orElseThrow(),
                false
        );
        require(
                helper,
                barrel.tank().contents().orElseThrow().number(ResourceId.parse("alcoholic:sugar"), 0.0) > 0.80,
                "Create round-trip lost rich batch metadata"
        );
        helper.succeed();
    }

    private static void shelterBarrel(GameTestHelper helper, BlockPos origin) {
        helper.setBlock(origin.below(), Blocks.STONE.defaultBlockState());
        helper.setBlock(origin.above(), Blocks.STONE.defaultBlockState());
        for (Direction direction : new Direction[] {
                Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
        }) {
            helper.setBlock(origin.relative(direction), Blocks.STONE.defaultBlockState());
        }
    }

    private static LiquidBatch youngWine(int volume, double sugar) {
        return LiquidBatch.of(
                AlcoholicIds.YOUNG_RED_WINE,
                volume,
                PropertyBag.empty()
                        .with(ResourceId.parse("alcoholic:sugar"), sugar)
                        .with(ResourceId.parse("alcoholic:maturity"), 0.0)
        );
    }

    private static Block createFluidTank() {
        Block tankBlock = ForgeRegistries.BLOCKS.getValue(
                ResourceLocation.fromNamespaceAndPath("create", "fluid_tank")
        );
        if (tankBlock == null || tankBlock == net.minecraft.world.level.block.Blocks.AIR) {
            throw new IllegalStateException("Create is loaded but create:fluid_tank is missing");
        }
        return tankBlock;
    }

    private static Block block(String path) {
        Block block = ForgeRegistries.BLOCKS.getValue(
                ResourceLocation.fromNamespaceAndPath(AlcoholicIds.MOD_ID, path)
        );
        if (block == null) {
            throw new IllegalStateException("Missing block alcoholic:" + path);
        }
        return block;
    }

    private static Item item(String path) {
        Item item = ForgeRegistries.ITEMS.getValue(
                ResourceLocation.fromNamespaceAndPath(AlcoholicIds.MOD_ID, path)
        );
        if (item == null) {
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

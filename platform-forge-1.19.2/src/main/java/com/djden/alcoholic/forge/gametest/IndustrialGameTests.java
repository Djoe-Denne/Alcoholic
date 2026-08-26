package com.djden.alcoholic.forge.gametest;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.domain.multiblock.Box3;
import com.djden.alcoholic.domain.multiblock.PressStrokeState;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.mechanical.PrimitiveCombustionEngineBlockEntity;
import com.djden.alcoholic.minecraft.multiblock.HollowCuboidPlacer;
import com.djden.alcoholic.minecraft.multiblock.MultiblockControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(AlcoholicIds.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IndustrialGameTests {
    private static final BlockPos ORIGIN = new BlockPos(1, 1, 1);
    private static final BlockPos OTHER = new BlockPos(6, 1, 1);

    private IndustrialGameTests() {
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void formsMinimumTank(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_tank_controller", "industrial_casing", null);
        MultiblockControllerBlockEntity tank = revalidate(helper, ORIGIN);
        require(helper, tank.formed(), "Minimum tank did not form: " + tank.debugDump());
        require(helper, tank.tank().capacity() == 32_000, "Unexpected min tank capacity " + tank.tank().capacity());
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void formsLargerTank(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 5, 5, 5, "industrial_tank_controller", "industrial_casing", null);
        MultiblockControllerBlockEntity tank = revalidate(helper, ORIGIN);
        require(helper, tank.formed(), "Larger tank did not form: " + tank.debugDump());
        require(helper, tank.tank().capacity() == 27 * 16_000, "Unexpected large tank capacity " + tank.tank().capacity());
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void rejectsInvalidCasing(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_tank_controller", "industrial_casing", null);
        helper.setBlock(ORIGIN.offset(2, 0, 0), Blocks.STONE.defaultBlockState());
        MultiblockControllerBlockEntity tank = revalidate(helper, ORIGIN);
        require(helper, !tank.formed(), "Stone casing should keep the tank unformed");
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void rejectsBlockedInterior(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_tank_controller", "industrial_casing", null);
        helper.setBlock(ORIGIN.offset(1, 1, 1), Blocks.STONE.defaultBlockState());
        MultiblockControllerBlockEntity tank = revalidate(helper, ORIGIN);
        require(helper, !tank.formed(), "Blocked interior should keep the tank unformed");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void isolatedFluidPortRejectsFill(GameTestHelper helper) {
        helper.setBlock(ORIGIN, block("fluid_port").defaultBlockState());
        IFluidHandler handler = helper.getBlockEntity(ORIGIN)
                .getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        int filled = handler.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        require(helper, filled == 0, "Unbound fluid port accepted " + filled + " mB");
        require(helper, handler.drain(1000, IFluidHandler.FluidAction.EXECUTE).isEmpty(), "Unbound fluid port yielded liquid");
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void acceptsFluidPortOnShell(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_tank_controller", "industrial_casing", null);
        helper.setBlock(ORIGIN.offset(2, 1, 0), block("fluid_port").defaultBlockState());
        MultiblockControllerBlockEntity tank = revalidate(helper, ORIGIN);
        require(helper, tank.formed(), "Valid fluid port should keep the tank formed: " + tank.debugDump());
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void saveReloadKeepsFormedBatch(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_tank_controller", "industrial_casing", null);
        MultiblockControllerBlockEntity tank = revalidate(helper, ORIGIN);
        tank.tank().fill(must(2000, 0.77), false);
        CompoundTag tag = tank.saveWithoutMetadata();
        tank.load(tag);
        require(helper, tank.tank().contents().isPresent(), "Tank lost contents after NBT reload");
        require(
                helper,
                tank.tank().contents().orElseThrow().volumeMillibuckets() == 2000,
                "Tank volume was not conserved"
        );
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void breakAndReformKeepsContents(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_tank_controller", "industrial_casing", null);
        MultiblockControllerBlockEntity tank = revalidate(helper, ORIGIN);
        tank.tank().fill(must(1500, 0.66), false);
        BlockPos casing = ORIGIN.offset(2, 0, 0);
        helper.setBlock(casing, Blocks.AIR.defaultBlockState());
        tank = revalidate(helper, ORIGIN);
        require(helper, !tank.formed(), "Broken casing should unform the tank");
        require(helper, tank.tank().contents().orElseThrow().volumeMillibuckets() == 1500, "Unform deleted liquid");
        helper.setBlock(casing, block("industrial_casing").defaultBlockState());
        tank = revalidate(helper, ORIGIN);
        require(helper, tank.formed(), "Restored casing should reform the tank");
        require(helper, tank.tank().contents().orElseThrow().volumeMillibuckets() == 1500, "Reform duplicated or lost liquid");
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void refuseSmallerReformWhileOverCapacity(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 5, 5, 5, "industrial_tank_controller", "industrial_casing", null);
        MultiblockControllerBlockEntity tank = revalidate(helper, ORIGIN);
        tank.tank().fill(must(100_000, 0.5), false);
        helper.setBlock(ORIGIN.offset(4, 0, 0), Blocks.AIR.defaultBlockState());
        helper.setBlock(ORIGIN.offset(4, 1, 0), Blocks.AIR.defaultBlockState());
        helper.setBlock(ORIGIN.offset(4, 2, 0), Blocks.AIR.defaultBlockState());
        helper.setBlock(ORIGIN.offset(4, 3, 0), Blocks.AIR.defaultBlockState());
        helper.setBlock(ORIGIN.offset(4, 4, 0), Blocks.AIR.defaultBlockState());
        tank = revalidate(helper, ORIGIN);
        require(helper, !tank.formed(), "Over-capacity shrink should not reform");
        require(helper, tank.tank().contents().orElseThrow().volumeMillibuckets() == 100_000, "Resize deleted liquid");
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 80)
    public static void industrialPressExecutesGenericPress(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_press_controller", "industrial_casing", "kinetic_port");
        MultiblockControllerBlockEntity press = revalidate(helper, ORIGIN);
        require(helper, press.formed(), "Press did not form: " + press.debugDump());
        press.debugForceRpm(64);
        press.insert(new ItemStack(item("red_grapes"), 8));
        helper.runAtTickTime(20, () -> {
            MultiblockControllerBlockEntity entity = controller(helper, ORIGIN);
            require(helper, entity.tank().contents().isPresent(), "Industrial press produced no liquid");
            require(
                    helper,
                    entity.tank().contents().orElseThrow().baseLiquid()
                            .filter(AlcoholicIds.RED_GRAPE_MUST::equals)
                            .isPresent(),
                    "Industrial press did not produce must"
            );
            require(helper, !entity.getItem(1).isEmpty(), "Industrial press discarded pomace");
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 80)
    public static void industrialPressRunsFromPrimitiveEngine(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_press_controller", "industrial_casing", "kinetic_port");
        BlockPos enginePos = ORIGIN.offset(3, 0, 0);
        helper.setBlock(enginePos, block("primitive_combustion_engine").defaultBlockState());
        MultiblockControllerBlockEntity press = revalidate(helper, ORIGIN);
        require(helper, press.formed(), "Press did not form: " + press.debugDump());
        PrimitiveCombustionEngineBlockEntity engine =
                (PrimitiveCombustionEngineBlockEntity) helper.getBlockEntity(enginePos);
        engine.insertFuel(new ItemStack(net.minecraft.world.item.Items.COAL));
        PrimitiveCombustionEngineBlockEntity.serverTick(
                helper.getLevel(),
                helper.absolutePos(enginePos),
                helper.getBlockState(enginePos),
                engine
        );
        press.insert(new ItemStack(item("red_grapes"), 8));
        helper.runAtTickTime(20, () -> {
            MultiblockControllerBlockEntity entity = controller(helper, ORIGIN);
            require(
                    helper,
                    entity.tank().contents().isPresent(),
                    "Industrial press produced no liquid with the primitive engine: " + entity.debugDump()
            );
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 80)
    public static void industrialPressRunsFromElectricMotor(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_press_controller", "industrial_casing", "kinetic_port");
        BlockPos motorPos = ORIGIN.offset(3, 0, 0);
        helper.setBlock(motorPos, block("electric_motor").defaultBlockState());
        MultiblockControllerBlockEntity press = revalidate(helper, ORIGIN);
        require(helper, press.formed(), "Press did not form: " + press.debugDump());
        IEnergyStorage energy = helper.getBlockEntity(motorPos)
                .getCapability(ForgeCapabilities.ENERGY)
                .orElseThrow(IllegalStateException::new);
        for (int i = 0; i < 20; i++) {
            energy.receiveEnergy(80, false);
        }
        press.insert(new ItemStack(item("red_grapes"), 8));
        helper.runAtTickTime(20, () -> {
            MultiblockControllerBlockEntity entity = controller(helper, ORIGIN);
            require(
                    helper,
                    entity.tank().contents().isPresent(),
                    "Industrial press produced no liquid with the electric motor: " + entity.debugDump()
            );
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void industrialPressRequiresKineticPower(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_press_controller", "industrial_casing", "kinetic_port");
        MultiblockControllerBlockEntity press = revalidate(helper, ORIGIN);
        press.insert(new ItemStack(item("red_grapes"), 8));
        MultiblockControllerBlockEntity.tick(press.getLevel(), press.getBlockPos(), press.getBlockState(), press);
        require(helper, press.tank().contents().isEmpty(), "Press ran without kinetic power");
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void crushZoneOnlyDuringCompression(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_press_controller", "industrial_casing", "kinetic_port");
        MultiblockControllerBlockEntity press = revalidate(helper, ORIGIN);
        Box3 crush = press.crushVolume().orElseThrow();
        double cx = (crush.minX() + crush.maxX()) / 2.0;
        double cy = (crush.minY() + crush.maxY()) / 2.0;
        double cz = (crush.minZ() + crush.maxZ()) / 2.0;
        Box3 body = new Box3(cx - 0.3, cy - 0.4, cz - 0.3, cx + 0.3, cy + 0.4, cz + 0.3);
        press.debugSetStroke(PressStrokeState.IDLE);
        require(helper, !press.wouldCrush(body, cx, cy, cz), "Idle press crushed a player");
        press.debugSetStroke(PressStrokeState.LOADING);
        require(helper, !press.wouldCrush(body, cx, cy, cz), "Loading press crushed a player");
        press.debugSetStroke(PressStrokeState.COMPRESSING);
        require(helper, press.wouldCrush(body, cx, cy, cz), "Center occupancy was not lethal while compressing");
        Box3 edge = new Box3(crush.minX() - 0.4, crush.minY(), crush.minZ(), crush.minX() + 0.02, crush.minY() + 0.2, crush.minZ() + 0.2);
        require(helper, !press.wouldCrush(edge, crush.minX() - 0.2, crush.minY() + 0.1, crush.minZ() + 0.1), "Edge contact was lethal");
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void fluidMovesThroughIndustrialPort(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_press_controller", "industrial_casing", "kinetic_port");
        buildHollow(helper, OTHER, 3, 4, 3, "industrial_tank_controller", "industrial_casing", null);
        helper.setBlock(ORIGIN.offset(2, 1, 0), block("fluid_port").defaultBlockState());
        helper.setBlock(OTHER.offset(0, 1, 0), block("fluid_port").defaultBlockState());
        MultiblockControllerBlockEntity press = revalidate(helper, ORIGIN);
        MultiblockControllerBlockEntity tank = revalidate(helper, OTHER);
        press.tank().fill(must(1000, 0.71), false);
        IFluidHandler from = helper.getBlockEntity(ORIGIN.offset(2, 1, 0))
                .getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        IFluidHandler to = helper.getBlockEntity(OTHER.offset(0, 1, 0))
                .getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        FluidStack drained = from.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        require(helper, to.fill(drained, IFluidHandler.FluidAction.EXECUTE) == 1000, "Fluid port transfer failed");
        require(helper, tank.tank().contents().isPresent(), "Tank did not accept port transfer");
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void industrialTankMovesIntoCreateTankWhenPresent(GameTestHelper helper) {
        if (!ModList.get().isLoaded("create")) {
            helper.succeed();
            return;
        }
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_tank_controller", "industrial_casing", null);
        helper.setBlock(ORIGIN.offset(2, 1, 0), block("fluid_port").defaultBlockState());
        helper.setBlock(OTHER, createFluidTank().defaultBlockState());
        MultiblockControllerBlockEntity tank = revalidate(helper, ORIGIN);
        tank.tank().fill(must(1000, 0.8), false);
        IFluidHandler from = helper.getBlockEntity(ORIGIN.offset(2, 1, 0))
                .getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        IFluidHandler createTank = helper.getBlockEntity(OTHER)
                .getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        FluidStack drained = from.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        require(helper, createTank.fill(drained, IFluidHandler.FluidAction.EXECUTE) == 1000, "Create tank rejected industrial liquid");
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 80)
    public static void industrialMaltHouseExecutesGenericMalt(GameTestHelper helper) {
        helper.setBlock(ORIGIN.below(), Blocks.MAGMA_BLOCK.defaultBlockState());
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_malt_house_controller", "industrial_casing", null);
        MultiblockControllerBlockEntity house = revalidate(helper, ORIGIN);
        require(helper, house.formed(), "Malt house did not form: " + house.debugDump());
        house.insert(new ItemStack(item("barley"), 1));
        helper.runAtTickTime(8, () -> {
            MultiblockControllerBlockEntity mid = controller(helper, ORIGIN);
            require(helper, "steeping".equals(mid.processStage()), "Expected steeping, was " + mid.processStage());
        });
        helper.runAtTickTime(20, () -> {
            MultiblockControllerBlockEntity mid = controller(helper, ORIGIN);
            require(
                    helper,
                    "germination".equals(mid.processStage()) || "kilning".equals(mid.processStage()),
                    "Expected germination or kilning, was " + mid.processStage()
            );
        });
        helper.runAtTickTime(55, () -> {
            MultiblockControllerBlockEntity done = controller(helper, ORIGIN);
            require(
                    helper,
                    !done.getItem(MultiblockControllerBlockEntity.OUTPUT_SLOT).isEmpty(),
                    "Malt house produced no malted grain: " + done.debugDump()
            );
            require(
                    helper,
                    done.getItem(MultiblockControllerBlockEntity.OUTPUT_SLOT).is(item("malted_barley")),
                    "Malt house output was not malted barley"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 80)
    public static void industrialMaltHouseSurvivesSaveReloadMidProcess(GameTestHelper helper) {
        helper.setBlock(ORIGIN.below(), Blocks.MAGMA_BLOCK.defaultBlockState());
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_malt_house_controller", "industrial_casing", null);
        MultiblockControllerBlockEntity house = revalidate(helper, ORIGIN);
        house.insert(new ItemStack(item("barley"), 1));
        helper.runAtTickTime(12, () -> {
            MultiblockControllerBlockEntity mid = controller(helper, ORIGIN);
            require(helper, mid.processProgress() > 0, "Malt house had not started");
            String stage = mid.processStage();
            int progress = mid.processProgress();
            CompoundTag tag = mid.saveWithoutMetadata();
            mid.load(tag);
            require(helper, stage.equals(mid.processStage()), "Process stage was lost");
            require(helper, progress == mid.processProgress(), "Process progress was lost");
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 80)
    public static void industrialRollerMillExecutesGenericMill(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_roller_mill_controller", "industrial_casing", "kinetic_port");
        MultiblockControllerBlockEntity mill = revalidate(helper, ORIGIN);
        require(helper, mill.formed(), "Roller mill did not form: " + mill.debugDump());
        mill.debugForceRpm(16);
        mill.insert(new ItemStack(item("malted_barley"), 1));
        helper.runAtTickTime(30, () -> {
            MultiblockControllerBlockEntity entity = controller(helper, ORIGIN);
            require(
                    helper,
                    entity.getItem(MultiblockControllerBlockEntity.OUTPUT_SLOT).is(item("grist")),
                    "Roller mill did not produce grist: " + entity.debugDump()
            );
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 50)
    public static void industrialRollerMillStopsAdvancingUnderOutputBackpressure(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_roller_mill_controller", "industrial_casing", "kinetic_port");
        MultiblockControllerBlockEntity mill = revalidate(helper, ORIGIN);
        mill.debugForceRpm(16);
        mill.setItem(MultiblockControllerBlockEntity.OUTPUT_SLOT, new ItemStack(Items.DIRT));
        mill.insert(new ItemStack(item("malted_barley"), 1));
        helper.runAtTickTime(30, () -> {
            MultiblockControllerBlockEntity entity = controller(helper, ORIGIN);
            require(
                    helper,
                    entity.processProgress() == entity.processDuration(),
                    "Blocked roller mill continued advancing: " + entity.debugDump()
            );
            require(
                    helper,
                    entity.getItem(MultiblockControllerBlockEntity.INPUT_SLOT).is(item("malted_barley")),
                    "Blocked roller mill consumed its input"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void industrialRollerMillRequiresKineticPower(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_roller_mill_controller", "industrial_casing", "kinetic_port");
        MultiblockControllerBlockEntity mill = revalidate(helper, ORIGIN);
        mill.insert(new ItemStack(item("malted_barley"), 1));
        MultiblockControllerBlockEntity.tick(mill.getLevel(), mill.getBlockPos(), mill.getBlockState(), mill);
        require(
                helper,
                mill.getItem(MultiblockControllerBlockEntity.OUTPUT_SLOT).isEmpty(),
                "Roller mill ran without kinetic power"
        );
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 80)
    public static void industrialRollerMillRunsFromPrimitiveEngine(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_roller_mill_controller", "industrial_casing", "kinetic_port");
        BlockPos enginePos = ORIGIN.offset(3, 0, 0);
        helper.setBlock(enginePos, block("primitive_combustion_engine").defaultBlockState());
        MultiblockControllerBlockEntity mill = revalidate(helper, ORIGIN);
        PrimitiveCombustionEngineBlockEntity engine =
                (PrimitiveCombustionEngineBlockEntity) helper.getBlockEntity(enginePos);
        engine.insertFuel(new ItemStack(Items.COAL));
        PrimitiveCombustionEngineBlockEntity.serverTick(
                helper.getLevel(),
                helper.absolutePos(enginePos),
                helper.getBlockState(enginePos),
                engine
        );
        mill.insert(new ItemStack(item("malted_barley"), 1));
        helper.runAtTickTime(30, () -> {
            MultiblockControllerBlockEntity entity = controller(helper, ORIGIN);
            require(
                    helper,
                    entity.getItem(MultiblockControllerBlockEntity.OUTPUT_SLOT).is(item("grist")),
                    "Primitive engine did not drive the roller mill: " + entity.debugDump()
            );
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 80)
    public static void industrialRollerMillRunsFromElectricMotor(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_roller_mill_controller", "industrial_casing", "kinetic_port");
        BlockPos motorPos = ORIGIN.offset(3, 0, 0);
        helper.setBlock(motorPos, block("electric_motor").defaultBlockState());
        MultiblockControllerBlockEntity mill = revalidate(helper, ORIGIN);
        IEnergyStorage energy = helper.getBlockEntity(motorPos)
                .getCapability(ForgeCapabilities.ENERGY)
                .orElseThrow(IllegalStateException::new);
        for (int i = 0; i < 40; i++) {
            energy.receiveEnergy(80, false);
        }
        mill.insert(new ItemStack(item("malted_barley"), 1));
        helper.runAtTickTime(30, () -> {
            MultiblockControllerBlockEntity entity = controller(helper, ORIGIN);
            require(
                    helper,
                    entity.getItem(MultiblockControllerBlockEntity.OUTPUT_SLOT).is(item("grist")),
                    "Electric motor did not drive the roller mill: " + entity.debugDump()
            );
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void industrialRollerMillKeepsCreateAndCrossroadsOptional(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_roller_mill_controller", "industrial_casing", "kinetic_port");
        MultiblockControllerBlockEntity mill = revalidate(helper, ORIGIN);
        require(helper, mill.formed(), "Roller mill should form without Create or Crossroads");
        if (ModList.get().isLoaded("crossroads")) {
            require(
                    helper,
                    hasCrossroadsAxle(helper.getBlockEntity(ORIGIN.offset(2, 0, 0))),
                    "Crossroads is loaded but the kinetic port has no AXLE_CAPABILITY"
            );
        }
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 80)
    public static void industrialMashTunProducesWortAndSpentGrain(GameTestHelper helper) {
        helper.setBlock(ORIGIN.below(), Blocks.MAGMA_BLOCK.defaultBlockState());
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_mash_tun_controller", "industrial_casing", null);
        MultiblockControllerBlockEntity mash = revalidate(helper, ORIGIN);
        require(helper, mash.formed(), "Mash tun did not form: " + mash.debugDump());
        require(helper, mash.tank().capacity() == 16_000, "Unexpected min mash capacity " + mash.tank().capacity());
        mash.insert(new ItemStack(item("grist"), 1));
        mash.tank().fill(LiquidBatch.of(ResourceId.parse("minecraft:water"), 1000, PropertyBag.empty()), false);
        helper.runAtTickTime(50, () -> {
            MultiblockControllerBlockEntity entity = controller(helper, ORIGIN);
            require(helper, entity.tank().contents().isPresent(), "Industrial mash produced no liquid");
            require(
                    helper,
                    entity.tank().contents().orElseThrow().baseLiquid().filter(AlcoholicIds.WORT::equals).isPresent(),
                    "Industrial mash did not produce wort"
            );
            require(
                    helper,
                    entity.getItem(MultiblockControllerBlockEntity.OUTPUT_SLOT).is(item("spent_grain")),
                    "Spent grain was not extractable"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 140)
    public static void industrialMashStartsEachBatchWithAFreshClock(GameTestHelper helper) {
        helper.setBlock(ORIGIN.below(), Blocks.MAGMA_BLOCK.defaultBlockState());
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_mash_tun_controller", "industrial_casing", null);
        MultiblockControllerBlockEntity mash = revalidate(helper, ORIGIN);
        mash.insert(new ItemStack(item("grist"), 1));
        mash.tank().fill(LiquidBatch.of(ResourceId.parse("minecraft:water"), 1000, PropertyBag.empty()), false);

        helper.runAtTickTime(50, () -> {
            MultiblockControllerBlockEntity entity = controller(helper, ORIGIN);
            require(
                    helper,
                    entity.tank().contents().flatMap(LiquidBatch::baseLiquid).filter(AlcoholicIds.WORT::equals).isPresent(),
                    "First mash batch did not finish"
            );
            entity.tank().clear();
            entity.setItem(MultiblockControllerBlockEntity.OUTPUT_SLOT, ItemStack.EMPTY);
        });
        helper.runAtTickTime(80, () -> {
            MultiblockControllerBlockEntity entity = controller(helper, ORIGIN);
            entity.insert(new ItemStack(item("grist"), 1));
            entity.tank().fill(
                    LiquidBatch.of(ResourceId.parse("minecraft:water"), 1000, PropertyBag.empty()),
                    false
            );
        });
        helper.runAtTickTime(82, () -> {
            MultiblockControllerBlockEntity entity = controller(helper, ORIGIN);
            require(
                    helper,
                    entity.tank().contents().flatMap(LiquidBatch::baseLiquid)
                            .filter(ResourceId.parse("minecraft:water")::equals)
                            .isPresent(),
                    "Second mash inherited idle time and completed immediately"
            );
        });
        helper.runAtTickTime(130, () -> {
            MultiblockControllerBlockEntity entity = controller(helper, ORIGIN);
            require(
                    helper,
                    entity.tank().contents().flatMap(LiquidBatch::baseLiquid).filter(AlcoholicIds.WORT::equals).isPresent(),
                    "Second mash did not finish normally"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void industrialMashTunCapacityScalesWithInterior(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_mash_tun_controller", "industrial_casing", null);
        MultiblockControllerBlockEntity small = revalidate(helper, ORIGIN);
        buildHollow(helper, OTHER, 5, 5, 5, "industrial_mash_tun_controller", "industrial_casing", null);
        MultiblockControllerBlockEntity large = revalidate(helper, OTHER);
        require(helper, small.formed() && large.formed(), "Mash tuns did not form");
        require(helper, small.tank().capacity() == 16_000, "Min mash capacity " + small.tank().capacity());
        require(helper, large.tank().capacity() == 27 * 8_000, "Large mash capacity " + large.tank().capacity());
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 80)
    public static void industrialBrewingKettleBoilsWortWithHops(GameTestHelper helper) {
        helper.setBlock(
                ORIGIN.below(),
                Blocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.LIT, true)
        );
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_brewing_kettle_controller", "industrial_casing", null);
        MultiblockControllerBlockEntity kettle = revalidate(helper, ORIGIN);
        require(helper, kettle.formed(), "Kettle did not form: " + kettle.debugDump());
        kettle.tank().fill(
                LiquidBatch.of(
                        AlcoholicIds.WORT,
                        1000,
                        PropertyBag.empty().with(ResourceId.parse("alcoholic:sugar"), 0.77)
                ),
                false
        );
        kettle.insert(new ItemStack(item("hops"), 1));
        helper.runAtTickTime(50, () -> {
            MultiblockControllerBlockEntity entity = controller(helper, ORIGIN);
            LiquidBatch hopped = entity.tank().contents().orElseThrow();
            require(
                    helper,
                    hopped.baseLiquid().filter(AlcoholicIds.HOPPED_WORT::equals).isPresent(),
                    "Kettle did not produce hopped wort: " + entity.debugDump()
            );
            require(helper, hopped.number(ResourceId.parse("alcoholic:sugar"), 0.0) == 0.77, "Sugar was lost");
            require(
                    helper,
                    hopped.number(ResourceId.parse("alcoholic:bitterness"), 0.0) > 0.0,
                    "Bitterness was not extracted"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 70)
    public static void industrialBrewingKettleRejectsNonHopAdditions(GameTestHelper helper) {
        helper.setBlock(
                ORIGIN.below(),
                Blocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.LIT, true)
        );
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_brewing_kettle_controller", "industrial_casing", null);
        MultiblockControllerBlockEntity kettle = revalidate(helper, ORIGIN);
        kettle.tank().fill(LiquidBatch.of(AlcoholicIds.WORT, 1000, PropertyBag.empty()), false);
        kettle.insert(new ItemStack(Items.DIRT, 1));
        helper.runAtTickTime(50, () -> {
            MultiblockControllerBlockEntity entity = controller(helper, ORIGIN);
            require(
                    helper,
                    entity.tank().contents().flatMap(LiquidBatch::baseLiquid).filter(AlcoholicIds.WORT::equals).isPresent(),
                    "A non-hop addition incorrectly completed the boil"
            );
            require(helper, entity.getItem(MultiblockControllerBlockEntity.INPUT_SLOT).is(Items.DIRT),
                    "Rejected addition was consumed");
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void interruptedBoilRefundsCommittedHops(GameTestHelper helper) {
        helper.setBlock(
                ORIGIN.below(),
                Blocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.LIT, true)
        );
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_brewing_kettle_controller", "industrial_casing", null);
        MultiblockControllerBlockEntity kettle = revalidate(helper, ORIGIN);
        kettle.tank().fill(LiquidBatch.of(AlcoholicIds.WORT, 1000, PropertyBag.empty()), false);
        kettle.insert(new ItemStack(item("hops"), 1));
        helper.runAtTickTime(10, () -> controller(helper, ORIGIN).tank().clear());
        helper.runAtTickTime(15, () -> {
            MultiblockControllerBlockEntity entity = controller(helper, ORIGIN);
            require(
                    helper,
                    entity.getItem(MultiblockControllerBlockEntity.INPUT_SLOT).is(item("hops")),
                    "Interrupted boil did not refund its committed hops"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 200)
    public static void industrialVatFermentsHoppedWortThroughGenericFerment(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_vat_controller", "industrial_casing", null);
        MultiblockControllerBlockEntity vat = revalidate(helper, ORIGIN);
        require(helper, vat.formed(), "Vat did not form: " + vat.debugDump());
        vat.tank().fill(
                LiquidBatch.of(
                        AlcoholicIds.HOPPED_WORT,
                        1000,
                        PropertyBag.empty()
                                .with(ResourceId.parse("alcoholic:sugar"), 0.80)
                                .with(ResourceId.parse("alcoholic:ethanol"), 0.0)
                                .with(ResourceId.parse("alcoholic:bitterness"), 0.40)
                ),
                false
        );
        vat.insert(new ItemStack(item("yeast"), 1));
        helper.runAtTickTime(160, () -> {
            LiquidBatch batch = controller(helper, ORIGIN).tank().contents().orElseThrow();
            require(
                    helper,
                    batch.number(ResourceId.parse("alcoholic:sugar"), 1.0) < 0.80,
                    "Industrial vat did not consume sugar through generic FERMENT"
            );
            require(
                    helper,
                    batch.number(ResourceId.parse("alcoholic:ethanol"), 0.0) > 0.0,
                    "Industrial vat did not produce ethanol through generic FERMENT"
            );
            require(
                    helper,
                    batch.number(ResourceId.parse("alcoholic:bitterness"), 0.0) > 0.39,
                    "Bitterness was lost during generic FERMENT"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 120)
    public static void industrialConditioningVesselMaturesFinishedBeerOptionally(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_conditioning_vessel_controller", "industrial_casing", null);
        MultiblockControllerBlockEntity vessel = revalidate(helper, ORIGIN);
        require(helper, vessel.formed(), "Conditioning vessel did not form: " + vessel.debugDump());
        vessel.tank().fill(
                LiquidBatch.of(
                        AlcoholicIds.BEER,
                        1000,
                        PropertyBag.empty()
                                .with(ResourceId.parse("alcoholic:sugar"), 0.08)
                                .with(ResourceId.parse("alcoholic:ethanol"), 0.40)
                ),
                false
        );
        vessel.insert(new ItemStack(item("yeast"), 1));
        helper.runAtTickTime(80, () -> {
            LiquidBatch batch = controller(helper, ORIGIN).tank().contents().orElseThrow();
            require(
                    helper,
                    batch.baseLiquid().filter(AlcoholicIds.BEER::equals).isPresent(),
                    "Optional CONDITION did not mature beer: " + controller(helper, ORIGIN).debugDump()
            );
            helper.succeed();
        });
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void industrialTankDoesNotExecuteProcesses(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_tank_controller", "industrial_casing", null);
        MultiblockControllerBlockEntity tank = revalidate(helper, ORIGIN);
        tank.tank().fill(
                LiquidBatch.of(
                        AlcoholicIds.HOPPED_WORT,
                        1000,
                        PropertyBag.empty().with(ResourceId.parse("alcoholic:sugar"), 0.80)
                ),
                false
        );
        tank.insert(new ItemStack(item("yeast"), 1));
        MultiblockControllerBlockEntity.tick(tank.getLevel(), tank.getBlockPos(), tank.getBlockState(), tank);
        require(
                helper,
                tank.tank().contents().orElseThrow().baseLiquid().filter(AlcoholicIds.HOPPED_WORT::equals).isPresent(),
                "Passive tank mutated hopped wort"
        );
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void wortMovesFromMashTunToKettleThroughFluidPorts(GameTestHelper helper) {
        buildHollow(helper, ORIGIN, 3, 4, 3, "industrial_mash_tun_controller", "industrial_casing", null);
        buildHollow(helper, OTHER, 3, 4, 3, "industrial_brewing_kettle_controller", "industrial_casing", null);
        helper.setBlock(ORIGIN.offset(2, 1, 0), block("fluid_port").defaultBlockState());
        helper.setBlock(OTHER.offset(0, 1, 0), block("fluid_port").defaultBlockState());
        MultiblockControllerBlockEntity mash = revalidate(helper, ORIGIN);
        MultiblockControllerBlockEntity kettle = revalidate(helper, OTHER);
        mash.tank().fill(LiquidBatch.of(AlcoholicIds.WORT, 1000, PropertyBag.empty()), false);
        IFluidHandler from = helper.getBlockEntity(ORIGIN.offset(2, 1, 0))
                .getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        IFluidHandler to = helper.getBlockEntity(OTHER.offset(0, 1, 0))
                .getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        FluidStack drained = from.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        require(helper, to.fill(drained, IFluidHandler.FluidAction.EXECUTE) == 1000, "Wort port transfer failed");
        require(
                helper,
                kettle.tank().contents().flatMap(LiquidBatch::baseLiquid).filter(AlcoholicIds.WORT::equals).isPresent(),
                "Kettle did not accept wort from the mash tun"
        );
        helper.succeed();
    }

    private static boolean hasCrossroadsAxle(net.minecraft.world.level.block.entity.BlockEntity entity) {
        try {
            Class<?> capabilities = Class.forName("com.Da_Technomancer.crossroads.api.Capabilities");
            Object axle = capabilities.getField("AXLE_CAPABILITY").get(null);
            Class<?> capabilityClass = Class.forName("net.minecraftforge.common.capabilities.Capability");
            java.lang.reflect.Method getCapability = entity.getClass().getMethod(
                    "getCapability",
                    capabilityClass,
                    Direction.class
            );
            Object optional = getCapability.invoke(entity, axle, Direction.NORTH);
            return (boolean) optional.getClass().getMethod("isPresent").invoke(optional);
        } catch (ReflectiveOperationException exception) {
            return false;
        }
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

    private static LiquidBatch must(int volume, double sugar) {
        return LiquidBatch.of(
                AlcoholicIds.RED_GRAPE_MUST,
                volume,
                PropertyBag.empty().with(ResourceId.parse("alcoholic:sugar"), sugar)
        );
    }

    private static Block createFluidTank() {
        Block tankBlock = ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("create", "fluid_tank"));
        if (tankBlock == null || tankBlock == Blocks.AIR) {
            throw new IllegalStateException("Create is loaded but create:fluid_tank is missing");
        }
        return tankBlock;
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

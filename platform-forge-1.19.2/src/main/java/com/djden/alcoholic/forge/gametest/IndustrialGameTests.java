package com.djden.alcoholic.forge.gametest;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.domain.multiblock.Box3;
import com.djden.alcoholic.domain.multiblock.PressStrokeState;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.mechanical.PrimitiveCombustionEngineBlockEntity;
import com.djden.alcoholic.minecraft.multiblock.MultiblockControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    boolean shell = x == 0 || y == 0 || z == 0 || x == width - 1 || y == height - 1 || z == depth - 1;
                    BlockPos pos = origin.offset(x, y, z);
                    if (!shell) {
                        helper.setBlock(pos, Blocks.AIR.defaultBlockState());
                    } else if (x == 0 && y == 0 && z == 0) {
                        helper.setBlock(pos, block(controller).defaultBlockState());
                    } else if (extraPort != null && x == width - 1 && y == 0 && z == 0) {
                        helper.setBlock(pos, block(extraPort).defaultBlockState());
                    } else {
                        helper.setBlock(pos, block(casing).defaultBlockState());
                    }
                }
            }
        }
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

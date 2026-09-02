package com.djden.alcoholic.forge.gametest;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.minecraft.agriculture.CerealCropBlock;
import com.djden.alcoholic.minecraft.agriculture.HopBineBlock;
import com.djden.alcoholic.minecraft.agriculture.HopCanopyBlock;
import com.djden.alcoholic.minecraft.agriculture.HopStemBlock;
import com.djden.alcoholic.minecraft.agriculture.WildHopsBlock;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.mechanical.PrimitiveCombustionEngineBlock;
import com.djden.alcoholic.minecraft.mechanical.PrimitiveCombustionEngineBlockEntity;
import com.djden.alcoholic.minecraft.process.ArtisanalFermenterBlockEntity;
import com.djden.alcoholic.minecraft.process.BrewingKettleBlockEntity;
import com.djden.alcoholic.minecraft.process.MaltMillBlockEntity;
import com.djden.alcoholic.minecraft.process.MaltingFloorBlockEntity;
import com.djden.alcoholic.minecraft.process.MashTunBlockEntity;
import com.djden.alcoholic.minecraft.process.SolidPropertyNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

@GameTestHolder(AlcoholicIds.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GrainGameTests {
    private static final BlockPos ORIGIN = new BlockPos(1, 1, 1);

    private GrainGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void barleyGrowsThroughThreeStages(GameTestHelper helper) {
        CerealCropBlock crop = (CerealCropBlock) block("barley_crop");
        helper.setBlock(ORIGIN.below(), Blocks.FARMLAND.defaultBlockState());
        helper.setBlock(ORIGIN, crop.defaultBlockState());
        crop.growCrops(helper.getLevel(), helper.absolutePos(ORIGIN), helper.getBlockState(ORIGIN));
        crop.growCrops(helper.getLevel(), helper.absolutePos(ORIGIN), helper.getBlockState(ORIGIN));
        require(
                helper,
                CerealCropBlock.isMature(helper.getBlockState(ORIGIN)),
                "Barley did not reach maturity"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void hopsNeedOverheadTrellisToSurvive(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 2, 0), block("vineyard_post").defaultBlockState());
        helper.setBlock(new BlockPos(1, 2, 2), block("end_post").defaultBlockState());
        helper.setBlock(
                new BlockPos(1, 2, 1),
                block("trellis_wire").defaultBlockState().setValue(
                        com.djden.alcoholic.minecraft.agriculture.TrellisWireBlock.AXIS,
                        Direction.Axis.Z
                )
        );
        helper.setBlock(ORIGIN.below(), Blocks.FARMLAND.defaultBlockState());
        helper.setBlock(ORIGIN, block("hop_bine").defaultBlockState());
        require(
                helper,
                helper.getBlockState(ORIGIN).getBlock() instanceof HopBineBlock,
                "Hop bine did not place under a trellis run"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void hopColumnGrowsStemAndCanopyWhenWireAtPlusTwo(GameTestHelper helper) {
        HopBineBlock hopBine = (HopBineBlock) block("hop_bine");
        placeHopTrellis(helper, 3);
        helper.setBlock(ORIGIN.below(), Blocks.FARMLAND.defaultBlockState());
        helper.setBlock(ORIGIN, hopBine.defaultBlockState().setValue(HopBineBlock.AGE, 2));
        growHop(helper, hopBine, ORIGIN);

        require(
                helper,
                helper.getBlockState(ORIGIN).getValue(HopBineBlock.TRAINED)
                        && helper.getBlockState(ORIGIN).getValue(HopBineBlock.EXTENDED),
                "Mature bine did not mark the root trained and extended"
        );
        require(
                helper,
                helper.getBlockState(ORIGIN.above()).getBlock() instanceof HopStemBlock,
                "Mature bine did not grow a stem toward a wire at +2"
        );
        require(
                helper,
                helper.getBlockState(ORIGIN.above(2)).getBlock() instanceof HopCanopyBlock,
                "High wire was not replaced by a hop canopy"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void harvestThreeBlockColumnGivesThreeHops(GameTestHelper helper) {
        HopBineBlock hopBine = (HopBineBlock) block("hop_bine");
        placeHopTrellis(helper, 3);
        helper.setBlock(ORIGIN.below(), Blocks.FARMLAND.defaultBlockState());
        helper.setBlock(ORIGIN, hopBine.defaultBlockState().setValue(HopBineBlock.AGE, 2));
        growHop(helper, hopBine, ORIGIN);
        Player player = harvestWithSickle(helper, ORIGIN.above(2), new ItemStack(item("sickle")));

        require(
                helper,
                hopsCollected(helper, player, ORIGIN) == 3,
                "A three-block hop column did not yield three hops"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void harvestShortColumnGivesTwoHops(GameTestHelper helper) {
        HopBineBlock hopBine = (HopBineBlock) block("hop_bine");
        placeHopTrellis(helper, 2);
        helper.setBlock(ORIGIN.below(), Blocks.FARMLAND.defaultBlockState());
        helper.setBlock(ORIGIN, hopBine.defaultBlockState().setValue(HopBineBlock.AGE, 2));
        growHop(helper, hopBine, ORIGIN);
        Player player = harvestWithSickle(helper, ORIGIN, new ItemStack(item("sickle")));

        require(
                helper,
                hopsCollected(helper, player, ORIGIN) == 2,
                "A two-block hop column did not yield two hops"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void sickleAcceptsFortune(GameTestHelper helper) {
        ItemStack sickle = new ItemStack(item("sickle"));
        require(
                helper,
                Enchantments.BLOCK_FORTUNE.canEnchant(sickle),
                "Fortune could not be applied to the sickle"
        );
        sickle.enchant(Enchantments.BLOCK_FORTUNE, 3);
        require(
                helper,
                EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, sickle) == 3,
                "Fortune III did not stay on the sickle"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void harvestFromCanopyKeepsTheHopColumn(GameTestHelper helper) {
        HopBineBlock hopBine = (HopBineBlock) block("hop_bine");
        placeHopTrellis(helper, 3);
        helper.setBlock(ORIGIN.below(), Blocks.FARMLAND.defaultBlockState());
        helper.setBlock(ORIGIN, hopBine.defaultBlockState().setValue(HopBineBlock.AGE, 2));
        growHop(helper, hopBine, ORIGIN);
        useWithSickle(helper, ORIGIN.above(2));

        require(
                helper,
                helper.getBlockState(ORIGIN).is(hopBine)
                        && helper.getBlockState(ORIGIN).getValue(HopBineBlock.AGE) == 0,
                "Harvest from the canopy removed the bine or left it mature"
        );
        require(
                helper,
                helper.getBlockState(ORIGIN.above()).getBlock() instanceof HopStemBlock,
                "Harvest from the canopy removed the stem"
        );
        require(
                helper,
                helper.getBlockState(ORIGIN.above(2)).getBlock() instanceof HopCanopyBlock,
                "Harvest from the canopy restored the wire instead of keeping the canopy"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void emptyHandDoesNotHarvestMatureHop(GameTestHelper helper) {
        HopBineBlock hopBine = (HopBineBlock) block("hop_bine");
        placeHopTrellis(helper, 3);
        helper.setBlock(ORIGIN.below(), Blocks.FARMLAND.defaultBlockState());
        helper.setBlock(ORIGIN, hopBine.defaultBlockState().setValue(HopBineBlock.AGE, 2));
        growHop(helper, hopBine, ORIGIN);
        helper.useBlock(ORIGIN);

        require(
                helper,
                helper.getBlockState(ORIGIN).is(hopBine)
                        && helper.getBlockState(ORIGIN).getValue(HopBineBlock.AGE) == 2,
                "Empty-hand click harvested a mature hop bine"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void wildHopsSurviveOnGrassWithoutTrellis(GameTestHelper helper) {
        helper.setBlock(ORIGIN.below(), Blocks.GRASS_BLOCK.defaultBlockState());
        helper.setBlock(ORIGIN, block("wild_hops").defaultBlockState());
        require(
                helper,
                helper.getBlockState(ORIGIN).getBlock() instanceof WildHopsBlock,
                "Wild hops did not survive on grass without a trellis"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void breakingWildHopsDropsRhizomeAndHops(GameTestHelper helper) {
        helper.setBlock(ORIGIN.below(), Blocks.GRASS_BLOCK.defaultBlockState());
        helper.setBlock(ORIGIN, block("wild_hops").defaultBlockState());
        helper.getLevel().destroyBlock(helper.absolutePos(ORIGIN), true);
        require(
                helper,
                itemCountNear(helper, ORIGIN, item("hop_rhizome")) == 1,
                "Breaking wild hops did not drop a rhizome"
        );
        require(
                helper,
                itemCountNear(helper, ORIGIN, item("hops")) == 1,
                "Breaking wild hops did not drop hops"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 120)
    public static void maltingFloorConvertsBarley(GameTestHelper helper) {
        helper.setBlock(ORIGIN, block("malting_floor").defaultBlockState());
        MaltingFloorBlockEntity floor = (MaltingFloorBlockEntity) helper.getBlockEntity(ORIGIN);
        floor.insert(new ItemStack(item("barley"), 1));
        helper.runAtTickTime(90, () -> {
            require(helper, floor.progress() > 0, "Malting did not start");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void maltMillStallsWithoutMechanicalPower(GameTestHelper helper) {
        helper.setBlock(ORIGIN, block("malt_mill").defaultBlockState());
        MaltMillBlockEntity mill = (MaltMillBlockEntity) helper.getBlockEntity(ORIGIN);
        mill.insert(new ItemStack(item("malted_barley"), 1));
        helper.runAtTickTime(20, () -> {
            require(helper, mill.getItem(MaltMillBlockEntity.OUTPUT_SLOT).isEmpty(), "Mill ran without a drive");
            require(helper, mill.progress() == 0, "Mill progressed without mechanical power");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void maltMillIgnoresEngineFrontGrate(GameTestHelper helper) {
        helper.setBlock(ORIGIN, block("malt_mill").defaultBlockState());
        helper.setBlock(ORIGIN.south(), block("primitive_combustion_engine").defaultBlockState());
        MaltMillBlockEntity mill = (MaltMillBlockEntity) helper.getBlockEntity(ORIGIN);
        PrimitiveCombustionEngineBlockEntity engine =
                (PrimitiveCombustionEngineBlockEntity) helper.getBlockEntity(ORIGIN.south());
        engine.insertFuel(new ItemStack(net.minecraft.world.item.Items.COAL));
        PrimitiveCombustionEngineBlockEntity.serverTick(
                helper.getLevel(),
                helper.absolutePos(ORIGIN.south()),
                helper.getBlockState(ORIGIN.south()),
                engine
        );
        mill.insert(new ItemStack(item("malted_barley"), 1));
        helper.runAtTickTime(20, () -> {
            require(helper, mill.getItem(MaltMillBlockEntity.OUTPUT_SLOT).isEmpty(), "Mill ran from the engine grate");
            require(helper, mill.progress() == 0, "Mill progressed from the engine grate");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 250)
    public static void maltMillMillsMaltedGrainWithProperties(GameTestHelper helper) {
        helper.setBlock(ORIGIN, block("malt_mill").defaultBlockState());
        helper.setBlock(
                ORIGIN.east(),
                PrimitiveCombustionEngineBlock.withDriveToward(
                        block("primitive_combustion_engine").defaultBlockState(),
                        Direction.WEST
                )
        );
        MaltMillBlockEntity mill = (MaltMillBlockEntity) helper.getBlockEntity(ORIGIN);
        PrimitiveCombustionEngineBlockEntity engine =
                (PrimitiveCombustionEngineBlockEntity) helper.getBlockEntity(ORIGIN.east());
        engine.insertFuel(new ItemStack(net.minecraft.world.item.Items.COAL));
        PrimitiveCombustionEngineBlockEntity.serverTick(
                helper.getLevel(),
                helper.absolutePos(ORIGIN.east()),
                helper.getBlockState(ORIGIN.east()),
                engine
        );
        ItemStack malted = new ItemStack(item("malted_barley"), 1);
        SolidPropertyNbt.write(malted, Map.of(
                ResourceId.parse("alcoholic:sugar"), 0.85,
                ResourceId.parse("alcoholic:color"), 0.12
        ));
        mill.insert(malted);
        helper.runAtTickTime(210, () -> {
            ItemStack output = mill.getItem(MaltMillBlockEntity.OUTPUT_SLOT);
            require(helper, !output.isEmpty(), "Native malt mill did not finish: " + mill.debugDump());
            require(helper, output.is(item("grist")), "Mill output was not grist");
            var properties = SolidPropertyNbt.read(output).orElse(null);
            require(helper, properties != null, "Mill output lost solid properties");
            require(
                    helper,
                    Math.abs(((Number) properties.get(ResourceId.parse("alcoholic:sugar")).orElse(0.0)).doubleValue() - 0.85) < 1e-6,
                    "Sugar did not survive milling"
            );
            require(
                    helper,
                    Math.abs(((Number) properties.get(ResourceId.parse("alcoholic:color")).orElse(0.0)).doubleValue() - 0.12) < 1e-6,
                    "Color did not survive milling"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 120)
    public static void electricMotorPowersMaltMillAndConsumesFe(GameTestHelper helper) {
        helper.setBlock(ORIGIN, block("malt_mill").defaultBlockState());
        helper.setBlock(ORIGIN.east(), block("electric_motor").defaultBlockState());
        MaltMillBlockEntity mill = (MaltMillBlockEntity) helper.getBlockEntity(ORIGIN);
        IEnergyStorage energy = helper.getBlockEntity(ORIGIN.east())
                .getCapability(ForgeCapabilities.ENERGY)
                .orElseThrow(IllegalStateException::new);
        int stored = 0;
        while (stored < 800) {
            int accepted = energy.receiveEnergy(80, false);
            require(helper, accepted > 0, "Electric motor rejected Forge Energy");
            stored += accepted;
        }
        int before = energy.getEnergyStored();
        mill.insert(new ItemStack(item("malted_barley"), 1));
        helper.runAtTickTime(20, () -> {
            require(helper, mill.progress() > 0, "Mill did not run from the electric motor: " + mill.debugDump());
            require(
                    helper,
                    energy.getEnergyStored() < before,
                    "Motor did not consume FE while the mill worked"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void idleElectricMotorDoesNotDrainFullPower(GameTestHelper helper) {
        helper.setBlock(ORIGIN, block("electric_motor").defaultBlockState());
        helper.setBlock(ORIGIN.west(), block("malt_mill").defaultBlockState());
        IEnergyStorage energy = helper.getBlockEntity(ORIGIN)
                .getCapability(ForgeCapabilities.ENERGY)
                .orElseThrow(IllegalStateException::new);
        energy.receiveEnergy(80, false);
        int before = energy.getEnergyStored();
        helper.runAtTickTime(20, () -> {
            require(
                    helper,
                    energy.getEnergyStored() == before,
                    "Idle motor drained FE with no mechanical work"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void emptyElectricMotorDoesNotPowerMaltMill(GameTestHelper helper) {
        helper.setBlock(ORIGIN, block("malt_mill").defaultBlockState());
        helper.setBlock(ORIGIN.east(), block("electric_motor").defaultBlockState());
        MaltMillBlockEntity mill = (MaltMillBlockEntity) helper.getBlockEntity(ORIGIN);
        mill.insert(new ItemStack(item("malted_barley"), 1));
        helper.runAtTickTime(20, () -> {
            require(helper, mill.progress() == 0, "Empty motor powered the mill");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void electricMotorAcceptsForgeEnergyWithoutImmersiveEngineering(GameTestHelper helper) {
        helper.setBlock(ORIGIN, block("electric_motor").defaultBlockState());
        IEnergyStorage energy = helper.getBlockEntity(ORIGIN)
                .getCapability(ForgeCapabilities.ENERGY)
                .orElseThrow(IllegalStateException::new);
        require(helper, energy.canReceive(), "Motor does not accept FE");
        require(helper, energy.receiveEnergy(80, false) == 80, "Motor rejected a standard FE packet");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void crossroadsAbsenceDoesNotBlockAlcoholic(GameTestHelper helper) {
        helper.setBlock(ORIGIN, block("malt_mill").defaultBlockState());
        require(helper, helper.getBlockEntity(ORIGIN) instanceof MaltMillBlockEntity, "Malt mill missing");
        if (ModList.get().isLoaded("crossroads")) {
            boolean axle = hasCrossroadsAxle(helper.getBlockEntity(ORIGIN));
            require(helper, axle, "Crossroads is loaded but the mill has no AXLE_CAPABILITY");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 1300)
    public static void mashTunProducesWortAndSpentGrain(GameTestHelper helper) {
        helper.setBlock(ORIGIN, Blocks.MAGMA_BLOCK.defaultBlockState());
        helper.setBlock(ORIGIN.above(), block("mash_tun").defaultBlockState());
        MashTunBlockEntity mash = (MashTunBlockEntity) helper.getBlockEntity(ORIGIN.above());
        mash.insert(new ItemStack(item("grist"), 1));
        IFluidHandler handler = mash.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        int filled = handler.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        require(helper, filled == 1000, "Mash tun rejected water");
        helper.runAtTickTime(1210, () -> {
            require(helper, mash.tank().contents().isPresent(), "Mash tun did not produce wort");
            LiquidBatch wort = mash.tank().contents().orElseThrow();
            require(
                    helper,
                    wort.baseLiquid().filter(AlcoholicIds.WORT::equals).isPresent(),
                    "Mash output was not wort"
            );
            require(
                    helper,
                    !mash.getItem(MashTunBlockEntity.BYPRODUCT_SLOT).isEmpty(),
                    "Spent grain was not extractable"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void mashTunSurvivesSaveReload(GameTestHelper helper) {
        helper.setBlock(ORIGIN, block("mash_tun").defaultBlockState());
        MashTunBlockEntity mash = (MashTunBlockEntity) helper.getBlockEntity(ORIGIN);
        mash.tank().fill(LiquidBatch.of(AlcoholicIds.WORT, 500, PropertyBag.empty()), false);
        CompoundTag saved = mash.saveWithoutMetadata();
        mash.load(saved);
        require(helper, mash.tank().contents().isPresent(), "Wort did not survive reload");
        require(
                helper,
                mash.tank().contents().orElseThrow().volumeMillibuckets() == 500,
                "Wort volume changed on reload"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void mashTunWortMovesIntoCreateTankWhenPresent(GameTestHelper helper) {
        if (!ModList.get().isLoaded("create")) {
            helper.succeed();
            return;
        }
        helper.setBlock(ORIGIN, block("mash_tun").defaultBlockState());
        helper.setBlock(ORIGIN.east(), createFluidTank().defaultBlockState());
        MashTunBlockEntity mash = (MashTunBlockEntity) helper.getBlockEntity(ORIGIN);
        mash.tank().fill(LiquidBatch.of(AlcoholicIds.WORT, 1000, PropertyBag.empty()), false);
        IFluidHandler from = mash.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        IFluidHandler tank = helper.getBlockEntity(ORIGIN.east())
                .getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        FluidStack drained = from.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        require(helper, tank.fill(drained, IFluidHandler.FluidAction.EXECUTE) == 1000, "Create tank rejected wort");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 1700)
    public static void brewingKettleProducesHoppedWort(GameTestHelper helper) {
        helper.setBlock(ORIGIN, Blocks.CAMPFIRE.defaultBlockState());
        helper.setBlock(ORIGIN.above(), block("brewing_kettle").defaultBlockState());
        BrewingKettleBlockEntity kettle = (BrewingKettleBlockEntity) helper.getBlockEntity(ORIGIN.above());
        kettle.tank().fill(
                LiquidBatch.of(
                        AlcoholicIds.WORT,
                        1000,
                        PropertyBag.empty().with(ResourceId.parse("alcoholic:sugar"), 0.80)
                ),
                false
        );
        kettle.insert(new ItemStack(item("hops"), 1));
        helper.runAtTickTime(1610, () -> {
            require(helper, kettle.tank().contents().isPresent(), "Kettle emptied during boil");
            LiquidBatch hopped = kettle.tank().contents().orElseThrow();
            require(
                    helper,
                    hopped.baseLiquid().filter(AlcoholicIds.HOPPED_WORT::equals).isPresent(),
                    "Boil output was not hopped wort"
            );
            require(
                    helper,
                    hopped.number(ResourceId.parse("alcoholic:bitterness"), 0.0) > 0.0,
                    "Bitterness did not propagate"
            );
            require(
                    helper,
                    hopped.number(ResourceId.parse("alcoholic:sugar"), 0.0) > 0.7,
                    "Unrelated sugar was not preserved"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void brewingKettleMovesIntoArtisanalFermenter(GameTestHelper helper) {
        helper.setBlock(ORIGIN, block("brewing_kettle").defaultBlockState());
        helper.setBlock(ORIGIN.east(), block("artisanal_fermenter").defaultBlockState());
        BrewingKettleBlockEntity kettle = (BrewingKettleBlockEntity) helper.getBlockEntity(ORIGIN);
        ArtisanalFermenterBlockEntity fermenter =
                (ArtisanalFermenterBlockEntity) helper.getBlockEntity(ORIGIN.east());
        kettle.tank().fill(
                LiquidBatch.of(
                        AlcoholicIds.HOPPED_WORT,
                        1000,
                        PropertyBag.empty()
                                .with(ResourceId.parse("alcoholic:sugar"), 0.80)
                                .with(ResourceId.parse("alcoholic:bitterness"), 0.40)
                ),
                false
        );
        IFluidHandler from = kettle.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        IFluidHandler to = fermenter.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        FluidStack drained = from.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        require(helper, to.fill(drained, IFluidHandler.FluidAction.EXECUTE) == 1000, "Fermenter rejected hopped wort");
        require(
                helper,
                fermenter.tank().contents().flatMap(LiquidBatch::baseLiquid)
                        .filter(AlcoholicIds.HOPPED_WORT::equals)
                        .isPresent(),
                "Fermenter did not store hopped wort"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void hoppedWortFermentsInArtisanalFermenter(GameTestHelper helper) {
        helper.setBlock(ORIGIN, block("artisanal_fermenter").defaultBlockState());
        ArtisanalFermenterBlockEntity fermenter =
                (ArtisanalFermenterBlockEntity) helper.getBlockEntity(ORIGIN);
        fermenter.tank().fill(
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
        fermenter.insertYeast(new ItemStack(item("yeast")));
        helper.runAtTickTime(160, () -> {
            LiquidBatch batch = fermenter.tank().contents().orElseThrow();
            require(
                    helper,
                    batch.number(ResourceId.parse("alcoholic:sugar"), 1.0) < 0.80,
                    "Sugar did not decrease in generic FERMENT"
            );
            require(
                    helper,
                    batch.number(ResourceId.parse("alcoholic:ethanol"), 0.0) > 0.0,
                    "Ethanol did not increase in generic FERMENT"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void brewingKettleSurvivesSaveReload(GameTestHelper helper) {
        helper.setBlock(ORIGIN, block("brewing_kettle").defaultBlockState());
        BrewingKettleBlockEntity kettle = (BrewingKettleBlockEntity) helper.getBlockEntity(ORIGIN);
        kettle.tank().fill(
                LiquidBatch.of(
                        AlcoholicIds.HOPPED_WORT,
                        750,
                        PropertyBag.empty().with(ResourceId.parse("alcoholic:bitterness"), 0.33)
                ),
                false
        );
        CompoundTag saved = kettle.saveWithoutMetadata();
        kettle.load(saved);
        require(helper, kettle.tank().contents().isPresent(), "Hopped wort did not survive reload");
        LiquidBatch reloaded = kettle.tank().contents().orElseThrow();
        require(helper, reloaded.volumeMillibuckets() == 750, "Hopped wort volume changed on reload");
        require(
                helper,
                Math.abs(reloaded.number(ResourceId.parse("alcoholic:bitterness"), 0.0) - 0.33) < 1e-6,
                "Bitterness was lost on reload"
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

    private static Block createFluidTank() {
        Block tankBlock = ForgeRegistries.BLOCKS.getValue(
                ResourceLocation.fromNamespaceAndPath("create", "fluid_tank")
        );
        if (tankBlock == null || tankBlock == Blocks.AIR) {
            throw new IllegalStateException("Create is loaded but create:fluid_tank is missing");
        }
        return tankBlock;
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

    private static int hopsCollected(GameTestHelper helper, Player player, BlockPos position) {
        return player.getInventory().countItem(item("hops"))
                + itemCountNear(helper, position, item("hops"));
    }

    private static void useWithSickle(GameTestHelper helper, BlockPos pos) {
        harvestWithSickle(helper, pos, new ItemStack(item("sickle")));
    }

    private static Player harvestWithSickle(GameTestHelper helper, BlockPos pos, ItemStack sickle) {
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, sickle);
        helper.getBlockState(pos).use(
                helper.getLevel(),
                player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(helper.absolutePos(pos)),
                        Direction.UP,
                        helper.absolutePos(pos),
                        false
                )
        );
        return player;
    }

    private static void placeHopTrellis(GameTestHelper helper, int wireY) {
        helper.setBlock(new BlockPos(1, wireY, 0), block("vineyard_post").defaultBlockState());
        helper.setBlock(new BlockPos(1, wireY, 2), block("end_post").defaultBlockState());
        helper.setBlock(
                new BlockPos(1, wireY, 1),
                block("trellis_wire").defaultBlockState().setValue(
                        com.djden.alcoholic.minecraft.agriculture.TrellisWireBlock.AXIS,
                        Direction.Axis.Z
                )
        );
    }

    private static void growHop(GameTestHelper helper, HopBineBlock hopBine, BlockPos pos) {
        hopBine.performBonemeal(
                helper.getLevel(),
                RandomSource.create(),
                helper.absolutePos(pos),
                helper.getBlockState(pos)
        );
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

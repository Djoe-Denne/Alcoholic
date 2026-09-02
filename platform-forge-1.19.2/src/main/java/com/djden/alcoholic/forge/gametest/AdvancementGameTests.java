package com.djden.alcoholic.forge.gametest;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.viticulture.VineVarieties;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.domain.viticulture.PruningLevel;
import com.djden.alcoholic.domain.viticulture.Vine;
import com.djden.alcoholic.domain.viticulture.VineGrowthStage;
import com.djden.alcoholic.domain.viticulture.VineHealth;
import com.djden.alcoholic.minecraft.advancement.AdvancementHooks;
import com.djden.alcoholic.minecraft.agriculture.CerealCropBlock;
import com.djden.alcoholic.minecraft.agriculture.HopBineBlock;
import com.djden.alcoholic.minecraft.agriculture.VineBlock;
import com.djden.alcoholic.minecraft.agriculture.VineBlockEntity;
import com.djden.alcoholic.minecraft.agriculture.VineStage;
import com.djden.alcoholic.minecraft.bottle.Bottling;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.multiblock.IndustrialHullPlacer;
import com.djden.alcoholic.minecraft.multiblock.MultiblockControllerBlockEntity;
import com.djden.alcoholic.minecraft.process.ArtisanalFermenterBlockEntity;
import com.djden.alcoholic.minecraft.process.ArtisanalPressBlockEntity;
import com.djden.alcoholic.minecraft.process.OakBarrelBlockEntity;
import com.djden.alcoholic.minecraft.viticulture.HarvestLotNbt;
import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

@GameTestHolder(AlcoholicIds.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AdvancementGameTests {
    private static final BlockPos ORIGIN = new BlockPos(1, 1, 1);

    private AdvancementGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void harvestingGrapesGrantsHarvestAdvancement(GameTestHelper helper) {
        VineBlock vineBlock = (VineBlock) block("red_grapevine");
        helper.setBlock(ORIGIN.below(), Blocks.DIRT.defaultBlockState());
        helper.setBlock(
                ORIGIN,
                vineBlock.defaultBlockState()
                        .setValue(VineBlock.STAGE, VineStage.fromDomain(VineGrowthStage.HARVEST_READY))
                        .setValue(VineBlock.TRAINED, false)
                        .setValue(VineBlock.EXTENDED, false)
                        .setValue(VineBlock.AGE, 4)
        );
        if (!(helper.getBlockEntity(ORIGIN) instanceof VineBlockEntity entity)) {
            helper.fail("Vine block entity was not created");
            return;
        }
        entity.setVine(new Vine<>(
                VineVarieties.RED_GRAPE,
                VineGrowthStage.HARVEST_READY,
                0,
                true,
                VineHealth.HEALTHY,
                PruningLevel.BALANCED,
                0.0,
                Vine.NO_HARVEST
        ));
        ServerPlayer player = mockPlayer(helper);
        use(helper, ORIGIN, player, new ItemStack(item("sickle")));
        requireDone(helper, player, "harvest_grapes");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void harvestingBarleyGrantsHarvestAdvancement(GameTestHelper helper) {
        CerealCropBlock barley = (CerealCropBlock) block("barley_crop");
        helper.setBlock(ORIGIN.below(), Blocks.FARMLAND.defaultBlockState());
        helper.setBlock(ORIGIN, barley.defaultBlockState().setValue(CerealCropBlock.AGE, 2));
        ServerPlayer player = mockPlayer(helper);
        BlockState mature = helper.getBlockState(ORIGIN);
        barley.playerDestroy(
                helper.getLevel(),
                player,
                helper.absolutePos(ORIGIN),
                mature,
                null,
                ItemStack.EMPTY
        );
        requireDone(helper, player, "harvest_barley");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void harvestingHopsGrantsHarvestAdvancement(GameTestHelper helper) {
        HopBineBlock hopBine = (HopBineBlock) block("hop_bine");
        helper.setBlock(ORIGIN, hopBine.defaultBlockState().setValue(HopBineBlock.AGE, 2));
        ServerPlayer player = mockPlayer(helper);
        require(
                helper,
                hopBine.harvestColumn(player, helper.getLevel(), helper.absolutePos(ORIGIN), helper.getBlockState(ORIGIN)),
                "Mature hop bine did not harvest"
        );
        requireDone(helper, player, "harvest_hops");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 250)
    public static void pressWithLastActorGrantsMustAdvancement(GameTestHelper helper) {
        helper.setBlock(ORIGIN, block("artisanal_press").defaultBlockState());
        ArtisanalPressBlockEntity press = (ArtisanalPressBlockEntity) helper.getBlockEntity(ORIGIN);
        ServerPlayer player = mockPlayer(helper);
        ItemStack grapes = new ItemStack(item("red_grapes"), 8);
        HarvestLotNbt.write(grapes, VineVarieties.RED_GRAPE.id(), 0.7, 0.82, 0.31);
        player.setItemInHand(InteractionHand.MAIN_HAND, grapes);
        use(helper, ORIGIN, player, grapes);
        helper.runAtTickTime(210, () -> {
            require(helper, press.tank().contents().isPresent(), "Press did not produce must");
            requireDone(helper, player, "produce_must");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void bottlingGrantsBottleAdvancement(GameTestHelper helper) {
        helper.setBlock(ORIGIN, block("oak_barrel").defaultBlockState());
        OakBarrelBlockEntity barrel = (OakBarrelBlockEntity) helper.getBlockEntity(ORIGIN);
        barrel.tank().fill(
                LiquidBatch.of(
                        AlcoholicIds.YOUNG_RED_WINE,
                        1000,
                        PropertyBag.empty().with(ResourceId.parse("alcoholic:sugar"), 0.1)
                ),
                false
        );
        ServerPlayer player = mockPlayer(helper);
        ItemStack bottle = new ItemStack(item("empty_bottle"));
        player.setItemInHand(InteractionHand.MAIN_HAND, bottle);
        require(helper, Bottling.bottle(player, bottle, barrel.tank()), "Bottling failed");
        requireDone(helper, player, "bottle");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void offlineProcessCompletionFlushesOnNextUse(GameTestHelper helper) {
        helper.setBlock(ORIGIN, block("artisanal_fermenter").defaultBlockState());
        ArtisanalFermenterBlockEntity fermenter =
                (ArtisanalFermenterBlockEntity) helper.getBlockEntity(ORIGIN);
        fermenter.advancementState().assignActor(UUID.randomUUID());
        fermenter.advancementState().complete(
                helper.getLevel(),
                AdvancementHooks.location(BuiltinRegistrations.FERMENT),
                AdvancementHooks.location(AlcoholicIds.YOUNG_RED_WINE)
        );
        require(helper, fermenter.advancementState().hasPending(), "Offline completion did not queue");
        ServerPlayer player = mockPlayer(helper);
        use(helper, ORIGIN, player, ItemStack.EMPTY);
        require(helper, !fermenter.advancementState().hasPending(), "Pending criteria were not flushed");
        requireDone(helper, player, "ferment_beverage");
        helper.succeed();
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 40)
    public static void formingIndustrialTankGrantsFormAdvancement(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        BlockPos origin = helper.absolutePos(ORIGIN);
        BlockPos controller = IndustrialHullPlacer.place(
                helper.getLevel(),
                origin,
                3,
                4,
                3,
                block("industrial_tank_controller"),
                block("industrial_casing"),
                block("machine_window"),
                block("access_hatch"),
                block("item_port"),
                block("fluid_port"),
                block("kinetic_port"),
                false
        );
        if (!(helper.getLevel().getBlockEntity(controller) instanceof MultiblockControllerBlockEntity tank)) {
            helper.fail("Industrial tank controller was not created");
            return;
        }
        AdvancementHooks.touch(player, tank);
        tank.markStructureDirty();
        MultiblockControllerBlockEntity.tick(tank.getLevel(), tank.getBlockPos(), tank.getBlockState(), tank);
        require(helper, tank.formed(), "Min hull did not form: " + tank.debugDump());
        requireDone(helper, player, "form_industrial_tank");
        helper.succeed();
    }

    private static ServerPlayer mockPlayer(GameTestHelper helper) {
        if (helper.makeMockPlayer() instanceof ServerPlayer player) {
            return player;
        }
        ServerLevel level = helper.getLevel();
        ServerPlayer player = new ServerPlayer(
                level.getServer(),
                level,
                new GameProfile(UUID.randomUUID(), "GameTest"),
                null
        ) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return true;
            }
        };
        new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player);
        return player;
    }

    private static void use(GameTestHelper helper, BlockPos pos, ServerPlayer player, ItemStack held) {
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        BlockState state = helper.getBlockState(pos);
        state.use(
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
    }

    private static void requireDone(GameTestHelper helper, ServerPlayer player, String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(AlcoholicIds.MOD_ID, path);
        Advancement advancement = helper.getLevel().getServer().getAdvancements().getAdvancement(id);
        require(helper, advancement != null, "Missing advancement alcoholic:" + path);
        require(
                helper,
                player.getAdvancements().getOrStartProgress(advancement).isDone(),
                "Advancement alcoholic:" + path + " was not granted"
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

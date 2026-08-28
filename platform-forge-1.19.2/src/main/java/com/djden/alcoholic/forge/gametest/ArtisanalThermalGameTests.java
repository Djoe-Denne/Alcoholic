package com.djden.alcoholic.forge.gametest;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.process.BrewingKettleBlockEntity;
import com.djden.alcoholic.minecraft.process.MashTunBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(AlcoholicIds.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ArtisanalThermalGameTests {
    private static final BlockPos ORIGIN = new BlockPos(1, 1, 1);

    private ArtisanalThermalGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void mashTunAdvancesOnMagma(GameTestHelper helper) {
        helper.setBlock(ORIGIN, Blocks.MAGMA_BLOCK.defaultBlockState());
        helper.setBlock(ORIGIN.above(), block("mash_tun").defaultBlockState());
        MashTunBlockEntity mash = (MashTunBlockEntity) helper.getBlockEntity(ORIGIN.above());
        mash.insert(new ItemStack(item("grist"), 1));
        IFluidHandler handler = mash.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        int filled = handler.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        require(helper, filled == 1000, "Mash tun rejected water");
        helper.runAtTickTime(50, () -> {
            require(helper, mash.progress() > 0 || mash.tank().contents().isPresent(), "Mash did not advance on magma");
            require(helper, mash.tank().contents().isPresent(), "Mash tun did not produce wort on magma");
            LiquidBatch wort = mash.tank().contents().orElseThrow();
            require(
                    helper,
                    wort.baseLiquid().filter(AlcoholicIds.WORT::equals).isPresent(),
                    "Mash output was not wort"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void mashTunDoesNotCompleteOnLitFurnace(GameTestHelper helper) {
        helper.setBlock(
                ORIGIN,
                Blocks.FURNACE.defaultBlockState().setValue(BlockStateProperties.LIT, true)
        );
        helper.setBlock(ORIGIN.above(), block("mash_tun").defaultBlockState());
        MashTunBlockEntity mash = (MashTunBlockEntity) helper.getBlockEntity(ORIGIN.above());
        mash.insert(new ItemStack(item("grist"), 1));
        IFluidHandler handler = mash.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new);
        int filled = handler.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        require(helper, filled == 1000, "Mash tun rejected water");
        helper.runAtTickTime(50, () -> {
            require(helper, mash.progress() == 0, "Mash progressed outside the operating band");
            require(helper, mash.tank().contents().isEmpty(), "Mash completed on a lit furnace");
            require(
                    helper,
                    !mash.getItem(MashTunBlockEntity.INPUT_SLOT).isEmpty(),
                    "Grist was consumed while stalled"
            );
            require(
                    helper,
                    mash.tank(MashTunBlockEntity.INPUT_TANK).contents().isPresent(),
                    "Water was consumed while stalled"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void brewingKettleAdvancesOnCampfire(GameTestHelper helper) {
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
        helper.runAtTickTime(50, () -> {
            require(
                    helper,
                    kettle.progress() > 0 || kettle.tank().contents()
                            .flatMap(LiquidBatch::baseLiquid)
                            .filter(AlcoholicIds.HOPPED_WORT::equals)
                            .isPresent(),
                    "Kettle did not advance on campfire"
            );
            require(helper, kettle.tank().contents().isPresent(), "Kettle emptied during boil");
            LiquidBatch hopped = kettle.tank().contents().orElseThrow();
            require(
                    helper,
                    hopped.baseLiquid().filter(AlcoholicIds.HOPPED_WORT::equals).isPresent(),
                    "Boil output was not hopped wort"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void brewingKettleDoesNotCompleteOnMagma(GameTestHelper helper) {
        helper.setBlock(ORIGIN, Blocks.MAGMA_BLOCK.defaultBlockState());
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
        helper.runAtTickTime(50, () -> {
            require(helper, kettle.progress() == 0, "Kettle progressed outside the operating band");
            require(helper, kettle.tank().contents().isPresent(), "Kettle emptied while stalled");
            LiquidBatch wort = kettle.tank().contents().orElseThrow();
            require(
                    helper,
                    wort.baseLiquid().filter(AlcoholicIds.WORT::equals).isPresent(),
                    "Kettle completed on magma"
            );
            require(
                    helper,
                    !kettle.getItem(BrewingKettleBlockEntity.ADDITION_SLOT).isEmpty(),
                    "Hops were consumed while stalled"
            );
            helper.succeed();
        });
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

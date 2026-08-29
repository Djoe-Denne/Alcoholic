package com.djden.alcoholic.forge.gametest;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.minecraft.bottle.Bottling;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.process.OakBarrelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(AlcoholicIds.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BottlingGuardGameTests {
    private static final BlockPos ORIGIN = new BlockPos(1, 1, 1);

    private BottlingGuardGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void bottlesYoungRedWine(GameTestHelper helper) {
        require(helper, bottle(helper, AlcoholicIds.YOUNG_RED_WINE), "Young red wine should bottle");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void bottlesRedWine(GameTestHelper helper) {
        require(helper, bottle(helper, AlcoholicIds.RED_WINE), "Finished red wine should bottle");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void bottlesBeer(GameTestHelper helper) {
        require(helper, bottle(helper, AlcoholicIds.BEER), "Beer should bottle");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void refusesRedGrapeMust(GameTestHelper helper) {
        OakBarrelBlockEntity barrel = barrelWith(helper, AlcoholicIds.RED_GRAPE_MUST, 1000);
        LiquidBatch before = barrel.tank().contents().orElseThrow();
        Player player = helper.makeMockPlayer();
        ItemStack emptyBottle = new ItemStack(item("empty_bottle"));
        player.setItemInHand(InteractionHand.MAIN_HAND, emptyBottle);
        require(helper, !Bottling.bottle(player, emptyBottle, barrel.tank()), "Red grape must must not bottle");
        require(helper, barrel.tank().contents().isPresent(), "Must tank was cleared");
        LiquidBatch after = barrel.tank().contents().orElseThrow();
        require(helper, after.volume() == before.volume(), "Must tank volume changed");
        require(helper, after.baseLiquid().equals(before.baseLiquid()), "Must tank liquid changed");
        require(helper, emptyBottle.getCount() == 1, "Empty bottle was consumed");
        helper.succeed();
    }

    private static boolean bottle(GameTestHelper helper, ResourceId liquid) {
        OakBarrelBlockEntity barrel = barrelWith(helper, liquid, 1000);
        Player player = helper.makeMockPlayer();
        ItemStack emptyBottle = new ItemStack(item("empty_bottle"));
        player.setItemInHand(InteractionHand.MAIN_HAND, emptyBottle);
        return Bottling.bottle(player, emptyBottle, barrel.tank());
    }

    private static OakBarrelBlockEntity barrelWith(GameTestHelper helper, ResourceId liquid, double volume) {
        helper.setBlock(ORIGIN, block("oak_barrel").defaultBlockState());
        if (!(helper.getBlockEntity(ORIGIN) instanceof OakBarrelBlockEntity barrel)) {
            throw new IllegalStateException("Missing oak barrel entity");
        }
        barrel.tank().fill(LiquidBatch.of(liquid, volume, PropertyBag.empty()), false);
        return barrel;
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

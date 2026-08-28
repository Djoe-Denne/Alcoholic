package com.djden.alcoholic.forge.gametest;

import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.process.ArtisanalPressBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(AlcoholicIds.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PressInsertGameTests {
    private static final BlockPos ORIGIN = new BlockPos(1, 1, 1);

    private PressInsertGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void insertRejectsApple(GameTestHelper helper) {
        ArtisanalPressBlockEntity press = press(helper);
        ItemStack apple = new ItemStack(Items.APPLE);
        require(helper, !press.insert(apple), "Apple should not insert");
        require(helper, apple.getCount() == 1, "Apple left the hand");
        require(helper, press.getItem(ArtisanalPressBlockEntity.INPUT_SLOT).isEmpty(), "Press accepted apple");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void insertAcceptsRedGrapes(GameTestHelper helper) {
        ArtisanalPressBlockEntity press = press(helper);
        ItemStack grapes = new ItemStack(item("red_grapes"), 8);
        require(helper, press.insert(grapes), "Red grapes should insert");
        require(helper, grapes.isEmpty(), "Red grapes stayed in the hand");
        require(
                helper,
                press.getItem(ArtisanalPressBlockEntity.INPUT_SLOT).getCount() == 8,
                "Press did not take red grapes"
        );
        helper.succeed();
    }

    private static ArtisanalPressBlockEntity press(GameTestHelper helper) {
        helper.setBlock(ORIGIN, block("artisanal_press").defaultBlockState());
        if (!(helper.getBlockEntity(ORIGIN) instanceof ArtisanalPressBlockEntity press)) {
            throw new IllegalStateException("Missing artisanal press entity");
        }
        return press;
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

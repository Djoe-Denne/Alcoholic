package com.djden.alcoholic.minecraft.agriculture;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Harvest tool for vines and hop columns. Extends {@link DiggerItem} so
 * Fortune applies at the table and on the anvil (vanilla crop formula).
 */
public final class SickleItem extends DiggerItem {
    public SickleItem(Properties properties) {
        super(0.0F, -2.0F, Tiers.IRON, BlockTags.MINEABLE_WITH_HOE, properties);
    }

    public static boolean isSickle(ItemStack stack) {
        return stack.getItem() instanceof SickleItem;
    }

    public static ItemStack heldSickle(Player player) {
        ItemStack main = player.getMainHandItem();
        if (isSickle(main)) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        return isSickle(off) ? off : ItemStack.EMPTY;
    }

    public static int fortuneLevel(ItemStack tool) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
    }

    public static int fortuneAdjustedCount(int base, int fortune, RandomSource random) {
        return SickleHarvest.fortuneAdjustedCount(base, fortune, random::nextInt);
    }

    public static int fortuneAdjustedCount(int base, ItemStack tool, RandomSource random) {
        return fortuneAdjustedCount(base, fortuneLevel(tool), random);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(
                Component.translatable("tooltip.alcoholic.sickle.use")
                        .withStyle(ChatFormatting.GRAY)
        );
        tooltip.add(
                Component.translatable("tooltip.alcoholic.sickle.fortune")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );
    }
}

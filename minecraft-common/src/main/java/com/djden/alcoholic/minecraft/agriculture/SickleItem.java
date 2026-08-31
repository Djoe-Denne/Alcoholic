package com.djden.alcoholic.minecraft.agriculture;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public final class SickleItem extends Item {
    public SickleItem(Properties properties) {
        super(properties);
    }

    public static boolean isSickle(ItemStack stack) {
        return stack.getItem() instanceof SickleItem;
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
    }
}

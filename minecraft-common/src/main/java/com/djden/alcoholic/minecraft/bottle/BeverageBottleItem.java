package com.djden.alcoholic.minecraft.bottle;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public final class BeverageBottleItem extends Item {
    public BeverageBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        BottleSnapshotNbt.read(stack).ifPresent(snapshot -> {
            BottleSnapshotNbt.definition(snapshot).ifPresent(id -> tooltip.add(
                    Component.literal(id.toString()).withStyle(ChatFormatting.GRAY)
            ));
            tooltip.add(Component.translatable(
                    "tooltip.alcoholic.bottle.ethanol",
                    String.format(java.util.Locale.ROOT, "%.2f", BottleSnapshotNbt.number(snapshot, "Ethanol"))
            ));
            tooltip.add(Component.translatable(
                    "tooltip.alcoholic.bottle.maturity",
                    String.format(java.util.Locale.ROOT, "%.2f", BottleSnapshotNbt.number(snapshot, "Maturity"))
            ));
            tooltip.add(Component.translatable(
                    "tooltip.alcoholic.bottle.quality",
                    String.format(java.util.Locale.ROOT, "%.2f", BottleSnapshotNbt.number(snapshot, "Quality"))
            ));
            if (flag.isAdvanced()) {
                tooltip.add(Component.translatable(
                        "tooltip.alcoholic.bottle.purity",
                        String.format(java.util.Locale.ROOT, "%.2f", BottleSnapshotNbt.number(snapshot, "Purity"))
                ).withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.translatable(
                        "tooltip.alcoholic.bottle.complexity",
                        String.format(java.util.Locale.ROOT, "%.2f", BottleSnapshotNbt.number(snapshot, "Complexity"))
                ).withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.translatable(
                        "tooltip.alcoholic.bottle.debug",
                        String.format(java.util.Locale.ROOT, "%.2f", BottleSnapshotNbt.number(snapshot, "Sugar")),
                        String.format(java.util.Locale.ROOT, "%.2f", BottleSnapshotNbt.number(snapshot, "Acidity")),
                        String.format(java.util.Locale.ROOT, "%.2f", BottleSnapshotNbt.number(snapshot, "Balance")),
                        String.format(java.util.Locale.ROOT, "%.2f", BottleSnapshotNbt.number(snapshot, "Defects"))
                ).withStyle(ChatFormatting.DARK_GRAY));
            }
        });
    }
}

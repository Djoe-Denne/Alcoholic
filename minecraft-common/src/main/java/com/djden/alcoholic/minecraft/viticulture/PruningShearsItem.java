package com.djden.alcoholic.minecraft.viticulture;

import com.djden.alcoholic.domain.viticulture.PruningLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public final class PruningShearsItem extends Item {
    private static final String LEVEL_TAG = "AlcoholicPruningLevel";

    public PruningShearsItem(Properties properties) {
        super(properties);
    }

    public PruningLevel selectedLevel(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(LEVEL_TAG)) {
            try {
                return PruningLevel.valueOf(stack.getTag().getString(LEVEL_TAG));
            } catch (IllegalArgumentException ignored) {
                // Fall through to the stable default.
            }
        }
        return PruningLevel.BALANCED;
    }

    public void setSelectedLevel(ItemStack stack, PruningLevel level) {
        stack.getOrCreateTag().putString(LEVEL_TAG, level.name());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide) {
            PruningLevel selected = next(selectedLevel(stack));
            setSelectedLevel(stack, selected);
            player.displayClientMessage(
                    Component.translatable(
                            "message.alcoholic.pruning_shears.selected",
                            Component.translatable(levelKey(selected))
                    ),
                    true
            );
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(
                Component.translatable(
                                "tooltip.alcoholic.pruning_shears.level",
                                Component.translatable(levelKey(selectedLevel(stack)))
                        )
                        .withStyle(ChatFormatting.GRAY)
        );
        tooltip.add(
                Component.translatable("tooltip.alcoholic.pruning_shears.change")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );
    }

    private static PruningLevel next(PruningLevel level) {
        PruningLevel[] values = PruningLevel.values();
        return values[(level.ordinal() + 1) % values.length];
    }

    private static String levelKey(PruningLevel level) {
        return switch (level) {
            case LIGHT -> "message.alcoholic.vine.pruning.light";
            case BALANCED -> "message.alcoholic.vine.pruning.balanced";
            case SEVERE -> "message.alcoholic.vine.pruning.severe";
        };
    }
}

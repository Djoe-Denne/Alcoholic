package com.djden.alcoholic.minecraft.guide;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Objects;

public final class GrimoireItem extends Item {
    private final GrimoireKind kind;

    public GrimoireItem(GrimoireKind kind, Properties properties) {
        super(properties);
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    public GrimoireKind kind() {
        return kind;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            GrimoireClientOpen.open(kind);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}

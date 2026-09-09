package com.djden.alcoholic.forge.event;

import com.djden.alcoholic.minecraft.multiblock.PortBlocks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Vanilla skips {@code Block.use} when sneaking if either hand holds an item.
 * Force the block interaction so sneak + empty main hand can toggle port IO
 * even with a Create wrench or funnel in the offhand.
 */
public final class ForgePortInteractEvents {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void allowSneakToggle(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Player player = event.getEntity();
        if (!player.isSecondaryUseActive() || !player.getMainHandItem().isEmpty()) {
            return;
        }
        if (!event.getLevel().getBlockState(event.getPos()).hasProperty(PortBlocks.MODE)) {
            return;
        }
        event.setUseBlock(Event.Result.ALLOW);
    }
}

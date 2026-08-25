package com.djden.alcoholic.forge.fluid;

import com.djden.alcoholic.minecraft.fluid.LiquidVessel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidUtil;

public final class ForgeFluidInteraction {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void interact(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof LiquidVessel)) {
            return;
        }
        ItemStack held = event.getItemStack();
        if (held.isEmpty() || FluidUtil.getFluidHandler(held).map(handler -> handler.getTankCapacity(0)).orElse(0) <= 0) {
            return;
        }
        if (FluidUtil.interactWithFluidHandler(
                event.getEntity(),
                event.getHand(),
                event.getLevel(),
                event.getPos(),
                event.getFace()
        )) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}

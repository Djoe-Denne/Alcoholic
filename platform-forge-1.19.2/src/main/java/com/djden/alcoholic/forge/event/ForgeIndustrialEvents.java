package com.djden.alcoholic.forge.event;

import com.djden.alcoholic.minecraft.machine.MachineDataReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Datapack overlay for industrial machine families.
 */
public final class ForgeIndustrialEvents {
    @SubscribeEvent
    public void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new MachineDataReloadListener());
    }
}

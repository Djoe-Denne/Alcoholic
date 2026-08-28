package com.djden.alcoholic.forge.event;

import com.djden.alcoholic.minecraft.machine.MachineDataReloadListener;
import com.djden.alcoholic.minecraft.multiblock.MultiblockNotifier;
import com.djden.alcoholic.minecraft.multiblock.MultiblockPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Datapack overlay for industrial machine families, plus actor attribution
 * when a player places a hull part.
 */
public final class ForgeIndustrialEvents {
    @SubscribeEvent
    public void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new MachineDataReloadListener());
    }

    @SubscribeEvent
    public void onIndustrialPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player player) || !(event.getLevel() instanceof Level level)) {
            return;
        }
        Block placed = event.getPlacedBlock().getBlock();
        if (!(placed instanceof MultiblockPart)) {
            return;
        }
        MultiblockNotifier.touchNearby(player, level, event.getPos());
    }
}

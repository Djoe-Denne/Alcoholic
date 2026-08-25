package com.djden.alcoholic.forge.event;

import com.djden.alcoholic.minecraft.beverage.BeverageDataReloadListener;
import com.djden.alcoholic.minecraft.beverage.BeverageRuntime;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Objects;

/**
 * Forge-only wiring for beverage-framework datapack reloads.
 */
public final class ForgeBeverageEvents {
    private final BeverageRuntime runtime;

    public ForgeBeverageEvents(BeverageRuntime runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
    }

    @SubscribeEvent
    public void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new BeverageDataReloadListener(runtime));
    }
}

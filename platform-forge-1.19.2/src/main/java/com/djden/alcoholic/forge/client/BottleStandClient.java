package com.djden.alcoholic.forge.client;

import com.djden.alcoholic.minecraft.content.ProcessingContent;
import com.djden.alcoholic.minecraft.process.BottleStandBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.client.event.EntityRenderersEvent;

/** Client-only renderer registration for both bottle stand styles. */
public final class BottleStandClient {
    private BottleStandClient() {
    }

    public static void register(IEventBus modEventBus, ProcessingContent processing) {
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            @SuppressWarnings("unchecked")
            BlockEntityType<BottleStandBlockEntity> rackType =
                    (BlockEntityType<BottleStandBlockEntity>) processing.bottleRackEntity().get();
            @SuppressWarnings("unchecked")
            BlockEntityType<BottleStandBlockEntity> shelfType =
                    (BlockEntityType<BottleStandBlockEntity>) processing.bottleShelfEntity().get();
            event.registerBlockEntityRenderer(rackType, BottleStandRenderer::new);
            event.registerBlockEntityRenderer(shelfType, BottleStandRenderer::new);
        });
    }
}

package com.djden.alcoholic.forge.client;

import com.djden.alcoholic.minecraft.content.ProcessingContent;
import com.djden.alcoholic.minecraft.process.MaltMillBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Client-only registration for the Malt Mill's cosmetic moving parts. */
public final class MaltMillClient {
    private MaltMillClient() {
    }

    public static void register(IEventBus modEventBus, ProcessingContent processing) {
        modEventBus.addListener((ModelEvent.RegisterAdditional event) -> {
            event.register(MaltMillRenderer.ROLLER_FRONT_MODEL);
            event.register(MaltMillRenderer.ROLLER_REAR_MODEL);
            event.register(MaltMillRenderer.DRIVE_AXLE_MODEL);
        });
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            @SuppressWarnings("unchecked")
            BlockEntityType<MaltMillBlockEntity> type =
                    (BlockEntityType<MaltMillBlockEntity>) processing.maltMillEntity().get();
            event.registerBlockEntityRenderer(type, MaltMillRenderer::new);
        });
    }
}

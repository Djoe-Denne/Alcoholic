package com.djden.alcoholic.forge.client;

import com.djden.alcoholic.minecraft.content.ProcessingContent;
import com.djden.alcoholic.minecraft.mechanical.PrimitiveCombustionEngineBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Client-only registration for the combustion-engine shaft and flywheel. */
public final class PrimitiveCombustionEngineClient {
    private PrimitiveCombustionEngineClient() {
    }

    public static void register(IEventBus modEventBus, ProcessingContent processing) {
        modEventBus.addListener((ModelEvent.RegisterAdditional event) -> {
            event.register(PrimitiveCombustionEngineRenderer.SHAFT_MODEL);
            event.register(PrimitiveCombustionEngineRenderer.FLYWHEEL_MODEL);
        });
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            @SuppressWarnings("unchecked")
            BlockEntityType<PrimitiveCombustionEngineBlockEntity> type =
                    (BlockEntityType<PrimitiveCombustionEngineBlockEntity>) processing
                            .primitiveCombustionEngineEntity()
                            .get();
            event.registerBlockEntityRenderer(type, PrimitiveCombustionEngineRenderer::new);
        });
    }
}

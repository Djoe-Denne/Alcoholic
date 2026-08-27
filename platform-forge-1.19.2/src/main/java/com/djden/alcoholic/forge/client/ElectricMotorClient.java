package com.djden.alcoholic.forge.client;

import com.djden.alcoholic.minecraft.content.ProcessingContent;
import com.djden.alcoholic.minecraft.mechanical.ElectricMotorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Client-only registration for the electric motor shaft. */
public final class ElectricMotorClient {
    private ElectricMotorClient() {
    }

    public static void register(IEventBus modEventBus, ProcessingContent processing) {
        modEventBus.addListener((ModelEvent.RegisterAdditional event) ->
                event.register(ElectricMotorRenderer.SHAFT_MODEL));
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            @SuppressWarnings("unchecked")
            BlockEntityType<ElectricMotorBlockEntity> type =
                    (BlockEntityType<ElectricMotorBlockEntity>) processing.electricMotorEntity().get();
            event.registerBlockEntityRenderer(type, ElectricMotorRenderer::new);
        });
    }
}

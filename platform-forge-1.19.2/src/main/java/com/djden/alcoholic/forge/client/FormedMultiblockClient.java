package com.djden.alcoholic.forge.client;

import com.djden.alcoholic.minecraft.content.IndustrialContent;
import com.djden.alcoholic.minecraft.multiblock.MultiblockControllerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class FormedMultiblockClient {
    private FormedMultiblockClient() {
    }

    public static void register(IEventBus modEventBus, IndustrialContent industrial) {
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> registerPress(event, industrial));
    }

    @SuppressWarnings("unchecked")
    private static void registerPress(EntityRenderersEvent.RegisterRenderers event, IndustrialContent industrial) {
        event.registerBlockEntityRenderer(
                (BlockEntityType<MultiblockControllerBlockEntity>) industrial.pressControllerEntity().get(),
                FormedMultiblockRenderer::new
        );
    }
}

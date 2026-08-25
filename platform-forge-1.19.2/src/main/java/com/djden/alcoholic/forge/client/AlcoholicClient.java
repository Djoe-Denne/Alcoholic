package com.djden.alcoholic.forge.client;

import com.djden.alcoholic.minecraft.content.AlcoholicContent;
import com.djden.alcoholic.minecraft.content.GrainContent;
import com.djden.alcoholic.minecraft.content.IndustrialContent;
import com.djden.alcoholic.minecraft.content.ProcessingContent;
import com.djden.alcoholic.minecraft.multiblock.MultiblockControllerBlockEntity;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class AlcoholicClient {
    private AlcoholicClient() {
    }

    public static void register(
            IEventBus modEventBus,
            AlcoholicContent content,
            ProcessingContent processing,
            GrainContent grain,
            IndustrialContent industrial
    ) {
        modEventBus.addListener(
                (FMLClientSetupEvent event) -> event.enqueueWork(() -> {
                    ItemBlockRenderTypes.setRenderLayer(content.redGrapevine().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(content.whiteGrapevine().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(content.trellisWire().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(processing.artisanalPress().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(processing.artisanalFermenter().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(processing.oakBarrel().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(processing.blendingCrock().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(processing.maltingFloor().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(processing.mashTun().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(processing.brewingKettle().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(grain.barleyCrop().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(grain.hopBine().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(industrial.machineWindow().get(), RenderType.translucent());
                    ItemBlockRenderTypes.setRenderLayer(industrial.pressController().get(), RenderType.cutout());
                })
        );
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            @SuppressWarnings("unchecked")
            BlockEntityType<MultiblockControllerBlockEntity> pressType =
                    (BlockEntityType<MultiblockControllerBlockEntity>) industrial.pressControllerEntity().get();
            event.registerBlockEntityRenderer(pressType, IndustrialPressRenderer::new);
        });
    }
}

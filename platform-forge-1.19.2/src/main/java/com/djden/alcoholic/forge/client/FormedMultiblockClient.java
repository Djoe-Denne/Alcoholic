package com.djden.alcoholic.forge.client;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.multiblock.FormedArtSize;
import com.djden.alcoholic.minecraft.content.CraftContent;
import com.djden.alcoholic.minecraft.content.IndustrialContent;
import com.djden.alcoholic.minecraft.multiblock.MultiblockControllerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class FormedMultiblockClient {
    private FormedMultiblockClient() {
    }

    public static void register(IEventBus modEventBus, IndustrialContent industrial, CraftContent craft) {
        modEventBus.addListener((ModelEvent.RegisterAdditional event) -> {
            for (ResourceId id : FormedArtSize.all().keySet()) {
                event.register(FormedMultiblockRenderer.meshModel(id));
            }
        });
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            register(event, industrial.pressControllerEntity().get());
            register(event, industrial.vatControllerEntity().get());
            register(event, industrial.tankControllerEntity().get());
            register(event, industrial.maltHouseControllerEntity().get());
            register(event, industrial.rollerMillControllerEntity().get());
            register(event, industrial.mashTunControllerEntity().get());
            register(event, industrial.brewingKettleControllerEntity().get());
            register(event, industrial.conditioningVesselControllerEntity().get());
            register(event, industrial.agingVesselControllerEntity().get());
            register(event, craft.maltHouseControllerEntity().get());
            register(event, craft.millControllerEntity().get());
            register(event, craft.mashTunControllerEntity().get());
            register(event, craft.brewingKettleControllerEntity().get());
            register(event, craft.vatControllerEntity().get());
        });
    }

    @SuppressWarnings("unchecked")
    private static void register(
            EntityRenderersEvent.RegisterRenderers event,
            BlockEntityType<?> type
    ) {
        event.registerBlockEntityRenderer(
                (BlockEntityType<MultiblockControllerBlockEntity>) type,
                FormedMultiblockRenderer::new
        );
    }
}

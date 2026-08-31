package com.djden.alcoholic.forge.client;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.minecraft.content.AlcoholicContent;
import com.djden.alcoholic.minecraft.content.GrainContent;
import com.djden.alcoholic.minecraft.content.CraftContent;
import com.djden.alcoholic.minecraft.content.IndustrialContent;
import com.djden.alcoholic.minecraft.content.ProcessingContent;
import com.djden.alcoholic.minecraft.fluid.FluidContent;
import com.djden.alcoholic.minecraft.guide.GrimoireClientOpen;
import com.djden.alcoholic.minecraft.menu.MachineMenu;
import com.djden.alcoholic.minecraft.menu.MachineMenuContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
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
            IndustrialContent industrial,
            CraftContent craft,
            MachineMenuContent menus,
            FluidContent fluids
    ) {
        modEventBus.addListener(
                (FMLClientSetupEvent event) -> event.enqueueWork(() -> {
                    ItemBlockRenderTypes.setRenderLayer(content.redGrapevine().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(content.whiteGrapevine().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(content.redGrapevineStem().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(content.whiteGrapevineStem().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(content.redGrapevineCanopy().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(content.whiteGrapevineCanopy().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(content.trellisWire().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(processing.artisanalPress().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(processing.artisanalFermenter().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(processing.oakBarrel().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(processing.blendingCrock().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(processing.maltingFloor().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(processing.mashTun().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(processing.brewingKettle().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(processing.maltMill().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(processing.primitiveCombustionEngine().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(processing.electricMotor().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(grain.barleyCrop().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(grain.hopBine().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(grain.hopBineStem().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(grain.hopBineCanopy().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(grain.wildHops().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(industrial.machineWindow().get(), RenderType.translucent());
                    ItemBlockRenderTypes.setRenderLayer(industrial.pressController().get(), RenderType.cutout());
                    registerFluidLayers(fluids);
                    registerMenus(menus);
                })
        );
        GrimoireClientOpen.bind(kind -> Minecraft.getInstance().setScreen(new GrimoireScreen(kind)));
        MaltMillClient.register(modEventBus, processing);
        ElectricMotorClient.register(modEventBus, processing);
        PrimitiveCombustionEngineClient.register(modEventBus, processing);
        FormedMultiblockClient.register(modEventBus, industrial, craft);
    }

    private static void registerFluidLayers(FluidContent fluids) {
        for (ResourceId id : fluids.ids()) {
            Fluid source = fluids.source(id);
            if (source instanceof FlowingFluid flowing) {
                ItemBlockRenderTypes.setRenderLayer(flowing, RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(flowing.getFlowing(), RenderType.translucent());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void registerMenus(MachineMenuContent menus) {
        MenuScreens.register((MenuType<MachineMenu>) menus.twoSlots().get(), AlcoholicMachineScreen::new);
        MenuScreens.register((MenuType<MachineMenu>) menus.twoSlotsOneTank().get(), AlcoholicMachineScreen::new);
        MenuScreens.register((MenuType<MachineMenu>) menus.twoSlotsTwoTanks().get(), AlcoholicMachineScreen::new);
        MenuScreens.register((MenuType<MachineMenu>) menus.oneSlotOneTank().get(), AlcoholicMachineScreen::new);
        MenuScreens.register((MenuType<MachineMenu>) menus.oneTank().get(), AlcoholicMachineScreen::new);
        MenuScreens.register((MenuType<MachineMenu>) menus.twoTanks().get(), AlcoholicMachineScreen::new);
        MenuScreens.register((MenuType<MachineMenu>) menus.fuel().get(), AlcoholicMachineScreen::new);
        MenuScreens.register((MenuType<MachineMenu>) menus.energy().get(), AlcoholicMachineScreen::new);
    }
}

package com.djden.alcoholic.forge.client;

import com.djden.alcoholic.minecraft.menu.MachineLayout;
import com.djden.alcoholic.minecraft.menu.MachineMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public final class AlcoholicMachineScreen extends AbstractContainerScreen<MachineMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("alcoholic", "textures/gui/machine.png");
    private static final int BURN_X = 81;
    private static final int BURN_Y = 54;

    public AlcoholicMachineScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = MachineLayout.PANEL_WIDTH;
        imageHeight = MachineLayout.PANEL_HEIGHT;
    }

    @Override
    protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        blit(pose, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        MachineLayout layout = menu.layout();
        RenderSystem.setShaderTexture(0, FluidGaugeWidget.ELEMENTS);
        for (MachineLayout.SlotPos slot : layout.slots()) {
            blit(pose, leftPos + slot.x() - 1, topPos + slot.y() - 1, 0, 0, 18, 18, 256, 256);
        }
        if (layout.progressArrow() && layout.arrow().present()) {
            blit(
                    pose,
                    leftPos + layout.arrow().x(),
                    topPos + layout.arrow().y(),
                    36,
                    0,
                    MachineLayout.ARROW_WIDTH,
                    MachineLayout.ARROW_HEIGHT,
                    256,
                    256
            );
            int filled = scaled(menu.progress(), menu.duration(), MachineLayout.ARROW_WIDTH);
            if (filled > 0) {
                blit(
                        pose,
                        leftPos + layout.arrow().x(),
                        topPos + layout.arrow().y(),
                        36,
                        17,
                        filled,
                        MachineLayout.ARROW_HEIGHT,
                        256,
                        256
                );
            }
        }
        if (layout.fuelBar()) {
            blit(pose, leftPos + BURN_X, topPos + BURN_Y, 60, 0, MachineLayout.BURN_SIZE, MachineLayout.BURN_SIZE, 256, 256);
            int remaining = scaled(menu.extra(), Math.max(1, menu.extra2()), MachineLayout.BURN_SIZE);
            if (remaining > 0) {
                blit(
                        pose,
                        leftPos + BURN_X,
                        topPos + BURN_Y + MachineLayout.BURN_SIZE - remaining,
                        74,
                        MachineLayout.BURN_SIZE - remaining,
                        MachineLayout.BURN_SIZE,
                        remaining,
                        256,
                        256
                );
            }
        }
        MachineLayout.GaugePos[] gauges = layout.gauges();
        if (layout.energyGauge() && gauges.length > 0) {
            FluidGaugeWidget.renderEnergy(
                    pose,
                    leftPos + gauges[0].x(),
                    topPos + gauges[0].y(),
                    menu.extra(),
                    menu.extra2()
            );
            return;
        }
        for (int index = 0; index < gauges.length; index++) {
            FluidGaugeWidget.renderFluid(
                    pose,
                    leftPos + gauges[index].x(),
                    topPos + gauges[index].y(),
                    menu.tankFluidId(index),
                    menu.tankVolume(index),
                    menu.tankCapacity(index)
            );
        }
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTick);
        renderTooltip(pose, mouseX, mouseY);
        renderGaugeTooltips(pose, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
        super.renderLabels(pose, mouseX, mouseY);
        if (menu.temperatureDeci() != 0) {
            font.draw(
                    pose,
                    Component.translatable("gui.alcoholic.temperature", formatDeci(menu.temperatureDeci())),
                    8,
                    18,
                    0x404040
            );
        }
        if (menu.layout() == MachineLayout.TWO_SLOTS && menu.extra() > 0) {
            font.draw(
                    pose,
                    Component.translatable("gui.alcoholic.drive", menu.extra()),
                    8,
                    18,
                    0x404040
            );
        }
    }

    private void renderGaugeTooltips(PoseStack pose, int mouseX, int mouseY) {
        MachineLayout layout = menu.layout();
        MachineLayout.GaugePos[] gauges = layout.gauges();
        if (layout.energyGauge() && gauges.length > 0) {
            int x = leftPos + gauges[0].x();
            int y = topPos + gauges[0].y();
            if (FluidGaugeWidget.isHovering(x, y, mouseX, mouseY)) {
                renderComponentTooltip(pose, FluidGaugeWidget.energyTooltip(menu.extra(), menu.extra2()), mouseX, mouseY);
            }
            return;
        }
        for (int index = 0; index < gauges.length; index++) {
            int x = leftPos + gauges[index].x();
            int y = topPos + gauges[index].y();
            if (FluidGaugeWidget.isHovering(x, y, mouseX, mouseY)) {
                List<Component> tooltip = FluidGaugeWidget.fluidTooltip(
                        menu.tankFluidId(index),
                        menu.tankVolume(index),
                        menu.tankCapacity(index)
                );
                renderComponentTooltip(pose, tooltip, mouseX, mouseY);
            }
        }
    }

    private static int scaled(int value, int max, int pixels) {
        if (value <= 0 || max <= 0) {
            return 0;
        }
        return Math.max(1, Math.min(pixels, value * pixels / max));
    }

    private static String formatDeci(int deci) {
        return String.format(java.util.Locale.ROOT, "%.1f", deci / 10.0);
    }
}

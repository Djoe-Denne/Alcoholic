package com.djden.alcoholic.forge.client;

import com.djden.alcoholic.minecraft.menu.MachineLayout;
import com.djden.alcoholic.minecraft.menu.MachineMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

public final class AlcoholicMachineScreen extends AbstractContainerScreen<MachineMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("alcoholic", "textures/gui/machine.png");
    private static final int BURN_X = 81;
    private static final int BURN_Y = 54;
    private static final int TELEMETRY_X = MachineTelemetryOverlay.OFFSET_X;
    private static final int TELEMETRY_WIDTH = MachineTelemetryOverlay.WIDTH;
    private static final int TELEMETRY_LINE = 11;
    private static final int TEXT = 0xE0E0E0;
    private static final int MUTED = 0x9A9A9A;
    private static final int GOOD = 0x72D572;
    private static final int WARN = 0xE5C65C;
    private static final int BAD = 0xE56A6A;

    public AlcoholicMachineScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        MachineLayout layout = menu.layout();
        imageWidth = layout.panelWidth();
        imageHeight = layout.panelHeight();
        inventoryLabelY = imageHeight - 94;
    }

    public Optional<Rect2i> telemetryExtraArea() {
        return MachineTelemetryOverlay.extraArea(
                menu.hasControllerTelemetry(),
                leftPos,
                topPos,
                imageHeight
        ).map(area -> new Rect2i(area.x(), area.y(), area.width(), area.height()));
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        boolean insideTelemetry = MachineTelemetryOverlay.extraArea(
                menu.hasControllerTelemetry(),
                guiLeft,
                guiTop,
                imageHeight
        ).map(area -> area.contains(mouseX, mouseY)).orElse(false);
        if (insideTelemetry) {
            return false;
        }
        return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton);
    }

    @Override
    protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        blitPanel(pose);
        if (menu.hasControllerTelemetry()) {
            int x = leftPos + TELEMETRY_X;
            fill(pose, x, topPos, x + TELEMETRY_WIDTH, topPos + imageHeight, 0xDC181818);
            fill(pose, x, topPos, x + TELEMETRY_WIDTH, topPos + 1, 0xFF7A5B3A);
            fill(pose, x, topPos + imageHeight - 1, x + TELEMETRY_WIDTH, topPos + imageHeight, 0xFF7A5B3A);
            fill(pose, x, topPos, x + 1, topPos + imageHeight, 0xFF7A5B3A);
            fill(pose, x + TELEMETRY_WIDTH - 1, topPos, x + TELEMETRY_WIDTH, topPos + imageHeight, 0xFF7A5B3A);
        }
        MachineLayout layout = menu.layout();
        RenderSystem.setShaderTexture(0, FluidGaugeWidget.ELEMENTS);
        for (MachineLayout.SlotPos slot : layout.slots()) {
            blitSlot(pose, slot);
        }
        for (MachineLayout.SlotPos slot : layout.playerSlots()) {
            blitSlot(pose, slot);
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
        if (menu.hasControllerTelemetry()) {
            renderControllerTelemetry(pose);
            return;
        }
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

    private void renderControllerTelemetry(PoseStack pose) {
        int x = TELEMETRY_X + 7;
        int y = 7;
        font.draw(pose, Component.translatable("gui.alcoholic.telemetry"), x, y, 0xF2D2A2);
        y += TELEMETRY_LINE + 1;

        font.draw(
                pose,
                Component.translatable(menu.formed()
                        ? "gui.alcoholic.structure.formed"
                        : "gui.alcoholic.structure.unformed"),
                x,
                y,
                menu.formed() ? GOOD : BAD
        );
        y += TELEMETRY_LINE;

        Optional<ResourceLocation> process = menu.processDefinition(minecraft == null ? null : minecraft.level)
                .map(id -> ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path()));
        if (process.isPresent()) {
            font.draw(
                    pose,
                    Component.translatable("gui.alcoholic.profile", process.orElseThrow().getPath()),
                    x,
                    y,
                    TEXT
            );
            y += TELEMETRY_LINE;
        }

        font.draw(
                pose,
                Component.translatable(
                        "gui.alcoholic.stage",
                        Component.translatable(menu.processStage().translationKey())
                ),
                x,
                y,
                menu.processStage() == com.djden.alcoholic.minecraft.menu.MachineProcessStage.IDLE ? MUTED : TEXT
        );
        y += TELEMETRY_LINE;

        int percent = menu.duration() <= 1 ? 0 : Math.min(100, menu.progress() * 100 / menu.duration());
        font.draw(pose, Component.translatable("gui.alcoholic.progress", percent), x, y, TEXT);
        y += TELEMETRY_LINE;

        int ambientColor = menu.processUsesAmbient()
                ? temperatureColor(menu.ambientTemperatureDeci())
                : MUTED;
        drawTemperature(pose, "gui.alcoholic.ambient", menu.ambientTemperatureDeci(), x, y, ambientColor);
        y += TELEMETRY_LINE;

        int heatColor = MUTED;
        if (menu.processUsesHeat()) {
            heatColor = temperatureColor(menu.heatTemperatureDeci());
        } else if (menu.kilnHeatRequired()) {
            heatColor = thresholdColor(menu.heatTemperatureDeci(), menu.requiredHeatTemperatureDeci());
        }
        drawTemperature(pose, "gui.alcoholic.heat", menu.heatTemperatureDeci(), x, y, heatColor);
        y += TELEMETRY_LINE;

        if (available(menu.preferredTemperatureMinDeci()) && available(menu.preferredTemperatureMaxDeci())) {
            font.draw(
                    pose,
                    Component.translatable(
                            "gui.alcoholic.preferred",
                            formatDeci(menu.preferredTemperatureMinDeci()),
                            formatDeci(menu.preferredTemperatureMaxDeci())
                    ),
                    x,
                    y,
                    MUTED
            );
            y += TELEMETRY_LINE;
        }
        if (available(menu.operatingTemperatureMinDeci()) && available(menu.operatingTemperatureMaxDeci())) {
            font.draw(
                    pose,
                    Component.translatable(
                            "gui.alcoholic.operating",
                            formatDeci(menu.operatingTemperatureMinDeci()),
                            formatDeci(menu.operatingTemperatureMaxDeci())
                    ),
                    x,
                    y,
                    MUTED
            );
            y += TELEMETRY_LINE;
        }

        if (menu.humidityRequired()) {
            int color = thresholdColor(menu.humidityPermille(), menu.requiredHumidityPermille());
            font.draw(
                    pose,
                    Component.translatable(
                            "gui.alcoholic.humidity",
                            formatPermille(menu.humidityPermille()),
                            formatPermille(menu.requiredHumidityPermille())
                    ),
                    x,
                    y,
                    color
            );
            y += TELEMETRY_LINE;
        }
        if (menu.kilnHeatRequired()) {
            font.draw(
                    pose,
                    Component.translatable(
                            "gui.alcoholic.kiln_minimum",
                            formatDeci(menu.requiredHeatTemperatureDeci())
                    ),
                    x,
                    y,
                    MUTED
            );
            y += TELEMETRY_LINE;
        }
        if (menu.driveRequired()) {
            int speedColor = rangeColor(
                    menu.driveSpeedDeci(),
                    menu.driveMinimumDeci(),
                    menu.driveMaximumDeci()
            );
            font.draw(
                    pose,
                    Component.translatable(
                            "gui.alcoholic.drive_telemetry",
                            formatDeci(menu.driveSpeedDeci()),
                            formatDeci(menu.driveMinimumDeci()),
                            formatDeci(menu.driveMaximumDeci())
                    ),
                    x,
                    y,
                    speedColor
            );
            y += TELEMETRY_LINE;
            font.draw(
                    pose,
                    Component.translatable(
                            "gui.alcoholic.drive_capacity",
                            formatDeci(menu.driveCapacityDeci()),
                            formatDeci(menu.requiredDriveCapacityDeci())
                    ),
                    x,
                    y,
                    thresholdColor(menu.driveCapacityDeci(), menu.requiredDriveCapacityDeci())
            );
        }
    }

    private void drawTemperature(PoseStack pose, String key, int value, int x, int y, int color) {
        if (available(value)) {
            font.draw(pose, Component.translatable(key, formatDeci(value)), x, y, color);
        }
    }

    private int temperatureColor(int value) {
        if (!available(value)) {
            return MUTED;
        }
        if (rangeContains(value, menu.preferredTemperatureMinDeci(), menu.preferredTemperatureMaxDeci())) {
            return GOOD;
        }
        if (rangeContains(value, menu.operatingTemperatureMinDeci(), menu.operatingTemperatureMaxDeci())) {
            return WARN;
        }
        return BAD;
    }

    private static int thresholdColor(int value, int minimum) {
        if (!available(value) || !available(minimum)) {
            return MUTED;
        }
        return value >= minimum ? GOOD : BAD;
    }

    private static int rangeColor(int value, int minimum, int maximum) {
        if (!available(value) || !available(minimum) || !available(maximum)) {
            return MUTED;
        }
        return value >= minimum && value <= maximum ? GOOD : BAD;
    }

    private static boolean rangeContains(int value, int minimum, int maximum) {
        return available(value) && available(minimum) && available(maximum)
                && value >= minimum && value <= maximum;
    }

    private static boolean available(int value) {
        return value != com.djden.alcoholic.minecraft.menu.MachineAccess.DATA_UNAVAILABLE;
    }

    private static String formatPermille(int permille) {
        return available(permille)
                ? Integer.toString((int) Math.round(permille / 10.0))
                : "-";
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

    private void blitSlot(PoseStack pose, MachineLayout.SlotPos slot) {
        blit(
                pose,
                leftPos + slot.x() - 1,
                topPos + slot.y() - 1,
                0,
                0,
                MachineLayout.SLOT_SIZE,
                MachineLayout.SLOT_SIZE,
                256,
                256
        );
    }

    private void blitPanel(PoseStack pose) {
        MachineLayout layout = menu.layout();
        int width = layout.panelWidth();
        int height = layout.panelHeight();
        if (height <= MachineLayout.PANEL_HEIGHT) {
            blit(pose, leftPos, topPos, 0, 0, width, height, 256, 256);
            return;
        }
        int topHeight = 79;
        blit(pose, leftPos, topPos, 0, 0, width, topHeight, 256, 256);
        int playerDestY = layout.playerInvY() - 5;
        for (int y = topHeight; y < playerDestY; y++) {
            blit(pose, leftPos, topPos + y, 0, 16, width, 1, 256, 256);
        }
        blit(
                pose,
                leftPos,
                topPos + playerDestY,
                0,
                topHeight,
                width,
                height - playerDestY,
                256,
                256
        );
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

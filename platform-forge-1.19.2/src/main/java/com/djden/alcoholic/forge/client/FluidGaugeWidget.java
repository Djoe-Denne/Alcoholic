package com.djden.alcoholic.forge.client;

import com.djden.alcoholic.minecraft.menu.MachineLayout;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import java.util.List;

final class FluidGaugeWidget {
    static final ResourceLocation ELEMENTS =
            ResourceLocation.fromNamespaceAndPath("alcoholic", "textures/gui/elements.png");
    private static final int INNER_WIDTH = 16;
    private static final int INNER_HEIGHT = 50;

    private FluidGaugeWidget() {
    }

    static void renderFrame(PoseStack pose, int x, int y) {
        RenderSystem.setShaderTexture(0, ELEMENTS);
        GuiComponent.blit(pose, x, y, 18, 0, MachineLayout.GAUGE_WIDTH, MachineLayout.GAUGE_HEIGHT, 256, 256);
    }

    static void renderFluid(PoseStack pose, int x, int y, int fluidId, int volume, int capacity) {
        renderFrame(pose, x, y);
        if (volume <= 0 || capacity <= 0 || fluidId <= 0) {
            return;
        }
        Fluid fluid = Registry.FLUID.byId(fluidId);
        if (fluid == Fluids.EMPTY) {
            return;
        }
        int fill = Math.max(1, Math.min(INNER_HEIGHT, (int) Math.round(INNER_HEIGHT * (double) volume / capacity)));
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation still = extensions.getStillTexture();
        if (still == null) {
            return;
        }
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
        int color = extensions.getTintColor();
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float alpha = ((color >> 24) & 0xFF) / 255.0F;
        if (alpha <= 0.0F) {
            alpha = 1.0F;
        }
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.setShaderColor(red, green, blue, alpha);
        int drawn = 0;
        int innerX = x + 1;
        int innerBottom = y + 1 + INNER_HEIGHT;
        while (drawn < fill) {
            int slice = Math.min(16, fill - drawn);
            blitSprite(
                    pose,
                    innerX,
                    innerBottom - drawn - slice,
                    INNER_WIDTH,
                    slice,
                    sprite,
                    0,
                    16 - slice,
                    16,
                    slice
            );
            drawn += slice;
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    static void renderEnergy(PoseStack pose, int x, int y, int stored, int capacity) {
        renderFrame(pose, x, y);
        if (stored <= 0 || capacity <= 0) {
            return;
        }
        int fill = Math.max(1, Math.min(INNER_HEIGHT, (int) Math.round(INNER_HEIGHT * (double) stored / capacity)));
        GuiComponent.fill(pose, x + 1, y + 1 + INNER_HEIGHT - fill, x + 1 + INNER_WIDTH, y + 1 + INNER_HEIGHT, 0xFF3DCEFB);
    }

    static List<Component> fluidTooltip(int fluidId, int volume, int capacity) {
        if (volume <= 0 || fluidId <= 0) {
            return List.of(Component.translatable("tooltip.alcoholic.gauge.empty"));
        }
        Fluid fluid = Registry.FLUID.byId(fluidId);
        Component name = fluid == Fluids.EMPTY
                ? Component.translatable("tooltip.alcoholic.gauge.empty")
                : fluid.getFluidType().getDescription();
        return List.of(Component.translatable(
                "tooltip.alcoholic.gauge.fluid",
                name,
                volume,
                Math.max(0, capacity)
        ));
    }

    static List<Component> energyTooltip(int stored, int capacity) {
        return List.of(Component.translatable("tooltip.alcoholic.gauge.energy", stored, Math.max(0, capacity)));
    }

    static boolean isHovering(int x, int y, int mouseX, int mouseY) {
        return mouseX >= x
                && mouseY >= y
                && mouseX < x + MachineLayout.GAUGE_WIDTH
                && mouseY < y + MachineLayout.GAUGE_HEIGHT;
    }

    private static void blitSprite(
            PoseStack pose,
            int x,
            int y,
            int width,
            int height,
            TextureAtlasSprite sprite,
            int spriteU,
            int spriteV,
            int spriteWidth,
            int spriteHeight
    ) {
        float u0 = sprite.getU(spriteU);
        float u1 = sprite.getU(spriteU + spriteWidth);
        float v0 = sprite.getV(spriteV);
        float v1 = sprite.getV(spriteV + spriteHeight);
        Matrix4f matrix = pose.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, x, y + height, 0).uv(u0, v1).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).uv(u1, v1).endVertex();
        buffer.vertex(matrix, x + width, y, 0).uv(u1, v0).endVertex();
        buffer.vertex(matrix, x, y, 0).uv(u0, v0).endVertex();
        tesselator.end();
    }
}

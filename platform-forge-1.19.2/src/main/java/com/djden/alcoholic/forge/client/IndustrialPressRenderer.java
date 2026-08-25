package com.djden.alcoholic.forge.client;

import com.djden.alcoholic.domain.multiblock.Box3;
import com.djden.alcoholic.minecraft.multiblock.MultiblockControllerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Moving platen plus optional F3+B crush-volume outline.
 */
public final class IndustrialPressRenderer implements BlockEntityRenderer<MultiblockControllerBlockEntity> {
    public IndustrialPressRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            MultiblockControllerBlockEntity entity,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        if (!entity.formed()) {
            return;
        }
        float travel = (float) platenOffset(entity.strokeCycle());
        pose.pushPose();
        pose.translate(0.25, 0.75 - travel, 0.25);
        pose.scale(0.5F, 0.12F, 0.5F);
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        dispatcher.renderSingleBlock(
                Blocks.IRON_BLOCK.defaultBlockState(),
                pose,
                buffers,
                packedLight,
                packedOverlay
        );
        pose.popPose();

        if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
            entity.crushVolume().ifPresent(crush -> renderCrush(entity.getBlockPos(), crush, pose, buffers));
        }
    }

    private static double platenOffset(double cycle) {
        double clamped = Math.min(1.0, Math.max(0.0, cycle));
        if (clamped < 0.15) {
            return clamped / 0.15 * 0.15;
        }
        if (clamped < 0.55) {
            return 0.15 + (clamped - 0.15) / 0.40 * 0.55;
        }
        if (clamped < 0.70) {
            return 0.70;
        }
        return 0.70 * (1.0 - (clamped - 0.70) / 0.30);
    }

    private static void renderCrush(BlockPos origin, Box3 crush, PoseStack pose, MultiBufferSource buffers) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.lines());
        Vec3 min = new Vec3(crush.minX(), crush.minY(), crush.minZ()).subtract(origin.getX(), origin.getY(), origin.getZ());
        Vec3 max = new Vec3(crush.maxX(), crush.maxY(), crush.maxZ()).subtract(origin.getX(), origin.getY(), origin.getZ());
        LevelRenderer.renderLineBox(
                pose,
                consumer,
                min.x,
                min.y,
                min.z,
                max.x,
                max.y,
                max.z,
                0.95F,
                0.15F,
                0.15F,
                1.0F
        );
    }

    @Override
    public boolean shouldRenderOffScreen(MultiblockControllerBlockEntity entity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}

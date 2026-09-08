package com.djden.alcoholic.forge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.djden.alcoholic.minecraft.process.BottleStandBlock;
import com.djden.alcoholic.minecraft.process.BottleStandBlockEntity;
import com.djden.alcoholic.minecraft.process.BottleStandStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Draws the actual stored item stacks in the nine front-facing cells of one stand block. */
public final class BottleStandRenderer implements BlockEntityRenderer<BottleStandBlockEntity> {
    private static final float[] CELL_COORDINATES = {3.0F / 16.0F, 8.0F / 16.0F, 13.0F / 16.0F};
    private static final float FRONT_DEPTH = 2.0F / 16.0F;

    public BottleStandRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            BottleStandBlockEntity entity,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        if (entity.getLevel() == null || !(entity.getBlockState().getBlock() instanceof BottleStandBlock block)) {
            return;
        }
        BlockState state = entity.getBlockState();
        pose.pushPose();
        pose.translate(0.5D, 0.5D, 0.5D);
        pose.mulPose(Vector3f.YP.rotationDegrees(-facingDegrees(state.getValue(BottleStandBlock.FACING))));
        pose.translate(-0.5D, -0.5D, -0.5D);

        for (int slot = 0; slot < BottleStandBlockEntity.SLOT_COUNT; slot++) {
            ItemStack stack = entity.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            int column = slot % 3;
            int row = slot / 3;
            pose.pushPose();
            pose.translate(CELL_COORDINATES[column], CELL_COORDINATES[2 - row], FRONT_DEPTH);
            if (block.style() == BottleStandStyle.RACK) {
                pose.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
            }
            pose.scale(0.42F, 0.42F, 0.42F);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack,
                    ItemTransforms.TransformType.FIXED,
                    packedLight,
                    packedOverlay,
                    pose,
                    buffers,
                    0
            );
            pose.popPose();
        }
        pose.popPose();
    }

    private static float facingDegrees(net.minecraft.core.Direction facing) {
        return switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }
}

package com.djden.alcoholic.forge.client;

import com.djden.alcoholic.minecraft.process.MaltMillBlock;
import com.djden.alcoholic.minecraft.process.MaltMillBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import java.util.Map;
import java.util.WeakHashMap;

/** Renders the Malt Mill rollers and drive axle around their Blockbench pivots. */
public final class MaltMillRenderer implements BlockEntityRenderer<MaltMillBlockEntity> {
    static final float DEGREES_PER_TICK = 6.0F;
    static final ResourceLocation ROLLER_FRONT_MODEL = model("malt_mill_roller_front");
    static final ResourceLocation ROLLER_REAR_MODEL = model("malt_mill_roller_rear");
    static final ResourceLocation DRIVE_AXLE_MODEL = model("malt_mill_drive_axle");

    private static final double FRONT_PIVOT_X = 8.0 / 16.0;
    private static final double FRONT_PIVOT_Y = 8.5 / 16.0;
    private static final double FRONT_PIVOT_Z = 5.75 / 16.0;
    private static final double REAR_PIVOT_X = 8.0 / 16.0;
    private static final double REAR_PIVOT_Y = 8.5 / 16.0;
    private static final double REAR_PIVOT_Z = 10.25 / 16.0;

    private final Map<MaltMillBlockEntity, SpinState> spinStates = new WeakHashMap<>();

    public MaltMillRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            MaltMillBlockEntity entity,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        if (entity.getLevel() == null) {
            return;
        }

        double renderTime = entity.getLevel().getGameTime() + partialTick;
        float angle = spinStates.computeIfAbsent(entity, ignored -> new SpinState())
                .update(renderTime, entity.visualRunning());
        BlockState state = entity.getBlockState();

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Vector3f.YP.rotationDegrees(-facingDegrees(state.getValue(MaltMillBlock.FACING))));
        pose.translate(-0.5, -0.5, -0.5);

        renderPart(
                ROLLER_FRONT_MODEL,
                FRONT_PIVOT_X,
                FRONT_PIVOT_Y,
                FRONT_PIVOT_Z,
                frontRotation(angle),
                state,
                pose,
                buffers,
                packedLight,
                packedOverlay
        );
        renderPart(
                ROLLER_REAR_MODEL,
                REAR_PIVOT_X,
                REAR_PIVOT_Y,
                REAR_PIVOT_Z,
                rearRotation(angle),
                state,
                pose,
                buffers,
                packedLight,
                packedOverlay
        );
        renderPart(
                DRIVE_AXLE_MODEL,
                FRONT_PIVOT_X,
                FRONT_PIVOT_Y,
                FRONT_PIVOT_Z,
                axleRotation(angle),
                state,
                pose,
                buffers,
                packedLight,
                packedOverlay
        );
        pose.popPose();
    }

    private static void renderPart(
            ResourceLocation location,
            double pivotX,
            double pivotY,
            double pivotZ,
            float angle,
            BlockState state,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        ModelManager models = minecraft.getModelManager();
        BakedModel model = models.getModel(location);
        if (model == models.getMissingModel()) {
            return;
        }

        pose.pushPose();
        pose.translate(pivotX, pivotY, pivotZ);
        pose.mulPose(Vector3f.XP.rotationDegrees(angle));
        pose.translate(-pivotX, -pivotY, -pivotZ);

        RenderType renderType = RenderType.cutout();
        VertexConsumer vertices = buffers.getBuffer(renderType);
        ModelBlockRenderer renderer = minecraft.getBlockRenderer().getModelRenderer();
        renderer.renderModel(
                pose.last(),
                vertices,
                state,
                model,
                1.0F,
                1.0F,
                1.0F,
                packedLight,
                packedOverlay,
                ModelData.EMPTY,
                renderType
        );
        pose.popPose();
    }

    static float advanceAngle(float current, double deltaTicks, boolean running) {
        if (!running) {
            return current;
        }
        double clampedDelta = Mth.clamp(deltaTicks, 0.0, 1.0);
        return Mth.wrapDegrees(current + (float) clampedDelta * DEGREES_PER_TICK);
    }

    static float frontRotation(float angle) {
        return angle;
    }

    static float rearRotation(float angle) {
        return -angle;
    }

    static float axleRotation(float angle) {
        return angle;
    }

    static float facingDegrees(Direction facing) {
        return switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }

    private static ResourceLocation model(String path) {
        return new ResourceLocation("alcoholic", "block/" + path);
    }

    private static final class SpinState {
        private double lastRenderTime = Double.NaN;
        private float angle;

        private float update(double renderTime, boolean running) {
            if (Double.isNaN(lastRenderTime)) {
                lastRenderTime = renderTime;
                return angle;
            }
            double deltaTicks = renderTime - lastRenderTime;
            lastRenderTime = renderTime;
            angle = advanceAngle(angle, deltaTicks, running);
            return angle;
        }
    }
}

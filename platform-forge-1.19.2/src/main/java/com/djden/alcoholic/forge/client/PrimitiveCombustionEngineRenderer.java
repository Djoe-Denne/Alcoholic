package com.djden.alcoholic.forge.client;

import com.djden.alcoholic.minecraft.mechanical.PrimitiveCombustionEngineBlock;
import com.djden.alcoholic.minecraft.mechanical.PrimitiveCombustionEngineBlockEntity;
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

/**
 * Always draws the right-hand shaft and flywheel. They spin around the Blockbench
 * X-axis pivot while the engine is {@code LIT}.
 */
public final class PrimitiveCombustionEngineRenderer
        implements BlockEntityRenderer<PrimitiveCombustionEngineBlockEntity> {
    static final float DEGREES_PER_TICK = 8.0F;
    static final ResourceLocation SHAFT_MODEL =
            new ResourceLocation("alcoholic", "block/primitive_combustion_engine_shaft");
    static final ResourceLocation FLYWHEEL_MODEL =
            new ResourceLocation("alcoholic", "block/primitive_combustion_engine_flywheel");

    private static final double PIVOT_X = 8.0 / 16.0;
    private static final double PIVOT_Y = 10.0 / 16.0;
    private static final double PIVOT_Z = 8.0 / 16.0;

    private final Map<PrimitiveCombustionEngineBlockEntity, SpinState> spinStates = new WeakHashMap<>();

    public PrimitiveCombustionEngineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            PrimitiveCombustionEngineBlockEntity entity,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        if (entity.getLevel() == null) {
            return;
        }

        BlockState state = entity.getBlockState();
        boolean running = state.hasProperty(PrimitiveCombustionEngineBlock.LIT)
                && state.getValue(PrimitiveCombustionEngineBlock.LIT);
        double renderTime = entity.getLevel().getGameTime() + partialTick;
        float angle = spinStates.computeIfAbsent(entity, ignored -> new SpinState())
                .update(renderTime, running);

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Vector3f.YP.rotationDegrees(-facingDegrees(state.getValue(PrimitiveCombustionEngineBlock.FACING))));
        pose.translate(-0.5, -0.5, -0.5);

        renderPart(SHAFT_MODEL, angle, state, pose, buffers, packedLight, packedOverlay);
        renderPart(FLYWHEEL_MODEL, angle, state, pose, buffers, packedLight, packedOverlay);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(PrimitiveCombustionEngineBlockEntity entity) {
        return true;
    }

    private static void renderPart(
            ResourceLocation location,
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
        pose.translate(PIVOT_X, PIVOT_Y, PIVOT_Z);
        pose.mulPose(Vector3f.XP.rotationDegrees(angle));
        pose.translate(-PIVOT_X, -PIVOT_Y, -PIVOT_Z);

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

    static float facingDegrees(Direction facing) {
        return switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
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

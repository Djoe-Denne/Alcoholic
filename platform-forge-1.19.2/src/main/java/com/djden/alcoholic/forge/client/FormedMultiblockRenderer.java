package com.djden.alcoholic.forge.client;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.multiblock.AxisBox;
import com.djden.alcoholic.domain.multiblock.Box3;
import com.djden.alcoholic.domain.multiblock.FormedArtSize;
import com.djden.alcoholic.domain.multiblock.FormedHullKit;
import com.djden.alcoholic.domain.multiblock.MachineScale;
import com.djden.alcoholic.domain.multiblock.MultiblockGeometry;
import com.djden.alcoholic.minecraft.multiblock.MultiblockControllerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;

/**
 * Formed look: welded 9-slice hull at every legal size, fittings stay as
 * 1×1 world blocks, sculpted mega-mesh overlay only at the art size.
 */
public class FormedMultiblockRenderer implements BlockEntityRenderer<MultiblockControllerBlockEntity> {
    static final ResourceLocation INDUSTRIAL_CASING_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("alcoholic", "textures/block/industrial_casing.png");
    static final ResourceLocation CRAFT_CASING_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("alcoholic", "textures/block/craft_casing.png");

    public FormedMultiblockRenderer(BlockEntityRendererProvider.Context context) {
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
        entity.geometry().ifPresent(geometry -> {
            renderHull(entity, geometry, pose, buffers, packedLight, packedOverlay);
            renderArtMesh(entity, geometry, pose, buffers, packedLight, packedOverlay);
        });
        if (FormedArtSize.PRESS_ID.equals(entity.definitionId())) {
            renderPlaten(entity, pose, buffers, packedLight, packedOverlay);
        }
        if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
            entity.crushVolume().ifPresent(crush -> renderCrush(entity.getBlockPos(), crush, pose, buffers));
        }
    }

    private static void renderHull(
            MultiblockControllerBlockEntity entity,
            MultiblockGeometry geometry,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        AxisBox box = geometry.bounds();
        List<FormedHullKit.HullQuad> quads = FormedHullKit.quads(box.width(), box.height(), box.depth());
        BlockPos origin = entity.getBlockPos();
        pose.pushPose();
        pose.translate(
                box.minX() - origin.getX(),
                box.minY() - origin.getY(),
                box.minZ() - origin.getZ()
        );
        VertexConsumer consumer = buffers.getBuffer(RenderType.entitySolid(hullTexture(entity)));
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        for (FormedHullKit.HullQuad quad : quads) {
            emit(consumer, matrix, normal, quad, packedLight, packedOverlay);
        }
        pose.popPose();
    }

    private static ResourceLocation hullTexture(MultiblockControllerBlockEntity entity) {
        if (entity.definition().map(definition -> definition.scale() == MachineScale.CRAFT).orElse(false)) {
            return CRAFT_CASING_TEXTURE;
        }
        return INDUSTRIAL_CASING_TEXTURE;
    }

    private static void renderArtMesh(
            MultiblockControllerBlockEntity entity,
            MultiblockGeometry geometry,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        FormedArtSize.overlayMesh(entity.definitionId(), geometry.bounds()).ifPresent(size -> {
            ResourceLocation location = meshModel(entity.definitionId());
            Minecraft minecraft = Minecraft.getInstance();
            ModelManager models = minecraft.getModelManager();
            BakedModel model = models.getModel(location);
            if (model == models.getMissingModel()) {
                return;
            }
            BlockPos origin = entity.getBlockPos();
            AxisBox box = geometry.bounds();
            pose.pushPose();
            pose.translate(
                    box.minX() - origin.getX(),
                    box.minY() - origin.getY(),
                    box.minZ() - origin.getZ()
            );
            pose.scale(size.width(), size.height(), size.depth());
            RenderType renderType = RenderType.cutout();
            VertexConsumer vertices = buffers.getBuffer(renderType);
            ModelBlockRenderer renderer = minecraft.getBlockRenderer().getModelRenderer();
            BlockState state = entity.getBlockState();
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
        });
    }

    private static void renderPlaten(
            MultiblockControllerBlockEntity entity,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        float travel = (float) platenOffset(entity.strokeCycle());
        pose.pushPose();
        pose.translate(0.25, 0.75 - travel, 0.25);
        pose.scale(0.5F, 0.12F, 0.5F);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                Blocks.IRON_BLOCK.defaultBlockState(),
                pose,
                buffers,
                packedLight,
                packedOverlay
        );
        pose.popPose();
    }

    static ResourceLocation meshModel(ResourceId definitionId) {
        return ResourceLocation.fromNamespaceAndPath(definitionId.namespace(), "block/formed/" + definitionId.path());
    }

    private static void emit(
            VertexConsumer consumer,
            Matrix4f matrix,
            Matrix3f normal,
            FormedHullKit.HullQuad quad,
            int packedLight,
            int packedOverlay
    ) {
        vertex(consumer, matrix, normal, quad.ax(), quad.ay(), quad.az(), quad.u0(), quad.v0(),
                quad.nx(), quad.ny(), quad.nz(), packedLight, packedOverlay);
        vertex(consumer, matrix, normal, quad.bx(), quad.by(), quad.bz(), quad.u1(), quad.v0(),
                quad.nx(), quad.ny(), quad.nz(), packedLight, packedOverlay);
        vertex(consumer, matrix, normal, quad.cx(), quad.cy(), quad.cz(), quad.u1(), quad.v1(),
                quad.nx(), quad.ny(), quad.nz(), packedLight, packedOverlay);
        vertex(consumer, matrix, normal, quad.dx(), quad.dy(), quad.dz(), quad.u0(), quad.v1(),
                quad.nx(), quad.ny(), quad.nz(), packedLight, packedOverlay);
    }

    private static void vertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            Matrix3f normal,
            float x,
            float y,
            float z,
            float u,
            float v,
            float nx,
            float ny,
            float nz,
            int packedLight,
            int packedOverlay
    ) {
        consumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normal, nx, ny, nz)
                .endVertex();
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
        Vec3 min = new Vec3(crush.minX(), crush.minY(), crush.minZ())
                .subtract(origin.getX(), origin.getY(), origin.getZ());
        Vec3 max = new Vec3(crush.maxX(), crush.maxY(), crush.maxZ())
                .subtract(origin.getX(), origin.getY(), origin.getZ());
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

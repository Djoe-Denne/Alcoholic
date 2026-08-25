package com.djden.alcoholic.integration.crossroads.forge;

import com.Da_Technomancer.crossroads.api.Capabilities;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.multiblock.KineticSource;
import com.djden.alcoholic.minecraft.process.MaltMillBlock;
import com.djden.alcoholic.minecraft.process.MaltMillBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Exposes {@code AXLE_CAPABILITY} on Alcoholic mechanical consumers so a
 * Crossroads axle joins the rotary network instead of being polled.
 */
public final class CrossroadsAxleCapability {
    private static final ResourceLocation KEY =
            ResourceLocation.fromNamespaceAndPath(AlcoholicIds.MOD_ID, "crossroads_axle");

    @SubscribeEvent
    public void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        BlockEntity entity = event.getObject();
        if (!isConsumer(entity)) {
            return;
        }
        CrossroadsMachineAxle axle = new CrossroadsMachineAxle(entity);
        CrossroadsAxleAttachments.put(entity, axle);
        LazyOptional<CrossroadsMachineAxle> optional = LazyOptional.of(() -> axle);
        event.addCapability(KEY, new ICapabilityProvider() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                if (cap != Capabilities.AXLE_CAPABILITY || !accepts(entity, side)) {
                    return LazyOptional.empty();
                }
                return optional.cast();
            }
        });
        event.addListener(() -> {
            optional.invalidate();
            axle.disconnect();
            CrossroadsAxleAttachments.remove(entity);
        });
    }

    private static boolean isConsumer(BlockEntity entity) {
        return entity instanceof MaltMillBlockEntity || entity instanceof KineticSource;
    }

    private static boolean accepts(BlockEntity entity, @Nullable Direction side) {
        if (side == null) {
            return true;
        }
        BlockState state = entity.getBlockState();
        if (entity instanceof MaltMillBlockEntity && state.hasProperty(MaltMillBlock.FACING)) {
            Direction facing = state.getValue(MaltMillBlock.FACING);
            return side == facing || side == facing.getOpposite();
        }
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
            return side == facing || side == facing.getOpposite();
        }
        return side.getAxis().isHorizontal();
    }
}

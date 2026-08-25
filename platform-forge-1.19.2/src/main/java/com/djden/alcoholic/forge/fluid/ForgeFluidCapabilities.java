package com.djden.alcoholic.forge.fluid;

import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.fluid.LiquidVessel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ForgeFluidCapabilities {
    private static final ResourceLocation KEY =
            ResourceLocation.fromNamespaceAndPath(AlcoholicIds.MOD_ID, "liquid_tank");
    private final ForgeFluidContent fluids;

    public ForgeFluidCapabilities(ForgeFluidContent fluids) {
        this.fluids = fluids;
    }

    @SubscribeEvent
    public void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        if (!(event.getObject() instanceof LiquidVessel vessel)) {
            return;
        }
        BlockEntity blockEntity = event.getObject();
        LazyOptional<IFluidHandler> handler = LazyOptional.of(() -> new TankFluidHandler(vessel, fluids, () -> {
            blockEntity.setChanged();
            Level level = blockEntity.getLevel();
            if (level != null && !level.isClientSide) {
                BlockState state = blockEntity.getBlockState();
                level.sendBlockUpdated(blockEntity.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
            }
        }));
        event.addCapability(KEY, new ICapabilityProvider() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, handler);
            }
        });
        event.addListener(handler::invalidate);
    }
}

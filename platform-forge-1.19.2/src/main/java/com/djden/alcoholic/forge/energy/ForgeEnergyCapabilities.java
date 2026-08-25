package com.djden.alcoholic.forge.energy;

import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.energy.EnergyBuffer;
import com.djden.alcoholic.minecraft.energy.EnergyHolder;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Exposes {@link ForgeCapabilities#ENERGY} over Alcoholic {@link EnergyHolder}
 * blocks so any FE provider — including an IE Energy Connector — can charge
 * the electric motor.
 */
public final class ForgeEnergyCapabilities {
    private static final ResourceLocation KEY =
            ResourceLocation.fromNamespaceAndPath(AlcoholicIds.MOD_ID, "energy");

    @SubscribeEvent
    public void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        if (!(event.getObject() instanceof EnergyHolder holder)) {
            return;
        }
        LazyOptional<IEnergyStorage> storage = LazyOptional.of(() -> {
            EnergyBuffer buffer = holder.energy();
            return new IEnergyStorage() {
                @Override
                public int receiveEnergy(int maxReceive, boolean simulate) {
                    int accepted = buffer.receive(maxReceive, simulate);
                    if (!simulate && accepted > 0) {
                        event.getObject().setChanged();
                    }
                    return accepted;
                }

                @Override
                public int extractEnergy(int maxExtract, boolean simulate) {
                    return 0;
                }

                @Override
                public int getEnergyStored() {
                    return buffer.stored();
                }

                @Override
                public int getMaxEnergyStored() {
                    return buffer.capacity();
                }

                @Override
                public boolean canExtract() {
                    return false;
                }

                @Override
                public boolean canReceive() {
                    return true;
                }
            };
        });
        event.addCapability(KEY, new ICapabilityProvider() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                return ForgeCapabilities.ENERGY.orEmpty(cap, storage);
            }
        });
        event.addListener(storage::invalidate);
    }
}

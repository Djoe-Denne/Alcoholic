package com.djden.alcoholic.forge.item;

import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Exposes {@code ITEM_HANDLER} on Alcoholic worldly containers so Create
 * belts, funnels, and chutes can insert and extract without a custom conveyor.
 */
public final class ForgeItemCapabilities {
    private static final ResourceLocation KEY =
            ResourceLocation.fromNamespaceAndPath(AlcoholicIds.MOD_ID, "item_handler");

    @SubscribeEvent
    public void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        BlockEntity entity = event.getObject();
        if (!(entity instanceof WorldlyContainer container)) {
            return;
        }
        if (!entity.getClass().getName().startsWith("com.djden.alcoholic")) {
            return;
        }
        event.addCapability(KEY, new ICapabilityProvider() {
            private final LazyOptional<IItemHandlerModifiable>[] handlers =
                    SidedInvWrapper.create(container, Direction.values());

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                if (cap != ForgeCapabilities.ITEM_HANDLER) {
                    return LazyOptional.empty();
                }
                if (side == null) {
                    return handlers[0].cast();
                }
                return handlers[side.ordinal()].cast();
            }
        });
    }
}

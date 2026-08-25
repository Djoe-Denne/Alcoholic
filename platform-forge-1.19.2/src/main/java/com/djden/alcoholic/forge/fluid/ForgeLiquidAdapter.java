package com.djden.alcoholic.forge.fluid;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.minecraft.fluid.LiquidBatchNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public final class ForgeLiquidAdapter {
    private ForgeLiquidAdapter() {
    }

    public static FluidStack toStack(LiquidBatch batch, ForgeFluidContent fluids) {
        if (batch.volumeMillibuckets() <= 0 || batch.baseLiquid().isEmpty()) {
            return FluidStack.EMPTY;
        }
        ResourceId definition = batch.baseLiquid().orElseThrow();
        Fluid fluid = fluids.source(definition);
        if (fluid == null) {
            fluid = ForgeRegistries.FLUIDS.getValue(
                    ResourceLocation.fromNamespaceAndPath(definition.namespace(), definition.path())
            );
        }
        if (fluid == null) {
            return FluidStack.EMPTY;
        }
        FluidStack stack = new FluidStack(fluid, batch.volumeMillibuckets());
        CompoundTag tag = LiquidBatchNbt.toTag(batch);
        tag.remove("Volume");
        stack.setTag(tag);
        return stack;
    }

    public static Optional<LiquidBatch> fromStack(FluidStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        ResourceLocation key = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
        if (key == null) {
            return Optional.empty();
        }
        ResourceId definition = new ResourceId(key.getNamespace(), key.getPath());
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Version")) {
            return LiquidBatchNbt.fromTag(tag).map(batch -> batch.withVolume(stack.getAmount()));
        }
        if (tag != null && tag.contains(LiquidBatchNbt.ROOT_TAG)) {
            return LiquidBatchNbt.fromTag(tag.getCompound(LiquidBatchNbt.ROOT_TAG))
                    .map(batch -> batch.withVolume(stack.getAmount()));
        }
        return Optional.of(LiquidBatch.of(definition, stack.getAmount(), PropertyBag.empty()));
    }
}

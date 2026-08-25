package com.djden.alcoholic.forge.fluid;

import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.minecraft.fluid.LiquidTank;
import com.djden.alcoholic.minecraft.fluid.LiquidVessel;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public final class TankFluidHandler implements IFluidHandler {
    private final LiquidVessel vessel;
    private final ForgeFluidContent fluids;

    public TankFluidHandler(LiquidTank tank, ForgeFluidContent fluids) {
        this(new SingleTankVessel(tank), fluids);
    }

    public TankFluidHandler(LiquidVessel vessel, ForgeFluidContent fluids) {
        this.vessel = vessel;
        this.fluids = fluids;
    }

    @Override
    public int getTanks() {
        return vessel.tankCount();
    }

    @Override
    public FluidStack getFluidInTank(int tankIndex) {
        return vessel.tank(tankIndex).contents()
                .map(batch -> ForgeLiquidAdapter.toStack(batch, fluids))
                .orElse(FluidStack.EMPTY);
    }

    @Override
    public int getTankCapacity(int tankIndex) {
        return vessel.tank(tankIndex).capacity();
    }

    @Override
    public boolean isFluidValid(int tankIndex, FluidStack stack) {
        return ForgeLiquidAdapter.fromStack(stack).isPresent();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return ForgeLiquidAdapter.fromStack(resource)
                .map(batch -> {
                    int remaining = batch.volumeMillibuckets();
                    int accepted = 0;
                    for (int index = 0; index < vessel.tankCount() && remaining > 0; index++) {
                        LiquidBatch slice = batch.split(remaining).extracted();
                        int filled = vessel.tank(index).fill(slice, action.simulate());
                        accepted += filled;
                        remaining -= filled;
                    }
                    return accepted;
                })
                .orElse(0);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        for (int index = 0; index < vessel.tankCount(); index++) {
            FluidStack stored = getFluidInTank(index);
            if (!stored.isEmpty() && stored.getFluid() == resource.getFluid()) {
                LiquidBatch drained = vessel.tank(index).drain(resource.getAmount(), action.simulate());
                return ForgeLiquidAdapter.toStack(drained, fluids);
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        for (int index = 0; index < vessel.tankCount(); index++) {
            if (vessel.tank(index).contents().isPresent()) {
                LiquidBatch drained = vessel.tank(index).drain(maxDrain, action.simulate());
                return ForgeLiquidAdapter.toStack(drained, fluids);
            }
        }
        return FluidStack.EMPTY;
    }

    private record SingleTankVessel(LiquidTank tank) implements LiquidVessel {
    }
}

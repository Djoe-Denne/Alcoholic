package com.djden.alcoholic.forge.fluid;

import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.minecraft.fluid.LiquidTank;
import com.djden.alcoholic.minecraft.fluid.LiquidVessel;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public final class TankFluidHandler implements IFluidHandler {
    private final LiquidVessel vessel;
    private final ForgeFluidContent fluids;
    private final Runnable onChanged;

    public TankFluidHandler(LiquidTank tank, ForgeFluidContent fluids) {
        this(new SingleTankVessel(tank), fluids, () -> {
        });
    }

    public TankFluidHandler(LiquidVessel vessel, ForgeFluidContent fluids) {
        this(vessel, fluids, () -> {
        });
    }

    public TankFluidHandler(LiquidVessel vessel, ForgeFluidContent fluids, Runnable onChanged) {
        this.vessel = vessel;
        this.fluids = fluids;
        this.onChanged = onChanged == null ? () -> {
        } : onChanged;
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
        return vessel.canFillTank(tankIndex) && ForgeLiquidAdapter.fromStack(stack).isPresent();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return ForgeLiquidAdapter.fromStack(resource)
                .map(batch -> {
                    int remaining = batch.volumeMillibuckets();
                    int accepted = 0;
                    for (int index = 0; index < vessel.tankCount() && remaining > 0; index++) {
                        if (!vessel.canFillTank(index)) {
                            continue;
                        }
                        LiquidBatch slice = batch.split(remaining).extracted();
                        int filled = vessel.tank(index).fill(slice, action.simulate());
                        accepted += filled;
                        remaining -= filled;
                    }
                    if (action.execute() && accepted > 0) {
                        onChanged.run();
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
            if (!vessel.canDrainTank(index)) {
                continue;
            }
            FluidStack stored = getFluidInTank(index);
            if (stored.isEmpty() || stored.getFluid() != resource.getFluid()) {
                continue;
            }
            FluidStack drained = drainRepresentable(index, resource.getAmount(), action);
            if (!drained.isEmpty()) {
                return drained;
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        for (int index = 0; index < vessel.tankCount(); index++) {
            if (!vessel.canDrainTank(index) || vessel.tank(index).contents().isEmpty()) {
                continue;
            }
            FluidStack drained = drainRepresentable(index, maxDrain, action);
            if (!drained.isEmpty()) {
                return drained;
            }
        }
        return FluidStack.EMPTY;
    }

    private FluidStack drainRepresentable(int index, int amount, FluidAction action) {
        LiquidBatch simulated = vessel.tank(index).drain(amount, true);
        FluidStack stack = ForgeLiquidAdapter.toStack(simulated, fluids);
        if (stack.isEmpty()) {
            return FluidStack.EMPTY;
        }
        if (action.execute()) {
            vessel.tank(index).drain(amount, false);
            onChanged.run();
        }
        return stack;
    }

    private record SingleTankVessel(LiquidTank tank) implements LiquidVessel {
    }
}

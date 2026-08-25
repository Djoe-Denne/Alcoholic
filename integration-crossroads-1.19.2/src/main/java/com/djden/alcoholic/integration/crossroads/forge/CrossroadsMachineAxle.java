package com.djden.alcoholic.integration.crossroads.forge;

import com.Da_Technomancer.crossroads.api.rotary.IAxisHandler;
import com.Da_Technomancer.crossroads.api.rotary.IAxleHandler;
import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;
import com.djden.alcoholic.integration.crossroads.CrossroadsRotaryMapping;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

/**
 * Terminal Crossroads axle on an Alcoholic mechanical consumer. Joins the
 * rotary network through {@link IAxleHandler#propagate} (it is not a neighbor
 * poll). When the Alcoholic machine works, {@link #consumeAlcoholicWork}
 * removes joules from this axle so the axis actually loses energy.
 */
public final class CrossroadsMachineAxle implements IAxleHandler {
    private final BlockEntity host;
    private IAxisHandler axis;
    private byte updateKey;
    private double rotRatio = 1.0;
    private double energy;

    CrossroadsMachineAxle(BlockEntity host) {
        this.host = host;
    }

    public MechanicalDriveState driveState() {
        return CrossroadsRotaryMapping.driveState(getSpeed(), getEnergy());
    }

    public boolean consumeAlcoholicWork(double load) {
        double joules = CrossroadsRotaryMapping.joulesForLoad(load);
        if (joules <= 0.0) {
            return false;
        }
        if (Math.abs(energy) + 1e-9 < joules) {
            setEnergy(0.0);
            return false;
        }
        double sign = energy == 0.0 ? 1.0 : Math.signum(energy);
        setEnergy(sign * (Math.abs(energy) - joules));
        return true;
    }

    @Override
    public void propagate(
            @Nonnull IAxisHandler masterIn,
            byte key,
            double rotRatioIn,
            double lastRadius,
            boolean renderOffset
    ) {
        if (updateKey == key || masterIn.addToList(this)) {
            return;
        }
        axis = masterIn;
        updateKey = key;
        rotRatio = rotRatioIn == 0.0 ? 1.0 : rotRatioIn;
    }

    @Override
    public void disconnect() {
        axis = null;
        updateKey = 0;
    }

    @Override
    public double getSpeed() {
        if (axis == null) {
            return 0.0;
        }
        return axis.getBaseSpeed() * rotRatio;
    }

    @Override
    public double getEnergy() {
        return energy;
    }

    @Override
    public void setEnergy(double newEnergy) {
        energy = Double.isFinite(newEnergy) ? newEnergy : 0.0;
        dirty();
    }

    @Override
    public double getMoInertia() {
        return CrossroadsRotaryMapping.MOMENT_OF_INERTIA;
    }

    @Override
    public double getRotationRatio() {
        return rotRatio;
    }

    @Override
    public float getAngle(float partialTicks) {
        return 0.0F;
    }

    private void dirty() {
        host.setChanged();
    }
}

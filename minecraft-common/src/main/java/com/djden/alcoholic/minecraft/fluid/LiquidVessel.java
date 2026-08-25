package com.djden.alcoholic.minecraft.fluid;

public interface LiquidVessel {
    LiquidTank tank();

    default int tankCount() {
        return 1;
    }

    default LiquidTank tank(int index) {
        return tank();
    }

    default boolean canFillTank(int index) {
        return index == 0;
    }

    default boolean canDrainTank(int index) {
        return tankCount() == 1 ? index == 0 : index == tankCount() - 1;
    }
}

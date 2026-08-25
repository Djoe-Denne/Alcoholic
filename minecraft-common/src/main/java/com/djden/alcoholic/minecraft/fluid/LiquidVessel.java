package com.djden.alcoholic.minecraft.fluid;

public interface LiquidVessel {
    LiquidTank tank();

    default int tankCount() {
        return 1;
    }

    default LiquidTank tank(int index) {
        return tank();
    }
}

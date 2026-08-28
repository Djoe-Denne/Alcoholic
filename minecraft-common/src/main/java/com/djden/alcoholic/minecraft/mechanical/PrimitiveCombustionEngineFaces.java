package com.djden.alcoholic.minecraft.mechanical;

import net.minecraft.core.Direction;

/**
 * Kinetic geometry for the primitive combustion engine. The Blockbench shaft
 * sits on {@code +X} when the model faces north, which is the facing's clockwise face.
 */
public final class PrimitiveCombustionEngineFaces {
    private PrimitiveCombustionEngineFaces() {
    }

    public static Direction driveFace(Direction facing) {
        return facing.getClockWise();
    }

    public static Direction facingForDriveToward(Direction driveToward) {
        if (driveToward.getAxis().isVertical()) {
            throw new IllegalArgumentException("drive face must be horizontal: " + driveToward);
        }
        return driveToward.getCounterClockWise();
    }
}

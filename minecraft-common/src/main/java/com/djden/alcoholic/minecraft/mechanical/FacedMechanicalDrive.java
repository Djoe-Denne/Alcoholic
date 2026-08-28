package com.djden.alcoholic.minecraft.mechanical;

import net.minecraft.core.Direction;

/**
 * Optional face filter for a {@link com.djden.alcoholic.domain.mechanical.MechanicalDrivePort}.
 * Domain stays direction-agnostic; Minecraft blocks declare which cube face emits torque.
 */
public interface FacedMechanicalDrive {
    boolean transmitsToward(Direction face);
}

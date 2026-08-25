package com.djden.alcoholic.minecraft.multiblock;

/**
 * RPM probe used by industrial machines. Vanilla ports store a number;
 * the Create adapter reports network speed through the same surface.
 */
public interface KineticSource {
    double rpm();

    void setRpm(double rpm);
}

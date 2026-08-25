package com.djden.alcoholic.domain.mechanical;

/**
 * Rotary input seen by an Alcoholic machine. Native engines and optional
 * adapters implement this; machines never import a foreign kinetic API.
 */
public interface MechanicalDrivePort {
    MechanicalDriveState driveState();

    /**
     * {@code true} when this port generates or translates power. Relays such
     * as a vanilla kinetic port return {@code false} so adjacent sampling
     * cannot recurse through two ports.
     */
    default boolean isSource() {
        return true;
    }
}

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

    /**
     * Called when a machine actually performs mechanical work this tick.
     * Sources that store energy (electric motors, rotary adapters) drain here.
     * Idle machines must not call this.
     */
    default void consumeWork(double load) {
    }
}

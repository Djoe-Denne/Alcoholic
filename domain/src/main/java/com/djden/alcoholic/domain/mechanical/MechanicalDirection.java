package com.djden.alcoholic.domain.mechanical;

/**
 * Rotation sense for a mechanical drive. {@link #NONE} means the consumer
 * does not care; adapters may still report a concrete direction.
 */
public enum MechanicalDirection {
    NONE,
    CLOCKWISE,
    COUNTER_CLOCKWISE
}

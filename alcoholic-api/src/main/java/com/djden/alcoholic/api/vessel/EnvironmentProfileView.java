package com.djden.alcoholic.api.vessel;

import com.djden.alcoholic.api.PublicApi;

/**
 * Normalized surroundings for long-running processes. Contains no biome IDs.
 */
@PublicApi
public interface EnvironmentProfileView {
    double temperature();

    /**
     * 0 (unstable) to 1 (stable).
     */
    double stability();

    boolean sheltered();

    /**
     * 0 (dry) to 1 (saturated). Defaults to a temperate cellar.
     */
    default double humidity() {
        return 0.5;
    }
}

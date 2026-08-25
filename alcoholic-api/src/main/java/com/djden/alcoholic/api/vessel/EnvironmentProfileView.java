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
}

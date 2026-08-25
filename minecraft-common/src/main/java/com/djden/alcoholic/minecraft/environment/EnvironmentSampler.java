package com.djden.alcoholic.minecraft.environment;

import com.djden.alcoholic.domain.vessel.EnvironmentProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Samples a compact cellar-like profile from vanilla surroundings only.
 * Neighbours: six faces. No cubic region scan.
 */
public final class EnvironmentSampler {
    public static final int REFRESH_INTERVAL_TICKS = 200;

    private EnvironmentSampler() {
    }

    public static EnvironmentProfile sample(Level level, BlockPos position) {
        float biome = level.getBiome(position).value().getBaseTemperature();
        double ambient = biome * 18.0 + 6.0;
        boolean sky = level.canSeeSky(position);
        int solid = 0;
        for (Direction direction : Direction.values()) {
            BlockState neighbour = level.getBlockState(position.relative(direction));
            if (neighbour.canOcclude()) {
                solid++;
            }
        }
        boolean sheltered = !sky && solid >= 3;
        double depth = Math.max(0.0, (level.getSeaLevel() - position.getY()) / 32.0);
        double temperature = sheltered ? (ambient * 0.35) + (13.0 * 0.65) : ambient;
        double stability = Math.min(1.0, (sheltered ? 0.55 : 0.25) + (solid / 12.0) + Math.min(0.25, depth));
        if (sky) {
            stability *= 0.6;
        }
        return new EnvironmentProfile(temperature, stability, sheltered, humidity(level, position));
    }

    public static double humidity(Level level, BlockPos position) {
        if (level == null) {
            return 0.5;
        }
        float downfall = level.getBiome(position).value().getDownfall();
        if (!Float.isFinite(downfall)) {
            return 0.5;
        }
        return Math.max(0.0, Math.min(1.0, downfall));
    }
}

package com.djden.alcoholic.minecraft.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Samples process heat from the block below a machine. Vanilla sources live
 * here; optional probes (Create burners) register from the integration layer.
 */
public final class HeatSources {
    /**
     * Share of the below-block temperature delta a fermenting vessel absorbs.
     * Magma (65 °C) under a 14 °C cellar warms it to ~21 °C, inside the
     * preferred band without reaching mash temperatures.
     */
    public static final double FERMENT_WARMING_SHARE = 0.15;

    @FunctionalInterface
    public interface Probe {
        OptionalDouble sample(Level level, BlockPos heatPos, BlockState state);
    }

    private static final List<Probe> PROBES = new CopyOnWriteArrayList<>();

    private HeatSources() {
    }

    public static void register(Probe probe) {
        if (probe != null) {
            PROBES.add(probe);
        }
    }

    public static double celsius(Level level, BlockPos machine) {
        return celsius(level, machine, List.of(machine.below()));
    }

    public static double celsius(Level level, BlockPos machine, List<BlockPos> heatPositions) {
        if (level == null) {
            return 20.0;
        }
        double hottest = Double.NEGATIVE_INFINITY;
        List<BlockPos> positions = heatPositions == null || heatPositions.isEmpty()
                ? List.of(machine.below())
                : heatPositions;
        for (BlockPos heatPos : positions) {
            hottest = Math.max(hottest, sample(level, machine, heatPos));
        }
        return Double.isFinite(hottest) ? hottest : 20.0;
    }

    private static double sample(Level level, BlockPos machine, BlockPos heatPos) {
        BlockState state = level.getBlockState(heatPos);
        for (Probe probe : PROBES) {
            OptionalDouble sampled = probe.sample(level, heatPos, state);
            if (sampled.isPresent()) {
                return sampled.getAsDouble();
            }
        }
        return vanilla(state, EnvironmentSampler.sample(level, machine).temperature());
    }

    static double vanilla(BlockState state, double ambient) {
        if (state.is(Blocks.LAVA) || state.is(Blocks.LAVA_CAULDRON)) {
            return 100.0;
        }
        if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
            return 95.0;
        }
        if (state.is(Blocks.MAGMA_BLOCK)) {
            return 65.0;
        }
        if ((state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE))
                && state.hasProperty(BlockStateProperties.LIT)
                && state.getValue(BlockStateProperties.LIT)) {
            return state.is(Blocks.SOUL_CAMPFIRE) ? 95.0 : 100.0;
        }
        if (state.is(Blocks.BLAST_FURNACE)
                && state.hasProperty(BlockStateProperties.LIT)
                && state.getValue(BlockStateProperties.LIT)) {
            return 110.0;
        }
        if ((state.is(Blocks.FURNACE) || state.is(Blocks.SMOKER))
                && state.hasProperty(BlockStateProperties.LIT)
                && state.getValue(BlockStateProperties.LIT)) {
            return 80.0;
        }
        return ambient;
    }

    /**
     * Fermentation vessels absorb a damped share of the heat below: enough to
     * lift a cold cellar into the preferred band, never a mash-tun blanket.
     */
    public static double warmedAmbient(double ambient, double heatBelow) {
        if (!Double.isFinite(ambient) || !Double.isFinite(heatBelow) || heatBelow <= ambient) {
            return ambient;
        }
        return ambient + (heatBelow - ambient) * FERMENT_WARMING_SHARE;
    }
}

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
        if (level == null) {
            return 20.0;
        }
        BlockPos below = machine.below();
        BlockState state = level.getBlockState(below);
        for (Probe probe : PROBES) {
            OptionalDouble sampled = probe.sample(level, below, state);
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
}

package com.djden.alcoholic.minecraft.mechanical;

import com.djden.alcoholic.domain.mechanical.MechanicalDrivePort;
import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Samples adjacent rotary sources for a machine. Native engines implement
 * {@link MechanicalDrivePort}; optional adapters (Create, later Crossroads)
 * register a {@link Probe} so machines never import those APIs.
 */
public final class MechanicalDrives {
    @FunctionalInterface
    public interface Probe {
        Optional<MechanicalDriveState> sample(Level level, BlockPos drivePos, BlockState state);
    }

    private static final List<Probe> PROBES = new CopyOnWriteArrayList<>();

    private MechanicalDrives() {
    }

    public static void register(Probe probe) {
        if (probe != null) {
            PROBES.add(probe);
        }
    }

    public static MechanicalDriveState adjacent(Level level, BlockPos machine) {
        if (level == null || machine == null) {
            return MechanicalDriveState.idle();
        }
        MechanicalDriveState best = MechanicalDriveState.idle();
        for (Direction direction : Direction.values()) {
            best = MechanicalDriveState.stronger(best, at(level, machine.relative(direction)));
        }
        return best;
    }

    public static MechanicalDriveState at(Level level, BlockPos position) {
        if (level == null || position == null) {
            return MechanicalDriveState.idle();
        }
        BlockEntity entity = level.getBlockEntity(position);
        if (entity instanceof MechanicalDrivePort port && port.isSource()) {
            return port.driveState();
        }
        BlockState state = level.getBlockState(position);
        MechanicalDriveState best = MechanicalDriveState.idle();
        for (Probe probe : PROBES) {
            Optional<MechanicalDriveState> sampled = probe.sample(level, position, state);
            if (sampled.isPresent()) {
                best = MechanicalDriveState.stronger(best, sampled.get());
            }
        }
        return best;
    }
}

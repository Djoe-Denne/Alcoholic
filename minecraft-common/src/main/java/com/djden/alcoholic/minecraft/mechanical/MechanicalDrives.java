package com.djden.alcoholic.minecraft.mechanical;

import com.djden.alcoholic.domain.mechanical.MechanicalDrivePort;
import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;
import com.djden.alcoholic.domain.mechanical.MechanicalRequirement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Samples rotary sources for a machine. Native engines implement
 * {@link MechanicalDrivePort}; a {@link FacedMechanicalDrive} source only
 * counts on the face it declares. Optional adapters register a {@link Probe}
 * (adjacent foreign blocks) or a {@link LocalAdapter} (capability attached
 * to the machine itself, e.g. a Crossroads axle handler).
 */
public final class MechanicalDrives {
    @FunctionalInterface
    public interface Probe {
        Optional<MechanicalDriveState> sample(Level level, BlockPos drivePos, BlockState state);
    }

    public interface LocalAdapter {
        Optional<MechanicalDriveState> sample(Level level, BlockPos machine, BlockEntity entity);

        default boolean consumeWork(Level level, BlockPos machine, BlockEntity entity, double load) {
            return false;
        }
    }

    private record Selection(
            MechanicalDriveState state,
            MechanicalDrivePort adjacentPort,
            boolean localWon
    ) {
        static Selection idle() {
            return new Selection(MechanicalDriveState.idle(), null, false);
        }
    }

    private static final List<Probe> PROBES = new CopyOnWriteArrayList<>();
    private static final List<LocalAdapter> LOCAL = new CopyOnWriteArrayList<>();

    private MechanicalDrives() {
    }

    public static void register(Probe probe) {
        if (probe != null) {
            PROBES.add(probe);
        }
    }

    public static void registerLocal(LocalAdapter adapter) {
        if (adapter != null) {
            LOCAL.add(adapter);
        }
    }

    public static MechanicalDriveState adjacent(Level level, BlockPos machine) {
        return select(level, machine).state();
    }

    /**
     * Drive available to a consumer: adapters attached to the machine itself
     * plus adjacent native/probed sources. Machines should use this.
     */
    public static MechanicalDriveState forMachine(Level level, BlockPos machine) {
        return select(level, machine).state();
    }

    public static MechanicalDriveState forMachine(
            Level level,
            BlockPos machine,
            MechanicalRequirement requirement
    ) {
        return select(level, machine, requirement).state();
    }

    public static void consumeWork(Level level, BlockPos machine, double load) {
        consumeWork(level, machine, load, null);
    }

    public static void consumeWork(
            Level level,
            BlockPos machine,
            double load,
            MechanicalRequirement requirement
    ) {
        if (level == null || machine == null || load <= 0.0) {
            return;
        }
        Selection selection = select(level, machine, requirement);
        if (!selection.state().usable()) {
            return;
        }
        if (selection.localWon()) {
            BlockEntity self = level.getBlockEntity(machine);
            if (self == null) {
                return;
            }
            for (LocalAdapter adapter : LOCAL) {
                if (adapter.consumeWork(level, machine, self, load)) {
                    return;
                }
            }
            return;
        }
        if (selection.adjacentPort() != null) {
            selection.adjacentPort().consumeWork(load);
        }
    }

    public static MechanicalDriveState at(Level level, BlockPos position) {
        return adjacentSource(level, position).state();
    }

    private static Selection select(Level level, BlockPos machine) {
        return select(level, machine, null);
    }

    private static Selection select(Level level, BlockPos machine, MechanicalRequirement requirement) {
        if (level == null || machine == null) {
            return Selection.idle();
        }
        MechanicalDriveState local = localState(level, machine, requirement);
        Adjacent adjacent = strongestAdjacent(level, machine, requirement);
        MechanicalDriveState best = MechanicalDriveState.stronger(local, adjacent.state(), requirement);
        boolean localWon = best == local && local.usable();
        return new Selection(best, adjacent.port(), localWon);
    }

    private static MechanicalDriveState localState(
            Level level,
            BlockPos machine,
            MechanicalRequirement requirement
    ) {
        BlockEntity entity = level.getBlockEntity(machine);
        if (entity == null) {
            return MechanicalDriveState.idle();
        }
        MechanicalDriveState best = MechanicalDriveState.idle();
        for (LocalAdapter adapter : LOCAL) {
            Optional<MechanicalDriveState> sampled = adapter.sample(level, machine, entity);
            if (sampled.isPresent()) {
                best = MechanicalDriveState.stronger(best, sampled.get(), requirement);
            }
        }
        return best;
    }

    private record Adjacent(MechanicalDriveState state, MechanicalDrivePort port) {
        static Adjacent idle() {
            return new Adjacent(MechanicalDriveState.idle(), null);
        }
    }

    private static Adjacent strongestAdjacent(
            Level level,
            BlockPos machine,
            MechanicalRequirement requirement
    ) {
        Adjacent best = Adjacent.idle();
        for (Direction direction : Direction.values()) {
            Adjacent candidate = adjacentSource(
                    level,
                    machine.relative(direction),
                    requirement,
                    direction.getOpposite()
            );
            if (MechanicalDriveState.stronger(best.state(), candidate.state(), requirement) == candidate.state()) {
                best = candidate;
            }
        }
        return best;
    }

    private static Adjacent adjacentSource(Level level, BlockPos position) {
        return adjacentSource(level, position, null, null);
    }

    private static Adjacent adjacentSource(
            Level level,
            BlockPos position,
            MechanicalRequirement requirement,
            Direction sourceOutputFace
    ) {
        if (level == null || position == null) {
            return Adjacent.idle();
        }
        BlockEntity entity = level.getBlockEntity(position);
        if (entity instanceof MechanicalDrivePort port && port.isSource()) {
            if (sourceOutputFace != null
                    && entity instanceof FacedMechanicalDrive faced
                    && !faced.transmitsToward(sourceOutputFace)) {
                return Adjacent.idle();
            }
            return new Adjacent(port.driveState(), port);
        }
        BlockState state = level.getBlockState(position);
        MechanicalDriveState best = MechanicalDriveState.idle();
        for (Probe probe : PROBES) {
            Optional<MechanicalDriveState> sampled = probe.sample(level, position, state);
            if (sampled.isPresent()) {
                best = MechanicalDriveState.stronger(best, sampled.get(), requirement);
            }
        }
        return new Adjacent(best, null);
    }
}

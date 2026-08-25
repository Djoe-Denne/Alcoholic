package com.djden.alcoholic.integration.crossroads.forge;

import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * Tracks Crossroads axle handlers attached to Alcoholic consumers so the
 * {@link com.djden.alcoholic.minecraft.mechanical.MechanicalDrives.LocalAdapter}
 * can read and drain them without machines importing Crossroads types.
 */
public final class CrossroadsAxleAttachments {
    private static final Map<BlockEntity, CrossroadsMachineAxle> ATTACHED =
            Collections.synchronizedMap(new WeakHashMap<>());

    private CrossroadsAxleAttachments() {
    }

    static void put(BlockEntity entity, CrossroadsMachineAxle axle) {
        if (entity != null && axle != null) {
            ATTACHED.put(entity, axle);
        }
    }

    static void remove(BlockEntity entity) {
        ATTACHED.remove(entity);
    }

    public static Optional<CrossroadsMachineAxle> get(BlockEntity entity) {
        return Optional.ofNullable(ATTACHED.get(entity));
    }
}

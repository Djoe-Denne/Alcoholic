package com.djden.alcoholic.integration.crossroads.forge;

import com.djden.alcoholic.application.compatibility.CompatibilitySnapshot;
import com.djden.alcoholic.application.compatibility.KnownMod;
import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;
import com.djden.alcoholic.integration.crossroads.CrossroadsIntegration;
import com.djden.alcoholic.minecraft.mechanical.MechanicalDrives;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

import java.util.Optional;

/**
 * Optional Crossroads rotary adapter. Alcoholic machines keep depending on
 * {@link MechanicalDrives}; this module owns {@code IAxleHandler}.
 */
public final class ForgeCrossroadsIntegration {
    private ForgeCrossroadsIntegration() {
    }

    public static boolean shouldActivate(CompatibilitySnapshot compatibility) {
        return compatibility.isPresent(KnownMod.CROSSROADS);
    }

    public static void install() {
        if (!ModList.get().isLoaded(CrossroadsIntegration.MOD_ID)) {
            return;
        }
        MinecraftForge.EVENT_BUS.register(new CrossroadsAxleCapability());
        MechanicalDrives.registerLocal(new MechanicalDrives.LocalAdapter() {
            @Override
            public Optional<MechanicalDriveState> sample(Level level, BlockPos machine, BlockEntity entity) {
                return CrossroadsAxleAttachments.get(entity).map(CrossroadsMachineAxle::driveState);
            }

            @Override
            public boolean consumeWork(Level level, BlockPos machine, BlockEntity entity, double load) {
                return CrossroadsAxleAttachments.get(entity)
                        .map(axle -> axle.consumeAlcoholicWork(load))
                        .orElse(false);
            }
        });
    }
}

package com.djden.alcoholic.minecraft.multiblock;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

/**
 * Platform hook for the press easter egg. Forge installs a named damage source.
 */
public final class IndustrialDamage {
    private static volatile Function<Player, Boolean> crush = player -> {
        player.hurt(DamageSource.GENERIC, Float.MAX_VALUE);
        return player.isDeadOrDying();
    };

    private IndustrialDamage() {
    }

    public static void install(Function<Player, Boolean> hook) {
        if (hook != null) {
            crush = hook;
        }
    }

    public static boolean crush(Player player) {
        return crush.apply(player);
    }
}

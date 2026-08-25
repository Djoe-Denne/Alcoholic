package com.djden.alcoholic.forge.damage;

import com.djden.alcoholic.minecraft.multiblock.IndustrialDamage;
import net.minecraft.world.damagesource.DamageSource;

/**
 * Named crush damage for the industrial press easter egg.
 */
public final class IndustrialDamageSources {
    public static final DamageSource INDUSTRIAL_PRESS =
            new DamageSource("alcoholic.industrial_press").bypassArmor();

    private IndustrialDamageSources() {
    }

    public static void install() {
        IndustrialDamage.install(player -> player.hurt(INDUSTRIAL_PRESS, Float.MAX_VALUE));
    }
}

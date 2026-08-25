package com.djden.alcoholic.minecraft.viticulture;

import com.djden.alcoholic.domain.viticulture.VineEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;

import java.util.Objects;

/**
 * Derives a physical-ish vine environment from vanilla biome and light data.
 * No biome identifiers are special-cased.
 */
public final class MinecraftClimateResolver {
    private static final double MINECRAFT_TEMPERATURE_SCALE = 20.0;
    private static final double MINECRAFT_TEMPERATURE_OFFSET = -5.0;

    public VineEnvironment resolve(Level level, BlockPos position) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(position, "position");

        Biome biome = level.getBiome(position).value();
        double temperatureCelsius = toCelsius(biome.getBaseTemperature());
        double humidity = Mth.clamp(biome.getDownfall(), 0.0F, 1.0F);

        BlockPos lightPosition = position.above();
        double skyLight = level.getBrightness(LightLayer.SKY, lightPosition) / 15.0;
        double sunlight = level.canSeeSky(lightPosition)
                ? skyLight
                : skyLight * 0.25;

        return new VineEnvironment(
                temperatureCelsius,
                humidity,
                Mth.clamp(sunlight, 0.0, 1.0)
        );
    }

    public static double toCelsius(double minecraftTemperature) {
        double result = minecraftTemperature * MINECRAFT_TEMPERATURE_SCALE
                + MINECRAFT_TEMPERATURE_OFFSET;
        return Mth.clamp(result, -100.0, 100.0);
    }
}

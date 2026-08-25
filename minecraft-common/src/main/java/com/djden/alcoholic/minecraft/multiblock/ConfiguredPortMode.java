package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.domain.multiblock.PortMode;
import net.minecraft.util.StringRepresentable;

/**
 * Block-state IO mode. Domain {@link PortMode} stays Minecraft-free.
 */
public enum ConfiguredPortMode implements StringRepresentable {
    INPUT,
    OUTPUT,
    BOTH;

    public PortMode domain() {
        return PortMode.valueOf(name());
    }

    public boolean allowsInsert() {
        return this == INPUT || this == BOTH;
    }

    public boolean allowsExtract() {
        return this == OUTPUT || this == BOTH;
    }

    public ConfiguredPortMode next() {
        return switch (this) {
            case INPUT -> OUTPUT;
            case OUTPUT -> BOTH;
            case BOTH -> INPUT;
        };
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}

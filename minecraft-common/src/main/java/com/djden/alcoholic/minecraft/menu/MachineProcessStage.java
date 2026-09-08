package com.djden.alcoholic.minecraft.menu;

import java.util.Locale;

public enum MachineProcessStage {
    IDLE,
    PRESSING,
    FERMENTING,
    STEEPING,
    GERMINATION,
    KILNING,
    MILLING,
    MASH,
    BOIL,
    CONDITION,
    CONDITIONED,
    AGE,
    AGED;

    public static int encode(String id) {
        if (id == null || id.isBlank()) {
            return IDLE.ordinal();
        }
        try {
            return valueOf(id.trim().toUpperCase(Locale.ROOT)).ordinal();
        } catch (IllegalArgumentException ignored) {
            return IDLE.ordinal();
        }
    }

    public static MachineProcessStage decode(int code) {
        MachineProcessStage[] values = values();
        return code >= 0 && code < values.length ? values[code] : IDLE;
    }

    public String translationKey() {
        return "gui.alcoholic.stage." + name().toLowerCase(Locale.ROOT);
    }
}

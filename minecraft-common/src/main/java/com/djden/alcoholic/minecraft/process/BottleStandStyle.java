package com.djden.alcoholic.minecraft.process;

/** The physical presentation used by a bottle stand. */
public enum BottleStandStyle {
    RACK("bottle_rack"),
    SHELF("bottle_shelf");

    private final String id;

    BottleStandStyle(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}

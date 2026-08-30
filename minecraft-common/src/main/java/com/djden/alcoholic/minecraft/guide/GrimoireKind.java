package com.djden.alcoholic.minecraft.guide;

public enum GrimoireKind {
    WINE("wine"),
    BEER("beer");

    private final String path;

    GrimoireKind(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}

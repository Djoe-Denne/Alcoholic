package com.djden.alcoholic.application.compatibility;

public enum KnownMod {
    VINERY("vinery"),
    BREWERY("brewery"),
    CREATE("create");

    private final String modId;

    KnownMod(String modId) {
        this.modId = modId;
    }

    public String modId() {
        return modId;
    }
}

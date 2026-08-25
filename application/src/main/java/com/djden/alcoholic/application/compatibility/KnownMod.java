package com.djden.alcoholic.application.compatibility;

public enum KnownMod {
    VINERY("vinery"),
    BREWERY("brewery"),
    CREATE("create"),
    CROSSROADS("crossroads"),
    IMMERSIVE_ENGINEERING("immersiveengineering");

    private final String modId;

    KnownMod(String modId) {
        this.modId = modId;
    }

    public String modId() {
        return modId;
    }
}

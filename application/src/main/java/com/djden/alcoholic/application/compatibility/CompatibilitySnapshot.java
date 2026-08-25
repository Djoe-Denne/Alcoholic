package com.djden.alcoholic.application.compatibility;

import java.util.EnumSet;
import java.util.Set;

public final class CompatibilitySnapshot {
    private final Set<KnownMod> presentMods;

    public CompatibilitySnapshot(Set<KnownMod> presentMods) {
        this.presentMods = presentMods.isEmpty()
                ? Set.of()
                : Set.copyOf(EnumSet.copyOf(presentMods));
    }

    public boolean isPresent(KnownMod mod) {
        return presentMods.contains(mod);
    }

    public Set<KnownMod> presentMods() {
        return presentMods;
    }
}

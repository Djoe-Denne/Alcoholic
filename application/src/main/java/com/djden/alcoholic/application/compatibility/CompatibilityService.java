package com.djden.alcoholic.application.compatibility;

import com.djden.alcoholic.platform.api.mod.ModPresencePort;

import java.util.Arrays;
import java.util.EnumSet;

public final class CompatibilityService {
    private final ModPresencePort modPresence;

    public CompatibilityService(ModPresencePort modPresence) {
        this.modPresence = modPresence;
    }

    public CompatibilitySnapshot snapshot() {
        EnumSet<KnownMod> present = Arrays.stream(KnownMod.values())
                .filter(mod -> modPresence.isLoaded(mod.modId()))
                .collect(
                        () -> EnumSet.noneOf(KnownMod.class),
                        EnumSet::add,
                        EnumSet::addAll
                );
        return new CompatibilitySnapshot(present);
    }
}

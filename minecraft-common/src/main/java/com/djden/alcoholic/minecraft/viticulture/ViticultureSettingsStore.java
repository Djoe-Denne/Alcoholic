package com.djden.alcoholic.minecraft.viticulture;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Publishes complete settings snapshots atomically to game threads.
 */
public final class ViticultureSettingsStore {
    private final AtomicReference<ViticultureSettings> current;

    public ViticultureSettingsStore() {
        this(ViticultureSettings.defaults());
    }

    public ViticultureSettingsStore(ViticultureSettings initial) {
        current = new AtomicReference<>(Objects.requireNonNull(initial, "initial"));
    }

    public ViticultureSettings snapshot() {
        return current.get();
    }

    public void replace(ViticultureSettings settings) {
        current.set(Objects.requireNonNull(settings, "settings"));
    }
}

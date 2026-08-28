package com.djden.alcoholic.application.machine;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public final class MachineCatalogStore {
    private final AtomicReference<MachineCatalog> current = new AtomicReference<>(MachineCatalog.builtins());
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    public MachineCatalog snapshot() {
        return current.get();
    }

    public void addListener(Runnable listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void replace(MachineCatalog catalog) {
        current.set(Objects.requireNonNull(catalog, "catalog"));
        for (Runnable listener : listeners) {
            listener.run();
        }
    }
}

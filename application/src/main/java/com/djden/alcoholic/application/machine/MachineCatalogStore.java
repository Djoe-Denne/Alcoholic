package com.djden.alcoholic.application.machine;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class MachineCatalogStore {
    private final AtomicReference<MachineCatalog> current = new AtomicReference<>(MachineCatalog.builtins());

    public MachineCatalog snapshot() {
        return current.get();
    }

    public void replace(MachineCatalog catalog) {
        current.set(Objects.requireNonNull(catalog, "catalog"));
    }
}

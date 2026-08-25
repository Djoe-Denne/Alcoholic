package com.djden.alcoholic.application.beverage;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Publishes complete beverage-framework snapshots atomically.
 */
public final class BeverageCatalogStore {
    private final AtomicReference<BeverageCatalog> current;

    public BeverageCatalogStore() {
        this(BeverageCatalog.empty());
    }

    public BeverageCatalogStore(BeverageCatalog initial) {
        current = new AtomicReference<>(Objects.requireNonNull(initial, "initial"));
    }

    public BeverageCatalog snapshot() {
        return current.get();
    }

    public void replace(BeverageCatalog catalog) {
        current.set(Objects.requireNonNull(catalog, "catalog"));
    }
}

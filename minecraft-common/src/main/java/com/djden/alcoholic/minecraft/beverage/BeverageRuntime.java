package com.djden.alcoholic.minecraft.beverage;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.application.beverage.BeverageCatalogStore;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;

import java.util.Objects;

/**
 * Minecraft-side composition root for the beverage framework.
 */
public final class BeverageRuntime {
    private static final BeverageRuntime SHARED = new BeverageRuntime();

    private final AlcoholicApi api;
    private final BeverageCatalogStore store;

    public BeverageRuntime() {
        this(AlcoholicApi.shared(), new BeverageCatalogStore());
    }

    public BeverageRuntime(AlcoholicApi api, BeverageCatalogStore store) {
        this.api = Objects.requireNonNull(api, "api");
        this.store = Objects.requireNonNull(store, "store");
        BuiltinRegistrations.install(this.api, this.store::snapshot);
    }

    public static BeverageRuntime shared() {
        return SHARED;
    }

    public AlcoholicApi api() {
        return api;
    }

    public BeverageCatalog catalog() {
        return store.snapshot();
    }

    public BeverageCatalogStore store() {
        return store;
    }

    public void freeze() {
        api.freeze();
    }
}

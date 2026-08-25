package com.djden.alcoholic.minecraft.content;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.minecraft.beverage.BeverageRuntime;

import java.util.Objects;

/**
 * Installs Alcoholic-provided process capabilities and properties.
 */
public final class BeverageFrameworkBootstrap {
    private BeverageFrameworkBootstrap() {
    }

    public static void install(AlcoholicApi api) {
        BuiltinRegistrations.install(Objects.requireNonNull(api, "api"));
    }

    public static void install(BeverageRuntime runtime) {
        Objects.requireNonNull(runtime, "runtime");
        BuiltinRegistrations.install(runtime.api(), runtime.store()::snapshot);
    }
}

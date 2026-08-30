package com.djden.alcoholic.minecraft.guide;

import java.util.Objects;
import java.util.function.Consumer;

public final class GrimoireClientOpen {
    private static volatile Consumer<GrimoireKind> open = kind -> {
    };

    private GrimoireClientOpen() {
    }

    public static void bind(Consumer<GrimoireKind> handler) {
        open = Objects.requireNonNull(handler, "handler");
    }

    public static void open(GrimoireKind kind) {
        open.accept(Objects.requireNonNull(kind, "kind"));
    }
}

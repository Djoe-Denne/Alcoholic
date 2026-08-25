package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.application.machine.MachineCatalog;
import com.djden.alcoholic.application.machine.MachineCatalogStore;
import com.djden.alcoholic.application.process.CapabilityProcessExecutor;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;

public final class IndustrialRuntime {
    private final MachineCatalogStore machines = new MachineCatalogStore();
    private final CapabilityProcessExecutor press = new CapabilityProcessExecutor(BuiltinRegistrations.PRESS);
    private final CapabilityProcessExecutor ferment = new CapabilityProcessExecutor(BuiltinRegistrations.FERMENT);

    public static IndustrialRuntime shared() {
        return Holder.INSTANCE;
    }

    public MachineCatalog machines() {
        return machines.snapshot();
    }

    public MachineCatalogStore store() {
        return machines;
    }

    public CapabilityProcessExecutor pressExecutor() {
        return press;
    }

    public CapabilityProcessExecutor fermentExecutor() {
        return ferment;
    }

    private static final class Holder {
        private static final IndustrialRuntime INSTANCE = new IndustrialRuntime();
    }
}

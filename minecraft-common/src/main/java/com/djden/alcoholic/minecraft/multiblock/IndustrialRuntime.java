package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.machine.MachineCatalog;
import com.djden.alcoholic.application.machine.MachineCatalogStore;
import com.djden.alcoholic.application.process.CapabilityProcessExecutor;
import com.djden.alcoholic.minecraft.process.ProcessRuntime;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class IndustrialRuntime {
    private final MachineCatalogStore machines = new MachineCatalogStore();
    private final Map<ResourceId, IndustrialProcessStrategy> strategies = new LinkedHashMap<>();

    public IndustrialRuntime() {
        register(BuiltinRegistrations.PRESS, MultiblockControllerBlockEntity::executePress);
        register(BuiltinRegistrations.FERMENT, MultiblockControllerBlockEntity::executeFerment);
        register(BuiltinRegistrations.MALT, MultiblockControllerBlockEntity::executeMalt);
        register(BuiltinRegistrations.MILL, MultiblockControllerBlockEntity::executeMill);
        register(BuiltinRegistrations.MASH, MultiblockControllerBlockEntity::executeMash);
        register(BuiltinRegistrations.BOIL, MultiblockControllerBlockEntity::executeBoil);
        register(BuiltinRegistrations.CONDITION, MultiblockControllerBlockEntity::executeCondition);
        register(BuiltinRegistrations.AGE, MultiblockControllerBlockEntity::executeAge);
    }

    public static IndustrialRuntime shared() {
        return Holder.INSTANCE;
    }

    public MachineCatalog machines() {
        return machines.snapshot();
    }

    public MachineCatalogStore store() {
        return machines;
    }

    public CapabilityProcessExecutor executor(ResourceId processType) {
        return ProcessRuntime.shared().executor(processType);
    }

    public CapabilityProcessExecutor pressExecutor() {
        return executor(BuiltinRegistrations.PRESS);
    }

    public CapabilityProcessExecutor fermentExecutor() {
        return executor(BuiltinRegistrations.FERMENT);
    }

    public void register(ResourceId processType, IndustrialProcessStrategy strategy) {
        Objects.requireNonNull(processType, "processType");
        Objects.requireNonNull(strategy, "strategy");
        strategies.put(processType, strategy);
    }

    public Optional<IndustrialProcessStrategy> strategy(ResourceId processType) {
        if (processType == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(strategies.get(processType));
    }

    private static final class Holder {
        private static final IndustrialRuntime INSTANCE = new IndustrialRuntime();
    }
}

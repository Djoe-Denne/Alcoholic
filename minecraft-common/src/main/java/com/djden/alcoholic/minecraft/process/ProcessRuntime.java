package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.property.PropertyAggregator;
import com.djden.alcoholic.api.property.PropertyMerge;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.process.CapabilityProcessExecutor;
import com.djden.alcoholic.application.process.ExecuteProcessUseCase;
import com.djden.alcoholic.application.process.PropertyMerges;
import com.djden.alcoholic.minecraft.beverage.BeverageRuntime;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public final class ProcessRuntime {
    private final BeverageRuntime beverages;
    private final ExecuteProcessUseCase engine;
    private final Map<ResourceId, CapabilityProcessExecutor> executors = new LinkedHashMap<>();

    public ProcessRuntime(BeverageRuntime beverages) {
        this.beverages = beverages;
        this.engine = new ExecuteProcessUseCase(beverages.api());
        register(BuiltinRegistrations.PRESS);
        register(BuiltinRegistrations.FERMENT);
        register(BuiltinRegistrations.AGE);
        register(BuiltinRegistrations.BLEND);
        register(BuiltinRegistrations.BOTTLE);
        register(BuiltinRegistrations.MALT);
        register(BuiltinRegistrations.MILL);
        register(BuiltinRegistrations.MASH);
        register(BuiltinRegistrations.BOIL);
        register(BuiltinRegistrations.CONDITION);
    }

    public static ProcessRuntime shared() {
        return Holder.INSTANCE;
    }

    public BeverageRuntime beverages() {
        return beverages;
    }

    public ExecuteProcessUseCase engine() {
        return engine;
    }

    public CapabilityProcessExecutor executor(ResourceId processType) {
        Objects.requireNonNull(processType, "processType");
        return executors.computeIfAbsent(processType, CapabilityProcessExecutor::new);
    }

    public CapabilityProcessExecutor pressExecutor() {
        return executor(BuiltinRegistrations.PRESS);
    }

    public CapabilityProcessExecutor fermentExecutor() {
        return executor(BuiltinRegistrations.FERMENT);
    }

    public CapabilityProcessExecutor ageExecutor() {
        return executor(BuiltinRegistrations.AGE);
    }

    public CapabilityProcessExecutor blendExecutor() {
        return executor(BuiltinRegistrations.BLEND);
    }

    public CapabilityProcessExecutor bottleExecutor() {
        return executor(BuiltinRegistrations.BOTTLE);
    }

    public CapabilityProcessExecutor maltExecutor() {
        return executor(BuiltinRegistrations.MALT);
    }

    public CapabilityProcessExecutor millExecutor() {
        return executor(BuiltinRegistrations.MILL);
    }

    public CapabilityProcessExecutor mashExecutor() {
        return executor(BuiltinRegistrations.MASH);
    }

    public CapabilityProcessExecutor boilExecutor() {
        return executor(BuiltinRegistrations.BOIL);
    }

    public CapabilityProcessExecutor conditionExecutor() {
        return executor(BuiltinRegistrations.CONDITION);
    }

    public Function<ResourceId, PropertyMerge> merges() {
        return PropertyMerges.from(beverages.api());
    }

    public Function<ResourceId, PropertyAggregator> aggregators() {
        return PropertyMerges.aggregators(beverages.api());
    }

    private void register(ResourceId processType) {
        executors.put(processType, new CapabilityProcessExecutor(processType));
    }

    private static final class Holder {
        private static final ProcessRuntime INSTANCE = new ProcessRuntime(BeverageRuntime.shared());
    }
}

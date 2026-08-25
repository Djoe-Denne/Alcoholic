package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.property.PropertyAggregator;
import com.djden.alcoholic.api.property.PropertyMerge;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.process.CapabilityProcessExecutor;
import com.djden.alcoholic.application.process.ExecuteProcessUseCase;
import com.djden.alcoholic.application.process.PropertyMerges;
import com.djden.alcoholic.minecraft.beverage.BeverageRuntime;

import java.util.function.Function;

public final class ProcessRuntime {
    private final BeverageRuntime beverages;
    private final ExecuteProcessUseCase engine;
    private final CapabilityProcessExecutor press;
    private final CapabilityProcessExecutor ferment;
    private final CapabilityProcessExecutor age;
    private final CapabilityProcessExecutor blend;
    private final CapabilityProcessExecutor bottle;

    public ProcessRuntime(BeverageRuntime beverages) {
        this.beverages = beverages;
        this.engine = new ExecuteProcessUseCase(beverages.api());
        this.press = new CapabilityProcessExecutor(BuiltinRegistrations.PRESS);
        this.ferment = new CapabilityProcessExecutor(BuiltinRegistrations.FERMENT);
        this.age = new CapabilityProcessExecutor(BuiltinRegistrations.AGE);
        this.blend = new CapabilityProcessExecutor(BuiltinRegistrations.BLEND);
        this.bottle = new CapabilityProcessExecutor(BuiltinRegistrations.BOTTLE);
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

    public CapabilityProcessExecutor pressExecutor() {
        return press;
    }

    public CapabilityProcessExecutor fermentExecutor() {
        return ferment;
    }

    public CapabilityProcessExecutor ageExecutor() {
        return age;
    }

    public CapabilityProcessExecutor blendExecutor() {
        return blend;
    }

    public CapabilityProcessExecutor bottleExecutor() {
        return bottle;
    }

    public Function<ResourceId, PropertyMerge> merges() {
        return PropertyMerges.from(beverages.api());
    }

    public Function<ResourceId, PropertyAggregator> aggregators() {
        return PropertyMerges.aggregators(beverages.api());
    }

    private static final class Holder {
        private static final ProcessRuntime INSTANCE = new ProcessRuntime(BeverageRuntime.shared());
    }
}

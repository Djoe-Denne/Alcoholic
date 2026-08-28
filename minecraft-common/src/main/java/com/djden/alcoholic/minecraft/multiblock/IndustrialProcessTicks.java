package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ItemOutput;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.process.BoilConfig;
import com.djden.alcoholic.application.process.ConditionConfig;
import com.djden.alcoholic.application.process.FermentConfig;
import com.djden.alcoholic.application.process.MaltConfig;
import com.djden.alcoholic.application.process.MashConfig;
import com.djden.alcoholic.application.process.MillConfig;
import com.djden.alcoholic.application.process.PressConfig;
import com.djden.alcoholic.application.process.ProcessRecipeResolver;
import com.djden.alcoholic.domain.ingredient.IngredientLot;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;
import com.djden.alcoholic.domain.multiblock.MultiblockDefinition;
import com.djden.alcoholic.domain.process.MaltExecutionStage;
import com.djden.alcoholic.domain.process.ThermalStability;
import com.djden.alcoholic.domain.vessel.EnvironmentProfile;
import com.djden.alcoholic.minecraft.advancement.AdvancementHooks;
import com.djden.alcoholic.minecraft.process.ItemLots;
import com.djden.alcoholic.minecraft.process.MinecraftSelectorMatcher;
import com.djden.alcoholic.minecraft.process.ProcessRuntime;
import com.djden.alcoholic.minecraft.process.SolidPropertyNbt;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generic industrial ticks keyed by process type. No drink-family branches.
 */
final class IndustrialProcessTicks {
    private static final ResourceId DEFAULT_MALT = ResourceId.parse("alcoholic:malt_pale");
    private static final ResourceId ADDITION_ROLE = ResourceId.parse("alcoholic:addition_role");
    private static final ResourceId ADDITION_PROGRESS = ResourceId.parse("alcoholic:addition_progress");

    private IndustrialProcessTicks() {
    }

    static void press(MultiblockControllerBlockEntity machine, MultiblockDefinition definition, long now) {
        MechanicalDriveState drive = machine.drive(definition);
        machine.noteRpm(drive.speed());
        if (!definition.kinetic().satisfied(drive)) {
            machine.stopPressProcess();
            return;
        }
        ItemStack input = machine.inventory().get(MultiblockControllerBlockEntity.INPUT_SLOT);
        if (input.isEmpty()) {
            machine.stopPressProcess();
            return;
        }
        ProcessRuntime runtime = ProcessRuntime.shared();
        Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                runtime.beverages().catalog(),
                runtime.beverages().api(),
                BuiltinRegistrations.PRESS,
                MinecraftSelectorMatcher.create(runtime.beverages()),
                Optional.of(ItemLots.id(input)),
                Optional.empty(),
                Optional.ofNullable(machine.boundDefinition())
        );
        if (invocation.isEmpty()) {
            machine.stopPressProcess();
            return;
        }
        PressConfig config = PressConfig.CODEC.decode(invocation.get().config());
        if (!config.executable() || input.getCount() < config.inputAmount()) {
            machine.stopPressProcess();
            return;
        }
        int duration = Math.max(
                1,
                (int) Math.round(config.processingTicks() / definition.modifiers().speedModifier())
        );
        machine.preparePressProcess(duration);
        if (!machine.pressProcessComplete()) {
            machine.consumeWork(definition);
            if (!machine.advancePressProcess()) {
                machine.markProcessDirty(machine.shouldSyncPressProcess());
                return;
            }
        }
        int units = Math.min(
                input.getCount() / config.inputAmount(),
                definition.modifiers().maxBatchUnits()
        );
        IngredientLot lot = ItemLots.lot(machine.copyOf(input, units * config.inputAmount()));
        ProcessResult result = runtime.engine().execute(
                IndustrialRuntime.shared().executor(BuiltinRegistrations.PRESS),
                invocation.get(),
                ProcessInputs.ofSolids("source", List.of(lot)),
                ProcessContext.of(
                        20.0,
                        1.0,
                        false,
                        Optional.empty(),
                        Optional.empty(),
                        now,
                        definition.modifiers()
                )
        );
        if (!result.success() || result.outputs().isEmpty()) {
            machine.stopPressProcess();
            return;
        }
        LiquidBatch produced = (LiquidBatch) result.outputs().get(0);
        if (machine.tank().fill(produced, true) < produced.volumeMillibuckets()) {
            machine.holdPressProcess();
            return;
        }
        if (!result.items().isEmpty()) {
            ItemOutput byproduct = result.items().get(0);
            ItemStack created = machine.itemStack(byproduct.item(), byproduct.amount());
            ItemStack existing = machine.inventory().get(MultiblockControllerBlockEntity.OUTPUT_SLOT);
            if (created.isEmpty()) {
                machine.stopPressProcess();
                return;
            }
            if (!existing.isEmpty() && (!ItemStack.isSameItemSameTags(existing, created)
                    || existing.getCount() + created.getCount() > machine.getMaxStackSize())) {
                machine.holdPressProcess();
                return;
            }
            if (existing.isEmpty()) {
                machine.inventory().set(MultiblockControllerBlockEntity.OUTPUT_SLOT, created);
            } else {
                existing.grow(created.getCount());
            }
        }
        machine.tank().fill(produced, false);
        input.shrink(units * config.inputAmount());
        machine.completePressProcess();
        AdvancementHooks.processCompleted(machine, BuiltinRegistrations.PRESS, produced.baseLiquid());
    }

    static void ferment(MultiblockControllerBlockEntity machine, MultiblockDefinition definition, long now) {
        double delta = machine.consumeElapsed(now);
        if (delta <= 0.0) {
            return;
        }
        Optional<LiquidBatch> contents = machine.tank().contents();
        if (contents.isEmpty() || contents.get().baseLiquid().isEmpty()) {
            machine.catalystConsumed(false);
            return;
        }
        LiquidBatch batch = contents.get();
        ProcessRuntime runtime = ProcessRuntime.shared();
        var matcher = MinecraftSelectorMatcher.create(runtime.beverages());
        Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                runtime.beverages().catalog(),
                runtime.beverages().api(),
                BuiltinRegistrations.FERMENT,
                matcher,
                Optional.empty(),
                batch.baseLiquid(),
                Optional.ofNullable(machine.boundDefinition())
        );
        if (invocation.isEmpty()) {
            return;
        }
        FermentConfig config = FermentConfig.CODEC.decode(invocation.get().config());
        double effective = ThermalStability.effectiveCelsius(
                machine.ambient(),
                config.temperature().preferredMidpoint(),
                definition.modifiers().thermalStability()
        );
        if (config.temperature().rateFactor(effective) <= 0.0) {
            return;
        }
        if (config.requireYeast() && !machine.catalystConsumed()) {
            ItemStack yeast = machine.inventory().get(MultiblockControllerBlockEntity.INPUT_SLOT);
            boolean matches = !yeast.isEmpty() && config.yeast()
                    .map(selector -> matcher.matches(selector, ItemLots.id(yeast)))
                    .orElseGet(() -> machine.matchesYeast(yeast));
            if (!matches) {
                return;
            }
            yeast.shrink(1);
            machine.catalystConsumed(true);
        }
        ProcessResult result = runtime.engine().execute(
                IndustrialRuntime.shared().executor(BuiltinRegistrations.FERMENT),
                invocation.get(),
                ProcessInputs.ofLiquid("input", batch),
                ProcessContext.of(
                        effective,
                        delta,
                        machine.catalystConsumed() || !config.requireYeast(),
                        Optional.empty(),
                        Optional.empty(),
                        now,
                        definition.modifiers()
                )
        );
        if (!result.success() || result.outputs().isEmpty()) {
            return;
        }
        LiquidBatch next = (LiquidBatch) result.outputs().get(0);
        double consumedSugar = Math.max(
                0.0,
                batch.number(config.sugarProperty(), 0.0) - next.number(config.sugarProperty(), 0.0)
        );
        machine.ventCo2(consumedSugar * config.kinetics().co2PerSugar());
        AdvancementHooks.changedIdentity(batch, next)
                .ifPresent(liquid -> AdvancementHooks.processCompleted(
                        machine,
                        BuiltinRegistrations.FERMENT,
                        Optional.of(liquid)
                ));
        machine.tank().set(next);
        machine.onProcessTankChanged();
    }

    static void malt(MultiblockControllerBlockEntity machine, MultiblockDefinition definition, long now) {
        ItemStack input = machine.inventory().get(MultiblockControllerBlockEntity.INPUT_SLOT);
        if (input.isEmpty()) {
            machine.resetProcess();
            return;
        }
        ProcessRuntime runtime = ProcessRuntime.shared();
        ResourceId selected = machine.selectedDefinition(DEFAULT_MALT);
        Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                runtime.beverages().catalog(),
                runtime.beverages().api(),
                BuiltinRegistrations.MALT,
                MinecraftSelectorMatcher.create(runtime.beverages()),
                Optional.of(ItemLots.id(input)),
                Optional.empty(),
                Optional.of(selected)
        );
        if (invocation.isEmpty()) {
            machine.resetProcess();
            return;
        }
        MaltConfig config = MaltConfig.CODEC.decode(invocation.get().config());
        if (!config.executable() || input.getCount() < config.inputAmount()) {
            machine.resetProcess();
            return;
        }
        EnvironmentProfile environment = enclosedEnvironment(machine);
        double humidity = environment.humidity();
        EnvironmentProfile processEnvironment = new EnvironmentProfile(
                environment.temperature(),
                environment.stability(),
                true,
                Math.max(humidity, config.moistureRequirement())
        );
        double effective = ThermalStability.effectiveCelsius(
                environment.temperature(),
                config.temperature().preferredMidpoint(),
                definition.modifiers().thermalStability()
        );
        double rate = config.temperature().rateFactor(effective);
        if (rate <= 0.0) {
            machine.pauseElapsed(now);
            machine.markProcessDirty(false);
            return;
        }
        int duration = Math.max(1, (int) Math.round(config.processingTicks() / definition.modifiers().speedModifier()));
        machine.beginProcessJob(invocation.get().nodeId() + "|" + ItemLots.id(input), duration);
        double fraction = machine.processDuration() <= 1
                ? 1.0
                : Math.min(1.0, (double) machine.processProgress() / machine.processDuration());
        MaltExecutionStage stage = MaltExecutionStage.at(fraction);
        machine.setProcessStage(stage.name().toLowerCase());
        if (stage.requiresMoisture() && humidity + 1e-9 < config.moistureRequirement()) {
            machine.pauseElapsed(now);
            machine.markProcessDirty(false);
            return;
        }
        if (stage.requiresKilnHeat() && machine.heatCelsius() < 40.0) {
            machine.pauseElapsed(now);
            machine.markProcessDirty(false);
            return;
        }
        if (!machine.advanceElapsed(now, rate)) {
            machine.markProcessDirty(false);
            return;
        }
        int units = units(input, config.inputAmount(), definition);
        IngredientLot lot = ItemLots.lot(machine.copyOf(input, units * config.inputAmount()));
        ProcessResult result = runtime.engine().execute(
                runtime.maltExecutor(),
                invocation.get(),
                ProcessInputs.ofSolids("grain", List.of(lot)),
                ProcessContext.of(
                        effective,
                        1.0,
                        false,
                        Optional.empty(),
                        Optional.of(processEnvironment),
                        now,
                        definition.modifiers()
                )
        );
        if (!offerItems(machine, result, units * config.inputAmount(), true)) {
            return;
        }
        machine.completeProcess();
        AdvancementHooks.processCompleted(machine, BuiltinRegistrations.MALT, Optional.empty());
        machine.markProcessDirty(true);
    }

    static void mill(MultiblockControllerBlockEntity machine, MultiblockDefinition definition, long now) {
        MechanicalDriveState drive = machine.drive(definition);
        machine.noteRpm(drive.speed());
        if (!definition.kinetic().satisfied(drive)) {
            machine.resetProcess();
            return;
        }
        ItemStack input = machine.inventory().get(MultiblockControllerBlockEntity.INPUT_SLOT);
        if (input.isEmpty()) {
            machine.resetProcess();
            return;
        }
        ProcessRuntime runtime = ProcessRuntime.shared();
        Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                runtime.beverages().catalog(),
                runtime.beverages().api(),
                BuiltinRegistrations.MILL,
                MinecraftSelectorMatcher.create(runtime.beverages()),
                Optional.of(ItemLots.id(input)),
                Optional.empty(),
                Optional.ofNullable(machine.boundDefinition())
        );
        if (invocation.isEmpty()) {
            machine.resetProcess();
            return;
        }
        MillConfig config = MillConfig.CODEC.decode(invocation.get().config());
        if (!config.executable() || input.getCount() < config.inputAmount()) {
            machine.resetProcess();
            return;
        }
        int duration = Math.max(1, (int) Math.round(config.processingTicks() / definition.modifiers().speedModifier()));
        machine.beginProcessJob(invocation.get().nodeId() + "|" + ItemLots.id(input), duration);
        machine.setProcessStage("milling");
        if (!machine.processComplete()) {
            machine.consumeWork(definition);
            if (!machine.advanceTick()) {
                machine.markProcessDirty(false);
                return;
            }
        }
        int units = units(input, config.inputAmount(), definition);
        IngredientLot lot = ItemLots.lot(machine.copyOf(input, units * config.inputAmount()));
        ProcessResult result = runtime.engine().execute(
                runtime.millExecutor(),
                invocation.get(),
                ProcessInputs.ofSolids("malt", List.of(lot)),
                ProcessContext.of(20.0, 1.0, false, Optional.empty(), Optional.empty(), now, definition.modifiers())
        );
        if (!offerItems(machine, result, units * config.inputAmount(), true)) {
            return;
        }
        machine.completeProcess();
        AdvancementHooks.processCompleted(machine, BuiltinRegistrations.MILL, Optional.empty());
        machine.markProcessDirty(true);
    }

    static void mash(MultiblockControllerBlockEntity machine, MultiblockDefinition definition, long now) {
        ItemStack input = machine.inventory().get(MultiblockControllerBlockEntity.INPUT_SLOT);
        Optional<LiquidBatch> water = machine.tank().contents();
        if (input.isEmpty() || water.isEmpty()) {
            machine.resetProcess();
            return;
        }
        LiquidBatch liquid = water.get();
        ProcessRuntime runtime = ProcessRuntime.shared();
        Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                runtime.beverages().catalog(),
                runtime.beverages().api(),
                BuiltinRegistrations.MASH,
                MinecraftSelectorMatcher.create(runtime.beverages()),
                Optional.of(ItemLots.id(input)),
                liquid.baseLiquid(),
                Optional.ofNullable(machine.boundDefinition())
        );
        if (invocation.isEmpty()) {
            machine.resetProcess();
            return;
        }
        MashConfig config = MashConfig.CODEC.decode(invocation.get().config());
        if (!config.executable()
                || input.getCount() < config.inputAmount()
                || liquid.volume() + 1e-9 < config.inputLiquidVolume()) {
            machine.resetProcess();
            return;
        }
        double target = machine.resolvedTarget(config.temperature().preferredMidpoint());
        double effective = ThermalStability.effectiveCelsius(
                machine.heatCelsius(),
                target,
                definition.modifiers().thermalStability()
        );
        double rate = config.temperature().rateFactor(effective);
        if (rate <= 0.0) {
            machine.pauseElapsed(now);
            machine.markProcessDirty(false);
            return;
        }
        int duration = Math.max(1, (int) Math.round(config.processingTicks() / (rate * definition.modifiers().speedModifier())));
        machine.beginProcessJob(
                invocation.get().nodeId() + "|" + ItemLots.id(input) + "|" + liquid.baseLiquid().map(ResourceId::toString).orElse(""),
                duration
        );
        machine.setProcessStage("mash");
        if (!machine.advanceElapsed(now, 1.0)) {
            machine.markProcessDirty(false);
            return;
        }
        int units = Math.min(
                units(input, config.inputAmount(), definition),
                (int) Math.floor(liquid.volume() / config.inputLiquidVolume())
        );
        int consume = Math.max(1, (int) Math.round(config.inputLiquidVolume() * units));
        LiquidBatch extracted = machine.tank().drain(consume, true);
        IngredientLot lot = ItemLots.lot(machine.copyOf(input, units * config.inputAmount()));
        ProcessResult result = runtime.engine().execute(
                runtime.mashExecutor(),
                invocation.get(),
                ProcessInputs.of("grist", List.of(lot), "water", extracted),
                ProcessContext.of(effective, 1.0, false, Optional.empty(), Optional.empty(), now, definition.modifiers())
        );
        if (!result.success() || result.outputs().isEmpty()) {
            machine.resetProcess();
            machine.markProcessDirty(false);
            return;
        }
        LiquidBatch produced = (LiquidBatch) result.outputs().get(0);
        LiquidBatch removed = machine.tank().drain(consume, false);
        if (machine.tank().fill(produced, true) < produced.volumeMillibuckets()) {
            if (removed != null) {
                machine.tank().fill(removed, false);
            }
            return;
        }
        if (!offerItems(machine, result, 0, false)) {
            if (removed != null) {
                machine.tank().fill(removed, false);
            }
            return;
        }
        machine.tank().fill(produced, false);
        input.shrink(units * config.inputAmount());
        machine.completeProcess();
        AdvancementHooks.processCompleted(machine, BuiltinRegistrations.MASH, produced.baseLiquid());
        machine.onProcessTankChanged();
    }

    static void boil(MultiblockControllerBlockEntity machine, MultiblockDefinition definition, long now) {
        Optional<LiquidBatch> contents = machine.tank().contents();
        if (contents.isEmpty()) {
            machine.resetProcess();
            return;
        }
        LiquidBatch batch = contents.get();
        ProcessRuntime runtime = ProcessRuntime.shared();
        Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                runtime.beverages().catalog(),
                runtime.beverages().api(),
                BuiltinRegistrations.BOIL,
                MinecraftSelectorMatcher.create(runtime.beverages()),
                Optional.empty(),
                batch.baseLiquid(),
                Optional.ofNullable(machine.boundDefinition())
        );
        if (invocation.isEmpty()) {
            machine.resetProcess();
            return;
        }
        BoilConfig config = BoilConfig.CODEC.decode(invocation.get().config());
        if (!config.executable()) {
            machine.resetProcess();
            return;
        }
        double effective = ThermalStability.effectiveCelsius(
                machine.heatCelsius(),
                config.temperature().preferredMidpoint(),
                definition.modifiers().thermalStability()
        );
        if (config.temperature().rateFactor(effective) <= 0.0) {
            machine.pauseElapsed(now);
            return;
        }
        int duration = Math.max(1, (int) Math.round(config.processingTicks() / definition.modifiers().speedModifier()));
        machine.beginProcessJob(invocation.get().nodeId() + "|" + batch.baseLiquid().map(ResourceId::toString).orElse(""), duration);
        double progress = machine.processDuration() <= 1
                ? 1.0
                : Math.min(1.0, (double) machine.processProgress() / machine.processDuration());
        machine.setProcessStage("boil");
        if (!commitAdditions(machine, config, progress)) {
            machine.markProcessDirty(false);
            return;
        }
        if (!machine.advanceElapsed(now, config.temperature().rateFactor(effective))) {
            machine.markProcessDirty(false);
            return;
        }
        if (!commitAdditions(machine, config, 1.0)) {
            machine.markProcessDirty(false);
            return;
        }
        List<IngredientLot> hops = committedLots(machine);
        long committedItems = hops.stream().mapToLong(IngredientLot::count).sum();
        if (committedItems < config.requiredAdditionItems()) {
            return;
        }
        ProcessResult result = runtime.engine().execute(
                runtime.boilExecutor(),
                invocation.get(),
                hops.isEmpty()
                        ? ProcessInputs.ofLiquid("wort", batch)
                        : ProcessInputs.of("hops", hops, "wort", batch),
                ProcessContext.of(effective, 1.0, false, Optional.empty(), Optional.empty(), now, definition.modifiers())
        );
        if (!result.success() || result.outputs().isEmpty()) {
            machine.resetProcess();
            machine.markProcessDirty(false);
            return;
        }
        LiquidBatch boiled = (LiquidBatch) result.outputs().get(0);
        machine.tank().set(boiled);
        machine.completeProcess();
        AdvancementHooks.processCompleted(machine, BuiltinRegistrations.BOIL, boiled.baseLiquid());
        machine.onProcessTankChanged();
    }

    static void condition(MultiblockControllerBlockEntity machine, MultiblockDefinition definition, long now) {
        Optional<LiquidBatch> contents = machine.tank().contents();
        if (contents.isEmpty() || contents.get().baseLiquid().isEmpty()) {
            machine.resetProcess();
            machine.catalystConsumed(false);
            return;
        }
        LiquidBatch batch = contents.get();
        ProcessRuntime runtime = ProcessRuntime.shared();
        Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                runtime.beverages().catalog(),
                runtime.beverages().api(),
                BuiltinRegistrations.CONDITION,
                MinecraftSelectorMatcher.create(runtime.beverages()),
                Optional.empty(),
                batch.baseLiquid(),
                Optional.ofNullable(machine.boundDefinition())
        );
        if (invocation.isEmpty()) {
            machine.resetProcess();
            return;
        }
        ConditionConfig config = ConditionConfig.CODEC.decode(invocation.get().config());
        if (!config.executable()) {
            machine.resetProcess();
            return;
        }
        double effective = ThermalStability.effectiveCelsius(
                machine.ambient(),
                config.temperature().preferredMidpoint(),
                definition.modifiers().thermalStability()
        );
        if (config.temperature().rateFactor(effective) <= 0.0) {
            machine.pauseElapsed(now);
            return;
        }
        machine.beginProcessJob(
                invocation.get().nodeId() + "|" + batch.baseLiquid().map(ResourceId::toString).orElse(""),
                config.processingTicks()
        );
        double maturity = batch.number(config.maturityProperty(), 0.0);
        double completion = config.kinetics().completionThreshold();
        if (maturity + 1e-9 >= completion) {
            machine.setProcessStage("conditioned");
            machine.setProcessProgressFraction(1.0);
            machine.pauseElapsed(now);
            return;
        }
        ItemStack yeastStack = machine.inventory().get(MultiblockControllerBlockEntity.INPUT_SLOT);
        if (!machine.catalystConsumed() && !yeastStack.isEmpty() && machine.matchesYeast(yeastStack)) {
            yeastStack.shrink(1);
            machine.catalystConsumed(true);
        }
        machine.setProcessStage("condition");
        double delta = machine.consumeElapsed(now);
        if (delta <= 0.0) {
            return;
        }
        ProcessResult result = runtime.engine().execute(
                runtime.conditionExecutor(),
                invocation.get(),
                ProcessInputs.ofLiquid("input", batch),
                ProcessContext.of(
                        effective,
                        delta,
                        machine.catalystConsumed(),
                        Optional.empty(),
                        Optional.empty(),
                        now,
                        definition.modifiers()
                )
        );
        if (!result.success() || result.outputs().isEmpty()) {
            return;
        }
        LiquidBatch conditioned = (LiquidBatch) result.outputs().get(0);
        AdvancementHooks.changedIdentity(batch, conditioned)
                .ifPresent(liquid -> AdvancementHooks.processCompleted(
                        machine,
                        BuiltinRegistrations.CONDITION,
                        Optional.of(liquid)
                ));
        machine.tank().set(conditioned);
        machine.setProcessProgressFraction(
                conditioned.number(config.maturityProperty(), 0.0) / Math.max(1e-9, completion)
        );
        machine.onProcessTankChanged();
    }

    private static EnvironmentProfile enclosedEnvironment(MultiblockControllerBlockEntity machine) {
        EnvironmentProfile sampled = machine.environment();
        return new EnvironmentProfile(sampled.temperature(), sampled.stability(), true, sampled.humidity());
    }

    private static int units(ItemStack input, int amount, MultiblockDefinition definition) {
        return Math.min(input.getCount() / amount, definition.modifiers().maxBatchUnits());
    }

    private static boolean commitAdditions(
            MultiblockControllerBlockEntity machine,
            BoilConfig config,
            double progress
    ) {
        List<BoilConfig.BoilAddition> schedule = config.additions();
        if (schedule.isEmpty() && config.additionSelector().isPresent()) {
            schedule = List.of(new BoilConfig.BoilAddition(config.additionSelector().orElseThrow(), 0.0));
        }
        if (schedule.isEmpty()) {
            return true;
        }
        ItemStack input = machine.inventory().get(MultiblockControllerBlockEntity.INPUT_SLOT);
        while (machine.additionsCommitted() < schedule.size()) {
            BoilConfig.BoilAddition addition = schedule.get(machine.additionsCommitted());
            if (progress + 1e-9 < addition.atProgress() && machine.processProgress() < machine.processDuration()) {
                return true;
            }
            if (input.isEmpty() || input.getCount() < config.additionAmount()) {
                return machine.additionsCommitted() > 0 && machine.processProgress() < machine.processDuration();
            }
            if (!MinecraftSelectorMatcher.create(ProcessRuntime.shared().beverages())
                    .matches(addition.selector(), ItemLots.id(input))) {
                return machine.additionsCommitted() > 0 && machine.processProgress() < machine.processDuration();
            }
            ItemStack taken = input.split(config.additionAmount());
            SolidPropertyNbt.write(taken, Map.of(
                    ADDITION_ROLE, addition.role(),
                    ADDITION_PROGRESS, addition.atProgress()
            ));
            if (!storeCommitted(machine, taken)) {
                input.grow(taken.getCount());
                return false;
            }
            machine.additionsCommitted(machine.additionsCommitted() + 1);
        }
        return true;
    }

    private static boolean storeCommitted(MultiblockControllerBlockEntity machine, ItemStack stack) {
        for (ItemStack existing : machine.committedSolids()) {
            if (ItemStack.isSameItemSameTags(existing, stack)
                    && existing.getCount() + stack.getCount() <= existing.getMaxStackSize()) {
                existing.grow(stack.getCount());
                return true;
            }
        }
        machine.committedSolids().add(stack);
        return true;
    }

    private static List<IngredientLot> committedLots(MultiblockControllerBlockEntity machine) {
        List<IngredientLot> lots = new ArrayList<>();
        for (ItemStack stack : machine.committedSolids()) {
            if (!stack.isEmpty()) {
                lots.add(ItemLots.lot(stack));
            }
        }
        return lots;
    }

    private static boolean offerItems(
            MultiblockControllerBlockEntity machine,
            ProcessResult result,
            int consumeInput,
            boolean requireItem
    ) {
        if (!result.success()) {
            machine.resetProcess();
            machine.markProcessDirty(false);
            return false;
        }
        if (result.items().isEmpty()) {
            if (requireItem) {
                machine.resetProcess();
                return false;
            }
            if (consumeInput > 0) {
                machine.inventory().get(MultiblockControllerBlockEntity.INPUT_SLOT).shrink(consumeInput);
            }
            return true;
        }
        ItemOutput produced = result.items().get(0);
        ItemStack created = machine.itemStack(produced.item(), produced.amount());
        if (created.isEmpty()) {
            machine.resetProcess();
            return false;
        }
        SolidPropertyNbt.write(created, produced.properties());
        ItemStack existing = machine.inventory().get(MultiblockControllerBlockEntity.OUTPUT_SLOT);
        if (!existing.isEmpty() && (!ItemStack.isSameItemSameTags(existing, created)
                || existing.getCount() + created.getCount() > machine.getMaxStackSize())) {
            return false;
        }
        if (existing.isEmpty()) {
            machine.inventory().set(MultiblockControllerBlockEntity.OUTPUT_SLOT, created);
        } else {
            existing.grow(created.getCount());
        }
        if (consumeInput > 0) {
            machine.inventory().get(MultiblockControllerBlockEntity.INPUT_SLOT).shrink(consumeInput);
        }
        return true;
    }
}

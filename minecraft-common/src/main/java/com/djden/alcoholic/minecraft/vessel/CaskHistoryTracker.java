package com.djden.alcoholic.minecraft.vessel;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.process.AgingConfig;
import com.djden.alcoholic.application.process.ProcessRecipeResolver;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.domain.vessel.BarrelHistory;
import com.djden.alcoholic.domain.vessel.CaskImprint;
import com.djden.alcoholic.minecraft.process.MinecraftSelectorMatcher;
import com.djden.alcoholic.minecraft.process.ProcessRuntime;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Shared empty/swap imprint bookkeeping for the oak barrel and industrial AGE vessel.
 */
public final class CaskHistoryTracker {
    private BarrelHistory history = BarrelHistory.empty();
    private boolean occupied;
    private Optional<ResourceId> lastDefinition = Optional.empty();
    private PropertyBag lastImprint = PropertyBag.empty();
    private int lastFillPeakVolume;

    public BarrelHistory history() {
        return history;
    }

    /**
     * @return true when an emptying or definition swap was recorded
     */
    public boolean sync(
            Optional<LiquidBatch> contents,
            int capacity,
            Function<LiquidBatch, Set<ResourceId>> axes
    ) {
        Optional<ResourceId> currentDefinition = contents.flatMap(LiquidBatch::baseLiquid);
        boolean swapped = occupied
                && contents.isPresent()
                && lastDefinition.isPresent()
                && currentDefinition.isPresent()
                && !lastDefinition.get().equals(currentDefinition.get());
        if (occupied && (contents.isEmpty() || swapped)) {
            double weight = CaskImprint.volumeWeight(lastFillPeakVolume, capacity);
            history = history.recordEmptying(
                    lastDefinition.orElse(null),
                    CaskImprint.fade(lastImprint, weight)
            );
            lastImprint = PropertyBag.empty();
            lastFillPeakVolume = 0;
            occupied = false;
            if (contents.isEmpty()) {
                return true;
            }
        }
        if (contents.isPresent()) {
            LiquidBatch batch = contents.get();
            occupied = true;
            lastDefinition = currentDefinition;
            lastImprint = CaskImprint.snapshot(batch.properties(), axes.apply(batch));
            lastFillPeakVolume = Math.max(lastFillPeakVolume, batch.volumeMillibuckets());
        }
        return occupied && (contents.isEmpty() || swapped);
    }

    public void save(CompoundTag tag) {
        tag.put("BarrelHistory", BarrelHistoryNbt.toTag(history));
        lastDefinition.ifPresent(id -> tag.putString("LastDefinition", id.toString()));
        CompoundTag cachedImprint = BarrelHistoryNbt.imprintTag(lastImprint);
        if (!cachedImprint.isEmpty()) {
            tag.put("LastImprint", cachedImprint);
        }
        if (lastFillPeakVolume > 0) {
            tag.putInt("LastFillPeakVolume", lastFillPeakVolume);
        }
        tag.putBoolean("Occupied", occupied);
    }

    public void load(
            CompoundTag tag,
            Optional<LiquidBatch> contents,
            Function<LiquidBatch, Set<ResourceId>> axes
    ) {
        history = BarrelHistoryNbt.fromTag(tag.getCompound("BarrelHistory"));
        lastDefinition = tag.contains("LastDefinition")
                ? Optional.of(ResourceId.parse(tag.getString("LastDefinition")))
                : contents.flatMap(LiquidBatch::baseLiquid);
        lastImprint = BarrelHistoryNbt.imprintFromTag(
                tag.contains("LastImprint") ? tag.getCompound("LastImprint") : null
        );
        if (lastImprint.asMap().isEmpty()) {
            lastImprint = contents
                    .map(batch -> CaskImprint.snapshot(batch.properties(), axes.apply(batch)))
                    .orElseGet(PropertyBag::empty);
        }
        lastFillPeakVolume = tag.contains("LastFillPeakVolume")
                ? tag.getInt("LastFillPeakVolume")
                : contents.map(LiquidBatch::volumeMillibuckets).orElse(0);
        occupied = tag.getBoolean("Occupied") || contents.isPresent();
    }

    public static Set<ResourceId> axesFor(LiquidBatch batch) {
        try {
            ProcessRuntime runtime = ProcessRuntime.shared();
            Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                    runtime.beverages().catalog(),
                    runtime.beverages().api(),
                    BuiltinRegistrations.AGE,
                    MinecraftSelectorMatcher.create(runtime.beverages()),
                    Optional.empty(),
                    batch.baseLiquid()
            );
            if (invocation.isEmpty()) {
                return CaskImprint.defaultProperties();
            }
            return AgingConfig.CODEC.decode(invocation.get().config(), "age").imprintProperties();
        } catch (RuntimeException ignored) {
            return CaskImprint.defaultProperties();
        }
    }
}

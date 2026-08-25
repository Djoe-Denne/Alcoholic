package com.djden.alcoholic.minecraft.bottle;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.BatchProvenance;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Consumer-facing snapshot. Runtime process fields are omitted.
 */
public final class BottleSnapshotNbt {
    public static final String ROOT = "AlcoholicBottle";
    public static final int VERSION = 1;
    public static final int BOTTLE_VOLUME = 250;

    private BottleSnapshotNbt() {
    }

    public static CompoundTag fromBatch(LiquidBatch batch) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Version", VERSION);
        batch.baseLiquid().ifPresent(id -> tag.putString("Definition", id.toString()));
        tag.putInt("Ethanol", quantize(batch.number(ResourceId.parse("alcoholic:ethanol"), 0.0)));
        tag.putInt("Sugar", quantize(batch.number(ResourceId.parse("alcoholic:sugar"), 0.0)));
        tag.putInt("Acidity", quantize(batch.number(ResourceId.parse("alcoholic:acidity"), 0.0)));
        tag.putInt("Maturity", quantize(batch.number(ResourceId.parse("alcoholic:maturity"), 0.0)));
        tag.putInt("Quality", quantize(batch.number(ResourceId.parse("alcoholic:quality"), 0.0)));
        CompoundTag origins = new CompoundTag();
        batch.batchProvenance().originComposition()
                .forEach((id, fraction) -> origins.putInt(id.toString(), quantize(fraction)));
        tag.put("Origins", origins);
        return tag;
    }

    public static Optional<CompoundTag> read(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(ROOT, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        CompoundTag snapshot = tag.getCompound(ROOT);
        return snapshot.getInt("Version") == VERSION ? Optional.of(snapshot) : Optional.empty();
    }

    public static void write(ItemStack stack, LiquidBatch batch) {
        stack.getOrCreateTag().put(ROOT, fromBatch(batch));
    }

    public static Optional<ResourceId> definition(CompoundTag snapshot) {
        return snapshot.contains("Definition", Tag.TAG_STRING)
                ? Optional.of(ResourceId.parse(snapshot.getString("Definition")))
                : Optional.empty();
    }

    public static double number(CompoundTag snapshot, String key) {
        return snapshot.getInt(key) / 1000.0;
    }

    public static Map<ResourceId, Double> origins(CompoundTag snapshot) {
        Map<ResourceId, Double> values = new LinkedHashMap<>();
        CompoundTag origins = snapshot.getCompound("Origins");
        for (String key : origins.getAllKeys()) {
            values.put(ResourceId.parse(key), origins.getInt(key) / 1000.0);
        }
        return values;
    }

    public static BatchProvenance provenance(CompoundTag snapshot) {
        return new BatchProvenance(origins(snapshot), Map.of(), 0.0, 0.0, 0.0, 0.0);
    }

    private static int quantize(double value) {
        return (int) Math.round(value * 1000.0);
    }
}

package com.djden.alcoholic.minecraft.viticulture;

import com.djden.alcoholic.api.ResourceId;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

/**
 * Versioned harvest metadata that is independent of the concrete grape item.
 */
public final class HarvestLotNbt {
    public static final String ROOT_TAG = "AlcoholicHarvestLot";
    public static final int VERSION = 2;
    public static final int QUANTIZATION_SCALE = 1_000;
    private static final int LEGACY_DOUBLE_VERSION = 1;

    private HarvestLotNbt() {
    }

    public static void write(
            ItemStack stack,
            ResourceId variety,
            double quality,
            double sugar,
            double acidity
    ) {
        Objects.requireNonNull(stack, "stack");
        HarvestLot lot = new HarvestLot(variety, quality, sugar, acidity);
        stack.getOrCreateTag().put(ROOT_TAG, toTag(lot));
    }

    public static CompoundTag toTag(HarvestLot lot) {
        Objects.requireNonNull(lot, "lot");
        CompoundTag data = new CompoundTag();
        data.putInt("Version", VERSION);
        data.putString("Variety", lot.variety().toString());
        data.putInt("Quality", quantize(lot.quality()));
        data.putInt("Sugar", quantize(lot.sugar()));
        data.putInt("Acidity", quantize(lot.acidity()));
        return data;
    }

    public static Optional<HarvestLot> read(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        return fromTag(root.getCompound(ROOT_TAG));
    }

    public static Optional<HarvestLot> fromTag(CompoundTag data) {
        Objects.requireNonNull(data, "data");
        int version = data.getInt("Version");
        if (version != VERSION && version != LEGACY_DOUBLE_VERSION) {
            return Optional.empty();
        }
        try {
            double quality = version == VERSION
                    ? dequantize(requiredQuantized(data, "Quality"))
                    : data.getDouble("Quality");
            double sugar = version == VERSION
                    ? dequantize(requiredQuantized(data, "Sugar"))
                    : data.getDouble("Sugar");
            double acidity = version == VERSION
                    ? dequantize(requiredQuantized(data, "Acidity"))
                    : data.getDouble("Acidity");
            return Optional.of(new HarvestLot(
                    ResourceId.parse(data.getString("Variety")),
                    quality,
                    sugar,
                    acidity
            ));
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return Optional.empty();
        }
    }

    private static int quantize(double value) {
        return Math.max(
                0,
                Math.min(QUANTIZATION_SCALE, (int) Math.round(value * QUANTIZATION_SCALE))
        );
    }

    private static double dequantize(int value) {
        if (value < 0 || value > QUANTIZATION_SCALE) {
            throw new IllegalArgumentException("quantized harvest value is out of range");
        }
        return value / (double) QUANTIZATION_SCALE;
    }

    private static int requiredQuantized(CompoundTag data, String name) {
        if (!data.contains(name, Tag.TAG_INT)) {
            throw new IllegalArgumentException(name + " must be an integer");
        }
        return data.getInt(name);
    }

    public record HarvestLot(
            ResourceId variety,
            double quality,
            double sugar,
            double acidity
    ) {
        public HarvestLot {
            Objects.requireNonNull(variety, "variety");
            requireUnit(quality, "quality");
            requireUnit(sugar, "sugar");
            requireUnit(acidity, "acidity");
        }

        private static void requireUnit(double value, String name) {
            if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
                throw new IllegalArgumentException(name + " must be between 0 and 1");
            }
        }
    }
}

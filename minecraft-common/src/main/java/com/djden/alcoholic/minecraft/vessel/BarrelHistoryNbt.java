package com.djden.alcoholic.minecraft.vessel;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.domain.vessel.BarrelHistory;
import com.djden.alcoholic.domain.vessel.CaskImprint;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BarrelHistoryNbt {
    public static final int SCALE = 1_000;

    private BarrelHistoryNbt() {
    }

    public static CompoundTag toTag(BarrelHistory history) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("UsageCount", history.usageCount());
        ListTag previous = new ListTag();
        history.previousContents().forEach(id -> previous.add(StringTag.valueOf(id.toString())));
        tag.put("PreviousContents", previous);
        history.toastLevel().ifPresent(value -> tag.putInt("ToastLevel", value));
        history.charLevel().ifPresent(value -> tag.putInt("CharLevel", value));
        history.woodExtractionRemaining().ifPresent(value -> tag.putDouble("WoodExtractionRemaining", value));
        CompoundTag imprint = imprintTag(history.imprint());
        if (!imprint.isEmpty()) {
            tag.put("CaskImprint", imprint);
        }
        return tag;
    }

    public static BarrelHistory fromTag(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return BarrelHistory.empty();
        }
        List<ResourceId> previous = new ArrayList<>();
        ListTag list = tag.getList("PreviousContents", Tag.TAG_STRING);
        for (int index = 0; index < list.size(); index++) {
            previous.add(ResourceId.parse(list.getString(index)));
        }
        return new BarrelHistory(
                tag.getInt("UsageCount"),
                previous,
                tag.contains("ToastLevel") ? Optional.of(tag.getInt("ToastLevel")) : Optional.empty(),
                tag.contains("CharLevel") ? Optional.of(tag.getInt("CharLevel")) : Optional.empty(),
                tag.contains("WoodExtractionRemaining")
                        ? Optional.of(tag.getDouble("WoodExtractionRemaining"))
                        : Optional.empty(),
                imprintFromTag(tag.contains("CaskImprint", Tag.TAG_COMPOUND) ? tag.getCompound("CaskImprint") : null)
        );
    }

    public static CompoundTag imprintTag(PropertyBag imprint) {
        CompoundTag tag = new CompoundTag();
        CaskImprint.toMap(imprint).forEach((id, amount) -> tag.putInt(id.toString(), quantize(amount)));
        return tag;
    }

    public static PropertyBag imprintFromTag(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return PropertyBag.empty();
        }
        Map<ResourceId, Double> values = new LinkedHashMap<>();
        for (String key : tag.getAllKeys()) {
            if (tag.contains(key, Tag.TAG_INT)) {
                double amount = dequantize(tag.getInt(key));
                if (amount > 0.0) {
                    values.put(ResourceId.parse(key), amount);
                }
            }
        }
        return CaskImprint.fromMap(values);
    }

    private static int quantize(double value) {
        return (int) Math.round(value * SCALE);
    }

    private static double dequantize(int value) {
        return value / (double) SCALE;
    }
}

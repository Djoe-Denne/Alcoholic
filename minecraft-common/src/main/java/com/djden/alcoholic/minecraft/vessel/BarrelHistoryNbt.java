package com.djden.alcoholic.minecraft.vessel;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.vessel.BarrelHistory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class BarrelHistoryNbt {
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
                        : Optional.empty()
        );
    }
}

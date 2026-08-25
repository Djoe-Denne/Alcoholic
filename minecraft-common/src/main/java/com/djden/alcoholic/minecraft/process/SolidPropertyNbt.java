package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Generic solid property bag on an item stack. Not an ingredient identity.
 */
public final class SolidPropertyNbt {
    public static final String ROOT_TAG = "AlcoholicSolidProperties";

    private SolidPropertyNbt() {
    }

    public static void write(ItemStack stack, Map<ResourceId, Object> properties) {
        Objects.requireNonNull(stack, "stack");
        if (properties == null || properties.isEmpty()) {
            return;
        }
        CompoundTag data = new CompoundTag();
        properties.forEach((id, value) -> {
            if (value instanceof Number number) {
                data.putDouble(id.toString(), number.doubleValue());
            } else if (value != null) {
                data.putString(id.toString(), String.valueOf(value));
            }
        });
        stack.getOrCreateTag().put(ROOT_TAG, data);
    }

    public static Optional<PropertyBag> read(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        CompoundTag data = root.getCompound(ROOT_TAG);
        Map<ResourceId, Object> values = new LinkedHashMap<>();
        for (String key : data.getAllKeys()) {
            if (data.contains(key, Tag.TAG_DOUBLE) || data.contains(key, Tag.TAG_INT)
                    || data.contains(key, Tag.TAG_FLOAT)) {
                values.put(ResourceId.parse(key), data.getDouble(key));
            } else if (data.contains(key, Tag.TAG_STRING)) {
                values.put(ResourceId.parse(key), data.getString(key));
            }
        }
        return values.isEmpty() ? Optional.empty() : Optional.of(new PropertyBag(values));
    }
}

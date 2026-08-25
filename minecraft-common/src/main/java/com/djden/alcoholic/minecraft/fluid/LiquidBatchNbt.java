package com.djden.alcoholic.minecraft.fluid;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.beverage.BeverageIdentity;
import com.djden.alcoholic.domain.liquid.BatchProvenance;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Vanilla NBT codec for {@link LiquidBatch}. Forge FluidStack adapters wrap this tag.
 */
public final class LiquidBatchNbt {
    public static final String ROOT_TAG = "AlcoholicLiquid";
    public static final int VERSION = 2;
    public static final int SCALE = 1_000;
    private static final Logger LOGGER = LogUtils.getLogger();

    private LiquidBatchNbt() {
    }

    public static CompoundTag toTag(LiquidBatch batch) {
        Objects.requireNonNull(batch, "batch");
        CompoundTag data = new CompoundTag();
        data.putInt("Version", VERSION);
        batch.baseLiquid().ifPresent(id -> data.putString("Definition", id.toString()));
        batch.identity().ifPresent(identity -> data.putString("Identity", identity.definitionId().toString()));
        data.putDouble("Volume", batch.volume());
        CompoundTag properties = new CompoundTag();
        batch.properties().asMap().forEach((id, value) -> writeProperty(properties, id, value));
        data.put("Properties", properties);
        data.put("Provenance", provenanceTag(batch.batchProvenance()));
        return data;
    }

    public static Optional<LiquidBatch> fromTag(CompoundTag data) {
        Objects.requireNonNull(data, "data");
        int version = data.getInt("Version");
        if (version != 1 && version != VERSION) {
            LOGGER.warn("Ignoring liquid batch NBT with unknown version {}", version);
            return Optional.empty();
        }
        try {
            Optional<ResourceId> definition = data.contains("Definition", Tag.TAG_STRING)
                    ? Optional.of(ResourceId.parse(data.getString("Definition")))
                    : Optional.empty();
            Optional<BeverageIdentity> identity = data.contains("Identity", Tag.TAG_STRING)
                    ? Optional.of(new BeverageIdentity(ResourceId.parse(data.getString("Identity"))))
                    : Optional.empty();
            double volume = data.getDouble("Volume");
            Map<ResourceId, Object> properties = new LinkedHashMap<>();
            CompoundTag stored = data.getCompound("Properties");
            for (String key : stored.getAllKeys()) {
                readProperty(stored, key).ifPresent(value -> properties.put(ResourceId.parse(key), value));
            }
            BatchProvenance provenance = version == 1
                    ? BatchProvenance.empty()
                    : readProvenance(data.getCompound("Provenance"));
            return Optional.of(new LiquidBatch(
                    identity,
                    definition,
                    volume,
                    new PropertyBag(properties),
                    provenance
            ));
        } catch (RuntimeException exception) {
            LOGGER.warn("Ignoring malformed liquid batch NBT", exception);
            return Optional.empty();
        }
    }

    public static Optional<LiquidBatch> readRoot(CompoundTag tag) {
        if (tag == null || !tag.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        return fromTag(tag.getCompound(ROOT_TAG));
    }

    public static void writeRoot(CompoundTag tag, LiquidBatch batch) {
        tag.put(ROOT_TAG, toTag(batch));
    }

    public static boolean hasVersionTag(CompoundTag tag) {
        return tag != null && tag.contains("Version", Tag.TAG_INT);
    }

    private static CompoundTag provenanceTag(BatchProvenance provenance) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Schema", BatchProvenance.SCHEMA_VERSION);
        tag.put("Origins", fractionTag(provenance.originComposition()));
        tag.put("Blends", fractionTag(provenance.blendComposition()));
        tag.putInt("FermentationStress", quantize(provenance.fermentationStress()));
        tag.putInt("TotalAgingTime", quantize(provenance.totalAgingTime()));
        tag.putInt("WoodExposure", quantize(provenance.woodExposure()));
        tag.putInt("OxidationExposure", quantize(provenance.oxidationExposure()));
        return tag;
    }

    private static BatchProvenance readProvenance(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return BatchProvenance.empty();
        }
        return new BatchProvenance(
                readFractions(tag.getCompound("Origins")),
                readFractions(tag.getCompound("Blends")),
                dequantize(tag.getInt("FermentationStress")),
                dequantize(tag.getInt("TotalAgingTime")),
                dequantize(tag.getInt("WoodExposure")),
                dequantize(tag.getInt("OxidationExposure"))
        );
    }

    private static CompoundTag fractionTag(Map<ResourceId, Double> values) {
        CompoundTag tag = new CompoundTag();
        values.forEach((id, fraction) -> tag.putInt(id.toString(), quantize(fraction)));
        return tag;
    }

    private static Map<ResourceId, Double> readFractions(CompoundTag tag) {
        Map<ResourceId, Double> values = new LinkedHashMap<>();
        for (String key : tag.getAllKeys()) {
            values.put(ResourceId.parse(key), dequantize(tag.getInt(key)));
        }
        return values;
    }

    private static void writeProperty(CompoundTag properties, ResourceId id, Object value) {
        if (value instanceof Number number) {
            properties.putInt(id.toString(), quantize(number.doubleValue()));
            return;
        }
        properties.putString(id.toString(), String.valueOf(value));
    }

    private static Optional<Object> readProperty(CompoundTag properties, String key) {
        if (properties.contains(key, Tag.TAG_INT)) {
            return Optional.of(dequantize(properties.getInt(key)));
        }
        if (properties.contains(key, Tag.TAG_STRING)) {
            return Optional.of(properties.getString(key));
        }
        return Optional.empty();
    }

    private static int quantize(double value) {
        return (int) Math.round(value * SCALE);
    }

    private static double dequantize(int value) {
        return value / (double) SCALE;
    }
}

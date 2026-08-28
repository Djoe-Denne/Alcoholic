package com.djden.alcoholic.minecraft.advancement;

import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

import org.jetbrains.annotations.Nullable;

public final class CropHarvestedTrigger extends SimpleCriterionTrigger<CropHarvestedTrigger.TriggerInstance> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            AlcoholicIds.MOD_ID,
            "crop_harvested"
    );

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(
            JsonObject json,
            EntityPredicate.Composite player,
            DeserializationContext context
    ) {
        return new TriggerInstance(player, location(json, "crop"));
    }

    public void trigger(ServerPlayer player, ResourceLocation crop) {
        this.trigger(player, instance -> instance.matches(crop));
    }

    public static TriggerInstance harvested() {
        return harvested(null);
    }

    public static TriggerInstance harvested(@Nullable ResourceLocation crop) {
        return new TriggerInstance(EntityPredicate.Composite.ANY, crop);
    }

    @Nullable
    static ResourceLocation location(JsonObject json, String key) {
        if (!json.has(key)) {
            return null;
        }
        return ResourceLocation.tryParse(GsonHelper.getAsString(json, key));
    }

    public static final class TriggerInstance extends AbstractCriterionTriggerInstance {
        @Nullable
        private final ResourceLocation crop;

        public TriggerInstance(EntityPredicate.Composite player, @Nullable ResourceLocation crop) {
            super(ID, player);
            this.crop = crop;
        }

        public boolean matches(ResourceLocation harvested) {
            return crop == null || crop.equals(harvested);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            if (crop != null) {
                json.addProperty("crop", crop.toString());
            }
            return json;
        }
    }
}

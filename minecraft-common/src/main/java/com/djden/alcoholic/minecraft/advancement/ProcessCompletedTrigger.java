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
import org.jetbrains.annotations.Nullable;

public final class ProcessCompletedTrigger extends SimpleCriterionTrigger<ProcessCompletedTrigger.TriggerInstance> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            AlcoholicIds.MOD_ID,
            "process_completed"
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
        return new TriggerInstance(
                player,
                CropHarvestedTrigger.location(json, "process"),
                CropHarvestedTrigger.location(json, "liquid")
        );
    }

    public void trigger(
            ServerPlayer player,
            ResourceLocation process,
            @Nullable ResourceLocation liquid
    ) {
        this.trigger(player, instance -> instance.matches(process, liquid));
    }

    public static TriggerInstance completed(ResourceLocation process) {
        return completed(process, null);
    }

    public static TriggerInstance completed(ResourceLocation process, @Nullable ResourceLocation liquid) {
        return new TriggerInstance(EntityPredicate.Composite.ANY, process, liquid);
    }

    public static final class TriggerInstance extends AbstractCriterionTriggerInstance {
        @Nullable
        private final ResourceLocation process;
        @Nullable
        private final ResourceLocation liquid;

        public TriggerInstance(
                EntityPredicate.Composite player,
                @Nullable ResourceLocation process,
                @Nullable ResourceLocation liquid
        ) {
            super(ID, player);
            this.process = process;
            this.liquid = liquid;
        }

        public boolean matches(ResourceLocation completedProcess, @Nullable ResourceLocation completedLiquid) {
            return (process == null || process.equals(completedProcess))
                    && (liquid == null || liquid.equals(completedLiquid));
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            if (process != null) {
                json.addProperty("process", process.toString());
            }
            if (liquid != null) {
                json.addProperty("liquid", liquid.toString());
            }
            return json;
        }
    }
}

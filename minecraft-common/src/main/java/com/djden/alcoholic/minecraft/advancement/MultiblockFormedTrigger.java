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

public final class MultiblockFormedTrigger extends SimpleCriterionTrigger<MultiblockFormedTrigger.TriggerInstance> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            AlcoholicIds.MOD_ID,
            "multiblock_formed"
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
        return new TriggerInstance(player, CropHarvestedTrigger.location(json, "machine"));
    }

    public void trigger(ServerPlayer player, ResourceLocation machine) {
        this.trigger(player, instance -> instance.matches(machine));
    }

    public static TriggerInstance formed(ResourceLocation machine) {
        return new TriggerInstance(EntityPredicate.Composite.ANY, machine);
    }

    public static final class TriggerInstance extends AbstractCriterionTriggerInstance {
        @Nullable
        private final ResourceLocation machine;

        public TriggerInstance(EntityPredicate.Composite player, @Nullable ResourceLocation machine) {
            super(ID, player);
            this.machine = machine;
        }

        public boolean matches(ResourceLocation formedMachine) {
            return machine == null || machine.equals(formedMachine);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            if (machine != null) {
                json.addProperty("machine", machine.toString());
            }
            return json;
        }
    }
}

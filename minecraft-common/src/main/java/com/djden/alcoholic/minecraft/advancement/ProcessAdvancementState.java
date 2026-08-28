package com.djden.alcoholic.minecraft.advancement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProcessAdvancementState {
    private static final String LAST_ACTOR = "LastActor";
    private static final String PENDING = "Pending";
    private static final String PROCESS = "Process";
    private static final String LIQUID = "Liquid";

    @Nullable
    private UUID lastActor;
    private final List<PendingCriterion> pending = new ArrayList<>();

    public void touch(Player player) {
        lastActor = player.getUUID();
        flush(player);
    }

    public void assignActor(UUID actor) {
        lastActor = actor;
    }

    public void complete(Level level, ResourceLocation process, @Nullable ResourceLocation liquid) {
        ServerPlayer actor = resolve(level);
        if (actor != null) {
            AlcoholicCriteria.PROCESS_COMPLETED.trigger(actor, process, liquid);
            return;
        }
        pending.add(new PendingCriterion(process, liquid));
    }

    public boolean hasPending() {
        return !pending.isEmpty();
    }

    public void save(CompoundTag tag) {
        if (lastActor != null) {
            tag.putUUID(LAST_ACTOR, lastActor);
        }
        if (pending.isEmpty()) {
            return;
        }
        ListTag list = new ListTag();
        for (PendingCriterion criterion : pending) {
            CompoundTag entry = new CompoundTag();
            entry.putString(PROCESS, criterion.process().toString());
            if (criterion.liquid() != null) {
                entry.putString(LIQUID, criterion.liquid().toString());
            }
            list.add(entry);
        }
        tag.put(PENDING, list);
    }

    public void load(CompoundTag tag) {
        lastActor = tag.hasUUID(LAST_ACTOR) ? tag.getUUID(LAST_ACTOR) : null;
        pending.clear();
        if (!tag.contains(PENDING, Tag.TAG_LIST)) {
            return;
        }
        ListTag list = tag.getList(PENDING, Tag.TAG_COMPOUND);
        for (int index = 0; index < list.size(); index++) {
            CompoundTag entry = list.getCompound(index);
            ResourceLocation process = ResourceLocation.tryParse(entry.getString(PROCESS));
            if (process == null) {
                continue;
            }
            ResourceLocation liquid = entry.contains(LIQUID)
                    ? ResourceLocation.tryParse(entry.getString(LIQUID))
                    : null;
            pending.add(new PendingCriterion(process, liquid));
        }
    }

    private void flush(Player player) {
        if (!(player instanceof ServerPlayer server) || pending.isEmpty()) {
            return;
        }
        for (PendingCriterion criterion : List.copyOf(pending)) {
            AlcoholicCriteria.PROCESS_COMPLETED.trigger(server, criterion.process(), criterion.liquid());
        }
        pending.clear();
    }

    @Nullable
    private ServerPlayer resolve(Level level) {
        if (lastActor == null || !(level instanceof ServerLevel server)) {
            return null;
        }
        return server.getServer().getPlayerList().getPlayer(lastActor);
    }

    private record PendingCriterion(ResourceLocation process, @Nullable ResourceLocation liquid) {
    }
}

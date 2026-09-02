package com.djden.alcoholic.minecraft.advancement;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProcessAdvancementState {
    private static final String LAST_ACTOR = "LastActor";
    private static final String PENDING = "Pending";
    private static final String KIND = "Kind";
    private static final String PROCESS = "Process";
    private static final String MACHINE = "Machine";
    private static final String LIQUID = "Liquid";
    private static final String KIND_PROCESS = "process";
    private static final String KIND_FORMED = "formed";
    private static final int NEARBY_RANGE = 16;

    @Nullable
    private UUID lastActor;
    @Nullable
    private ServerPlayer lastActorPlayer;
    private final List<PendingCriterion> pending = new ArrayList<>();

    public void touch(Player player) {
        lastActor = player.getUUID();
        lastActorPlayer = player instanceof ServerPlayer server ? server : null;
        flush(player);
    }

    public void assignActor(UUID actor) {
        lastActor = actor;
        lastActorPlayer = null;
    }

    public void complete(Level level, ResourceLocation process, @Nullable ResourceLocation liquid) {
        ServerPlayer actor = resolve(level);
        if (actor != null) {
            AlcoholicCriteria.PROCESS_COMPLETED.trigger(actor, process, liquid);
            return;
        }
        pending.add(PendingCriterion.process(process, liquid));
    }

    public void formed(Level level, BlockPos position, ResourceLocation machine) {
        ServerPlayer actor = resolve(level);
        if (actor != null) {
            AlcoholicCriteria.MULTIBLOCK_FORMED.trigger(actor, machine);
            return;
        }
        if (lastActor != null) {
            pending.add(PendingCriterion.formed(machine));
            return;
        }
        List<ServerPlayer> nearby = nearbyPlayers(level, position);
        if (!nearby.isEmpty()) {
            for (ServerPlayer player : nearby) {
                AlcoholicCriteria.MULTIBLOCK_FORMED.trigger(player, machine);
            }
            return;
        }
        pending.add(PendingCriterion.formed(machine));
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
            entry.putString(KIND, criterion.kind());
            if (KIND_FORMED.equals(criterion.kind())) {
                entry.putString(MACHINE, criterion.id().toString());
            } else {
                entry.putString(PROCESS, criterion.id().toString());
            }
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
            String kind = entry.contains(KIND) ? entry.getString(KIND) : KIND_PROCESS;
            if (KIND_FORMED.equals(kind) || entry.contains(MACHINE)) {
                ResourceLocation machine = ResourceLocation.tryParse(
                        entry.contains(MACHINE) ? entry.getString(MACHINE) : entry.getString(PROCESS)
                );
                if (machine != null) {
                    pending.add(PendingCriterion.formed(machine));
                }
                continue;
            }
            ResourceLocation process = ResourceLocation.tryParse(entry.getString(PROCESS));
            if (process == null) {
                continue;
            }
            ResourceLocation liquid = entry.contains(LIQUID)
                    ? ResourceLocation.tryParse(entry.getString(LIQUID))
                    : null;
            pending.add(PendingCriterion.process(process, liquid));
        }
    }

    private void flush(Player player) {
        if (!(player instanceof ServerPlayer server) || pending.isEmpty()) {
            return;
        }
        for (PendingCriterion criterion : List.copyOf(pending)) {
            if (KIND_FORMED.equals(criterion.kind())) {
                AlcoholicCriteria.MULTIBLOCK_FORMED.trigger(server, criterion.id());
            } else {
                AlcoholicCriteria.PROCESS_COMPLETED.trigger(server, criterion.id(), criterion.liquid());
            }
        }
        pending.clear();
    }

    @Nullable
    private ServerPlayer resolve(Level level) {
        if (lastActor == null || !(level instanceof ServerLevel server)) {
            return null;
        }
        if (lastActorPlayer != null
                && lastActor.equals(lastActorPlayer.getUUID())
                && lastActorPlayer.level == server) {
            return lastActorPlayer;
        }
        ServerPlayer listed = server.getServer().getPlayerList().getPlayer(lastActor);
        if (listed != null) {
            return listed;
        }
        for (ServerPlayer player : server.players()) {
            if (lastActor.equals(player.getUUID())) {
                return player;
            }
        }
        return null;
    }

    private static List<ServerPlayer> nearbyPlayers(Level level, BlockPos position) {
        if (!(level instanceof ServerLevel server)) {
            return List.of();
        }
        AABB box = new AABB(position).inflate(NEARBY_RANGE);
        return server.getEntitiesOfClass(ServerPlayer.class, box, player -> !player.isSpectator());
    }

    private record PendingCriterion(String kind, ResourceLocation id, @Nullable ResourceLocation liquid) {
        static PendingCriterion process(ResourceLocation process, @Nullable ResourceLocation liquid) {
            return new PendingCriterion(KIND_PROCESS, process, liquid);
        }

        static PendingCriterion formed(ResourceLocation machine) {
            return new PendingCriterion(KIND_FORMED, machine, null);
        }
    }
}

package com.djden.alcoholic.integration.create.forge;

import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.minecraft.process.SolidPropertyNbt;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlockEntity;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.IItemHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * Copies Alcoholic solid properties from Create mill/crusher inputs onto
 * newly produced output. Create recipes themselves stay identifier-only.
 */
public final class CreateMillPropertyBridge {
    private final Map<BlockEntity, ItemStack> lastInputs = new WeakHashMap<>();

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != LogicalSide.SERVER) {
            return;
        }
        if (!(event.level instanceof ServerLevel level)) {
            return;
        }
        for (LevelChunk chunk : loadedChunks(level)) {
            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                if (blockEntity instanceof MillstoneBlockEntity millstone) {
                    transfer(millstone, millstone.inputInv, millstone.outputInv);
                } else if (blockEntity instanceof CrushingWheelControllerBlockEntity crusher) {
                    transfer(crusher, crusher.inventory, crusher.inventory);
                }
            }
        }
    }

    private void transfer(BlockEntity entity, IItemHandler inputInv, IItemHandler outputInv) {
        ItemStack input = firstNonEmpty(inputInv);
        ItemStack previous = lastInputs.get(entity);
        if (previous != null && !previous.isEmpty() && input.getCount() < previous.getCount()) {
            Optional<PropertyBag> properties = SolidPropertyNbt.read(previous);
            if (properties.isPresent()) {
                for (int slot = 0; slot < outputInv.getSlots(); slot++) {
                    ItemStack output = outputInv.getStackInSlot(slot);
                    if (output.isEmpty() || SolidPropertyNbt.read(output).isPresent()) {
                        continue;
                    }
                    SolidPropertyNbt.write(output, properties.get().asMap());
                }
            }
        }
        lastInputs.put(entity, input.copy());
    }

    private static ItemStack firstNonEmpty(IItemHandler inventory) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static List<LevelChunk> loadedChunks(ServerLevel level) {
        List<LevelChunk> chunks = chunksFromMap(level);
        return chunks.isEmpty() ? playerChunks(level) : chunks;
    }

    private static List<LevelChunk> chunksFromMap(ServerLevel level) {
        List<LevelChunk> chunks = new ArrayList<>();
        try {
            Method method = level.getChunkSource().chunkMap.getClass().getDeclaredMethod("getChunks");
            method.setAccessible(true);
            Object holders = method.invoke(level.getChunkSource().chunkMap);
            if (!(holders instanceof Iterable<?> iterable)) {
                return chunks;
            }
            for (Object holder : iterable) {
                if (holder instanceof ChunkHolder chunkHolder) {
                    LevelChunk chunk = chunkHolder.getTickingChunk();
                    if (chunk != null) {
                        chunks.add(chunk);
                    }
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Player-loaded chunks are the fallback.
        }
        return chunks;
    }

    private static List<LevelChunk> playerChunks(ServerLevel level) {
        List<LevelChunk> chunks = new ArrayList<>();
        int view = Math.max(2, level.getServer().getPlayerList().getViewDistance());
        for (var player : level.players()) {
            ChunkPos origin = player.chunkPosition();
            for (int x = origin.x - view; x <= origin.x + view; x++) {
                for (int z = origin.z - view; z <= origin.z + view; z++) {
                    LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                    if (chunk != null) {
                        chunks.add(chunk);
                    }
                }
            }
        }
        return chunks;
    }
}

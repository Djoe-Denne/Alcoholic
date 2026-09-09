package com.djden.alcoholic.minecraft.process;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A short-lived view of a rectangular set of stands. Physical blocks retain ownership of their
 * nine cells; this class only supplies the chest-like aggregated view and quick insertion order.
 */
public final class BottleStandNetwork {
    public static final int MAX_WIDTH = 3;
    public static final int MAX_HEIGHT = 2;

    private final List<BottleStandBlockEntity> stands;

    private BottleStandNetwork(List<BottleStandBlockEntity> stands) {
        this.stands = List.copyOf(stands);
    }

    public static BottleStandNetwork find(Level level, BlockPos origin, BottleStandBlock block) {
        BlockState originState = level.getBlockState(origin);
        Direction facing = originState.getValue(BottleStandBlock.FACING);
        Direction lateral = facing.getClockWise();
        Set<BlockPos> seen = new HashSet<>();
        Set<BlockPos> connected = new HashSet<>();
        ArrayDeque<BlockPos> frontier = new ArrayDeque<>();
        frontier.add(origin);

        while (!frontier.isEmpty() && connected.size() <= MAX_WIDTH * MAX_HEIGHT) {
            BlockPos candidate = frontier.removeFirst();
            if (!seen.add(candidate) || !matches(level, candidate, block, facing)) {
                continue;
            }
            connected.add(candidate);
            frontier.add(candidate.relative(lateral));
            frontier.add(candidate.relative(lateral.getOpposite()));
            frontier.add(candidate.above());
            frontier.add(candidate.below());
        }

        List<BlockPos> positions = new ArrayList<>(connected);
        if (!isFullRectangle(positions, origin, lateral)) {
            positions = new ArrayList<>();
            positions.add(origin);
        }
        positions.sort(Comparator
                .comparingInt((BlockPos position) -> -position.getY())
                .thenComparingInt(position -> lateralOffset(position, origin, lateral)));

        List<BottleStandBlockEntity> stands = new ArrayList<>(positions.size());
        for (BlockPos position : positions) {
            if (level.getBlockEntity(position) instanceof BottleStandBlockEntity stand) {
                stands.add(stand);
            }
        }
        return new BottleStandNetwork(stands);
    }

    private static boolean matches(Level level, BlockPos position, BottleStandBlock block, Direction facing) {
        BlockState state = level.getBlockState(position);
        return state.getBlock() == block && state.getValue(BottleStandBlock.FACING) == facing;
    }

    private static boolean isFullRectangle(List<BlockPos> positions, BlockPos origin, Direction lateral) {
        if (positions.isEmpty() || positions.size() > MAX_WIDTH * MAX_HEIGHT) {
            return false;
        }
        int minLateral = positions.stream().mapToInt(position -> lateralOffset(position, origin, lateral)).min().orElse(0);
        int maxLateral = positions.stream().mapToInt(position -> lateralOffset(position, origin, lateral)).max().orElse(0);
        int minY = positions.stream().mapToInt(BlockPos::getY).min().orElse(origin.getY());
        int maxY = positions.stream().mapToInt(BlockPos::getY).max().orElse(origin.getY());
        int width = maxLateral - minLateral + 1;
        int height = maxY - minY + 1;
        return width <= MAX_WIDTH && height <= MAX_HEIGHT && positions.size() == width * height;
    }

    private static int lateralOffset(BlockPos position, BlockPos origin, Direction lateral) {
        return (position.getX() - origin.getX()) * lateral.getStepX()
                + (position.getZ() - origin.getZ()) * lateral.getStepZ();
    }

    public int rows() {
        return stands.size();
    }

    public boolean insertOne(ItemStack stack) {
        for (BottleStandBlockEntity stand : stands) {
            if (stand.insertOne(stack)) {
                return true;
            }
        }
        return false;
    }

    public Container inventory() {
        return new Inventory(stands);
    }

    private static final class Inventory implements Container {
        private final List<BottleStandBlockEntity> stands;

        private Inventory(List<BottleStandBlockEntity> stands) {
            this.stands = stands;
        }

        private BottleStandBlockEntity stand(int slot) {
            return stands.get(slot / BottleStandBlockEntity.SLOT_COUNT);
        }

        private int localSlot(int slot) {
            return slot % BottleStandBlockEntity.SLOT_COUNT;
        }

        @Override
        public int getContainerSize() {
            return stands.size() * BottleStandBlockEntity.SLOT_COUNT;
        }

        @Override
        public boolean isEmpty() {
            return stands.stream().allMatch(BottleStandBlockEntity::isEmpty);
        }

        @Override
        public ItemStack getItem(int slot) {
            return stand(slot).getItem(localSlot(slot));
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return stand(slot).removeItem(localSlot(slot), amount);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return stand(slot).removeItemNoUpdate(localSlot(slot));
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            stand(slot).setItem(localSlot(slot), stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean stillValid(Player player) {
            return stands.stream().allMatch(stand -> stand.stillValid(player));
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return stand(slot).canPlaceItem(localSlot(slot), stack);
        }

        @Override
        public void setChanged() {
            // Each delegated stand persists and synchronizes its own mutation.
        }

        @Override
        public void clearContent() {
            stands.forEach(BottleStandBlockEntity::clearContent);
        }
    }
}

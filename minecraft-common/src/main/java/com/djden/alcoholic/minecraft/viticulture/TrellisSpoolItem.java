package com.djden.alcoholic.minecraft.viticulture;

import com.djden.alcoholic.minecraft.agriculture.TrellisWireBlock;
import com.djden.alcoholic.minecraft.agriculture.VineyardPostBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public final class TrellisSpoolItem extends Item {
    private static final String SELECTION_TAG = "AlcoholicTrellisSelection";

    private final Supplier<? extends Block> wire;
    private final ViticultureRuntime runtime;

    public TrellisSpoolItem(
            Properties properties,
            Supplier<? extends Block> wire,
            ViticultureRuntime runtime
    ) {
        super(properties);
        this.wire = Objects.requireNonNull(wire, "wire");
        this.runtime = Objects.requireNonNull(runtime, "runtime");
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        if (!(level.getBlockState(clicked).getBlock() instanceof VineyardPostBlock)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        String dimension = level.dimension().location().toString();
        Optional<Selection> existing = readSelection(stack);
        if (existing.isEmpty()) {
            writeSelection(stack, new Selection(dimension, clicked));
            message(player, "message.alcoholic.trellis_spool.first_post");
            return InteractionResult.CONSUME;
        }

        Selection selection = existing.get();
        if (!dimension.equals(selection.dimension())) {
            clearSelection(stack);
            message(player, "message.alcoholic.trellis_spool.wrong_dimension");
            return InteractionResult.FAIL;
        }
        if (!(level.getBlockState(selection.position()).getBlock()
                instanceof VineyardPostBlock)) {
            clearSelection(stack);
            message(player, "message.alcoholic.trellis_spool.missing_post");
            return InteractionResult.FAIL;
        }

        Connection connection = Connection.between(
                selection.position(),
                clicked,
                runtime.settings().maxWireDistance()
        );
        if (connection == null) {
            message(
                    player,
                    "message.alcoholic.trellis_spool.invalid_alignment"
            );
            return InteractionResult.FAIL;
        }
        if (!canPlace(level, player, connection)) {
            message(player, "message.alcoholic.trellis_spool.obstructed");
            return InteractionResult.FAIL;
        }

        int segments = connection.distance() - 1;
        if (player != null
                && !player.getAbilities().instabuild
                && stack.isDamageableItem()
                && stack.getMaxDamage() - stack.getDamageValue() < segments) {
            message(player, "message.alcoholic.trellis_spool.insufficient_durability");
            return InteractionResult.FAIL;
        }

        place(level, connection);
        clearSelection(stack);
        if (player != null && !player.getAbilities().instabuild) {
            stack.hurtAndBreak(
                    segments,
                    player,
                    brokenBy -> brokenBy.broadcastBreakEvent(context.getHand())
            );
        }
        level.playSound(
                null,
                clicked,
                SoundEvents.CHAIN_PLACE,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );
        message(player, "message.alcoholic.trellis_spool.placed", segments);
        return InteractionResult.CONSUME;
    }

    public static Optional<Selection> readSelection(ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(SELECTION_TAG, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        CompoundTag data = root.getCompound(SELECTION_TAG);
        String dimension = data.getString("Dimension");
        if (dimension.isBlank() || !data.contains("Position", Tag.TAG_LONG)) {
            return Optional.empty();
        }
        return Optional.of(new Selection(dimension, BlockPos.of(data.getLong("Position"))));
    }

    public static void clearSelection(ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root != null) {
            root.remove(SELECTION_TAG);
        }
    }

    private static void writeSelection(ItemStack stack, Selection selection) {
        CompoundTag data = new CompoundTag();
        data.putString("Dimension", selection.dimension());
        data.putLong("Position", selection.position().asLong());
        stack.getOrCreateTag().put(SELECTION_TAG, data);
    }

    private boolean canPlace(Level level, Player player, Connection connection) {
        Block wireBlock = wire.get();
        for (int offset = 1; offset < connection.distance(); offset++) {
            BlockPos position = connection.start().relative(connection.direction(), offset);
            if (player != null && !level.mayInteract(player, position)) {
                return false;
            }
            BlockState state = level.getBlockState(position);
            boolean matchingWire = state.getBlock() == wireBlock
                    && state.hasProperty(TrellisWireBlock.AXIS)
                    && state.getValue(TrellisWireBlock.AXIS) == connection.axis();
            if (!matchingWire && !state.getMaterial().isReplaceable()) {
                return false;
            }
        }
        return true;
    }

    private void place(Level level, Connection connection) {
        BlockState wireState = wire.get().defaultBlockState()
                .setValue(TrellisWireBlock.AXIS, connection.axis());
        for (int offset = 1; offset < connection.distance(); offset++) {
            BlockPos position = connection.start().relative(connection.direction(), offset);
            if (level.getBlockState(position) != wireState) {
                level.setBlock(position, wireState, Block.UPDATE_ALL);
            }
        }
    }

    private static void message(Player player, String key, Object... arguments) {
        if (player != null) {
            player.displayClientMessage(Component.translatable(key, arguments), true);
        }
    }

    public record Selection(String dimension, BlockPos position) {
        public Selection {
            Objects.requireNonNull(dimension, "dimension");
            Objects.requireNonNull(position, "position");
        }
    }

    private record Connection(
            BlockPos start,
            Direction direction,
            Direction.Axis axis,
            int distance
    ) {
        private static Connection between(BlockPos first, BlockPos second, int maximum) {
            if (first.getY() != second.getY()) {
                return null;
            }
            int deltaX = second.getX() - first.getX();
            int deltaZ = second.getZ() - first.getZ();
            if ((deltaX == 0) == (deltaZ == 0)) {
                return null;
            }
            int distance = Math.abs(deltaX != 0 ? deltaX : deltaZ);
            if (distance < 2 || distance > maximum) {
                return null;
            }
            Direction direction;
            Direction.Axis axis;
            if (deltaX != 0) {
                direction = deltaX > 0 ? Direction.EAST : Direction.WEST;
                axis = Direction.Axis.X;
            } else {
                direction = deltaZ > 0 ? Direction.SOUTH : Direction.NORTH;
                axis = Direction.Axis.Z;
            }
            return new Connection(first, direction, axis, distance);
        }
    }
}

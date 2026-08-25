package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.api.process.ExecutorModifiers;
import com.djden.alcoholic.api.process.ItemOutput;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.process.MillConfig;
import com.djden.alcoholic.application.process.ProcessRecipeResolver;
import com.djden.alcoholic.domain.ingredient.IngredientLot;
import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;
import com.djden.alcoholic.domain.mechanical.MechanicalRequirement;
import com.djden.alcoholic.minecraft.mechanical.MechanicalDrives;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

/**
 * Native {@code MILL} executor. Requires an adjacent {@link com.djden.alcoholic.domain.mechanical.MechanicalDrivePort};
 * it contains no beverage-family logic.
 */
public final class MaltMillBlockEntity extends BlockEntity implements WorldlyContainer {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private int progress;
    private int duration = 80;
    private String runningJob = "";

    public MaltMillBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
    }

    public int progress() {
        return progress;
    }

    public int duration() {
        return duration;
    }

    public MechanicalDriveState driveState() {
        return MechanicalDrives.forMachine(level, worldPosition);
    }

    public static void serverTick(
            Level level,
            BlockPos position,
            BlockState state,
            MaltMillBlockEntity entity
    ) {
        entity.tick();
    }

    private void tick() {
        ItemStack input = items.get(INPUT_SLOT);
        if (input.isEmpty()) {
            progress = 0;
            runningJob = "";
            return;
        }
        MechanicalDriveState drive = driveState();
        MechanicalRequirement requirement = MechanicalRequirement.maltMill();
        if (!requirement.satisfied(drive)) {
            setChanged();
            return;
        }
        ProcessRuntime runtime = ProcessRuntime.shared();
        Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                runtime.beverages().catalog(),
                runtime.beverages().api(),
                BuiltinRegistrations.MILL,
                MinecraftSelectorMatcher.create(runtime.beverages()),
                Optional.of(ItemLots.id(input)),
                Optional.empty()
        );
        if (invocation.isEmpty()) {
            progress = 0;
            return;
        }
        MillConfig config = MillConfig.CODEC.decode(invocation.get().config());
        ExecutorModifiers modifiers = ExecutorModifiers.maltMill();
        if (!config.executable() || input.getCount() < config.inputAmount()) {
            progress = 0;
            return;
        }
        String job = invocation.get().nodeId() + "|" + ItemLots.id(input);
        if (!job.equals(runningJob)) {
            runningJob = job;
            progress = 0;
        }
        duration = Math.max(1, (int) Math.round(config.processingTicks() / modifiers.speedModifier()));
        MechanicalDrives.consumeWork(level, worldPosition, requirement.requiredCapacity());
        progress++;
        if (progress < duration) {
            setChanged();
            return;
        }
        IngredientLot lot = ItemLots.lot(MachineItemStacks.copyCount(input, config.inputAmount()));
        ProcessResult result = runtime.engine().execute(
                runtime.executor(BuiltinRegistrations.MILL),
                invocation.get(),
                ProcessInputs.ofSolids("malt", List.of(lot)),
                ProcessContext.of(20.0, 1.0, false, Optional.empty(), Optional.empty(), 0L, modifiers)
        );
        if (!result.success() || result.items().isEmpty()) {
            progress = 0;
            setChanged();
            return;
        }
        ItemOutput produced = result.items().get(0);
        ItemStack created = MachineItemStacks.stack(produced.item(), produced.amount());
        if (created.isEmpty()) {
            progress = 0;
            return;
        }
        SolidPropertyNbt.write(created, produced.properties());
        ItemStack existing = items.get(OUTPUT_SLOT);
        if (!existing.isEmpty() && (!ItemStack.isSameItemSameTags(existing, created)
                || existing.getCount() + created.getCount() > existing.getMaxStackSize())) {
            progress = duration;
            return;
        }
        if (existing.isEmpty()) {
            items.set(OUTPUT_SLOT, created);
        } else {
            existing.grow(created.getCount());
        }
        input.shrink(config.inputAmount());
        progress = 0;
        setChanged();
        sync();
    }

    public String debugDump() {
        MechanicalDriveState drive = driveState();
        return "malt-mill progress=" + progress + "/" + duration
                + " drive=" + drive.speed() + "/" + drive.availableCapacity()
                + " running=" + drive.running()
                + " stalled=" + drive.stalled()
                + " in=" + items.get(INPUT_SLOT)
                + " out=" + items.get(OUTPUT_SLOT);
    }

    public Component status() {
        return Component.translatable(
                "message.alcoholic.mill.status",
                progress,
                duration,
                String.format(java.util.Locale.ROOT, "%.0f", driveState().speed())
        );
    }

    public boolean insert(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ItemStack existing = items.get(INPUT_SLOT);
        if (existing.isEmpty()) {
            items.set(INPUT_SLOT, stack.split(Math.min(stack.getCount(), stack.getMaxStackSize())));
            setChanged();
            sync();
            return true;
        }
        if (ItemStack.isSameItemSameTags(existing, stack) && existing.getCount() < existing.getMaxStackSize()) {
            int move = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
            existing.grow(move);
            stack.shrink(move);
            setChanged();
            sync();
            return true;
        }
        return false;
    }

    public ItemStack extractOutput() {
        ItemStack existing = items.get(OUTPUT_SLOT);
        items.set(OUTPUT_SLOT, ItemStack.EMPTY);
        setChanged();
        sync();
        return existing;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("Progress", progress);
        tag.putInt("Duration", duration);
        tag.putString("Job", runningJob);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        progress = tag.getInt("Progress");
        duration = Math.max(1, tag.getInt("Duration"));
        runningJob = tag.getString("Job");
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return direction == Direction.DOWN ? new int[]{OUTPUT_SLOT} : new int[]{INPUT_SLOT};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == INPUT_SLOT && direction != Direction.DOWN;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == OUTPUT_SLOT && direction == Direction.DOWN;
    }
}

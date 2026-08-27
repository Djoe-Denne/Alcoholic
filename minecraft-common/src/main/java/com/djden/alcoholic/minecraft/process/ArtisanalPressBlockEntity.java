package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.process.PressConfig;
import com.djden.alcoholic.application.process.ProcessRecipeResolver;
import com.djden.alcoholic.domain.ingredient.IngredientLot;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.minecraft.fluid.LiquidBatchNbt;
import com.djden.alcoholic.minecraft.fluid.LiquidTank;
import com.djden.alcoholic.minecraft.fluid.LiquidVessel;
import com.djden.alcoholic.minecraft.menu.MachineAccess;
import com.djden.alcoholic.minecraft.menu.MachineLayout;
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

public final class ArtisanalPressBlockEntity extends BlockEntity implements WorldlyContainer, LiquidVessel, MachineAccess {
    public static final int INPUT_SLOT = 0;
    public static final int BYPRODUCT_SLOT = 1;
    public static final int CAPACITY = 8_000;

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private final LiquidTank tank;
    private int progress;
    private int duration = 200;

    public ArtisanalPressBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
        tank = new LiquidTank(CAPACITY, ProcessRuntime.shared().merges(), ProcessRuntime.shared().aggregators());
    }

    @Override
    public LiquidTank tank() {
        return tank;
    }

    public int progress() {
        return progress;
    }

    public int duration() {
        return duration;
    }

    @Override
    public MachineLayout layout() {
        return MachineLayout.TWO_SLOTS_ONE_TANK;
    }

    @Override
    public java.util.List<com.djden.alcoholic.api.ResourceId> displayedProcessTypes() {
        return java.util.List.of(BuiltinRegistrations.PRESS);
    }

    public static void serverTick(
            Level level,
            BlockPos position,
            BlockState state,
            ArtisanalPressBlockEntity entity
    ) {
        entity.tick();
    }

    private void tick() {
        ItemStack input = items.get(INPUT_SLOT);
        if (input.isEmpty()) {
            progress = 0;
            return;
        }
        ProcessRuntime runtime = ProcessRuntime.shared();
        Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                runtime.beverages().catalog(),
                runtime.beverages().api(),
                BuiltinRegistrations.PRESS,
                MinecraftSelectorMatcher.create(runtime.beverages()),
                Optional.of(ItemLots.id(input)),
                Optional.empty()
        );
        if (invocation.isEmpty()) {
            progress = 0;
            return;
        }
        PressConfig config = PressConfig.CODEC.decode(invocation.get().config());
        if (!config.executable() || input.getCount() < config.inputAmount()) {
            progress = 0;
            return;
        }
        duration = config.processingTicks();
        progress++;
        if (progress < duration) {
            setChanged();
            return;
        }
        IngredientLot lot = ItemLots.lot(copyCount(input, config.inputAmount()));
        ProcessResult result = runtime.engine().execute(
                runtime.pressExecutor(),
                invocation.get(),
                ProcessInputs.ofSolids("source", List.of(lot)),
                com.djden.alcoholic.api.process.ProcessContext.empty()
        );
        if (!result.success() || result.outputs().isEmpty()) {
            progress = 0;
            setChanged();
            return;
        }
        LiquidBatch produced = (LiquidBatch) result.outputs().get(0);
        if (tank.fill(produced, true) < produced.volumeMillibuckets()) {
            progress = duration;
            return;
        }
        if (!result.items().isEmpty()) {
            var byproduct = result.items().get(0);
            ItemStack existing = items.get(BYPRODUCT_SLOT);
            ItemStack created = stack(byproduct.item(), byproduct.amount());
            if (created.isEmpty()) {
                progress = 0;
                return;
            }
            if (!existing.isEmpty() && (!ItemStack.isSameItemSameTags(existing, created)
                    || existing.getCount() + created.getCount() > existing.getMaxStackSize())) {
                progress = duration;
                return;
            }
            if (existing.isEmpty()) {
                items.set(BYPRODUCT_SLOT, created);
            } else {
                existing.grow(created.getCount());
            }
        }
        tank.fill(produced, false);
        input.shrink(config.inputAmount());
        progress = 0;
        setChanged();
        sync();
    }

    public String debugDump() {
        return "press progress=" + progress + "/" + duration
                + " def=" + tank.contents().flatMap(LiquidBatch::baseLiquid)
                + " vol=" + tank.contents().map(LiquidBatch::volume).orElse(0.0)
                + " props=" + tank.contents().map(batch -> batch.properties().asMap()).orElse(java.util.Map.of());
    }

    public Component status() {
        int volume = tank.contents().map(LiquidBatch::volumeMillibuckets).orElse(0);
        return Component.translatable(
                "message.alcoholic.press.status",
                progress,
                duration,
                volume
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
        if (ItemStack.isSameItemSameTags(existing, stack)
                && existing.getCount() < existing.getMaxStackSize()) {
            int move = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
            existing.grow(move);
            stack.shrink(move);
            setChanged();
            sync();
            return true;
        }
        return false;
    }

    public ItemStack extractByproduct() {
        ItemStack existing = items.get(BYPRODUCT_SLOT);
        items.set(BYPRODUCT_SLOT, ItemStack.EMPTY);
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
        tank.contents().ifPresent(batch -> LiquidBatchNbt.writeRoot(tag, batch));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        progress = tag.getInt("Progress");
        duration = Math.max(1, tag.getInt("Duration"));
        tank.clear();
        LiquidBatchNbt.readRoot(tag).ifPresent(tank::set);
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

    private static ItemStack copyCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    private static ItemStack stack(com.djden.alcoholic.api.ResourceId id, int amount) {
        var item = net.minecraft.core.Registry.ITEM.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path())
        );
        if (item == net.minecraft.world.item.Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, amount);
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
        return direction == Direction.DOWN ? new int[]{BYPRODUCT_SLOT} : new int[]{INPUT_SLOT};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == INPUT_SLOT && direction != Direction.DOWN;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == BYPRODUCT_SLOT && direction == Direction.DOWN;
    }
}

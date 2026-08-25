package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.process.MashConfig;
import com.djden.alcoholic.application.process.ProcessRecipeResolver;
import com.djden.alcoholic.domain.ingredient.IngredientLot;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.minecraft.environment.HeatSources;
import com.djden.alcoholic.minecraft.fluid.LiquidBatchNbt;
import com.djden.alcoholic.minecraft.fluid.LiquidTank;
import com.djden.alcoholic.minecraft.fluid.LiquidVessel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import java.util.Locale;
import java.util.Optional;

public final class MashTunBlockEntity extends BlockEntity implements WorldlyContainer, LiquidVessel {
    public static final int INPUT_SLOT = 0;
    public static final int BYPRODUCT_SLOT = 1;
    public static final int CAPACITY = 8_000;
    public static final int INPUT_TANK = 0;
    public static final int OUTPUT_TANK = 1;

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private final LiquidTank inputTank;
    private final LiquidTank outputTank;
    private int progress;
    private int duration = 200;
    private String runningJob = "";

    public MashTunBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
        ProcessRuntime runtime = ProcessRuntime.shared();
        inputTank = new LiquidTank(CAPACITY, runtime.merges(), runtime.aggregators());
        outputTank = new LiquidTank(CAPACITY, runtime.merges(), runtime.aggregators());
    }

    @Override
    public LiquidTank tank() {
        return outputTank;
    }

    @Override
    public int tankCount() {
        return 2;
    }

    @Override
    public LiquidTank tank(int index) {
        return index == INPUT_TANK ? inputTank : outputTank;
    }

    @Override
    public boolean canFillTank(int index) {
        return index == INPUT_TANK;
    }

    @Override
    public boolean canDrainTank(int index) {
        return index == OUTPUT_TANK;
    }

    public int progress() {
        return progress;
    }

    public int duration() {
        return duration;
    }

    public double temperatureCelsius() {
        return HeatSources.celsius(level, worldPosition);
    }

    public static void serverTick(
            Level level,
            BlockPos position,
            BlockState state,
            MashTunBlockEntity entity
    ) {
        entity.tick();
    }

    private void tick() {
        Optional<LiquidBatch> water = inputTank.contents();
        ItemStack input = items.get(INPUT_SLOT);
        if (input.isEmpty() || water.isEmpty()) {
            progress = 0;
            return;
        }
        LiquidBatch liquid = water.get();
        ProcessRuntime runtime = ProcessRuntime.shared();
        Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                runtime.beverages().catalog(),
                runtime.beverages().api(),
                BuiltinRegistrations.MASH,
                MinecraftSelectorMatcher.create(runtime.beverages()),
                Optional.of(ItemLots.id(input)),
                liquid.baseLiquid()
        );
        if (invocation.isEmpty()) {
            progress = 0;
            return;
        }
        MashConfig config = MashConfig.CODEC.decode(invocation.get().config());
        if (!config.executable()
                || input.getCount() < config.inputAmount()
                || liquid.volume() + 1e-9 < config.inputLiquidVolume()) {
            progress = 0;
            return;
        }
        String job = invocation.get().nodeId() + "|" + ItemLots.id(input)
                + "|" + liquid.baseLiquid().map(ResourceId::toString).orElse("");
        if (!job.equals(runningJob)) {
            runningJob = job;
            progress = 0;
        }
        double rate = Math.max(0.10, config.temperature().rateFactor(temperatureCelsius()));
        duration = Math.max(1, (int) Math.round(config.processingTicks() / rate));
        progress++;
        if (progress < duration) {
            setChanged();
            return;
        }
        int consume = Math.max(1, (int) Math.round(config.inputLiquidVolume()));
        LiquidBatch extracted = inputTank.drain(consume, true);
        IngredientLot lot = ItemLots.lot(MachineItemStacks.copyCount(input, config.inputAmount()));
        ProcessResult result = runtime.engine().execute(
                runtime.mashExecutor(),
                invocation.get(),
                ProcessInputs.of("grist", List.of(lot), "water", extracted),
                ProcessContext.of(temperatureCelsius(), 1.0, false)
        );
        if (!result.success() || result.outputs().isEmpty()) {
            progress = 0;
            setChanged();
            return;
        }
        LiquidBatch produced = (LiquidBatch) result.outputs().get(0);
        if (outputTank.fill(produced, true) < produced.volumeMillibuckets()) {
            progress = duration;
            return;
        }
        if (!result.items().isEmpty()) {
            var byproduct = result.items().get(0);
            ItemStack existing = items.get(BYPRODUCT_SLOT);
            ItemStack created = MachineItemStacks.stack(byproduct.item(), byproduct.amount());
            if (created.isEmpty()
                    || (!existing.isEmpty() && (!ItemStack.isSameItemSameTags(existing, created)
                    || existing.getCount() + created.getCount() > existing.getMaxStackSize()))) {
                progress = duration;
                return;
            }
            if (existing.isEmpty()) {
                items.set(BYPRODUCT_SLOT, created);
            } else {
                existing.grow(created.getCount());
            }
        }
        inputTank.drain(consume, false);
        outputTank.fill(produced, false);
        input.shrink(config.inputAmount());
        progress = 0;
        setChanged();
        sync();
    }

    public String debugDump() {
        return "mash progress=" + progress + "/" + duration
                + " temp=" + temperatureCelsius()
                + " in=" + inputTank.contents().flatMap(LiquidBatch::baseLiquid)
                + " inVol=" + inputTank.contents().map(LiquidBatch::volume).orElse(0.0)
                + " out=" + outputTank.contents().flatMap(LiquidBatch::baseLiquid)
                + " outVol=" + outputTank.contents().map(LiquidBatch::volume).orElse(0.0)
                + " sugar=" + outputTank.contents().map(batch -> batch.number(
                        ResourceId.parse("alcoholic:sugar"), 0.0
                )).orElse(0.0);
    }

    public Component status() {
        return Component.translatable(
                "message.alcoholic.mash.status",
                String.format(Locale.ROOT, "%.1f", temperatureCelsius()),
                progress,
                duration
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
        inputTank.contents().ifPresent(batch -> tag.put("InputLiquid", LiquidBatchNbt.toTag(batch)));
        outputTank.contents().ifPresent(batch -> LiquidBatchNbt.writeRoot(tag, batch));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        progress = tag.getInt("Progress");
        duration = Math.max(1, tag.getInt("Duration"));
        inputTank.clear();
        outputTank.clear();
        if (tag.contains("InputLiquid", Tag.TAG_COMPOUND)) {
            LiquidBatchNbt.fromTag(tag.getCompound("InputLiquid")).ifPresent(inputTank::set);
        }
        LiquidBatchNbt.readRoot(tag).ifPresent(outputTank::set);
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

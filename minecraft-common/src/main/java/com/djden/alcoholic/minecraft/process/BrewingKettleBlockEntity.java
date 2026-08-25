package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.process.BoilConfig;
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

public final class BrewingKettleBlockEntity extends BlockEntity implements WorldlyContainer, LiquidVessel {
    public static final int ADDITION_SLOT = 0;
    public static final int CAPACITY = 8_000;

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final LiquidTank tank;
    private int progress;
    private int duration = 200;
    private String runningJob = "";

    public BrewingKettleBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
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

    public double temperatureCelsius() {
        return HeatSources.celsius(level, worldPosition);
    }

    public static void serverTick(
            Level level,
            BlockPos position,
            BlockState state,
            BrewingKettleBlockEntity entity
    ) {
        entity.tick();
    }

    private void tick() {
        Optional<LiquidBatch> contents = tank.contents();
        ItemStack addition = items.get(ADDITION_SLOT);
        if (contents.isEmpty() || addition.isEmpty()) {
            progress = 0;
            return;
        }
        LiquidBatch liquid = contents.get();
        ProcessRuntime runtime = ProcessRuntime.shared();
        Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                runtime.beverages().catalog(),
                runtime.beverages().api(),
                BuiltinRegistrations.BOIL,
                MinecraftSelectorMatcher.create(runtime.beverages()),
                Optional.of(ItemLots.id(addition)),
                liquid.baseLiquid()
        );
        if (invocation.isEmpty()) {
            progress = 0;
            return;
        }
        BoilConfig config = BoilConfig.CODEC.decode(invocation.get().config());
        if (!config.executable() || addition.getCount() < config.additionAmount()) {
            progress = 0;
            return;
        }
        if (config.temperature().stalled(temperatureCelsius())) {
            progress = 0;
            return;
        }
        String job = invocation.get().nodeId() + "|" + ItemLots.id(addition)
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
        IngredientLot lot = ItemLots.lot(MachineItemStacks.copyCount(addition, config.additionAmount()));
        ProcessResult result = runtime.engine().execute(
                runtime.boilExecutor(),
                invocation.get(),
                ProcessInputs.of("hops", List.of(lot), "wort", liquid),
                ProcessContext.of(temperatureCelsius(), 1.0, false)
        );
        if (!result.success() || result.outputs().isEmpty()) {
            progress = 0;
            setChanged();
            return;
        }
        tank.set((LiquidBatch) result.outputs().get(0));
        addition.shrink(config.additionAmount());
        progress = 0;
        setChanged();
        sync();
    }

    public String debugDump() {
        return "boil progress=" + progress + "/" + duration
                + " temp=" + temperatureCelsius()
                + " def=" + tank.contents().flatMap(LiquidBatch::baseLiquid)
                + " vol=" + tank.contents().map(LiquidBatch::volume).orElse(0.0)
                + " bitterness=" + tank.contents().map(batch -> batch.number(
                        ResourceId.parse("alcoholic:bitterness"), 0.0
                )).orElse(0.0)
                + " sugar=" + tank.contents().map(batch -> batch.number(
                        ResourceId.parse("alcoholic:sugar"), 0.0
                )).orElse(0.0);
    }

    public Component status() {
        return Component.translatable(
                "message.alcoholic.boil.status",
                String.format(Locale.ROOT, "%.1f", temperatureCelsius()),
                progress,
                duration
        );
    }

    public boolean insert(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ItemStack existing = items.get(ADDITION_SLOT);
        if (existing.isEmpty()) {
            items.set(ADDITION_SLOT, stack.split(Math.min(stack.getCount(), stack.getMaxStackSize())));
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
        return new int[]{ADDITION_SLOT};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == ADDITION_SLOT && direction != Direction.DOWN;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return false;
    }
}

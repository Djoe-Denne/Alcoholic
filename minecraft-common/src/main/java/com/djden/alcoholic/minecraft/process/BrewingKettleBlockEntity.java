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
import com.djden.alcoholic.minecraft.menu.MachineAccess;
import com.djden.alcoholic.minecraft.menu.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class BrewingKettleBlockEntity extends BlockEntity implements WorldlyContainer, LiquidVessel, MachineAccess {
    public static final int ADDITION_SLOT = 0;
    public static final int CAPACITY = 8_000;

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final LiquidTank tank;
    private int progress;
    private int duration = 200;
    private String runningJob = "";
    private String runningDefinition = "";
    private int additionsCommitted;
    private final List<ItemStack> committedSolids = new ArrayList<>();

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

    @Override
    public MachineLayout layout() {
        return MachineLayout.ONE_SLOT_ONE_TANK;
    }

    @Override
    public java.util.List<com.djden.alcoholic.api.ResourceId> displayedProcessTypes() {
        return java.util.List.of(BuiltinRegistrations.BOIL);
    }

    @Override
    public int temperatureDeci() {
        return MachineAccess.deci(temperatureCelsius());
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
        if (contents.isEmpty() || contents.get().baseLiquid().isEmpty()) {
            cancelProcess();
            return;
        }
        LiquidBatch liquid = contents.get();
        ItemStack addition = items.get(ADDITION_SLOT);
        ProcessRuntime runtime = ProcessRuntime.shared();
        Optional<ResourceId> selected = selectedDefinition();
        Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                runtime.beverages().catalog(),
                runtime.beverages().api(),
                BuiltinRegistrations.BOIL,
                MinecraftSelectorMatcher.create(runtime.beverages()),
                selected.isPresent() || addition.isEmpty()
                        ? Optional.empty()
                        : Optional.of(ItemLots.id(addition)),
                liquid.baseLiquid(),
                selected
        );
        if (invocation.isEmpty()) {
            if (selected.isPresent()) {
                cancelProcess();
            } else {
                progress = 0;
            }
            return;
        }
        BoilConfig config = BoilConfig.CODEC.decode(invocation.get().config());
        if (!config.executable()) {
            cancelProcess();
            return;
        }
        if (config.temperature().stalled(temperatureCelsius())) {
            return;
        }
        String job = invocation.get().nodeId() + "|" + liquid.baseLiquid().map(ResourceId::toString).orElse("");
        if (!job.equals(runningJob)) {
            cancelProcess();
            runningJob = job;
            runningDefinition = invocation.get().nodeId();
            progress = 0;
        }
        double rate = Math.max(0.10, config.temperature().rateFactor(temperatureCelsius()));
        duration = Math.max(1, (int) Math.round(config.processingTicks() / rate));
        double fraction = duration <= 1 ? 1.0 : Math.min(1.0, (double) progress / duration);
        if (!commitDueAdditions(config, fraction)) {
            setChanged();
            return;
        }
        if (progress < duration) {
            progress++;
        }
        if (progress < duration) {
            setChanged();
            return;
        }
        if (!commitDueAdditions(config, 1.0)) {
            setChanged();
            return;
        }
        List<IngredientLot> lots = committedSolids.stream()
                .filter(stack -> !stack.isEmpty())
                .map(ItemLots::lot)
                .toList();
        long committedItems = lots.stream().mapToLong(IngredientLot::count).sum();
        if (committedItems < config.requiredAdditionItems()) {
            return;
        }
        ProcessResult result = runtime.engine().execute(
                runtime.boilExecutor(),
                invocation.get(),
                lots.isEmpty()
                        ? ProcessInputs.ofLiquid("wort", liquid)
                        : ProcessInputs.of("hops", lots, "wort", liquid),
                ProcessContext.of(temperatureCelsius(), 1.0, false)
        );
        if (!result.success() || result.outputs().isEmpty()) {
            cancelProcess();
            setChanged();
            return;
        }
        tank.set((LiquidBatch) result.outputs().get(0));
        completeProcess();
        setChanged();
        sync();
    }

    private boolean commitDueAdditions(BoilConfig config, double fraction) {
        List<BoilConfig.BoilAddition> schedule = schedule(config);
        var matcher = MinecraftSelectorMatcher.create(ProcessRuntime.shared().beverages());
        while (additionsCommitted < schedule.size()) {
            BoilConfig.BoilAddition due = schedule.get(additionsCommitted);
            if (due.atProgress() > fraction + 1e-9) {
                return true;
            }
            ItemStack input = items.get(ADDITION_SLOT);
            if (input.isEmpty()
                    || input.getCount() < config.additionAmount()
                    || !matcher.matches(due.selector(), ItemLots.id(input))) {
                return false;
            }
            ItemStack taken = input.split(config.additionAmount());
            SolidPropertyNbt.write(
                    taken,
                    Map.of(
                            ResourceId.parse("alcoholic:addition_role"), due.role(),
                            ResourceId.parse("alcoholic:addition_progress"), due.atProgress()
                    )
            );
            committedSolids.add(taken);
            additionsCommitted++;
            setChanged();
        }
        return true;
    }

    private static List<BoilConfig.BoilAddition> schedule(BoilConfig config) {
        if (!config.additions().isEmpty()) {
            return config.additions();
        }
        return config.additionSelector()
                .map(selector -> List.of(new BoilConfig.BoilAddition(selector, 0.0)))
                .orElseGet(List::of);
    }

    private Optional<ResourceId> selectedDefinition() {
        if (runningDefinition.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(ResourceId.parse(runningDefinition));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    void cancelProcess() {
        refundCommittedSolids();
        completeProcess();
    }

    private void completeProcess() {
        progress = 0;
        duration = 200;
        runningJob = "";
        runningDefinition = "";
        additionsCommitted = 0;
        committedSolids.clear();
    }

    private void refundCommittedSolids() {
        for (ItemStack committed : committedSolids) {
            if (committed.isEmpty()) {
                continue;
            }
            ItemStack remainder = committed.copy();
            ItemStack input = items.get(ADDITION_SLOT);
            if (input.isEmpty()) {
                items.set(ADDITION_SLOT, remainder);
                continue;
            }
            if (ItemStack.isSameItemSameTags(input, remainder) && input.getCount() < input.getMaxStackSize()) {
                int moved = Math.min(remainder.getCount(), input.getMaxStackSize() - input.getCount());
                input.grow(moved);
                remainder.shrink(moved);
            }
            if (!remainder.isEmpty() && level != null && !level.isClientSide) {
                Block.popResource(level, worldPosition, remainder);
            }
        }
        committedSolids.clear();
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
        tag.putString("RunningJob", runningJob);
        tag.putString("RunningDefinition", runningDefinition);
        tag.putInt("AdditionsCommitted", additionsCommitted);
        ListTag committed = new ListTag();
        for (ItemStack stack : committedSolids) {
            if (!stack.isEmpty()) {
                committed.add(stack.save(new CompoundTag()));
            }
        }
        tag.put("CommittedSolids", committed);
        tank.contents().ifPresent(batch -> LiquidBatchNbt.writeRoot(tag, batch));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        progress = tag.getInt("Progress");
        duration = Math.max(1, tag.getInt("Duration"));
        runningJob = tag.getString("RunningJob");
        runningDefinition = tag.getString("RunningDefinition");
        additionsCommitted = Math.max(0, tag.getInt("AdditionsCommitted"));
        committedSolids.clear();
        ListTag committed = tag.getList("CommittedSolids", Tag.TAG_COMPOUND);
        for (int index = 0; index < committed.size(); index++) {
            ItemStack stack = ItemStack.of(committed.getCompound(index));
            if (!stack.isEmpty()) {
                committedSolids.add(stack);
            }
        }
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
        committedSolids.clear();
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

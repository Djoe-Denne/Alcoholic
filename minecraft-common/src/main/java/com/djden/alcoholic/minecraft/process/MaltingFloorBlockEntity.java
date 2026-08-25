package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ItemOutput;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.process.CapabilityProcessExecutor;
import com.djden.alcoholic.application.process.MaltConfig;
import com.djden.alcoholic.application.process.MillConfig;
import com.djden.alcoholic.application.process.ProcessRecipeResolver;
import com.djden.alcoholic.domain.ingredient.IngredientLot;
import com.djden.alcoholic.domain.process.ProcessDefinition;
import com.djden.alcoholic.domain.vessel.EnvironmentProfile;
import com.djden.alcoholic.minecraft.environment.EnvironmentSampler;
import com.djden.alcoholic.minecraft.environment.HeatSources;
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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class MaltingFloorBlockEntity extends BlockEntity implements WorldlyContainer {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;

    private static final ResourceId DEFAULT_DEFINITION = ResourceId.parse("alcoholic:malt_pale");

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private int progress;
    private int duration = 200;
    private ResourceId selectedDefinition = DEFAULT_DEFINITION;
    private String runningJob = "";

    public MaltingFloorBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
    }

    public int progress() {
        return progress;
    }

    public int duration() {
        return duration;
    }

    public static void serverTick(
            Level level,
            BlockPos position,
            BlockState state,
            MaltingFloorBlockEntity entity
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
        Optional<ProcessDefinition> selected = runtime.beverages().catalog().process(selectedDefinition);
        if (selected.isEmpty()) {
            progress = 0;
            return;
        }
        ResourceId processType = selected.get().processType();
        if (!BuiltinRegistrations.MALT.equals(processType) && !BuiltinRegistrations.MILL.equals(processType)) {
            progress = 0;
            return;
        }
        Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                runtime.beverages().catalog(),
                runtime.beverages().api(),
                processType,
                MinecraftSelectorMatcher.create(runtime.beverages()),
                Optional.of(ItemLots.id(input)),
                Optional.empty(),
                Optional.of(selectedDefinition)
        );
        if (invocation.isEmpty()) {
            progress = 0;
            return;
        }
        FloorJob job = job(runtime, processType, invocation.get(), input);
        if (job == null) {
            progress = 0;
            return;
        }
        if (!job.invocationId().equals(runningJob)) {
            runningJob = job.invocationId();
            progress = 0;
        }
        duration = job.processingTicks();
        progress++;
        if (progress < duration) {
            setChanged();
            return;
        }
        IngredientLot lot = ItemLots.lot(MachineItemStacks.copyCount(input, job.inputAmount()));
        ProcessResult result = runtime.engine().execute(
                job.executor(),
                invocation.get(),
                ProcessInputs.ofSolids(job.solidPort(), List.of(lot)),
                job.context()
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
        input.shrink(job.inputAmount());
        progress = 0;
        setChanged();
        sync();
    }

    private FloorJob job(
            ProcessRuntime runtime,
            ResourceId processType,
            ProcessInvocation invocation,
            ItemStack input
    ) {
        if (BuiltinRegistrations.MALT.equals(processType)) {
            MaltConfig config = MaltConfig.CODEC.decode(invocation.config());
            if (!config.executable() || input.getCount() < config.inputAmount()) {
                return null;
            }
            EnvironmentProfile environment = level == null
                    ? EnvironmentProfile.temperateCellar()
                    : EnvironmentSampler.sample(level, worldPosition);
            double temperature = level == null ? environment.temperature() : HeatSources.celsius(level, worldPosition);
            if (config.temperature().stalled(temperature)
                    || environment.humidity() + 1e-9 < config.moistureRequirement()) {
                return null;
            }
            return new FloorJob(
                    invocation.nodeId(),
                    config.inputAmount(),
                    config.processingTicks(),
                    runtime.executor(processType),
                    ProcessContext.of(
                            temperature,
                            1.0,
                            false,
                            Optional.empty(),
                            Optional.of(environment),
                            level == null ? 0L : level.getGameTime()
                    ),
                    "grain"
            );
        }
        MillConfig config = MillConfig.CODEC.decode(invocation.config());
        if (!config.executable() || input.getCount() < config.inputAmount()) {
            return null;
        }
        return new FloorJob(
                invocation.nodeId(),
                config.inputAmount(),
                config.processingTicks(),
                runtime.executor(processType),
                ProcessContext.empty(),
                "malt"
        );
    }

    private record FloorJob(
            String invocationId,
            int inputAmount,
            int processingTicks,
            CapabilityProcessExecutor executor,
            ProcessContext context,
            String solidPort
    ) {
    }

    public String debugDump() {
        return "malting progress=" + progress + "/" + duration
                + " def=" + selectedDefinition
                + " in=" + items.get(INPUT_SLOT)
                + " out=" + items.get(OUTPUT_SLOT);
    }

    public Component status() {
        return Component.translatable(
                "message.alcoholic.malting.status",
                progress,
                duration,
                selectedDefinition.toString()
        );
    }

    public ResourceId selectedDefinition() {
        return selectedDefinition;
    }

    public void cycleDefinition() {
        List<ResourceId> ids = ProcessRuntime.shared().beverages().catalog().processes().values().stream()
                .filter(definition -> BuiltinRegistrations.MALT.equals(definition.processType())
                        || BuiltinRegistrations.MILL.equals(definition.processType()))
                .map(ProcessDefinition::id)
                .sorted(Comparator.comparing(ResourceId::toString))
                .toList();
        if (ids.isEmpty()) {
            return;
        }
        int index = ids.indexOf(selectedDefinition);
        selectedDefinition = ids.get((index + 1) % ids.size());
        progress = 0;
        setChanged();
        sync();
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
        tag.putString("ProcessId", selectedDefinition.toString());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        progress = tag.getInt("Progress");
        duration = Math.max(1, tag.getInt("Duration"));
        if (tag.contains("ProcessId")) {
            try {
                selectedDefinition = ResourceId.parse(tag.getString("ProcessId"));
            } catch (RuntimeException ignored) {
                selectedDefinition = DEFAULT_DEFINITION;
            }
        }
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

package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.machine.MultiblockProfiler;
import com.djden.alcoholic.application.process.FermentConfig;
import com.djden.alcoholic.application.process.PressConfig;
import com.djden.alcoholic.application.process.ProcessRecipeResolver;
import com.djden.alcoholic.domain.ingredient.IngredientLot;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.multiblock.AxisBox;
import com.djden.alcoholic.domain.multiblock.Box3;
import com.djden.alcoholic.domain.multiblock.CellCoord;
import com.djden.alcoholic.domain.multiblock.CrushOccupancy;
import com.djden.alcoholic.domain.multiblock.HollowCuboidValidator;
import com.djden.alcoholic.domain.multiblock.MachineKind;
import com.djden.alcoholic.domain.multiblock.MultiblockDefinition;
import com.djden.alcoholic.domain.multiblock.MultiblockGeometry;
import com.djden.alcoholic.domain.multiblock.PressStrokeState;
import com.djden.alcoholic.domain.multiblock.ValidationResult;
import com.djden.alcoholic.domain.multiblock.ValidationStatus;
import com.djden.alcoholic.domain.process.ElapsedProcessClock;
import com.djden.alcoholic.domain.process.ThermalStability;
import com.djden.alcoholic.minecraft.fluid.LiquidBatchNbt;
import com.djden.alcoholic.minecraft.fluid.LiquidTank;
import com.djden.alcoholic.minecraft.fluid.LiquidVessel;
import com.djden.alcoholic.minecraft.process.ItemLots;
import com.djden.alcoholic.minecraft.process.MinecraftSelectorMatcher;
import com.djden.alcoholic.minecraft.process.ProcessRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
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
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MultiblockControllerBlockEntity extends BlockEntity
        implements WorldlyContainer, LiquidVessel {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;

    private final ResourceId definitionId;
    private final LiquidTank tank;
    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private boolean formed;
    private IndustrialAccess access = IndustrialAccess.CLOSED;
    private MultiblockGeometry geometry;
    private String lastReason = "unformed";
    private boolean structureDirty = true;
    private long lastProcessedGameTime;
    private boolean skipUnloadGap = true;
    private boolean yeastPitched;
    private double ventedCo2;
    private int pressProgress;
    private int pressDuration = 20;
    private double strokeCycle;
    private boolean pressWorking;
    private PressStrokeState stroke = PressStrokeState.IDLE;
    private double lastRpm;
    private List<BlockPos> boundParts = List.of();

    public MultiblockControllerBlockEntity(
            BlockEntityType<?> type,
            BlockPos position,
            BlockState state,
            ResourceId definitionId
    ) {
        super(type, position, state);
        this.definitionId = definitionId;
        this.tank = new LiquidTank(1, ProcessRuntime.shared().merges(), ProcessRuntime.shared().aggregators());
    }

    public ResourceId definitionId() {
        return definitionId;
    }

    public boolean formed() {
        return formed;
    }

    public IndustrialAccess access() {
        return access;
    }

    public Optional<MultiblockGeometry> geometry() {
        return Optional.ofNullable(geometry);
    }

    public PressStrokeState stroke() {
        return stroke;
    }

    public double strokeCycle() {
        return strokeCycle;
    }

    public double lastRpm() {
        return lastRpm;
    }

    public boolean owns(BlockPos part) {
        if (!formed || geometry == null) {
            return false;
        }
        return geometry.bounds().contains(WorldStructureSampler.coord(part));
    }

    @Override
    public LiquidTank tank() {
        return tank;
    }

    public void onTankChanged() {
        setChanged();
        if (access == IndustrialAccess.DRAIN_ONLY) {
            markStructureDirty();
        }
        sync();
    }

    public void markStructureDirty() {
        structureDirty = true;
    }

    public static void tick(
            Level level,
            BlockPos position,
            BlockState state,
            MultiblockControllerBlockEntity entity
    ) {
        long start = System.nanoTime();
        entity.serverTick();
        MultiblockProfiler.SHARED.recordTick(System.nanoTime() - start);
    }

    private void serverTick() {
        if (level == null) {
            return;
        }
        if (level.isClientSide) {
            return;
        }
        long now = level.getGameTime();
        boolean periodic = formed ? now % 200 == 0 : now % 20 == 0;
        if (structureDirty || periodic) {
            revalidate();
            structureDirty = false;
        }
        if (!access.canProcess()) {
            pressWorking = false;
            stroke = PressStrokeState.IDLE;
            return;
        }
        definition().ifPresent(definition -> {
            if (definition.kind() == MachineKind.PRESS) {
                tickPress(definition);
            } else if (definition.kind() == MachineKind.FERMENT) {
                tickFerment(definition, now);
            }
        });
    }

    private void revalidate() {
        if (level == null) {
            return;
        }
        Optional<MultiblockDefinition> definition = definition();
        if (definition.isEmpty()) {
            return;
        }
        long start = System.nanoTime();
        ValidationResult result = HollowCuboidValidator.validate(
                definition.get(),
                WorldStructureSampler.coord(worldPosition),
                new WorldStructureSampler(level),
                tank.contents().map(LiquidBatch::volumeMillibuckets).orElse(0)
        );
        MultiblockProfiler.SHARED.recordValidation(System.nanoTime() - start);
        lastReason = result.reason();
        if (result.status() == ValidationStatus.INCOMPLETE) {
            return;
        }
        if (result.status() == ValidationStatus.OVERCAPACITY) {
            unform(IndustrialAccess.DRAIN_ONLY, result.geometry().orElse(null));
            return;
        }
        if (!result.formed()) {
            unform(IndustrialAccess.CLOSED, null);
            return;
        }
        MultiblockGeometry next = result.geometry().orElseThrow();
        if (!tank.tryResize(next.capacityMillibuckets())) {
            unform(IndustrialAccess.DRAIN_ONLY, next);
            return;
        }
        formed = true;
        access = IndustrialAccess.OPEN;
        geometry = next;
        bindParts(next);
        if (getBlockState().hasProperty(MultiblockControllerBlock.FORMED)
                && !getBlockState().getValue(MultiblockControllerBlock.FORMED)) {
            level.setBlock(
                    worldPosition,
                    getBlockState().setValue(MultiblockControllerBlock.FORMED, true),
                    Block.UPDATE_CLIENTS
            );
        }
        setChanged();
        sync();
    }

    private void unform(IndustrialAccess nextAccess, MultiblockGeometry retained) {
        boolean wasFormed = formed;
        formed = false;
        access = nextAccess;
        if (retained != null) {
            geometry = retained;
        }
        pressWorking = false;
        stroke = PressStrokeState.IDLE;
        clearPartBindings();
        if (wasFormed && getBlockState().hasProperty(MultiblockControllerBlock.FORMED)) {
            level.setBlock(
                    worldPosition,
                    getBlockState().setValue(MultiblockControllerBlock.FORMED, false),
                    Block.UPDATE_CLIENTS
            );
        }
        setChanged();
        sync();
    }

    private void bindParts(MultiblockGeometry next) {
        List<BlockPos> parts = new ArrayList<>();
        AxisBox box = next.bounds();
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int y = box.minY(); y <= box.maxY(); y++) {
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (level.getBlockEntity(pos) instanceof ControllerBound bound) {
                        bound.bindController(worldPosition);
                        parts.add(pos);
                    }
                }
            }
        }
        boundParts = List.copyOf(parts);
    }

    private void clearPartBindings() {
        for (BlockPos pos : boundParts) {
            if (level != null && level.hasChunkAt(pos)
                    && level.getBlockEntity(pos) instanceof ControllerBound bound) {
                bound.clearController();
            }
        }
        boundParts = List.of();
    }

    private void tickPress(MultiblockDefinition definition) {
        lastRpm = collectRpm();
        if (!definition.kinetic().satisfied(lastRpm)) {
            pressWorking = false;
            stroke = PressStrokeState.IDLE;
            pressProgress = 0;
            return;
        }
        ItemStack input = items.get(INPUT_SLOT);
        if (input.isEmpty()) {
            pressWorking = false;
            stroke = PressStrokeState.IDLE;
            pressProgress = 0;
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
            pressWorking = false;
            return;
        }
        PressConfig config = PressConfig.CODEC.decode(invocation.get().config());
        if (!config.executable() || input.getCount() < config.inputAmount()) {
            pressWorking = false;
            return;
        }
        pressWorking = true;
        pressDuration = Math.max(1, (int) Math.round(config.processingTicks() / definition.modifiers().speedModifier()));
        pressProgress++;
        strokeCycle = pressDuration <= 1 ? 1.0 : (double) pressProgress / pressDuration;
        stroke = PressStrokeState.fromProgress(true, strokeCycle);
        applyCrush();
        if (pressProgress < pressDuration) {
            setChanged();
            if (pressProgress % 4 == 0) {
                sync();
            }
            return;
        }
        int units = Math.min(
                input.getCount() / config.inputAmount(),
                definition.modifiers().maxBatchUnits()
        );
        IngredientLot lot = ItemLots.lot(copyCount(input, units * config.inputAmount()));
        ProcessResult result = runtime.engine().execute(
                IndustrialRuntime.shared().pressExecutor(),
                invocation.get(),
                ProcessInputs.ofSolids("source", List.of(lot)),
                ProcessContext.of(
                        20.0,
                        1.0,
                        false,
                        Optional.empty(),
                        Optional.empty(),
                        level.getGameTime(),
                        definition.modifiers()
                )
        );
        if (!result.success() || result.outputs().isEmpty()) {
            pressProgress = 0;
            return;
        }
        LiquidBatch produced = (LiquidBatch) result.outputs().get(0);
        if (tank.fill(produced, true) < produced.volumeMillibuckets()) {
            pressProgress = pressDuration;
            return;
        }
        if (!result.items().isEmpty()) {
            var byproduct = result.items().get(0);
            ItemStack created = stack(byproduct.item(), byproduct.amount());
            ItemStack existing = items.get(OUTPUT_SLOT);
            if (created.isEmpty()) {
                pressProgress = 0;
                return;
            }
            if (!existing.isEmpty() && (!ItemStack.isSameItemSameTags(existing, created)
                    || existing.getCount() + created.getCount() > getMaxStackSize())) {
                pressProgress = pressDuration;
                return;
            }
            if (existing.isEmpty()) {
                items.set(OUTPUT_SLOT, created);
            } else {
                existing.grow(created.getCount());
            }
        }
        tank.fill(produced, false);
        input.shrink(units * config.inputAmount());
        pressProgress = 0;
        strokeCycle = 0.0;
        stroke = PressStrokeState.IDLE;
        pressWorking = false;
        onTankChanged();
    }

    private void tickFerment(MultiblockDefinition definition, long now) {
        if (skipUnloadGap) {
            lastProcessedGameTime = now;
            skipUnloadGap = false;
            return;
        }
        double delta = ElapsedProcessClock.deltaTicks(lastProcessedGameTime, now, 200);
        lastProcessedGameTime = now;
        if (delta <= 0.0) {
            return;
        }
        Optional<LiquidBatch> contents = tank.contents();
        if (contents.isEmpty() || contents.get().baseLiquid().isEmpty()) {
            yeastPitched = false;
            return;
        }
        LiquidBatch batch = contents.get();
        ProcessRuntime runtime = ProcessRuntime.shared();
        Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                runtime.beverages().catalog(),
                runtime.beverages().api(),
                BuiltinRegistrations.FERMENT,
                MinecraftSelectorMatcher.create(runtime.beverages()),
                Optional.empty(),
                batch.baseLiquid()
        );
        if (invocation.isEmpty()) {
            return;
        }
        FermentConfig config = FermentConfig.CODEC.decode(invocation.get().config());
        if (config.requireYeast() && !yeastPitched) {
            ItemStack yeast = items.get(INPUT_SLOT);
            if (yeast.isEmpty() || !yeastMatches(runtime, yeast)) {
                return;
            }
            yeast.shrink(1);
            yeastPitched = true;
        }
        double ambient = ambientTemperature();
        double effective = ThermalStability.effectiveCelsius(ambient, 18.0, definition.modifiers().thermalStability());
        ProcessResult result = runtime.engine().execute(
                IndustrialRuntime.shared().fermentExecutor(),
                invocation.get(),
                ProcessInputs.ofLiquid("must", batch),
                ProcessContext.of(
                        effective,
                        delta,
                        yeastPitched || !config.requireYeast(),
                        Optional.empty(),
                        Optional.empty(),
                        now,
                        definition.modifiers()
                )
        );
        if (!result.success() || result.outputs().isEmpty()) {
            return;
        }
        LiquidBatch next = (LiquidBatch) result.outputs().get(0);
        double consumedSugar = Math.max(
                0.0,
                batch.number(config.sugarProperty(), 0.0) - next.number(config.sugarProperty(), 0.0)
        );
        ventedCo2 += consumedSugar * config.kinetics().co2PerSugar();
        tank.set(next);
        onTankChanged();
    }

    private void applyCrush() {
        if (level == null || geometry == null || !stroke.crushActive()) {
            return;
        }
        Box3 crush = crushVolume().orElse(null);
        if (crush == null) {
            return;
        }
        AABB search = new AABB(crush.minX(), crush.minY(), crush.minZ(), crush.maxX(), crush.maxY(), crush.maxZ())
                .inflate(0.75);
        for (Player player : level.getEntitiesOfClass(Player.class, search)) {
            AABB box = player.getBoundingBox();
            if (CrushOccupancy.lethal(
                    stroke,
                    crush,
                    player.getX(),
                    player.getY() + (player.getBbHeight() * 0.6),
                    player.getZ(),
                    new Box3(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)
            )) {
                IndustrialDamage.crush(player);
            }
        }
    }

    public Optional<Box3> crushVolume() {
        if (geometry == null) {
            return Optional.empty();
        }
        AxisBox bounds = geometry.bounds();
        if (bounds.interiorVolume() <= 0) {
            return Optional.empty();
        }
        AxisBox interior = new AxisBox(
                bounds.minX() + 1,
                bounds.minY() + 1,
                bounds.minZ() + 1,
                bounds.maxX() - 1,
                bounds.maxY() - 1,
                bounds.maxZ() - 1
        );
        return Optional.of(Box3.fromInterior(interior, 0.25));
    }

    public boolean wouldCrush(Box3 body, double centerX, double centerY, double centerZ) {
        return crushVolume()
                .map(crush -> CrushOccupancy.lethal(stroke, crush, centerX, centerY, centerZ, body))
                .orElse(false);
    }

    public void debugForceRpm(double rpm) {
        lastRpm = rpm;
        if (level != null) {
            for (CellCoord port : geometry == null ? List.<CellCoord>of() : geometry.ports()) {
                if (level.getBlockEntity(WorldStructureSampler.pos(port)) instanceof KineticSource kinetic) {
                    kinetic.setRpm(rpm);
                }
            }
        }
    }

    public void debugSetStroke(PressStrokeState state) {
        this.stroke = state;
        this.pressWorking = state != PressStrokeState.IDLE;
    }

    private double collectRpm() {
        if (level == null || geometry == null) {
            return lastRpm;
        }
        double max = 0.0;
        for (CellCoord port : geometry.ports()) {
            if (level.getBlockEntity(WorldStructureSampler.pos(port)) instanceof KineticSource kinetic) {
                max = Math.max(max, kinetic.rpm());
            }
        }
        return max;
    }

    private double ambientTemperature() {
        if (level == null) {
            return 20.0;
        }
        return level.getBiome(worldPosition).value().getBaseTemperature() * 25.0 + 5.0;
    }

    public boolean insert(ItemStack stack) {
        if (stack.isEmpty() || definition().map(value -> value.kind() == MachineKind.STORAGE).orElse(true)) {
            return false;
        }
        ItemStack existing = items.get(INPUT_SLOT);
        int limit = getMaxStackSize();
        if (existing.isEmpty()) {
            items.set(INPUT_SLOT, stack.split(Math.min(stack.getCount(), limit)));
            setChanged();
            sync();
            return true;
        }
        if (ItemStack.isSameItemSameTags(existing, stack) && existing.getCount() < limit) {
            int move = Math.min(stack.getCount(), limit - existing.getCount());
            existing.grow(move);
            stack.shrink(move);
            setChanged();
            sync();
            return true;
        }
        return false;
    }

    public Optional<MultiblockDefinition> definition() {
        return IndustrialRuntime.shared().machines().get(definitionId);
    }

    public Component status() {
        return Component.literal(debugDump());
    }

    public String debugDump() {
        return "multiblock formed=" + formed
                + " access=" + access
                + " controller=" + worldPosition
                + " def=" + definitionId
                + " reason=" + lastReason
                + " dims=" + (geometry == null ? "-" : geometry.bounds().width() + "x"
                + geometry.bounds().height() + "x" + geometry.bounds().depth())
                + " interior=" + (geometry == null ? 0 : geometry.interiorVolume())
                + " capacity=" + tank.capacity()
                + " stored=" + tank.contents().map(LiquidBatch::volumeMillibuckets).orElse(0)
                + " liquid=" + tank.contents().flatMap(LiquidBatch::baseLiquid)
                + " ports=" + (geometry == null ? 0 : geometry.ports().size())
                + " process=" + definition().flatMap(MultiblockDefinition::processType)
                + " executor=" + definition().map(MultiblockDefinition::kind)
                + " rpm=" + lastRpm
                + " stroke=" + stroke
                + " crush=" + crushVolume();
    }

    private boolean yeastMatches(ProcessRuntime runtime, ItemStack stack) {
        return MinecraftSelectorMatcher.create(runtime.beverages()).matches(
                new com.djden.alcoholic.api.ingredient.IngredientSelector.Tag(
                        com.djden.alcoholic.application.ingredient.SemanticTags.YEAST
                ),
                ItemLots.id(stack)
        );
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

    private static ItemStack stack(ResourceId id, int amount) {
        var item = net.minecraft.core.Registry.ITEM.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path())
        );
        if (item == net.minecraft.world.item.Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, amount);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Definition", definitionId.toString());
        ContainerHelper.saveAllItems(tag, items);
        tank.contents().ifPresent(batch -> LiquidBatchNbt.writeRoot(tag, batch));
        tag.putBoolean("Formed", formed);
        tag.putString("Access", access.name());
        tag.putString("Reason", lastReason);
        tag.putBoolean("YeastPitched", yeastPitched);
        tag.putDouble("VentedCo2", ventedCo2);
        tag.putInt("PressProgress", pressProgress);
        tag.putInt("PressDuration", pressDuration);
        tag.putDouble("StrokeCycle", strokeCycle);
        tag.putString("Stroke", stroke.name());
        tag.putLong("LastProcessedGameTime", lastProcessedGameTime);
        tag.putBoolean("SkipUnloadGap", skipUnloadGap);
        tag.putDouble("LastRpm", lastRpm);
        if (geometry != null) {
            CompoundTag geo = new CompoundTag();
            geo.putInt("MinX", geometry.bounds().minX());
            geo.putInt("MinY", geometry.bounds().minY());
            geo.putInt("MinZ", geometry.bounds().minZ());
            geo.putInt("MaxX", geometry.bounds().maxX());
            geo.putInt("MaxY", geometry.bounds().maxY());
            geo.putInt("MaxZ", geometry.bounds().maxZ());
            geo.putInt("Interior", geometry.interiorVolume());
            geo.putInt("Capacity", geometry.capacityMillibuckets());
            tag.put("Geometry", geo);
        }
        ListTag parts = new ListTag();
        for (BlockPos part : boundParts) {
            parts.add(NbtUtils.writeBlockPos(part));
        }
        tag.put("BoundParts", parts);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        tank.clear();
        LiquidBatchNbt.readRoot(tag).ifPresent(tank::set);
        formed = tag.getBoolean("Formed");
        try {
            access = IndustrialAccess.valueOf(tag.getString("Access"));
        } catch (RuntimeException ignored) {
            access = formed ? IndustrialAccess.OPEN : IndustrialAccess.CLOSED;
        }
        lastReason = tag.getString("Reason");
        yeastPitched = tag.getBoolean("YeastPitched");
        ventedCo2 = tag.getDouble("VentedCo2");
        pressProgress = tag.getInt("PressProgress");
        pressDuration = Math.max(1, tag.getInt("PressDuration"));
        strokeCycle = tag.getDouble("StrokeCycle");
        try {
            stroke = PressStrokeState.valueOf(tag.getString("Stroke"));
        } catch (RuntimeException ignored) {
            stroke = PressStrokeState.IDLE;
        }
        lastProcessedGameTime = tag.getLong("LastProcessedGameTime");
        skipUnloadGap = tag.contains("SkipUnloadGap") ? tag.getBoolean("SkipUnloadGap") : true;
        lastRpm = tag.getDouble("LastRpm");
        if (tag.contains("Geometry")) {
            CompoundTag geo = tag.getCompound("Geometry");
            AxisBox box = new AxisBox(
                    geo.getInt("MinX"),
                    geo.getInt("MinY"),
                    geo.getInt("MinZ"),
                    geo.getInt("MaxX"),
                    geo.getInt("MaxY"),
                    geo.getInt("MaxZ")
            );
            geometry = new MultiblockGeometry(
                    box,
                    geo.getInt("Interior"),
                    geo.getInt("Capacity"),
                    List.of(),
                    WorldStructureSampler.coord(worldPosition)
            );
            tank.tryResize(Math.max(tank.capacity(), geometry.capacityMillibuckets()));
        }
        List<BlockPos> parts = new ArrayList<>();
        tag.getList("BoundParts", 10).forEach(entry -> parts.add(NbtUtils.readBlockPos((CompoundTag) entry)));
        boundParts = List.copyOf(parts);
        structureDirty = true;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public int getContainerSize() {
        return definition().map(value -> value.kind() == MachineKind.STORAGE ? 0 : 2).orElse(2);
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
    public int getMaxStackSize() {
        return 512;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return definition().map(value -> switch (value.kind()) {
            case PRESS -> direction == Direction.DOWN ? new int[]{OUTPUT_SLOT} : new int[]{INPUT_SLOT};
            case FERMENT -> new int[]{INPUT_SLOT};
            case STORAGE -> new int[0];
        }).orElse(new int[]{INPUT_SLOT});
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return access.canFill() && slot == INPUT_SLOT;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return access.canDrain()
                && (slot == OUTPUT_SLOT || definition().map(value -> value.kind() == MachineKind.FERMENT).orElse(false));
    }
}

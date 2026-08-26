package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.machine.MultiblockProfiler;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.mechanical.MechanicalDrivePort;
import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;
import com.djden.alcoholic.domain.multiblock.AxisBox;
import com.djden.alcoholic.domain.multiblock.Box3;
import com.djden.alcoholic.domain.multiblock.CellCoord;
import com.djden.alcoholic.domain.multiblock.CrushOccupancy;
import com.djden.alcoholic.domain.multiblock.HollowCuboidValidator;
import com.djden.alcoholic.domain.multiblock.MultiblockDefinition;
import com.djden.alcoholic.domain.multiblock.MultiblockGeometry;
import com.djden.alcoholic.domain.multiblock.PressStrokeState;
import com.djden.alcoholic.domain.multiblock.ValidationResult;
import com.djden.alcoholic.domain.multiblock.ValidationStatus;
import com.djden.alcoholic.domain.process.ElapsedProcessClock;
import com.djden.alcoholic.minecraft.fluid.LiquidBatchNbt;
import com.djden.alcoholic.minecraft.fluid.LiquidTank;
import com.djden.alcoholic.minecraft.fluid.LiquidVessel;
import com.djden.alcoholic.minecraft.mechanical.MechanicalDrives;
import com.djden.alcoholic.minecraft.menu.MachineAccess;
import com.djden.alcoholic.minecraft.menu.MachineContainerData;
import com.djden.alcoholic.minecraft.menu.MachineLayout;
import com.djden.alcoholic.minecraft.process.ItemLots;
import com.djden.alcoholic.minecraft.process.MinecraftSelectorMatcher;
import com.djden.alcoholic.minecraft.process.ProcessRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
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
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MultiblockControllerBlockEntity extends BlockEntity
        implements WorldlyContainer, LiquidVessel, MachineAccess {
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
    private int processProgress;
    private int processDuration = 1;
    private double processClock;
    private String processJob = "";
    private String processStage = "";
    private ResourceId boundDefinition;
    private double targetTemperature = Double.NaN;
    private int additionsCommitted;
    private final List<ItemStack> committedSolids = new ArrayList<>();

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

    public String lastReason() {
        return lastReason;
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
        if (geometry == null) {
            return false;
        }
        if (!formed && access != IndustrialAccess.DRAIN_ONLY) {
            return false;
        }
        return geometry.bounds().contains(WorldStructureSampler.coord(part));
    }

    public void executePress(MultiblockDefinition definition, long gameTime) {
        IndustrialProcessTicks.press(this, definition, gameTime);
    }

    public void executeFerment(MultiblockDefinition definition, long gameTime) {
        IndustrialProcessTicks.ferment(this, definition, gameTime);
    }

    public void executeMalt(MultiblockDefinition definition, long gameTime) {
        IndustrialProcessTicks.malt(this, definition, gameTime);
    }

    public void executeMill(MultiblockDefinition definition, long gameTime) {
        IndustrialProcessTicks.mill(this, definition, gameTime);
    }

    public void executeMash(MultiblockDefinition definition, long gameTime) {
        IndustrialProcessTicks.mash(this, definition, gameTime);
    }

    public void executeBoil(MultiblockDefinition definition, long gameTime) {
        IndustrialProcessTicks.boil(this, definition, gameTime);
    }

    public void executeCondition(MultiblockDefinition definition, long gameTime) {
        IndustrialProcessTicks.condition(this, definition, gameTime);
    }

    public int processProgress() {
        return processProgress;
    }

    public int processDuration() {
        return processDuration;
    }

    @Override
    public MachineLayout layout() {
        return definition()
                .map(value -> value.hasProcess() ? MachineLayout.TWO_SLOTS_ONE_TANK : MachineLayout.ONE_TANK)
                .orElse(MachineLayout.TWO_SLOTS_ONE_TANK);
    }

    @Override
    public int progress() {
        return processProgress;
    }

    @Override
    public int duration() {
        return Math.max(1, processDuration);
    }

    @Override
    public int temperatureDeci() {
        return MachineAccess.deci(targetTemperature);
    }

    @Override
    public int flags() {
        return formed ? MachineContainerData.FLAG_FORMED : 0;
    }

    public String processStage() {
        return processStage;
    }

    public ResourceId boundDefinition() {
        return boundDefinition;
    }

    public double targetTemperature() {
        return targetTemperature;
    }

    public void debugSetTargetTemperature(double celsius) {
        this.targetTemperature = celsius;
        setChanged();
    }

    public boolean cycleBoundDefinition() {
        Optional<ResourceId> processType = definition().flatMap(MultiblockDefinition::processType);
        if (processType.isEmpty()) {
            return false;
        }
        List<ResourceId> ids = ProcessRuntime.shared().beverages().catalog().processes().values().stream()
                .filter(definition -> processType.get().equals(definition.processType()))
                .map(com.djden.alcoholic.domain.process.ProcessDefinition::id)
                .sorted(java.util.Comparator.comparing(ResourceId::toString))
                .toList();
        if (ids.isEmpty()) {
            return false;
        }
        int index = boundDefinition == null ? -1 : ids.indexOf(boundDefinition);
        boundDefinition = ids.get((index + 1) % ids.size());
        resetProcess();
        setChanged();
        sync();
        return true;
    }

    private boolean fermentMachine() {
        return definition().flatMap(MultiblockDefinition::processType)
                .filter(BuiltinRegistrations.FERMENT::equals)
                .isPresent();
    }

    @Override
    public LiquidTank tank() {
        return tank;
    }

    public void onTankChanged() {
        resetProcess();
        markTankChanged();
    }

    void onProcessTankChanged() {
        markTankChanged();
    }

    private void markTankChanged() {
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
            pauseElapsed(now);
            return;
        }
        definition().ifPresent(definition -> definition.processType()
                .flatMap(IndustrialRuntime.shared()::strategy)
                .ifPresent(strategy -> strategy.tick(this, definition, now)));
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
        if (nextAccess != IndustrialAccess.DRAIN_ONLY) {
            clearPartBindings();
        }
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

    NonNullList<ItemStack> inventory() {
        return items;
    }

    List<ItemStack> committedSolids() {
        return committedSolids;
    }

    void stopPressProcess() {
        pressWorking = false;
        pressProgress = 0;
        strokeCycle = 0.0;
        stroke = PressStrokeState.IDLE;
    }

    void preparePressProcess(int durationTicks) {
        pressWorking = true;
        pressDuration = Math.max(1, durationTicks);
    }

    boolean pressProcessComplete() {
        return pressProgress >= pressDuration;
    }

    boolean advancePressProcess() {
        pressProgress++;
        strokeCycle = pressDuration <= 1 ? 1.0 : (double) pressProgress / pressDuration;
        stroke = PressStrokeState.fromProgress(true, strokeCycle);
        applyCrush();
        return pressProcessComplete();
    }

    boolean shouldSyncPressProcess() {
        return pressProgress % 4 == 0;
    }

    void holdPressProcess() {
        pressWorking = true;
        pressProgress = pressDuration;
        strokeCycle = 1.0;
        stroke = PressStrokeState.fromProgress(true, strokeCycle);
    }

    void completePressProcess() {
        stopPressProcess();
        onProcessTankChanged();
    }

    void ventCo2(double amount) {
        if (Double.isFinite(amount) && amount > 0.0) {
            ventedCo2 += amount;
        }
    }

    void resetProcess() {
        refundCommittedSolids();
        clearProcessState();
    }

    void completeProcess() {
        committedSolids.clear();
        clearProcessState();
    }

    private void clearProcessState() {
        processProgress = 0;
        processDuration = 1;
        processClock = 0.0;
        processJob = "";
        processStage = "";
        additionsCommitted = 0;
        yeastPitched = false;
        skipUnloadGap = true;
    }

    boolean beginProcessJob(String job, int durationTicks) {
        if (!job.equals(processJob)) {
            refundCommittedSolids();
            processJob = job;
            processProgress = 0;
            processClock = 0.0;
            processStage = "";
            additionsCommitted = 0;
            yeastPitched = false;
            skipUnloadGap = true;
        }
        processDuration = Math.max(1, durationTicks);
        return true;
    }

    boolean processComplete() {
        return processProgress >= processDuration;
    }

    void noteRpm(double rpm) {
        lastRpm = rpm;
    }

    boolean catalystConsumed() {
        return yeastPitched;
    }

    void catalystConsumed(boolean value) {
        yeastPitched = value;
    }

    double consumeElapsed(long now) {
        if (skipUnloadGap) {
            lastProcessedGameTime = now;
            skipUnloadGap = false;
            return 0.0;
        }
        double delta = ElapsedProcessClock.deltaTicks(lastProcessedGameTime, now, 200);
        lastProcessedGameTime = now;
        return Math.max(0.0, delta);
    }

    void pauseElapsed(long now) {
        lastProcessedGameTime = now;
        skipUnloadGap = false;
    }

    boolean advanceElapsed(long now, double rate) {
        if (skipUnloadGap) {
            lastProcessedGameTime = now;
            skipUnloadGap = false;
            return false;
        }
        double delta = ElapsedProcessClock.deltaTicks(lastProcessedGameTime, now, 200);
        lastProcessedGameTime = now;
        if (delta <= 0.0 || rate <= 0.0) {
            return false;
        }
        processClock += delta * rate;
        processProgress = Math.min(processDuration, (int) Math.floor(processClock));
        return processClock + 1e-9 >= processDuration;
    }

    boolean advanceTick() {
        processProgress++;
        processClock = processProgress;
        return processProgress >= processDuration;
    }

    void setProcessProgressFraction(double fraction) {
        double normalized = Double.isFinite(fraction) ? Math.max(0.0, Math.min(1.0, fraction)) : 0.0;
        processClock = normalized * processDuration;
        processProgress = Math.min(processDuration, (int) Math.floor(processClock));
    }

    void setProcessStage(String stage) {
        processStage = stage == null ? "" : stage;
    }

    int additionsCommitted() {
        return additionsCommitted;
    }

    void additionsCommitted(int value) {
        additionsCommitted = Math.max(0, value);
    }

    ResourceId selectedDefinition(ResourceId fallback) {
        return boundDefinition == null ? fallback : boundDefinition;
    }

    double resolvedTarget(double fallback) {
        return Double.isFinite(targetTemperature) ? targetTemperature : fallback;
    }

    double heatCelsius() {
        return com.djden.alcoholic.minecraft.environment.HeatSources.celsius(level, worldPosition);
    }

    com.djden.alcoholic.domain.vessel.EnvironmentProfile environment() {
        if (level == null) {
            return com.djden.alcoholic.domain.vessel.EnvironmentProfile.temperateCellar();
        }
        return com.djden.alcoholic.minecraft.environment.EnvironmentSampler.sample(level, worldPosition);
    }

    void consumeWork(MultiblockDefinition definition) {
        consumeMechanicalWork(definition);
    }

    MechanicalDriveState drive(MultiblockDefinition definition) {
        return collectDrive(definition);
    }

    double ambient() {
        return ambientTemperature();
    }

    ItemStack itemStack(ResourceId id, int amount) {
        return stack(id, amount);
    }

    ItemStack copyOf(ItemStack stack, int count) {
        return copyCount(stack, count);
    }

    boolean matchesYeast(ItemStack stack) {
        return yeastMatches(ProcessRuntime.shared(), stack);
    }

    void markProcessDirty(boolean syncNow) {
        setChanged();
        if (syncNow) {
            sync();
        }
    }

    private void refundCommittedSolids() {
        if (committedSolids.isEmpty()) {
            return;
        }
        List<ItemStack> undelivered = new ArrayList<>();
        for (ItemStack committed : committedSolids) {
            if (committed.isEmpty()) {
                continue;
            }
            ItemStack remainder = committed.copy();
            ItemStack input = items.get(INPUT_SLOT);
            if (input.isEmpty()) {
                int moved = Math.min(remainder.getCount(), getMaxStackSize());
                ItemStack restored = remainder.split(moved);
                items.set(INPUT_SLOT, restored);
            } else if (ItemStack.isSameItemSameTags(input, remainder) && input.getCount() < getMaxStackSize()) {
                int moved = Math.min(remainder.getCount(), getMaxStackSize() - input.getCount());
                input.grow(moved);
                remainder.shrink(moved);
            }
            if (!remainder.isEmpty()) {
                if (level != null && !level.isClientSide) {
                    Block.popResource(level, worldPosition, remainder);
                } else {
                    undelivered.add(remainder);
                }
            }
        }
        committedSolids.clear();
        committedSolids.addAll(undelivered);
        setChanged();
    }

    private void consumeMechanicalWork(MultiblockDefinition definition) {
        if (level == null || geometry == null) {
            return;
        }
        double load = definition.kinetic().asMechanical().requiredCapacity();
        if (load <= 0.0) {
            load = 1.0;
        }
        PortDrive winner = winningPortDrive(definition);
        if (winner != null) {
            MechanicalDrives.consumeWork(level, winner.pos(), load, definition.kinetic().asMechanical());
        }
    }

    private MechanicalDriveState collectDrive(MultiblockDefinition definition) {
        if (level == null || geometry == null) {
            return lastRpm > 0.0
                    ? MechanicalDriveState.running(lastRpm, Double.POSITIVE_INFINITY)
                    : MechanicalDriveState.idle();
        }
        PortDrive winner = winningPortDrive(definition);
        return winner == null ? MechanicalDriveState.idle() : winner.state();
    }

    private PortDrive winningPortDrive(MultiblockDefinition definition) {
        PortDrive best = null;
        var requirement = definition.kinetic().asMechanical();
        for (CellCoord port : geometry.ports()) {
            BlockPos pos = WorldStructureSampler.pos(port);
            if (level.getBlockEntity(pos) instanceof MechanicalDrivePort drive) {
                MechanicalDriveState state = drive instanceof KineticPortBlockEntity kinetic
                        ? kinetic.driveState(requirement)
                        : drive.driveState();
                if (best == null || MechanicalDriveState.stronger(best.state(), state, requirement) == state) {
                    best = new PortDrive(pos, state);
                }
            }
        }
        return best;
    }

    private record PortDrive(BlockPos pos, MechanicalDriveState state) {
    }

    private double ambientTemperature() {
        if (level == null) {
            return 20.0;
        }
        return level.getBiome(worldPosition).value().getBaseTemperature() * 25.0 + 5.0;
    }

    public boolean insert(ItemStack stack) {
        if (stack.isEmpty() || definition().map(value -> !value.hasProcess()).orElse(true)) {
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
                + " crush=" + crushVolume()
                + " job=" + processJob
                + " stage=" + processStage
                + " progress=" + processProgress + "/" + processDuration
                + " bound=" + boundDefinition
                + " targetC=" + targetTemperature;
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
        tag.putInt("ProcessProgress", processProgress);
        tag.putInt("ProcessDuration", processDuration);
        tag.putDouble("ProcessClock", processClock);
        tag.putString("ProcessJob", processJob);
        tag.putString("ProcessStage", processStage);
        if (boundDefinition != null) {
            tag.putString("BoundDefinition", boundDefinition.toString());
        }
        if (Double.isFinite(targetTemperature)) {
            tag.putDouble("TargetTemperature", targetTemperature);
        }
        tag.putInt("AdditionsCommitted", additionsCommitted);
        ListTag committed = new ListTag();
        for (ItemStack stack : committedSolids) {
            if (!stack.isEmpty()) {
                committed.add(stack.save(new CompoundTag()));
            }
        }
        tag.put("CommittedSolids", committed);
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
        processProgress = tag.getInt("ProcessProgress");
        processDuration = Math.max(1, tag.getInt("ProcessDuration"));
        processClock = tag.getDouble("ProcessClock");
        processJob = tag.getString("ProcessJob");
        processStage = tag.getString("ProcessStage");
        if (tag.contains("BoundDefinition")) {
            try {
                boundDefinition = ResourceId.parse(tag.getString("BoundDefinition"));
            } catch (RuntimeException ignored) {
                boundDefinition = null;
            }
        }
        targetTemperature = tag.contains("TargetTemperature") ? tag.getDouble("TargetTemperature") : Double.NaN;
        additionsCommitted = tag.getInt("AdditionsCommitted");
        committedSolids.clear();
        if (tag.contains("CommittedSolids", Tag.TAG_LIST)) {
            ListTag committed = tag.getList("CommittedSolids", Tag.TAG_COMPOUND);
            for (int index = 0; index < committed.size(); index++) {
                ItemStack stack = ItemStack.of(committed.getCompound(index));
                if (!stack.isEmpty()) {
                    committedSolids.add(stack);
                }
            }
        } else if (tag.contains("CommittedSolids", Tag.TAG_COMPOUND)) {
            NonNullList<ItemStack> legacy = NonNullList.withSize(4, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(tag.getCompound("CommittedSolids"), legacy);
            legacy.stream().filter(stack -> !stack.isEmpty()).forEach(committedSolids::add);
        }
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
        Optional<LiquidBatch> restored = LiquidBatchNbt.readRoot(tag);
        if (restored.isPresent()) {
            tank.set(restored.get());
        } else if (!tag.contains(LiquidBatchNbt.ROOT_TAG)) {
            tank.clear();
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
        return definition().map(value -> value.hasProcess() ? 2 : 0).orElse(2);
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
        return definition().map(value -> {
            if (!value.hasProcess()) {
                return new int[0];
            }
            if (inputOnlyProcess(value)) {
                return new int[]{INPUT_SLOT};
            }
            return direction == Direction.DOWN ? new int[]{OUTPUT_SLOT} : new int[]{INPUT_SLOT};
        }).orElse(new int[]{INPUT_SLOT});
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return access.canFill() && slot == INPUT_SLOT;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return access.canDrain()
                && (slot == OUTPUT_SLOT || inputOnlyProcess());
    }

    private boolean inputOnlyProcess() {
        return definition().map(this::inputOnlyProcess).orElse(false);
    }

    private boolean inputOnlyProcess(MultiblockDefinition definition) {
        return definition.processType()
                .filter(type -> BuiltinRegistrations.FERMENT.equals(type)
                        || BuiltinRegistrations.CONDITION.equals(type))
                .isPresent();
    }
}

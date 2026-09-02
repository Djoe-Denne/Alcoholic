package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ExecutorModifiers;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.process.ProcessRecipeResolver;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.process.ElapsedProcessClock;
import com.djden.alcoholic.domain.vessel.BarrelHistory;
import com.djden.alcoholic.domain.vessel.EnvironmentProfile;
import com.djden.alcoholic.domain.vessel.VesselProfile;
import com.djden.alcoholic.minecraft.advancement.AdvancementActor;
import com.djden.alcoholic.minecraft.advancement.AdvancementHooks;
import com.djden.alcoholic.minecraft.advancement.ProcessAdvancementState;
import com.djden.alcoholic.minecraft.bottle.Bottling;
import com.djden.alcoholic.minecraft.environment.EnvironmentSampler;
import com.djden.alcoholic.minecraft.fluid.LiquidBatchNbt;
import com.djden.alcoholic.minecraft.fluid.LiquidTank;
import com.djden.alcoholic.minecraft.fluid.LiquidVessel;
import com.djden.alcoholic.minecraft.menu.MachineAccess;
import com.djden.alcoholic.minecraft.menu.MachineLayout;
import com.djden.alcoholic.minecraft.vessel.CaskHistoryTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public final class OakBarrelBlockEntity extends BlockEntity implements LiquidVessel, MachineAccess, AdvancementActor {
    public static final int CAPACITY = 4_000;

    private final LiquidTank tank;
    private final ProcessAdvancementState advancements = new ProcessAdvancementState();
    private final CaskHistoryTracker imprint = new CaskHistoryTracker();
    private EnvironmentProfile environment = EnvironmentProfile.temperateCellar();
    private long lastProcessedGameTime;
    private long lastSampledGameTime;
    private boolean skipUnloadGap = true;
    private boolean environmentInvalid = true;

    public OakBarrelBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
        tank = new LiquidTank(CAPACITY, ProcessRuntime.shared().merges(), ProcessRuntime.shared().aggregators());
    }

    @Override
    public LiquidTank tank() {
        return tank;
    }

    @Override
    public ProcessAdvancementState advancementState() {
        return advancements;
    }

    public BarrelHistory history() {
        return imprint.history();
    }

    public EnvironmentProfile environment() {
        return environment;
    }

    @Override
    public MachineLayout layout() {
        return MachineLayout.ONE_TANK;
    }

    @Override
    public java.util.List<com.djden.alcoholic.api.ResourceId> displayedProcessTypes() {
        return java.util.List.of(BuiltinRegistrations.AGE);
    }

    @Override
    public int temperatureDeci() {
        return MachineAccess.deci(environment.temperature());
    }

    public VesselProfile vesselProfile() {
        return VesselProfile.oakBarrel().withHistory(imprint.history());
    }

    public void invalidateEnvironment() {
        environmentInvalid = true;
    }

    public boolean tryBottle(Player player, ItemStack held) {
        boolean bottled = Bottling.bottle(player, held, tank);
        if (bottled) {
            syncHistory();
            setChanged();
        }
        return bottled;
    }

    public static void serverTick(
            Level level,
            BlockPos position,
            BlockState state,
            OakBarrelBlockEntity entity
    ) {
        entity.tick();
    }

    private void tick() {
        if (level == null || level.isClientSide) {
            return;
        }
        long now = level.getGameTime();
        if (environmentInvalid || now - lastSampledGameTime >= EnvironmentSampler.REFRESH_INTERVAL_TICKS) {
            environment = EnvironmentSampler.sample(level, worldPosition);
            lastSampledGameTime = now;
            environmentInvalid = false;
        }
        syncHistory();
        if (skipUnloadGap) {
            lastProcessedGameTime = now;
            skipUnloadGap = false;
            return;
        }
        if (now % 20 != 0) {
            return;
        }
        Optional<LiquidBatch> contents = tank.contents();
        if (contents.isEmpty() || contents.get().baseLiquid().isEmpty()) {
            lastProcessedGameTime = now;
            return;
        }
        LiquidBatch batch = contents.get();
        ProcessRuntime runtime = ProcessRuntime.shared();
        Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                runtime.beverages().catalog(),
                runtime.beverages().api(),
                BuiltinRegistrations.AGE,
                MinecraftSelectorMatcher.create(runtime.beverages()),
                Optional.empty(),
                batch.baseLiquid()
        );
        if (invocation.isEmpty()) {
            lastProcessedGameTime = now;
            return;
        }
        double delta = ElapsedProcessClock.deltaTicks(lastProcessedGameTime, now);
        lastProcessedGameTime = now;
        if (delta <= 0.0) {
            return;
        }
        ProcessResult result = runtime.engine().execute(
                runtime.ageExecutor(),
                invocation.get(),
                ProcessInputs.ofLiquid("source", batch),
                ProcessContext.of(
                        environment.temperature(),
                        delta,
                        false,
                        Optional.of(vesselProfile()),
                        Optional.of(environment),
                        now,
                        ExecutorModifiers.artisanal()
                )
        );
        if (!result.success() || result.outputs().isEmpty()) {
            return;
        }
        LiquidBatch next = (LiquidBatch) result.outputs().get(0);
        tank.set(next);
        AdvancementHooks.changedIdentity(batch, next)
                .ifPresent(liquid -> AdvancementHooks.processCompleted(
                        this,
                        BuiltinRegistrations.AGE,
                        Optional.of(liquid)
                ));
        setChanged();
        if (now % 40 == 0) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public Component status() {
        Optional<LiquidBatch> contents = tank.contents();
        if (contents.isEmpty()) {
            Component empty = Component.translatable(
                    "message.alcoholic.barrel.empty",
                    history().usageCount(),
                    history().previousContents().isEmpty()
                            ? "-"
                            : history().previousContents().get(history().previousContents().size() - 1).toString()
            );
            return appendImprint(empty);
        }
        LiquidBatch batch = contents.get();
        Component status = Component.translatable(
                "message.alcoholic.barrel.status",
                String.format(java.util.Locale.ROOT, "%.1f", environment.temperature()),
                String.format(java.util.Locale.ROOT, "%.2f", batch.number(ResourceId.parse("alcoholic:maturity"), 0.0)),
                batch.baseLiquid().map(ResourceId::toString).orElse("-")
        );
        return appendImprint(status);
    }

    public String debugDump() {
        StringBuilder builder = new StringBuilder();
        builder.append("vessel=").append(vesselProfile().id())
                .append(" material=").append(vesselProfile().material())
                .append(" history=").append(history().usageCount())
                .append(" previous=").append(history().previousContents())
                .append(" imprint=").append(history().caskImprint())
                .append(" envT=").append(environment.temperature())
                .append(" stability=").append(environment.stability())
                .append(" sheltered=").append(environment.sheltered())
                .append(" lastProcessed=").append(lastProcessedGameTime);
        tank.contents().ifPresent(batch -> builder
                .append(" def=").append(batch.baseLiquid())
                .append(" vol=").append(batch.volume())
                .append(" props=").append(batch.properties().asMap())
                .append(" provenanceOrigins=").append(batch.batchProvenance().originComposition()));
        return builder.toString();
    }

    private void syncHistory() {
        if (imprint.sync(tank.contents(), CAPACITY, CaskHistoryTracker::axesFor)) {
            setChanged();
        }
    }

    private Component appendImprint(Component status) {
        if (history().caskImprint().isEmpty()) {
            return status;
        }
        return Component.empty()
                .append(status)
                .append(" · ")
                .append(Component.translatable("message.alcoholic.barrel.imprint", formatImprint()));
    }

    private String formatImprint() {
        StringBuilder text = new StringBuilder();
        history().caskImprint().forEach((id, amount) -> {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(id).append('=').append(String.format(java.util.Locale.ROOT, "%.2f", amount));
        });
        return text.toString();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tank.contents().ifPresent(batch -> LiquidBatchNbt.writeRoot(tag, batch));
        imprint.save(tag);
        tag.putLong("LastProcessedGameTime", lastProcessedGameTime);
        tag.putLong("LastSampledGameTime", lastSampledGameTime);
        tag.putDouble("EnvTemperature", environment.temperature());
        tag.putDouble("EnvStability", environment.stability());
        tag.putBoolean("EnvSheltered", environment.sheltered());
        advancements.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.clear();
        LiquidBatchNbt.readRoot(tag).ifPresent(tank::set);
        imprint.load(tag, tank.contents(), CaskHistoryTracker::axesFor);
        lastProcessedGameTime = tag.getLong("LastProcessedGameTime");
        lastSampledGameTime = tag.getLong("LastSampledGameTime");
        environment = new EnvironmentProfile(
                tag.contains("EnvTemperature") ? tag.getDouble("EnvTemperature") : 14.0,
                tag.contains("EnvStability") ? tag.getDouble("EnvStability") : 0.5,
                tag.getBoolean("EnvSheltered")
        );
        advancements.load(tag);
        skipUnloadGap = true;
        environmentInvalid = true;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

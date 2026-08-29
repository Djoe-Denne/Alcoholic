package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.api.process.ExecutorModifiers;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.process.ProcessRecipeResolver;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.minecraft.advancement.AdvancementActor;
import com.djden.alcoholic.minecraft.advancement.AdvancementHooks;
import com.djden.alcoholic.minecraft.advancement.ProcessAdvancementState;
import com.djden.alcoholic.minecraft.bottle.Bottling;
import com.djden.alcoholic.minecraft.fluid.LiquidBatchNbt;
import com.djden.alcoholic.minecraft.fluid.LiquidTank;
import com.djden.alcoholic.minecraft.fluid.LiquidVessel;
import com.djden.alcoholic.minecraft.menu.MachineAccess;
import com.djden.alcoholic.minecraft.menu.MachineLayout;
import com.djden.alcoholic.minecraft.menu.MachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ArtisanalBlendingCrockBlockEntity extends BlockEntity
        implements Container, LiquidVessel, MachineAccess, AdvancementActor {
    public static final int CAPACITY = 4_000;

    private final NonNullList<ItemStack> items = NonNullList.withSize(0, ItemStack.EMPTY);
    private final LiquidTank first;
    private final LiquidTank second;
    private final ProcessAdvancementState advancements = new ProcessAdvancementState();
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            playLidSound(SoundEvents.BARREL_OPEN);
            setOpen(state, true);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            playLidSound(SoundEvents.BARREL_CLOSE);
            setOpen(state, false);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int previous, int current) {
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof MachineMenu menu && menu.uses(ArtisanalBlendingCrockBlockEntity.this);
        }
    };

    public ArtisanalBlendingCrockBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
        ProcessRuntime runtime = ProcessRuntime.shared();
        first = new LiquidTank(CAPACITY, runtime.merges(), runtime.aggregators());
        second = new LiquidTank(CAPACITY, runtime.merges(), runtime.aggregators());
    }

    @Override
    public LiquidTank tank() {
        return first;
    }

    @Override
    public ProcessAdvancementState advancementState() {
        return advancements;
    }

    @Override
    public int tankCount() {
        return 2;
    }

    @Override
    public LiquidTank tank(int index) {
        return index == 1 ? second : first;
    }

    @Override
    public boolean canFillTank(int index) {
        return index == 0 || index == 1;
    }

    @Override
    public boolean canDrainTank(int index) {
        return index == 0 || index == 1;
    }

    @Override
    public MachineLayout layout() {
        return MachineLayout.TWO_TANKS;
    }

    @Override
    public java.util.List<com.djden.alcoholic.api.ResourceId> displayedProcessTypes() {
        return java.util.List.of(BuiltinRegistrations.BLEND);
    }

    public static void serverTick(
            Level level,
            BlockPos position,
            BlockState state,
            ArtisanalBlendingCrockBlockEntity entity
    ) {
        entity.openersCounter.recheckOpeners(level, position, state);
    }

    public boolean tryBottle(Player player, ItemStack held) {
        LiquidTank source = first.contents().isPresent() ? first : second;
        boolean bottled = Bottling.bottle(player, held, source);
        if (bottled) {
            setChanged();
        }
        return bottled;
    }

    public Component blend() {
        Optional<LiquidBatch> left = first.contents();
        Optional<LiquidBatch> right = second.contents();
        if (left.isEmpty() || right.isEmpty()) {
            return Component.translatable("message.alcoholic.crock.need_two");
        }
        ProcessRuntime runtime = ProcessRuntime.shared();
        Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                runtime.beverages().catalog(),
                runtime.beverages().api(),
                BuiltinRegistrations.BLEND,
                MinecraftSelectorMatcher.create(runtime.beverages()),
                Optional.empty(),
                left.get().baseLiquid()
        );
        if (invocation.isEmpty()) {
            return Component.translatable("message.alcoholic.crock.no_recipe");
        }
        Map<String, com.djden.alcoholic.api.liquid.LiquidBatchView> liquids = new LinkedHashMap<>();
        liquids.put("a", left.get());
        liquids.put("b", right.get());
        ProcessResult result = runtime.engine().execute(
                runtime.blendExecutor(),
                invocation.get(),
                new ProcessInputs(Map.of(), liquids),
                ProcessContext.of(20.0, 1.0, false, ExecutorModifiers.artisanal())
        );
        if (!result.success() || result.outputs().isEmpty()) {
            return Component.translatable("message.alcoholic.crock.rejected", result.message());
        }
        LiquidBatch blended = (LiquidBatch) result.outputs().get(0);
        first.set(blended);
        second.clear();
        AdvancementHooks.processCompleted(this, BuiltinRegistrations.BLEND, blended.baseLiquid());
        setChanged();
        return Component.translatable("message.alcoholic.crock.blended");
    }

    public Component status() {
        return Component.translatable(
                "message.alcoholic.crock.status",
                first.contents().map(batch -> batch.volumeMillibuckets()).orElse(0),
                second.contents().map(batch -> batch.volumeMillibuckets()).orElse(0)
        );
    }

    public String debugDump() {
        return "crock a=" + first.contents() + " b=" + second.contents();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        first.contents().ifPresent(batch -> tag.put("First", LiquidBatchNbt.toTag(batch)));
        second.contents().ifPresent(batch -> tag.put("Second", LiquidBatchNbt.toTag(batch)));
        advancements.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        restoreTank(first, tag, "First");
        restoreTank(second, tag, "Second");
        advancements.load(tag);
    }

    private static void restoreTank(LiquidTank tank, CompoundTag tag, String key) {
        if (!tag.contains(key)) {
            tank.clear();
            return;
        }
        LiquidBatchNbt.fromTag(tag.getCompound(key)).ifPresent(tank::set);
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
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
    }

    @Override
    public boolean stillValid(Player player) {
        return MachineAccess.super.stillValid(player);
    }

    @Override
    public void startOpen(Player player) {
        if (!remove && !player.isSpectator() && getLevel() != null) {
            openersCounter.incrementOpeners(player, getLevel(), getBlockPos(), getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!remove && !player.isSpectator() && getLevel() != null) {
            openersCounter.decrementOpeners(player, getLevel(), getBlockPos(), getBlockState());
        }
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    private void setOpen(BlockState state, boolean open) {
        if (level != null && state.hasProperty(ArtisanalBlendingCrockBlock.OPEN)
                && state.getValue(ArtisanalBlendingCrockBlock.OPEN) != open) {
            level.setBlock(worldPosition, state.setValue(ArtisanalBlendingCrockBlock.OPEN, open), Block.UPDATE_ALL);
        }
    }

    private void playLidSound(SoundEvent sound) {
        if (level == null) {
            return;
        }
        level.playSound(
                null,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5,
                sound,
                SoundSource.BLOCKS,
                0.5F,
                level.random.nextFloat() * 0.1F + 0.9F
        );
    }
}

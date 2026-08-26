package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.process.ProcessRecipeResolver;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.minecraft.bottle.Bottling;
import com.djden.alcoholic.minecraft.fluid.LiquidBatchNbt;
import com.djden.alcoholic.minecraft.fluid.LiquidTank;
import com.djden.alcoholic.minecraft.fluid.LiquidVessel;
import com.djden.alcoholic.minecraft.menu.MachineAccess;
import com.djden.alcoholic.minecraft.menu.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ArtisanalBlendingCrockBlockEntity extends BlockEntity implements LiquidVessel, MachineAccess {
    public static final int CAPACITY = 4_000;

    private final LiquidTank first;
    private final LiquidTank second;

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
                ProcessContext.empty()
        );
        if (!result.success() || result.outputs().isEmpty()) {
            return Component.translatable("message.alcoholic.crock.rejected", result.message());
        }
        first.set((LiquidBatch) result.outputs().get(0));
        second.clear();
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
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        restoreTank(first, tag, "First");
        restoreTank(second, tag, "Second");
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
}

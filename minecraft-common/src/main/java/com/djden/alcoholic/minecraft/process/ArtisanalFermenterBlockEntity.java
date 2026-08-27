package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.process.FermentConfig;
import com.djden.alcoholic.application.process.ProcessRecipeResolver;
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

import java.util.Optional;

public final class ArtisanalFermenterBlockEntity extends BlockEntity implements WorldlyContainer, LiquidVessel, MachineAccess {
    public static final int YEAST_SLOT = 0;
    public static final int CAPACITY = 8_000;

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final LiquidTank tank;
    private boolean yeastPitched;
    private double ventedCo2;

    public ArtisanalFermenterBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
        tank = new LiquidTank(CAPACITY, ProcessRuntime.shared().merges(), ProcessRuntime.shared().aggregators());
    }

    @Override
    public LiquidTank tank() {
        return tank;
    }

    public boolean yeastPitched() {
        return yeastPitched;
    }

    public double temperatureCelsius() {
        if (level == null) {
            return 20.0;
        }
        float biome = level.getBiome(worldPosition).value().getBaseTemperature();
        return biome * 25.0 + 5.0;
    }

    @Override
    public MachineLayout layout() {
        return MachineLayout.ONE_SLOT_ONE_TANK;
    }

    @Override
    public java.util.List<com.djden.alcoholic.api.ResourceId> displayedProcessTypes() {
        return java.util.List.of(BuiltinRegistrations.FERMENT);
    }

    @Override
    public int temperatureDeci() {
        return MachineAccess.deci(temperatureCelsius());
    }

    @Override
    public int extra() {
        return yeastPitched ? 1 : 0;
    }

    public static void serverTick(
            Level level,
            BlockPos position,
            BlockState state,
            ArtisanalFermenterBlockEntity entity
    ) {
        entity.tick();
    }

    private void tick() {
        Optional<LiquidBatch> contents = tank.contents();
        if (contents.isEmpty()) {
            yeastPitched = false;
            return;
        }
        LiquidBatch batch = contents.get();
        if (batch.baseLiquid().isEmpty()) {
            return;
        }
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
            ItemStack yeast = items.get(YEAST_SLOT);
            boolean yeastItem = !yeast.isEmpty() && yeastMatches(runtime, yeast);
            if (!yeastItem) {
                return;
            }
            yeast.shrink(1);
            yeastPitched = true;
        }
        ProcessResult result = runtime.engine().execute(
                runtime.fermentExecutor(),
                invocation.get(),
                ProcessInputs.ofLiquid("must", batch),
                ProcessContext.of(temperatureCelsius(), 1.0, yeastPitched || !config.requireYeast())
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
        setChanged();
        if (level != null && level.getGameTime() % 20 == 0) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public String debugDump() {
        return "fermenter yeast=" + yeastPitched
                + " co2=" + ventedCo2
                + " def=" + tank.contents().flatMap(LiquidBatch::baseLiquid)
                + " vol=" + tank.contents().map(LiquidBatch::volume).orElse(0.0)
                + " props=" + tank.contents().map(batch -> batch.properties().asMap()).orElse(java.util.Map.of());
    }

    public Component status() {
        Optional<LiquidBatch> contents = tank.contents();
        if (contents.isEmpty()) {
            return Component.translatable("message.alcoholic.fermenter.empty");
        }
        LiquidBatch batch = contents.get();
        return Component.translatable(
                "message.alcoholic.fermenter.status",
                String.format(java.util.Locale.ROOT, "%.1f", temperatureCelsius()),
                String.format(java.util.Locale.ROOT, "%.2f", batch.number(
                        com.djden.alcoholic.api.ResourceId.parse("alcoholic:sugar"),
                        0.0
                )),
                String.format(java.util.Locale.ROOT, "%.2f", batch.number(
                        com.djden.alcoholic.api.ResourceId.parse("alcoholic:ethanol"),
                        0.0
                )),
                yeastPitched
        );
    }

    public boolean insertYeast(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ItemStack existing = items.get(YEAST_SLOT);
        if (existing.isEmpty()) {
            items.set(YEAST_SLOT, stack.split(1));
            setChanged();
            return true;
        }
        if (ItemStack.isSameItemSameTags(existing, stack) && existing.getCount() < existing.getMaxStackSize()) {
            existing.grow(1);
            stack.shrink(1);
            setChanged();
            return true;
        }
        return false;
    }

    private boolean yeastMatches(ProcessRuntime runtime, ItemStack stack) {
        return MinecraftSelectorMatcher.create(runtime.beverages()).matches(
                new com.djden.alcoholic.api.ingredient.IngredientSelector.Tag(
                        com.djden.alcoholic.application.ingredient.SemanticTags.YEAST
                ),
                ItemLots.id(stack)
        );
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putBoolean("YeastPitched", yeastPitched);
        tag.putDouble("VentedCo2", ventedCo2);
        tank.contents().ifPresent(batch -> LiquidBatchNbt.writeRoot(tag, batch));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        yeastPitched = tag.getBoolean("YeastPitched");
        ventedCo2 = tag.getDouble("VentedCo2");
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

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(items, slot, amount);
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
        return new int[]{YEAST_SLOT};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return true;
    }
}

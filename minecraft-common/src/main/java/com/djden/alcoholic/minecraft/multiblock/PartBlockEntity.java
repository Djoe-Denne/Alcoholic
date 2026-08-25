package com.djden.alcoholic.minecraft.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class PartBlockEntity extends BlockEntity implements ControllerBound {
    private BlockPos controllerPos;

    protected PartBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
    }

    @Override
    public BlockPos cachedController() {
        return controllerPos;
    }

    @Override
    public void bindController(BlockPos controller) {
        this.controllerPos = controller.immutable();
        setChanged();
    }

    @Override
    public void clearController() {
        this.controllerPos = null;
        setChanged();
    }

    @Override
    public MultiblockControllerBlockEntity controller() {
        if (level == null || controllerPos == null || !level.hasChunkAt(controllerPos)) {
            return null;
        }
        if (level.getBlockEntity(controllerPos) instanceof MultiblockControllerBlockEntity controller
                && controller.owns(worldPosition)) {
            return controller;
        }
        controllerPos = null;
        return null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (controllerPos != null) {
            tag.put("Controller", NbtUtils.writeBlockPos(controllerPos));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        controllerPos = tag.contains("Controller") ? NbtUtils.readBlockPos(tag.getCompound("Controller")) : null;
    }
}

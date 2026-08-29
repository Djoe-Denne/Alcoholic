package com.djden.alcoholic.minecraft.inspect;

import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.minecraft.bottle.BottleSnapshotNbt;
import com.djden.alcoholic.minecraft.fluid.LiquidTank;
import com.djden.alcoholic.minecraft.fluid.LiquidVessel;
import com.djden.alcoholic.minecraft.process.ArtisanalBlendingCrockBlockEntity;
import com.djden.alcoholic.minecraft.process.ArtisanalFermenterBlockEntity;
import com.djden.alcoholic.minecraft.process.ArtisanalPressBlockEntity;
import com.djden.alcoholic.minecraft.process.BrewingKettleBlockEntity;
import com.djden.alcoholic.minecraft.mechanical.ElectricMotorBlockEntity;
import com.djden.alcoholic.minecraft.mechanical.PrimitiveCombustionEngineBlockEntity;
import com.djden.alcoholic.minecraft.process.MaltMillBlockEntity;
import com.djden.alcoholic.minecraft.process.MaltingFloorBlockEntity;
import com.djden.alcoholic.minecraft.process.MashTunBlockEntity;
import com.djden.alcoholic.minecraft.process.OakBarrelBlockEntity;
import com.djden.alcoholic.domain.mechanical.MechanicalDrivePort;
import com.djden.alcoholic.minecraft.multiblock.ControllerBound;
import com.djden.alcoholic.minecraft.multiblock.MultiblockControllerBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

/**
 * Formats a concise debug dump of a targeted block or held item.
 */
public final class AlcoholicInspect {
    private AlcoholicInspect() {
    }

    public static Optional<String> inspectBlock(BlockEntity entity) {
        if (entity instanceof OakBarrelBlockEntity barrel) {
            return Optional.of(barrel.debugDump());
        }
        if (entity instanceof ArtisanalBlendingCrockBlockEntity crock) {
            return Optional.of(crock.debugDump());
        }
        if (entity instanceof ArtisanalFermenterBlockEntity fermenter) {
            return Optional.of(fermenter.debugDump());
        }
        if (entity instanceof ArtisanalPressBlockEntity press) {
            return Optional.of(press.debugDump());
        }
        if (entity instanceof MaltingFloorBlockEntity malting) {
            return Optional.of(malting.debugDump());
        }
        if (entity instanceof MaltMillBlockEntity mill) {
            return Optional.of(mill.debugDump());
        }
        if (entity instanceof PrimitiveCombustionEngineBlockEntity engine) {
            return Optional.of(engine.debugDump());
        }
        if (entity instanceof ElectricMotorBlockEntity motor) {
            return Optional.of(motor.debugDump());
        }
        if (entity instanceof MashTunBlockEntity mash) {
            return Optional.of(mash.debugDump());
        }
        if (entity instanceof BrewingKettleBlockEntity kettle) {
            return Optional.of(kettle.debugDump());
        }
        if (entity instanceof MultiblockControllerBlockEntity controller) {
            return Optional.of(controller.debugDump());
        }
        if (entity instanceof ControllerBound bound && bound.controller() != null) {
            String extra = entity instanceof MechanicalDrivePort drive
                    ? " drive=" + drive.driveState().speed() + "/" + drive.driveState().availableCapacity()
                    : "";
            return Optional.of(bound.controller().debugDump() + extra);
        }
        if (entity instanceof LiquidVessel vessel) {
            return Optional.of(vesselDump(vessel));
        }
        return Optional.empty();
    }

    public static Optional<String> inspectItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        Optional<CompoundTag> snapshot = BottleSnapshotNbt.read(stack);
        if (snapshot.isPresent()) {
            CompoundTag tag = snapshot.orElseThrow();
            return Optional.of(
                    "bottle def=" + BottleSnapshotNbt.definition(tag)
                            + " ethanol=" + BottleSnapshotNbt.number(tag, "Ethanol")
                            + " sugar=" + BottleSnapshotNbt.number(tag, "Sugar")
                            + " acidity=" + BottleSnapshotNbt.number(tag, "Acidity")
                            + " maturity=" + BottleSnapshotNbt.number(tag, "Maturity")
                            + " quality=" + BottleSnapshotNbt.number(tag, "Quality")
                            + " purity=" + BottleSnapshotNbt.number(tag, "Purity")
                            + " complexity=" + BottleSnapshotNbt.number(tag, "Complexity")
                            + " origins=" + BottleSnapshotNbt.origins(tag)
            );
        }
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("AlcoholicSolidProperties")) {
            return Optional.of("solid-props=" + tag.getCompound("AlcoholicSolidProperties"));
        }
        if (tag != null && tag.contains("AlcoholicLiquid")) {
            return Optional.of("item-liquid=" + tag.getCompound("AlcoholicLiquid"));
        }
        return Optional.empty();
    }

    private static String vesselDump(LiquidVessel vessel) {
        StringBuilder builder = new StringBuilder("vessel tanks=").append(vessel.tankCount());
        for (int index = 0; index < vessel.tankCount(); index++) {
            LiquidTank tank = vessel.tank(index);
            builder.append(" [").append(index).append("]");
            Optional<LiquidBatch> contents = tank.contents();
            if (contents.isEmpty()) {
                builder.append(" empty");
                continue;
            }
            LiquidBatch batch = contents.get();
            builder.append(" def=").append(batch.baseLiquid())
                    .append(" vol=").append(batch.volume())
                    .append(" props=").append(batch.properties().asMap());
        }
        return builder.toString();
    }
}

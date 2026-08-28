package com.djden.alcoholic.minecraft.debug;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.mechanical.ElectricMotorBlockEntity;
import com.djden.alcoholic.minecraft.mechanical.PrimitiveCombustionEngineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;
import java.util.Objects;

/**
 * Debug layouts that show the fluid-port facing mismatch and the missing FE
 * antenna / shaft-height problems. Two rows per command: broken then aligned.
 */
public final class PortAuditPlacer {
    public static final int FLUID_WIDTH = 20;
    public static final int FLUID_DEPTH = 10;
    public static final int ENERGY_WIDTH = 14;
    public static final int ENERGY_DEPTH = 9;

    private PortAuditPlacer() {
    }

    public static List<Slot> fluidBroken() {
        return List.of(
                Slot.machine("mash_tun", AlcoholicIds.MASH_TUN, 2, 0, 2, Direction.NORTH, Heat.MAGMA),
                Slot.machine("brewing_kettle", AlcoholicIds.BREWING_KETTLE, 5, 0, 2, Direction.NORTH, Heat.CAMPFIRE),
                Slot.machine("fermenter", AlcoholicIds.ARTISANAL_FERMENTER, 8, 0, 2, Direction.NORTH, Heat.NONE),
                Slot.machine("press", AlcoholicIds.ARTISANAL_PRESS, 11, 0, 2, Direction.NORTH, Heat.NONE),
                Slot.machine("barrel", AlcoholicIds.OAK_BARREL, 14, 0, 2, Direction.NORTH, Heat.NONE),
                Slot.machine("crock", AlcoholicIds.ARTISANAL_BLENDING_CROCK, 17, 0, 2, Direction.NORTH, Heat.NONE)
        );
    }

    public static List<Slot> fluidAligned() {
        return List.of(
                Slot.machine("mash_tun", AlcoholicIds.MASH_TUN, 2, 0, 7, Direction.NORTH, Heat.MAGMA),
                Slot.machine("brewing_kettle", AlcoholicIds.BREWING_KETTLE, 3, 0, 7, Direction.WEST, Heat.CAMPFIRE),
                Slot.machine("brewing_kettle", AlcoholicIds.BREWING_KETTLE, 6, 0, 7, Direction.NORTH, Heat.CAMPFIRE),
                Slot.machine("fermenter", AlcoholicIds.ARTISANAL_FERMENTER, 6, 0, 6, Direction.EAST, Heat.NONE),
                Slot.machine("press", AlcoholicIds.ARTISANAL_PRESS, 8, 0, 7, Direction.NORTH, Heat.NONE),
                Slot.machine("fermenter", AlcoholicIds.ARTISANAL_FERMENTER, 8, 0, 6, Direction.EAST, Heat.NONE),
                Slot.machine("fermenter", AlcoholicIds.ARTISANAL_FERMENTER, 11, 0, 7, Direction.NORTH, Heat.NONE),
                Slot.machine("barrel", AlcoholicIds.OAK_BARREL, 12, 0, 7, Direction.WEST, Heat.NONE),
                Slot.machine("fermenter", AlcoholicIds.ARTISANAL_FERMENTER, 15, 0, 7, Direction.NORTH, Heat.NONE),
                Slot.machine("crock", AlcoholicIds.ARTISANAL_BLENDING_CROCK, 16, 0, 7, Direction.WEST, Heat.NONE)
        );
    }

    public static List<Slot> energyBroken() {
        return List.of(
                Slot.machine("malt_mill", AlcoholicIds.MALT_MILL, 2, 0, 2, Direction.NORTH, Heat.NONE),
                Slot.machine("electric_motor", AlcoholicIds.ELECTRIC_MOTOR, 4, 0, 2, Direction.NORTH, Heat.NONE),
                Slot.machine("engine", AlcoholicIds.PRIMITIVE_COMBUSTION_ENGINE, 7, 0, 2, Direction.NORTH, Heat.NONE),
                Slot.machine("malt_mill", AlcoholicIds.MALT_MILL, 7, 0, 1, Direction.NORTH, Heat.NONE)
        );
    }

    public static List<Slot> energyAligned() {
        return List.of(
                Slot.machine("malt_mill", AlcoholicIds.MALT_MILL, 2, 0, 6, Direction.NORTH, Heat.NONE),
                Slot.machine("electric_motor", AlcoholicIds.ELECTRIC_MOTOR, 3, 0, 6, Direction.WEST, Heat.NONE),
                Slot.machine("engine", AlcoholicIds.PRIMITIVE_COMBUSTION_ENGINE, 6, 0, 6, Direction.NORTH, Heat.NONE),
                Slot.machine("malt_mill", AlcoholicIds.MALT_MILL, 7, 0, 6, Direction.NORTH, Heat.NONE),
                Slot.machine("electric_motor", AlcoholicIds.ELECTRIC_MOTOR, 11, 0, 6, Direction.NORTH, Heat.NONE)
        );
    }

    public static int placeFluid(Level level, BlockPos origin) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(origin, "origin");
        pave(level, origin, FLUID_WIDTH, FLUID_DEPTH);
        placeSign(level, origin.offset(0, 0, 2), 4, "FLUIDE CASSE", "tous NORTH", "robinets", "pas en face");
        placeSign(level, origin.offset(0, 0, 7), 4, "FLUIDE OK", "ports face", "orients !=", "crock Y != ");
        int count = 0;
        for (Slot slot : fluidBroken()) {
            placeSlot(level, origin, slot);
            count++;
        }
        for (Slot slot : fluidAligned()) {
            placeSlot(level, origin, slot);
            count++;
        }
        return count;
    }

    public static int placeEnergy(Level level, BlockPos origin) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(origin, "origin");
        pave(level, origin, ENERGY_WIDTH, ENERGY_DEPTH);
        placeSign(level, origin.offset(0, 0, 2), 4, "CINE CASSE", "grille avant", "pas de cine", "seul +X");
        placeSign(level, origin.offset(0, 0, 6), 4, "CINE OK", "arbre droite", "moulin +X", "");
        placeSign(level, origin.offset(11, 0, 4), 0, "MOTEUR FE", "0 antenne", "fil IE ici", "");
        int count = 0;
        for (Slot slot : energyBroken()) {
            placeSlot(level, origin, slot);
            count++;
        }
        for (Slot slot : energyAligned()) {
            placeSlot(level, origin, slot);
            count++;
        }
        return count;
    }

    private static void placeSlot(Level level, BlockPos origin, Slot slot) {
        BlockPos pos = origin.offset(slot.dx(), slot.dy(), slot.dz());
        placeHeat(level, pos.below(), slot.heat());
        Block block = requireBlock(slot.blockId());
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, slot.facing());
        }
        level.setBlock(pos, state, Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof ElectricMotorBlockEntity motor) {
            motor.energy().setStored(motor.energy().capacity());
            motor.setChanged();
        }
        if (level.getBlockEntity(pos) instanceof PrimitiveCombustionEngineBlockEntity engine) {
            engine.insertFuel(new ItemStack(Items.COAL, 8));
        }
    }

    private static void placeHeat(Level level, BlockPos pos, Heat heat) {
        if (!level.isInWorldBounds(pos)) {
            return;
        }
        if (heat == Heat.MAGMA) {
            level.setBlock(pos, Blocks.MAGMA_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        if (heat == Heat.CAMPFIRE) {
            level.setBlock(
                    pos,
                    Blocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.LIT, true),
                    Block.UPDATE_ALL
            );
            return;
        }
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, Blocks.SMOOTH_STONE.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static void pave(Level level, BlockPos origin, int width, int depth) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                BlockPos floor = origin.offset(x, -1, z);
                if (level.isInWorldBounds(floor) && level.getBlockState(floor).isAir()) {
                    level.setBlock(floor, Blocks.SMOOTH_STONE.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private static void placeSign(Level level, BlockPos pos, int rotation, String a, String b, String c, String d) {
        if (!level.isInWorldBounds(pos)) {
            return;
        }
        if (level.getBlockState(pos.below()).isAir()) {
            level.setBlock(pos.below(), Blocks.SMOOTH_STONE.defaultBlockState(), Block.UPDATE_ALL);
        }
        level.setBlock(
                pos,
                Blocks.OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, rotation),
                Block.UPDATE_ALL
        );
        if (level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
            sign.setMessage(0, Component.literal(a));
            sign.setMessage(1, Component.literal(b));
            sign.setMessage(2, Component.literal(c));
            sign.setMessage(3, Component.literal(d));
            sign.setChanged();
        }
    }

    private static Block requireBlock(ResourceId id) {
        Block block = Registry.BLOCK.get(ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path()));
        if (block == Blocks.AIR) {
            throw new IllegalStateException("Missing block " + id.namespace() + ":" + id.path());
        }
        return block;
    }

    public enum Heat {
        NONE,
        MAGMA,
        CAMPFIRE
    }

    public record Slot(String id, ResourceId blockId, int dx, int dy, int dz, Direction facing, Heat heat) {
        static Slot machine(String id, ResourceId blockId, int dx, int dy, int dz, Direction facing, Heat heat) {
            return new Slot(id, blockId, dx, dy, dz, facing, heat);
        }
    }
}

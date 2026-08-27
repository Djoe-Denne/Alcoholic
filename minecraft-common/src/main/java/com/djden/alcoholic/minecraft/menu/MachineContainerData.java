package com.djden.alcoholic.minecraft.menu;

import net.minecraft.world.inventory.ContainerData;

public final class MachineContainerData implements ContainerData {
    public static final int SIZE = 15;
    public static final int PROGRESS = 0;
    public static final int DURATION = 1;
    public static final int TEMP_DECI = 2;
    public static final int EXTRA = 3;
    public static final int EXTRA2 = 4;
    public static final int TANK0_VOLUME = 5;
    public static final int TANK0_CAPACITY = 6;
    public static final int TANK0_FLUID = 7;
    public static final int TANK1_VOLUME = 8;
    public static final int TANK1_CAPACITY = 9;
    public static final int TANK1_FLUID = 10;
    public static final int FLAGS = 11;
    public static final int BE_X = 12;
    public static final int BE_Y = 13;
    public static final int BE_Z = 14;
    public static final int FLAG_FORMED = 1;

    private final MachineAccess access;

    public MachineContainerData(MachineAccess access) {
        this.access = access;
    }

    @Override
    public int get(int index) {
        return switch (index) {
            case PROGRESS -> access.progress();
            case DURATION -> Math.max(1, access.duration());
            case TEMP_DECI -> access.temperatureDeci();
            case EXTRA -> access.extra();
            case EXTRA2 -> access.extra2();
            case TANK0_VOLUME -> access.tankVolume(0);
            case TANK0_CAPACITY -> access.tankCapacity(0);
            case TANK0_FLUID -> access.tankFluidId(0);
            case TANK1_VOLUME -> access.tankVolume(1);
            case TANK1_CAPACITY -> access.tankCapacity(1);
            case TANK1_FLUID -> access.tankFluidId(1);
            case FLAGS -> access.flags();
            case BE_X -> access.blockX();
            case BE_Y -> access.blockY();
            case BE_Z -> access.blockZ();
            default -> 0;
        };
    }

    @Override
    public void set(int index, int value) {
    }

    @Override
    public int getCount() {
        return SIZE;
    }
}

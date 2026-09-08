package com.djden.alcoholic.minecraft.menu;

import net.minecraft.world.inventory.ContainerData;

public final class MachineContainerData implements ContainerData {
    public static final int SIZE = 31;
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
    public static final int AMBIENT_TEMP_DECI = 15;
    public static final int HEAT_TEMP_DECI = 16;
    public static final int HUMIDITY_PERMILLE = 17;
    public static final int PREFERRED_TEMP_MIN_DECI = 18;
    public static final int PREFERRED_TEMP_MAX_DECI = 19;
    public static final int OPERATING_TEMP_MIN_DECI = 20;
    public static final int OPERATING_TEMP_MAX_DECI = 21;
    public static final int REQUIRED_HUMIDITY_PERMILLE = 22;
    public static final int REQUIRED_HEAT_TEMP_DECI = 23;
    public static final int DRIVE_SPEED_DECI = 24;
    public static final int DRIVE_MIN_DECI = 25;
    public static final int DRIVE_MAX_DECI = 26;
    public static final int DRIVE_CAPACITY_DECI = 27;
    public static final int REQUIRED_DRIVE_CAPACITY_DECI = 28;
    public static final int PROCESS_STAGE = 29;
    public static final int PROCESS_DEFINITION = 30;
    public static final int FLAG_FORMED = 1;
    public static final int FLAG_CONTROLLER_TELEMETRY = 1 << 1;
    public static final int FLAG_PROCESS_USES_HEAT = 1 << 2;
    public static final int FLAG_PROCESS_USES_AMBIENT = 1 << 3;
    public static final int FLAG_HUMIDITY_REQUIRED = 1 << 4;
    public static final int FLAG_DRIVE_REQUIRED = 1 << 5;
    public static final int FLAG_KILN_HEAT_REQUIRED = 1 << 6;

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
            case AMBIENT_TEMP_DECI -> access.ambientTemperatureDeci();
            case HEAT_TEMP_DECI -> access.heatTemperatureDeci();
            case HUMIDITY_PERMILLE -> access.humidityPermille();
            case PREFERRED_TEMP_MIN_DECI -> access.preferredTemperatureMinDeci();
            case PREFERRED_TEMP_MAX_DECI -> access.preferredTemperatureMaxDeci();
            case OPERATING_TEMP_MIN_DECI -> access.operatingTemperatureMinDeci();
            case OPERATING_TEMP_MAX_DECI -> access.operatingTemperatureMaxDeci();
            case REQUIRED_HUMIDITY_PERMILLE -> access.requiredHumidityPermille();
            case REQUIRED_HEAT_TEMP_DECI -> access.requiredHeatTemperatureDeci();
            case DRIVE_SPEED_DECI -> access.driveSpeedDeci();
            case DRIVE_MIN_DECI -> access.driveMinimumDeci();
            case DRIVE_MAX_DECI -> access.driveMaximumDeci();
            case DRIVE_CAPACITY_DECI -> access.driveCapacityDeci();
            case REQUIRED_DRIVE_CAPACITY_DECI -> access.requiredDriveCapacityDeci();
            case PROCESS_STAGE -> access.processStageCode();
            case PROCESS_DEFINITION -> access.processDefinitionIndex();
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

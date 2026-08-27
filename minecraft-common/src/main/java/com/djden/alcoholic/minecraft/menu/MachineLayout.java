package com.djden.alcoholic.minecraft.menu;

/**
 * Widget coordinates for the shared 176×166 machine screen. Values are
 * relative to the GUI top-left, not the texture atlas.
 */
public enum MachineLayout {
    TWO_SLOTS(
            new SlotPos[]{new SlotPos(44, 32), new SlotPos(116, 32)},
            GaugePos.NONE,
            new ArrowPos(79, 34),
            false,
            false,
            true
    ),
    TWO_SLOTS_ONE_TANK(
            new SlotPos[]{new SlotPos(26, 32), new SlotPos(80, 32)},
            new GaugePos[]{new GaugePos(130, 17)},
            new ArrowPos(54, 34),
            false,
            false,
            true
    ),
    TWO_SLOTS_TWO_TANKS(
            new SlotPos[]{new SlotPos(26, 32), new SlotPos(72, 32)},
            new GaugePos[]{new GaugePos(118, 17), new GaugePos(142, 17)},
            new ArrowPos(50, 34),
            false,
            false,
            true
    ),
    ONE_SLOT_ONE_TANK(
            new SlotPos[]{new SlotPos(44, 32)},
            new GaugePos[]{new GaugePos(116, 17)},
            new ArrowPos(72, 34),
            false,
            false,
            true
    ),
    ONE_TANK(
            SlotPos.NONE,
            new GaugePos[]{new GaugePos(79, 17)},
            ArrowPos.NONE,
            false,
            false,
            false
    ),
    TWO_TANKS(
            SlotPos.NONE,
            new GaugePos[]{new GaugePos(62, 17), new GaugePos(98, 17)},
            ArrowPos.NONE,
            false,
            false,
            false
    ),
    FUEL(
            new SlotPos[]{new SlotPos(80, 32)},
            GaugePos.NONE,
            ArrowPos.NONE,
            true,
            false,
            false
    ),
    ENERGY(
            SlotPos.NONE,
            new GaugePos[]{new GaugePos(79, 17)},
            ArrowPos.NONE,
            false,
            true,
            false
    );

    public static final int PANEL_WIDTH = 176;
    public static final int PANEL_HEIGHT = 166;
    public static final int SLOT_SIZE = 18;
    public static final int PLAYER_INV_X = 8;
    public static final int PLAYER_INV_Y = 84;
    public static final int HOTBAR_Y = 142;
    public static final SlotPos[] PLAYER_SLOTS = playerInventorySlots();
    public static final int GAUGE_WIDTH = 18;
    public static final int GAUGE_HEIGHT = 52;
    public static final int ARROW_WIDTH = 24;
    public static final int ARROW_HEIGHT = 17;
    public static final int BURN_SIZE = 14;

    private final SlotPos[] slots;
    private final GaugePos[] gauges;
    private final ArrowPos arrow;
    private final boolean fuelBar;
    private final boolean energyGauge;
    private final boolean progressArrow;

    MachineLayout(
            SlotPos[] slots,
            GaugePos[] gauges,
            ArrowPos arrow,
            boolean fuelBar,
            boolean energyGauge,
            boolean progressArrow
    ) {
        this.slots = slots;
        this.gauges = gauges;
        this.arrow = arrow;
        this.fuelBar = fuelBar;
        this.energyGauge = energyGauge;
        this.progressArrow = progressArrow;
    }

    public int machineSlotCount() {
        return slots.length;
    }

    public int tankCount() {
        return energyGauge ? 0 : gauges.length;
    }

    public SlotPos[] slots() {
        return slots;
    }

    public GaugePos[] gauges() {
        return gauges;
    }

    public ArrowPos arrow() {
        return arrow;
    }

    public boolean fuelBar() {
        return fuelBar;
    }

    public boolean energyGauge() {
        return energyGauge;
    }

    public boolean progressArrow() {
        return progressArrow;
    }

    public record SlotPos(int x, int y) {
        static final SlotPos[] NONE = new SlotPos[0];
    }

    private static SlotPos[] playerInventorySlots() {
        SlotPos[] slots = new SlotPos[36];
        int index = 0;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                slots[index++] = new SlotPos(
                        PLAYER_INV_X + column * SLOT_SIZE,
                        PLAYER_INV_Y + row * SLOT_SIZE
                );
            }
        }
        for (int column = 0; column < 9; column++) {
            slots[index++] = new SlotPos(PLAYER_INV_X + column * SLOT_SIZE, HOTBAR_Y);
        }
        return slots;
    }

    public record GaugePos(int x, int y) {
        static final GaugePos[] NONE = new GaugePos[0];
    }

    public record ArrowPos(int x, int y) {
        static final ArrowPos NONE = new ArrowPos(-1, -1);

        public boolean present() {
            return x >= 0 && y >= 0;
        }
    }
}

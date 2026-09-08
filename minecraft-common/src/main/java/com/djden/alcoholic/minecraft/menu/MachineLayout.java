package com.djden.alcoholic.minecraft.menu;

import com.djden.alcoholic.domain.multiblock.MachineKind;
import com.djden.alcoholic.domain.multiblock.MachineScale;
import com.djden.alcoholic.domain.multiblock.MultiblockDefinition;

/**
 * Widget coordinates for the shared machine screen. Values are relative to
 * the GUI top-left, not the texture atlas.
 */
public enum MachineLayout {
    TWO_SLOTS(
            new SlotPos[]{new SlotPos(44, 32), new SlotPos(116, 32)},
            GaugePos.NONE,
            new ArrowPos(79, 34),
            false,
            false,
            true,
            1
    ),
    TWO_SLOTS_ONE_TANK(
            new SlotPos[]{new SlotPos(26, 32), new SlotPos(80, 32)},
            new GaugePos[]{new GaugePos(130, 17)},
            new ArrowPos(54, 34),
            false,
            false,
            true,
            1
    ),
    TWO_SLOTS_TWO_TANKS(
            new SlotPos[]{new SlotPos(26, 32), new SlotPos(72, 32)},
            new GaugePos[]{new GaugePos(118, 17), new GaugePos(142, 17)},
            new ArrowPos(50, 34),
            false,
            false,
            true,
            1
    ),
    ONE_SLOT_ONE_TANK(
            new SlotPos[]{new SlotPos(44, 32)},
            new GaugePos[]{new GaugePos(116, 17)},
            new ArrowPos(72, 34),
            false,
            false,
            true,
            1
    ),
    ONE_TANK(
            SlotPos.NONE,
            new GaugePos[]{new GaugePos(79, 17)},
            ArrowPos.NONE,
            false,
            false,
            false,
            0
    ),
    TWO_TANKS(
            SlotPos.NONE,
            new GaugePos[]{new GaugePos(62, 17), new GaugePos(98, 17)},
            ArrowPos.NONE,
            false,
            false,
            false,
            0
    ),
    FUEL(
            new SlotPos[]{new SlotPos(80, 32)},
            GaugePos.NONE,
            ArrowPos.NONE,
            true,
            false,
            false,
            1
    ),
    ENERGY(
            SlotPos.NONE,
            new GaugePos[]{new GaugePos(79, 17)},
            ArrowPos.NONE,
            false,
            true,
            false,
            0
    ),
    CRAFT_MALT(
            join(slotGrid(8, 18, 3, 2), slotGrid(114, 18, 3, 2)),
            GaugePos.NONE,
            new ArrowPos(76, 27),
            false,
            false,
            true,
            6
    ),
    INDUSTRIAL_MALT(
            join(slotGrid(8, 18, 9, 2), slotGrid(8, 76, 9, 2)),
            GaugePos.NONE,
            new ArrowPos(76, 55),
            false,
            false,
            true,
            176,
            18,
            208,
            126,
            184
    );

    public static final int PANEL_WIDTH = 176;
    public static final int PANEL_HEIGHT = 166;
    public static final int SLOT_SIZE = 18;
    public static final int PLAYER_INV_X = 8;
    public static final int PLAYER_INV_Y = 84;
    public static final int HOTBAR_Y = 142;
    public static final int MAX_MACHINE_SLOTS = 36;
    public static final SlotPos[] PLAYER_SLOTS = playerInventorySlots(PLAYER_INV_Y, HOTBAR_Y);
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
    private final int inputSlotCount;
    private final int panelWidth;
    private final int panelHeight;
    private final int playerInvY;
    private final int hotbarY;
    private final SlotPos[] playerSlots;

    MachineLayout(
            SlotPos[] slots,
            GaugePos[] gauges,
            ArrowPos arrow,
            boolean fuelBar,
            boolean energyGauge,
            boolean progressArrow,
            int inputSlotCount
    ) {
        this(
                slots,
                gauges,
                arrow,
                fuelBar,
                energyGauge,
                progressArrow,
                176,
                inputSlotCount,
                166,
                84,
                142
        );
    }

    MachineLayout(
            SlotPos[] slots,
            GaugePos[] gauges,
            ArrowPos arrow,
            boolean fuelBar,
            boolean energyGauge,
            boolean progressArrow,
            int panelWidth,
            int inputSlotCount,
            int panelHeight,
            int playerInvY,
            int hotbarY
    ) {
        this.slots = slots;
        this.gauges = gauges;
        this.arrow = arrow;
        this.fuelBar = fuelBar;
        this.energyGauge = energyGauge;
        this.progressArrow = progressArrow;
        this.inputSlotCount = Math.max(0, Math.min(inputSlotCount, slots.length));
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        this.playerInvY = playerInvY;
        this.hotbarY = hotbarY;
        this.playerSlots = playerInventorySlots(playerInvY, hotbarY);
    }

    public int machineSlotCount() {
        return slots.length;
    }

    public int inputSlotCount() {
        return inputSlotCount;
    }

    public int outputSlotCount() {
        return Math.max(0, slots.length - inputSlotCount);
    }

    public int firstOutputSlot() {
        return inputSlotCount;
    }

    public boolean isInput(int slot) {
        return slot >= 0 && slot < inputSlotCount;
    }

    public boolean isOutput(int slot) {
        return slot >= inputSlotCount && slot < slots.length;
    }

    public int[] inputSlots() {
        return range(0, inputSlotCount);
    }

    public int[] outputSlots() {
        return range(inputSlotCount, slots.length);
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

    public int panelWidth() {
        return panelWidth;
    }

    public int panelHeight() {
        return panelHeight;
    }

    public int playerInvY() {
        return playerInvY;
    }

    public int hotbarY() {
        return hotbarY;
    }

    public SlotPos[] playerSlots() {
        return playerSlots;
    }

    public static MachineLayout forMultiblock(MultiblockDefinition definition) {
        return forMultiblock(definition.kind(), definition.hasProcess(), definition.scale());
    }

    public static MachineLayout forMultiblock(MachineKind kind, boolean hasProcess) {
        return forMultiblock(kind, hasProcess, MachineScale.INDUSTRIAL);
    }

    public static MachineLayout forMultiblock(MachineKind kind, boolean hasProcess, MachineScale scale) {
        if (kind == MachineKind.MALT) {
            return scale == MachineScale.CRAFT ? CRAFT_MALT : INDUSTRIAL_MALT;
        }
        if (kind == MachineKind.MILL) {
            return TWO_SLOTS;
        }
        return hasProcess ? TWO_SLOTS_ONE_TANK : ONE_TANK;
    }

    public record SlotPos(int x, int y) {
        static final SlotPos[] NONE = new SlotPos[0];
    }

    private static SlotPos[] playerInventorySlots(int playerInvY, int hotbarY) {
        SlotPos[] slots = new SlotPos[36];
        int index = 0;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                slots[index++] = new SlotPos(
                        PLAYER_INV_X + column * SLOT_SIZE,
                        playerInvY + row * SLOT_SIZE
                );
            }
        }
        for (int column = 0; column < 9; column++) {
            slots[index++] = new SlotPos(PLAYER_INV_X + column * SLOT_SIZE, hotbarY);
        }
        return slots;
    }

    private static SlotPos[] slotGrid(int originX, int originY, int columns, int rows) {
        SlotPos[] slots = new SlotPos[columns * rows];
        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                slots[index++] = new SlotPos(originX + column * SLOT_SIZE, originY + row * SLOT_SIZE);
            }
        }
        return slots;
    }

    private static SlotPos[] join(SlotPos[] left, SlotPos[] right) {
        SlotPos[] slots = new SlotPos[left.length + right.length];
        System.arraycopy(left, 0, slots, 0, left.length);
        System.arraycopy(right, 0, slots, left.length, right.length);
        return slots;
    }

    private static int[] range(int start, int end) {
        int[] slots = new int[Math.max(0, end - start)];
        for (int index = 0; index < slots.length; index++) {
            slots[index] = start + index;
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

package com.djden.alcoholic.forge.jei;

import com.djden.alcoholic.application.process.ProcessDisplayRecipe;

final class JeiProcessLayout {
    static final int WIDTH = 176;
    static final int HEIGHT = 90;
    static final int SLOT = 18;
    static final int TANK_WIDTH = 16;
    static final int TANK_HEIGHT = 32;
    static final int STEP = 22;
    static final int LEFT = 8;
    static final int RIGHT = 116;
    static final int COLUMNS = 3;
    static final int ARROW_WIDTH = 24;
    static final int ARROW_HEIGHT = 17;

    record SlotPos(int x, int y) {
    }

    private final int fluidY;
    private final int itemY;

    JeiProcessLayout(boolean header) {
        this.fluidY = header ? 24 : 8;
        this.itemY = fluidY + TANK_HEIGHT + 6;
    }

    static JeiProcessLayout of(ProcessDisplayRecipe recipe) {
        return new JeiProcessLayout(
                recipe.durationTicks().isPresent() || recipe.preferredTemperature().isPresent()
        );
    }

    SlotPos itemIn(int index) {
        return cluster(LEFT, itemY, index, SLOT);
    }

    SlotPos itemOut(int index) {
        return cluster(RIGHT, itemY, index, SLOT);
    }

    SlotPos fluidIn(int index) {
        return cluster(LEFT, fluidY, index, TANK_WIDTH);
    }

    SlotPos fluidOut(int index) {
        return cluster(RIGHT, fluidY, index, TANK_WIDTH);
    }

    SlotPos arrow() {
        return new SlotPos((WIDTH - ARROW_WIDTH) / 2, fluidY + (TANK_HEIGHT - ARROW_HEIGHT) / 2);
    }

    int fluidY() {
        return fluidY;
    }

    int itemY() {
        return itemY;
    }

    private static SlotPos cluster(int originX, int originY, int index, int size) {
        int column = index % COLUMNS;
        int row = index / COLUMNS;
        int x = Math.min(originX + column * STEP, WIDTH - size - 8);
        return new SlotPos(x, originY + row * STEP);
    }
}

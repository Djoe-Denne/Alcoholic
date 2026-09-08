package com.djden.alcoholic.forge.client;

import java.util.Optional;

/**
 * Side panel geometry for controller telemetry. Lives left of the inventory
 * so JEI's ingredient list on the right does not cover it.
 */
public final class MachineTelemetryOverlay {
    public static final int WIDTH = 152;
    public static final int GAP = 4;
    public static final int OFFSET_X = -WIDTH - GAP;

    private MachineTelemetryOverlay() {
    }

    public static Optional<Area> extraArea(boolean hasTelemetry, int leftPos, int topPos, int height) {
        if (!hasTelemetry) {
            return Optional.empty();
        }
        return Optional.of(new Area(leftPos + OFFSET_X, topPos, WIDTH, height));
    }

    public record Area(int x, int y, int width, int height) {
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }
}

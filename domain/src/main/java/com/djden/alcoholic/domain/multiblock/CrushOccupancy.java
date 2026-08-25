package com.djden.alcoholic.domain.multiblock;

import java.util.Objects;

/**
 * Hitman-style crush rule: a body must be meaningfully inside the chamber
 * during the compression stroke. Edge pixels are ignored.
 */
public final class CrushOccupancy {
    public static final double MIN_OVERLAP_RATIO = 0.25;
    public static final double EDGE_OVERLAP_RATIO = 0.08;

    private CrushOccupancy() {
    }

    public static boolean lethal(
            PressStrokeState stroke,
            Box3 crushVolume,
            double centerX,
            double centerY,
            double centerZ,
            Box3 body
    ) {
        Objects.requireNonNull(stroke, "stroke");
        Objects.requireNonNull(crushVolume, "crushVolume");
        Objects.requireNonNull(body, "body");
        if (!stroke.crushActive()) {
            return false;
        }
        boolean centerInside = crushVolume.contains(centerX, centerY, centerZ);
        double bodyVolume = body.volume();
        if (bodyVolume <= 0.0) {
            return false;
        }
        double overlap = crushVolume.intersection(body).volume() / bodyVolume;
        if (overlap < EDGE_OVERLAP_RATIO) {
            return false;
        }
        return centerInside && overlap >= MIN_OVERLAP_RATIO;
    }
}

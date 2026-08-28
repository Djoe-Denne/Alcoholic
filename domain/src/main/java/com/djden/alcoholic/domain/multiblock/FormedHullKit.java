package com.djden.alcoholic.domain.multiblock;

import java.util.ArrayList;
import java.util.List;

/**
 * Welded formed hull: 9-slice cuboid (fixed 1-block corners, repeating
 * edges and faces) plus the 1×1 fittings that stay visible.
 */
public final class FormedHullKit {
    public enum Patch {
        CORNER,
        EDGE,
        FACE
    }

    /**
     * One textured quad in local block space, origin at the AABB minimum.
     * {@code a-b-c-d} is counter-clockwise when viewed from outside.
     */
    public record HullQuad(
            float ax, float ay, float az,
            float bx, float by, float bz,
            float cx, float cy, float cz,
            float dx, float dy, float dz,
            float u0, float v0, float u1, float v1,
            float nx, float ny, float nz,
            Patch patch
    ) {
        public float minX() {
            return min(ax, bx, cx, dx);
        }

        public float maxX() {
            return max(ax, bx, cx, dx);
        }

        public float minY() {
            return min(ay, by, cy, dy);
        }

        public float maxY() {
            return max(ay, by, cy, dy);
        }

        public float minZ() {
            return min(az, bz, cz, dz);
        }

        public float maxZ() {
            return max(az, bz, cz, dz);
        }

        private static float min(float a, float b, float c, float d) {
            return Math.min(Math.min(a, b), Math.min(c, d));
        }

        private static float max(float a, float b, float c, float d) {
            return Math.max(Math.max(a, b), Math.max(c, d));
        }
    }

    private FormedHullKit() {
    }

    public static boolean hideCasingCube(PartRole role, boolean formed) {
        return formed && role == PartRole.CASING;
    }

    public static boolean fittingVisible(PartRole role) {
        return role != PartRole.CASING;
    }

    public static boolean hullApplies(boolean formed) {
        return formed;
    }

    public static List<HullQuad> quads(int width, int height, int depth) {
        if (width < 1 || height < 1 || depth < 1) {
            throw new IllegalArgumentException("hull size must be positive");
        }
        List<HullQuad> quads = new ArrayList<>();
        for (Span u : cuts(width)) {
            for (Span v : cuts(height)) {
                Patch patch = classify(u, v);
                quads.add(quad(
                        u.start, v.start, 0,
                        u.end, v.start, 0,
                        u.end, v.end, 0,
                        u.start, v.end, 0,
                        u.length(), v.length(),
                        0, 0, -1,
                        patch
                ));
                quads.add(quad(
                        u.end, v.start, depth,
                        u.start, v.start, depth,
                        u.start, v.end, depth,
                        u.end, v.end, depth,
                        u.length(), v.length(),
                        0, 0, 1,
                        patch
                ));
            }
        }
        for (Span u : cuts(depth)) {
            for (Span v : cuts(height)) {
                Patch patch = classify(u, v);
                quads.add(quad(
                        0, v.start, u.end,
                        0, v.start, u.start,
                        0, v.end, u.start,
                        0, v.end, u.end,
                        u.length(), v.length(),
                        -1, 0, 0,
                        patch
                ));
                quads.add(quad(
                        width, v.start, u.start,
                        width, v.start, u.end,
                        width, v.end, u.end,
                        width, v.end, u.start,
                        u.length(), v.length(),
                        1, 0, 0,
                        patch
                ));
            }
        }
        for (Span u : cuts(width)) {
            for (Span v : cuts(depth)) {
                Patch patch = classify(u, v);
                quads.add(quad(
                        u.start, 0, v.start,
                        u.start, 0, v.end,
                        u.end, 0, v.end,
                        u.end, 0, v.start,
                        u.length(), v.length(),
                        0, -1, 0,
                        patch
                ));
                quads.add(quad(
                        u.start, height, v.end,
                        u.start, height, v.start,
                        u.end, height, v.start,
                        u.end, height, v.end,
                        u.length(), v.length(),
                        0, 1, 0,
                        patch
                ));
            }
        }
        return List.copyOf(quads);
    }

    private static HullQuad quad(
            float ax, float ay, float az,
            float bx, float by, float bz,
            float cx, float cy, float cz,
            float dx, float dy, float dz,
            float uSpan, float vSpan,
            float nx, float ny, float nz,
            Patch patch
    ) {
        return new HullQuad(
                ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz,
                0, 0, uSpan, vSpan,
                nx, ny, nz,
                patch
        );
    }

    private static Patch classify(Span u, Span v) {
        if (u.endCap && v.endCap) {
            return Patch.CORNER;
        }
        if (u.endCap || v.endCap) {
            return Patch.EDGE;
        }
        return Patch.FACE;
    }

    private static List<Span> cuts(int size) {
        if (size == 1) {
            return List.of(new Span(0, 1, false));
        }
        if (size == 2) {
            return List.of(new Span(0, 1, true), new Span(1, 2, true));
        }
        return List.of(new Span(0, 1, true), new Span(1, size - 1, false), new Span(size - 1, size, true));
    }

    private record Span(int start, int end, boolean endCap) {
        int length() {
            return end - start;
        }
    }
}

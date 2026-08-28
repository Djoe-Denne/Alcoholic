package com.djden.alcoholic.domain.multiblock;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormedHullKitTest {
    @Test
    void casingCubeHidesAsSoonAsFormed() {
        assertTrue(FormedHullKit.hideCasingCube(PartRole.CASING, true));
        assertFalse(FormedHullKit.hideCasingCube(PartRole.CASING, false));
    }

    @Test
    void fittingsStayVisibleWhenFormed() {
        for (PartRole role : List.of(
                PartRole.ITEM_PORT,
                PartRole.FLUID_PORT,
                PartRole.KINETIC_PORT,
                PartRole.WINDOW,
                PartRole.HATCH,
                PartRole.CONTROLLER
        )) {
            assertFalse(FormedHullKit.hideCasingCube(role, true), role.name());
            assertTrue(FormedHullKit.fittingVisible(role), role.name());
        }
    }

    @Test
    void nineSliceAppliesToMinAndMaxSizes() {
        List<FormedHullKit.HullQuad> min = FormedHullKit.quads(3, 4, 3);
        List<FormedHullKit.HullQuad> max = FormedHullKit.quads(9, 16, 9);
        assertEquals(54, min.size());
        assertEquals(54, max.size());
        assertEquals(
                EnumSet.of(FormedHullKit.Patch.CORNER, FormedHullKit.Patch.EDGE, FormedHullKit.Patch.FACE),
                patches(min)
        );
        assertEquals(
                EnumSet.of(FormedHullKit.Patch.CORNER, FormedHullKit.Patch.EDGE, FormedHullKit.Patch.FACE),
                patches(max)
        );
        assertCover(min, 3, 4, 3);
        assertCover(max, 9, 16, 9);
        assertEquals(1f, cornerSize(min));
        assertEquals(1f, cornerSize(max));
    }

    @Test
    void hullAppliesOnceFormedRegardlessOfSize() {
        assertTrue(FormedHullKit.hullApplies(true));
        assertFalse(FormedHullKit.hullApplies(false));
    }

    private static EnumSet<FormedHullKit.Patch> patches(List<FormedHullKit.HullQuad> quads) {
        EnumSet<FormedHullKit.Patch> patches = EnumSet.noneOf(FormedHullKit.Patch.class);
        for (FormedHullKit.HullQuad quad : quads) {
            patches.add(quad.patch());
        }
        return patches;
    }

    private static void assertCover(List<FormedHullKit.HullQuad> quads, int width, int height, int depth) {
        float maxX = 0;
        float maxY = 0;
        float maxZ = 0;
        for (FormedHullKit.HullQuad quad : quads) {
            maxX = Math.max(maxX, quad.maxX());
            maxY = Math.max(maxY, quad.maxY());
            maxZ = Math.max(maxZ, quad.maxZ());
        }
        assertEquals(width, maxX);
        assertEquals(height, maxY);
        assertEquals(depth, maxZ);
    }

    private static float cornerSize(List<FormedHullKit.HullQuad> quads) {
        return quads.stream()
                .filter(quad -> quad.patch() == FormedHullKit.Patch.CORNER)
                .map(quad -> Math.max(quad.u1() - quad.u0(), quad.v1() - quad.v0()))
                .max(Float::compare)
                .orElseThrow();
    }
}

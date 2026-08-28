package com.djden.alcoholic.minecraft.mechanical;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrimitiveCombustionEngineFacesTest {
    @Test
    void driveFaceIsTheRightHandWhenFacingNorth() {
        assertEquals(Direction.EAST, PrimitiveCombustionEngineFaces.driveFace(Direction.NORTH));
        assertEquals(Direction.SOUTH, PrimitiveCombustionEngineFaces.driveFace(Direction.EAST));
        assertEquals(Direction.WEST, PrimitiveCombustionEngineFaces.driveFace(Direction.SOUTH));
        assertEquals(Direction.NORTH, PrimitiveCombustionEngineFaces.driveFace(Direction.WEST));
    }

    @Test
    void facingForDriveTowardPointsTheShaftAtTheConsumer() {
        assertEquals(Direction.SOUTH, PrimitiveCombustionEngineFaces.facingForDriveToward(Direction.WEST));
        assertEquals(Direction.NORTH, PrimitiveCombustionEngineFaces.facingForDriveToward(Direction.EAST));
        assertEquals(Direction.EAST, PrimitiveCombustionEngineFaces.facingForDriveToward(Direction.SOUTH));
        assertEquals(Direction.WEST, PrimitiveCombustionEngineFaces.facingForDriveToward(Direction.NORTH));
    }

    @Test
    void facingForDriveTowardRejectsVerticalFaces() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PrimitiveCombustionEngineFaces.facingForDriveToward(Direction.UP)
        );
    }
}

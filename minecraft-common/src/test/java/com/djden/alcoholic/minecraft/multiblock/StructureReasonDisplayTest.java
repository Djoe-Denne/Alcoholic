package com.djden.alcoholic.minecraft.multiblock;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructureReasonDisplayTest {
    @Test
    void hidesBlankAndDefaultUnformed() {
        assertFalse(StructureReasonDisplay.present(""));
        assertFalse(StructureReasonDisplay.present("unformed"));
        assertFalse(StructureReasonDisplay.present(null));
        assertTrue(StructureReasonDisplay.present("interior is obstructed"));
    }

    @Test
    void mapsStaticValidatorReasons() {
        assertEquals(
                "gui.alcoholic.structure.reason.open_shell",
                key("shell is not a closed connected frame")
        );
        assertEquals(
                "gui.alcoholic.structure.reason.interior_blocked",
                key("interior is obstructed")
        );
        assertEquals(
                "gui.alcoholic.structure.reason.missing_port",
                key("missing required port KINETIC_PORT")
        );
    }

    @Test
    void mapsDimensionAndCapacityReasons() {
        TranslatableContents dimensions = contents("dimensions 3x8x3 outside allowed range");
        assertEquals("gui.alcoholic.structure.reason.dimensions", dimensions.getKey());
        assertArrayEquals(new Object[]{"3", "8", "3"}, dimensions.getArgs());

        TranslatableContents capacity = contents("stored 1500 mB exceeds new capacity 1000");
        assertEquals("gui.alcoholic.structure.reason.overcapacity", capacity.getKey());
        assertArrayEquals(new Object[]{"1500", "1000"}, capacity.getArgs());
    }

    private static String key(String reason) {
        return contents(reason).getKey();
    }

    private static TranslatableContents contents(String reason) {
        Component component = StructureReasonDisplay.component(reason);
        assertTrue(component.getContents() instanceof TranslatableContents);
        return (TranslatableContents) component.getContents();
    }
}

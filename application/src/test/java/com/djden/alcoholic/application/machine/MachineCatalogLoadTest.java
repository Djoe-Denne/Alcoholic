package com.djden.alcoholic.application.machine;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.domain.multiblock.MachineKind;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineCatalogLoadTest {
    @Test
    void builtinsArePresentWithoutDatapackOverlay() {
        MachineCatalog catalog = new LoadMachineCatalogUseCase().load(Map.of());
        assertEquals(8, catalog.machines().size());
        assertEquals(MachineKind.STORAGE, catalog.get(BuiltinMachines.INDUSTRIAL_TANK).orElseThrow().kind());
        assertEquals(MachineKind.MALT, catalog.get(BuiltinMachines.INDUSTRIAL_MALT_HOUSE).orElseThrow().kind());
        assertEquals(MachineKind.MILL, catalog.get(BuiltinMachines.INDUSTRIAL_ROLLER_MILL).orElseThrow().kind());
        assertEquals(MachineKind.MASH, catalog.get(BuiltinMachines.INDUSTRIAL_MASH_TUN).orElseThrow().kind());
        assertEquals(MachineKind.BOIL, catalog.get(BuiltinMachines.INDUSTRIAL_BREWING_KETTLE).orElseThrow().kind());
        assertEquals(MachineKind.CONDITION, catalog.get(BuiltinMachines.INDUSTRIAL_CONDITIONING_VESSEL).orElseThrow().kind());
        assertTrue(catalog.get(BuiltinMachines.INDUSTRIAL_PRESS).orElseThrow().processType().isPresent());
        assertTrue(catalog.get(BuiltinMachines.INDUSTRIAL_MASH_TUN).orElseThrow().processType().isPresent());
        assertEquals(4.0, catalog.get(BuiltinMachines.INDUSTRIAL_ROLLER_MILL).orElseThrow()
                .kinetic().requiredCapacity(), 1e-9);
    }

    @Test
    void datapackCanReplaceCapacityWithoutRenamingTheFamily() {
        DataNode overlay = DataNode.objectBuilder()
                .put("id", DataNode.string("alcoholic:industrial_storage_tank"))
                .put("kind", DataNode.string("storage"))
                .put("min_exterior", DataNode.objectBuilder()
                        .put("x", DataNode.number(3))
                        .put("y", DataNode.number(4))
                        .put("z", DataNode.number(3))
                        .build())
                .put("max_exterior", DataNode.objectBuilder()
                        .put("x", DataNode.number(5))
                        .put("y", DataNode.number(6))
                        .put("z", DataNode.number(5))
                        .build())
                .put("casing_tags", DataNode.list(java.util.List.of(DataNode.string("alcoholic:industrial_tank_casing"))))
                .put("capacity_per_internal_block", DataNode.number(12_000))
                .put("controller", DataNode.string("alcoholic:industrial_tank_controller"))
                .build();
        MachineCatalog catalog = new LoadMachineCatalogUseCase().load(Map.of(
                ResourceId.parse("alcoholic:machines/industrial_storage_tank"),
                overlay
        ));
        assertEquals(12_000, catalog.get(BuiltinMachines.INDUSTRIAL_TANK).orElseThrow().capacityPerInternalBlock());
        assertEquals(5, catalog.get(BuiltinMachines.INDUSTRIAL_TANK).orElseThrow().constraints().maxWidth());
    }
}

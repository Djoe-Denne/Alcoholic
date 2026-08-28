package com.djden.alcoholic.application.progression;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.machine.BuiltinMachines;
import com.djden.alcoholic.domain.multiblock.MultiblockDefinition;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressionCoverageTest {
    private static final Set<ResourceId> OFFICIAL_CROPS = Set.of(
            ResourceId.parse("alcoholic:red_grapes"),
            ResourceId.parse("alcoholic:white_grapes"),
            ResourceId.parse("alcoholic:hops"),
            ResourceId.parse("alcoholic:barley")
    );

    @Test
    void officialProcessesExceptStubsHaveANode() {
        ProgressionCatalog catalog = ProgressionCatalog.official();
        Set<ResourceId> covered = new HashSet<>();
        Map<ResourceId, MultiblockDefinition> machines = BuiltinMachines.all();
        for (ProgressionNode node : catalog.nodes()) {
            for (ProgressionCriterion criterion : node.criteria()) {
                criterion.process().ifPresent(covered::add);
                criterion.machine().ifPresent(machine -> machines.get(machine).processType().ifPresent(covered::add));
            }
        }
        assertTrue(
                covered.containsAll(BuiltinRegistrations.officialProcessIds()),
                "Missing process nodes: " + difference(BuiltinRegistrations.officialProcessIds(), covered)
        );
        for (ResourceId stub : BuiltinRegistrations.stubProcessIds()) {
            assertFalse(covered.contains(stub), "Stub process should stay off the graph: " + stub);
        }
    }

    @Test
    void everyIndustrialMachineHasExactlyOneFormedNode() {
        ProgressionCatalog catalog = ProgressionCatalog.official();
        for (ResourceId machine : BuiltinMachines.all().keySet()) {
            long count = catalog.nodes().stream()
                    .filter(node -> node.trigger() == ProgressionTriggerKind.FORMED)
                    .filter(node -> node.chapter() == ProgressionChapter.INDUSTRIAL)
                    .filter(node -> node.criteria().stream().anyMatch(criterion ->
                            criterion.machine().isPresent() && criterion.machine().orElseThrow().equals(machine)
                    ))
                    .count();
            assertEquals(1, count, "Industrial formed coverage for " + machine);
        }
    }

    @Test
    void officialCropsHaveHarvestNodes() {
        ProgressionCatalog catalog = ProgressionCatalog.official();
        Set<ResourceId> harvested = catalog.nodes().stream()
                .filter(node -> node.trigger() == ProgressionTriggerKind.HARVEST)
                .flatMap(node -> node.criteria().stream())
                .flatMap(criterion -> criterion.crop().stream())
                .collect(Collectors.toSet());
        assertTrue(
                harvested.containsAll(OFFICIAL_CROPS),
                "Missing harvest nodes: " + difference(OFFICIAL_CROPS, harvested)
        );
    }

    @Test
    void hexIdsAreUniqueAndColumnsMatchLineages() {
        ProgressionCatalog catalog = ProgressionCatalog.official();
        Set<String> quests = new HashSet<>();
        Set<String> tasks = new HashSet<>();
        for (ProgressionNode node : catalog.nodes()) {
            assertTrue(quests.add(node.questHex()), "Duplicate quest hex " + node.questHex());
            assertTrue(tasks.add(node.taskHex()), "Duplicate task hex " + node.taskHex());
            if (node.line() == ProgressionLine.WINE) {
                assertTrue(node.canvasX() < 0, node.id());
            } else if (node.line() == ProgressionLine.SHARED) {
                assertEquals(0.0, node.canvasX(), 1e-9, node.id());
            } else {
                assertTrue(node.canvasX() > 0, node.id());
            }
        }
        assertTrue(catalog.require("ferment_beverage").junctionOr());
        assertTrue(catalog.require("form_industrial_vat").junctionOr());
        assertEquals("root", catalog.require("ferment_beverage").vanillaParentId().orElseThrow());
        assertEquals("industrial_root", catalog.require("form_industrial_vat").vanillaParentId().orElseThrow());
    }

    @Test
    void snbtJunctionsUseOrDependencies() {
        ProgressionCatalog catalog = ProgressionCatalog.official();
        String artisanal = FtbQuestSnbt.render(
                catalog.chapter(ProgressionChapter.ARTISANAL),
                catalog.nodes(ProgressionChapter.ARTISANAL)
        );
        String industrial = FtbQuestSnbt.render(
                catalog.chapter(ProgressionChapter.INDUSTRIAL),
                catalog.nodes(ProgressionChapter.INDUSTRIAL)
        );
        assertTrue(artisanal.contains("min_required_dependencies: 1"));
        assertTrue(industrial.contains("min_required_dependencies: 1"));
        assertTrue(artisanal.contains("alcoholic:harvest_barley"));
        assertTrue(artisanal.contains("alcoholic:boil"));
        assertTrue(industrial.contains("A1C0A01C00000022"));
    }

    private static Set<ResourceId> difference(Set<ResourceId> expected, Set<ResourceId> actual) {
        Set<ResourceId> missing = new HashSet<>(expected);
        missing.removeAll(actual);
        return missing;
    }
}

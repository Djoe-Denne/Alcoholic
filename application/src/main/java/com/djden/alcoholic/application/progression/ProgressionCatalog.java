package com.djden.alcoholic.application.progression;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.machine.BuiltinMachines;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Official wine / beer quest graph. Machines stay drink-agnostic; this
 * catalogue only names presentation lineages.
 */
public final class ProgressionCatalog {
    private final List<ProgressionChapterSpec> chapters;
    private final List<ProgressionNode> nodes;
    private final Map<String, ProgressionNode> byId;

    ProgressionCatalog(List<ProgressionChapterSpec> chapters, List<ProgressionNode> nodes) {
        this.chapters = List.copyOf(chapters);
        this.nodes = List.copyOf(nodes);
        Map<String, ProgressionNode> indexed = new LinkedHashMap<>();
        for (ProgressionNode node : this.nodes) {
            if (indexed.put(node.id(), node) != null) {
                throw new IllegalStateException("Duplicate progression node " + node.id());
            }
        }
        this.byId = Map.copyOf(indexed);
        validate();
    }

    public static ProgressionCatalog official() {
        return new ProgressionCatalog(officialChapters(), officialNodes());
    }

    public List<ProgressionChapterSpec> chapters() {
        return chapters;
    }

    public List<ProgressionNode> nodes() {
        return nodes;
    }

    public List<ProgressionNode> nodes(ProgressionChapter chapter) {
        return nodes.stream().filter(node -> node.chapter() == chapter).collect(Collectors.toList());
    }

    public ProgressionChapterSpec chapter(ProgressionChapter chapter) {
        return chapters.stream()
                .filter(spec -> spec.chapter() == chapter)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing chapter " + chapter));
    }

    public ProgressionNode require(String id) {
        return Optional.ofNullable(byId.get(id)).orElseThrow(() -> new IllegalArgumentException("Unknown node " + id));
    }

    private void validate() {
        for (ProgressionNode node : nodes) {
            for (String parent : node.parents()) {
                ProgressionNode resolved = byId.get(parent);
                if (resolved == null) {
                    throw new IllegalStateException("Node " + node.id() + " parents unknown " + parent);
                }
                if (resolved.chapter() != node.chapter()) {
                    throw new IllegalStateException("Node " + node.id() + " crosses chapters to " + parent);
                }
            }
        }
    }

    private static List<ProgressionChapterSpec> officialChapters() {
        return List.of(
                new ProgressionChapterSpec(
                        ProgressionChapter.ARTISANAL,
                        "alcoholic",
                        "A1C0A01C00000001",
                        "ftbquests.alcoholic.chapter.title",
                        "ftbquests.alcoholic.chapter.subtitle",
                        "alcoholic:beverage_bottle",
                        "circle",
                        0,
                        List.of(
                                image("press", "ftbquests.alcoholic.hover.press", -3.2, 2.2),
                                image("mash_tun", "ftbquests.alcoholic.hover.mash_tun", 5.4, 4.4),
                                image("fermenter", "ftbquests.alcoholic.hover.fermenter", 2.0, 8.4),
                                image("barrel", "ftbquests.alcoholic.hover.barrel", -3.2, 12.0),
                                image("crock", "ftbquests.alcoholic.hover.crock", -1.6, 12.0),
                                image("bottle", "ftbquests.alcoholic.hover.bottle", 2.0, 10.4)
                        )
                ),
                new ProgressionChapterSpec(
                        ProgressionChapter.INDUSTRIAL,
                        "alcoholic_industrial",
                        "A1C0A01C00000002",
                        "ftbquests.alcoholic.industrial.chapter.title",
                        "ftbquests.alcoholic.industrial.chapter.subtitle",
                        "alcoholic:industrial_casing",
                        "hexagon",
                        1,
                        List.of(
                                image("form_press", "ftbquests.alcoholic.hover.form_press", -3.5, 0.6),
                                image("form_tank", "ftbquests.alcoholic.hover.form_tank", 0.0, 0.6),
                                image("form_malt_house", "ftbquests.alcoholic.hover.form_malt_house", 5.5, -1.2),
                                image("form_roller_mill", "ftbquests.alcoholic.hover.form_roller_mill", 5.5, 0.8),
                                image("form_mash_tun", "ftbquests.alcoholic.hover.form_mash_tun", 5.5, 2.8),
                                image("form_kettle", "ftbquests.alcoholic.hover.form_kettle", 5.5, 4.8),
                                image("form_vat", "ftbquests.alcoholic.hover.form_vat", 2.0, 6.8),
                                image("form_conditioning", "ftbquests.alcoholic.hover.form_conditioning", 5.5, 8.8)
                        )
                )
        );
    }

    private static List<ProgressionNode> officialNodes() {
        List<ProgressionNode> nodes = new ArrayList<>();
        nodes.addAll(artisanal());
        nodes.addAll(industrial());
        return nodes;
    }

    private static List<ProgressionNode> artisanal() {
        ProgressionChapter chapter = ProgressionChapter.ARTISANAL;
        return List.of(
                node("root", chapter, ProgressionLine.SHARED)
                        .icon("red_grape_cutting")
                        .noAnnounce()
                        .canvas(0.0, -3.5)
                        .hex(quest(0x10), task(0x10))
                        .inventory(
                                item("has_red_cutting", "red_grape_cutting"),
                                item("has_white_cutting", "white_grape_cutting"),
                                item("has_hops", "hops"),
                                item("has_barley_seeds", "barley_seeds")
                        )
                        .build(),
                node("harvest_grapes", chapter, ProgressionLine.WINE)
                        .parents("root")
                        .icon("red_grapes")
                        .canvas(-3.2, -1.6)
                        .hex(quest(0x11), task(0x11))
                        .harvest(
                                crop("harvest_red", "red_grapes"),
                                crop("harvest_white", "white_grapes")
                        )
                        .build(),
                node("produce_must", chapter, ProgressionLine.WINE)
                        .parents("harvest_grapes")
                        .icon("red_grape_must_bucket")
                        .canvas(-3.2, 0.4)
                        .hex(quest(0x13), task(0x13))
                        .process(
                                process("press_red_must", BuiltinRegistrations.PRESS, "red_grape_must"),
                                process("press_white_must", BuiltinRegistrations.PRESS, "white_grape_must")
                        )
                        .build(),
                node("harvest_barley", chapter, ProgressionLine.BEER)
                        .parents("root")
                        .icon("barley")
                        .canvas(3.2, -1.6)
                        .hex(quest(0x18), task(0x18))
                        .harvest(crop("harvest_barley", "barley"))
                        .build(),
                node("harvest_hops", chapter, ProgressionLine.BEER)
                        .parents("root")
                        .icon("hops")
                        .canvas(5.6, -1.6)
                        .hex(quest(0x12), task(0x12))
                        .harvest(crop("harvest_hops", "hops"))
                        .build(),
                node("malt", chapter, ProgressionLine.BEER)
                        .parents("harvest_barley")
                        .icon("malting_floor")
                        .canvas(3.2, 0.4)
                        .hex(quest(0x19), task(0x19))
                        .process(ProgressionCriterion.process("malt", BuiltinRegistrations.MALT))
                        .build(),
                node("mill", chapter, ProgressionLine.BEER)
                        .parents("malt")
                        .icon("malt_mill")
                        .canvas(3.2, 2.4)
                        .hex(quest(0x1A), task(0x1A))
                        .process(ProgressionCriterion.process("mill", BuiltinRegistrations.MILL))
                        .build(),
                node("mash", chapter, ProgressionLine.BEER)
                        .parents("mill")
                        .icon("wort_bucket")
                        .canvas(3.2, 4.4)
                        .hex(quest(0x1B), task(0x1B))
                        .process(process("mash_wort", BuiltinRegistrations.MASH, "wort"))
                        .build(),
                node("boil", chapter, ProgressionLine.BEER)
                        .parents("mash", "harvest_hops")
                        .icon("hopped_wort_bucket")
                        .canvas(3.2, 6.4)
                        .hex(quest(0x1C), task(0x1C))
                        .process(process("boil", BuiltinRegistrations.BOIL, "hopped_wort"))
                        .build(),
                node("ferment_beverage", chapter, ProgressionLine.SHARED)
                        .parents("produce_must", "boil")
                        .icon("artisanal_fermenter")
                        .frame(ProgressionFrame.GOAL)
                        .canvas(0.0, 8.4)
                        .hex(quest(0x14), task(0x14))
                        .minRequiredDependencies(1)
                        .process(
                                process("ferment_red", BuiltinRegistrations.FERMENT, "young_red_wine"),
                                process("ferment_white", BuiltinRegistrations.FERMENT, "young_white_wine"),
                                process("ferment_beer", BuiltinRegistrations.FERMENT, "beer")
                        )
                        .build(),
                node("age_wine", chapter, ProgressionLine.WINE)
                        .parents("ferment_beverage")
                        .icon("oak_barrel")
                        .frame(ProgressionFrame.GOAL)
                        .canvas(-3.2, 10.4)
                        .hex(quest(0x15), task(0x15))
                        .process(
                                process("age_red", BuiltinRegistrations.AGE, "red_wine"),
                                process("age_white", BuiltinRegistrations.AGE, "white_wine")
                        )
                        .build(),
                node("blend", chapter, ProgressionLine.WINE)
                        .parents("ferment_beverage")
                        .icon("artisanal_blending_crock")
                        .hidden()
                        .canvas(-1.6, 10.4)
                        .hex(quest(0x16), task(0x16))
                        .process(ProgressionCriterion.process("blend", BuiltinRegistrations.BLEND))
                        .build(),
                node("bottle", chapter, ProgressionLine.SHARED)
                        .parents("ferment_beverage")
                        .icon("beverage_bottle")
                        .frame(ProgressionFrame.CHALLENGE)
                        .canvas(0.0, 10.4)
                        .hex(quest(0x17), task(0x17))
                        .process(ProgressionCriterion.process("bottle", BuiltinRegistrations.BOTTLE))
                        .build()
        );
    }

    private static List<ProgressionNode> industrial() {
        ProgressionChapter chapter = ProgressionChapter.INDUSTRIAL;
        return List.of(
                node("industrial_root", chapter, ProgressionLine.SHARED)
                        .icon("industrial_casing")
                        .noAnnounce()
                        .canvas(0.0, -3.2)
                        .hex(quest(0x20), task(0x20))
                        .inventory(
                                item("has_casing", "industrial_casing"),
                                item("has_press", "industrial_press_controller"),
                                item("has_vat", "industrial_vat_controller"),
                                item("has_tank", "industrial_tank_controller"),
                                item("has_malt_house", "industrial_malt_house_controller"),
                                item("has_roller_mill", "industrial_roller_mill_controller"),
                                item("has_mash_tun", "industrial_mash_tun_controller"),
                                item("has_kettle", "industrial_brewing_kettle_controller"),
                                item("has_conditioning", "industrial_conditioning_vessel_controller")
                        )
                        .build(),
                formed(
                        "form_industrial_press",
                        chapter,
                        ProgressionLine.WINE,
                        "industrial_root",
                        "industrial_press_controller",
                        BuiltinMachines.INDUSTRIAL_PRESS,
                        -3.5,
                        -1.2,
                        0x21
                ),
                formed(
                        "form_industrial_tank",
                        chapter,
                        ProgressionLine.SHARED,
                        "industrial_root",
                        "industrial_tank_controller",
                        BuiltinMachines.INDUSTRIAL_TANK,
                        0.0,
                        -1.2,
                        0x23
                ),
                formed(
                        "form_industrial_malt_house",
                        chapter,
                        ProgressionLine.BEER,
                        "industrial_root",
                        "industrial_malt_house_controller",
                        BuiltinMachines.INDUSTRIAL_MALT_HOUSE,
                        3.5,
                        -1.2,
                        0x24
                ),
                formed(
                        "form_industrial_roller_mill",
                        chapter,
                        ProgressionLine.BEER,
                        "form_industrial_malt_house",
                        "industrial_roller_mill_controller",
                        BuiltinMachines.INDUSTRIAL_ROLLER_MILL,
                        3.5,
                        0.8,
                        0x25
                ),
                formed(
                        "form_industrial_mash_tun",
                        chapter,
                        ProgressionLine.BEER,
                        "form_industrial_roller_mill",
                        "industrial_mash_tun_controller",
                        BuiltinMachines.INDUSTRIAL_MASH_TUN,
                        3.5,
                        2.8,
                        0x26
                ),
                formed(
                        "form_industrial_kettle",
                        chapter,
                        ProgressionLine.BEER,
                        "form_industrial_mash_tun",
                        "industrial_brewing_kettle_controller",
                        BuiltinMachines.INDUSTRIAL_BREWING_KETTLE,
                        3.5,
                        4.8,
                        0x27
                ),
                node("form_industrial_vat", chapter, ProgressionLine.SHARED)
                        .parents("form_industrial_press", "form_industrial_kettle")
                        .icon("industrial_vat_controller")
                        .frame(ProgressionFrame.GOAL)
                        .canvas(0.0, 6.8)
                        .hex(quest(0x22), task(0x22))
                        .minRequiredDependencies(1)
                        .formed(ProgressionCriterion.formed("formed", BuiltinMachines.INDUSTRIAL_VAT))
                        .build(),
                formed(
                        "form_industrial_conditioning",
                        chapter,
                        ProgressionLine.BEER,
                        "form_industrial_vat",
                        "industrial_conditioning_vessel_controller",
                        BuiltinMachines.INDUSTRIAL_CONDITIONING_VESSEL,
                        3.5,
                        8.8,
                        0x28
                )
        );
    }

    private static ProgressionNode formed(
            String id,
            ProgressionChapter chapter,
            ProgressionLine line,
            String parent,
            String icon,
            ResourceId machine,
            double x,
            double y,
            int hex
    ) {
        return node(id, chapter, line)
                .parents(parent)
                .icon(icon)
                .frame(ProgressionFrame.GOAL)
                .canvas(x, y)
                .hex(quest(hex), task(hex))
                .formed(ProgressionCriterion.formed("formed", machine))
                .build();
    }

    private static ProgressionNode.Builder node(String id, ProgressionChapter chapter, ProgressionLine line) {
        return ProgressionNode.builder(id, chapter, line);
    }

    private static ProgressionImage image(String sprite, String hoverKey, double x, double y) {
        return new ProgressionImage(sprite, hoverKey, x, y);
    }

    private static ProgressionCriterion item(String name, String path) {
        return ProgressionCriterion.inventory(name, id(path));
    }

    private static ProgressionCriterion crop(String name, String path) {
        return ProgressionCriterion.harvest(name, id(path));
    }

    private static ProgressionCriterion process(String name, ResourceId process, String liquid) {
        return ProgressionCriterion.process(name, process, id(liquid));
    }

    private static ResourceId id(String path) {
        return new ResourceId("alcoholic", path);
    }

    private static String quest(int suffix) {
        return "A1C0A01C" + hex(suffix);
    }

    private static String task(int suffix) {
        return "A1C0A01C" + hex(0x10000000 + suffix);
    }

    private static String hex(int value) {
        String raw = Integer.toHexString(value).toUpperCase();
        return "0".repeat(Math.max(0, 8 - raw.length())) + raw;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ProgressionCatalog catalog
                && Objects.equals(nodes, catalog.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes);
    }
}

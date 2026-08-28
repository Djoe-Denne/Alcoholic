package com.djden.alcoholic.application.progression;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Renders the optional FTB chapter templates from {@link ProgressionCatalog}.
 */
public final class FtbQuestSnbt {
    private FtbQuestSnbt() {
    }

    public static void write(Path chaptersDir, ProgressionCatalog catalog) throws IOException {
        Files.createDirectories(chaptersDir);
        for (ProgressionChapterSpec spec : catalog.chapters()) {
            Files.writeString(
                    chaptersDir.resolve(spec.filename() + ".snbt"),
                    render(spec, catalog.nodes(spec.chapter())),
                    StandardCharsets.UTF_8
            );
        }
    }

    public static String render(ProgressionChapterSpec spec, List<ProgressionNode> nodes) {
        Map<String, String> questHex = nodes.stream()
                .collect(Collectors.toMap(ProgressionNode::id, ProgressionNode::questHex));
        StringBuilder out = new StringBuilder();
        out.append("{\n");
        out.append("\tid: \"").append(spec.chapterHex()).append("\"\n");
        out.append("\tgroup: \"\"\n");
        out.append("\torder_index: ").append(spec.orderIndex()).append("\n");
        out.append("\tfilename: \"").append(spec.filename()).append("\"\n");
        out.append("\ttitle: \"{translate:").append(spec.titleKey()).append("}\"\n");
        out.append("\ticon: \"").append(spec.icon()).append("\"\n");
        out.append("\tdefault_quest_shape: \"").append(spec.defaultQuestShape()).append("\"\n");
        out.append("\tdefault_hide_dependency_lines: false\n");
        out.append("\tsubtitle: [\n");
        out.append("\t\t\"{translate:").append(spec.subtitleKey()).append("}\"\n");
        out.append("\t]\n");
        out.append("\timages: [\n");
        for (int i = 0; i < spec.images().size(); i++) {
            ProgressionImage image = spec.images().get(i);
            out.append("\t\t{\n");
            out.append("\t\t\tx: ").append(decimal(image.x())).append("\n");
            out.append("\t\t\ty: ").append(decimal(image.y())).append("\n");
            out.append("\t\t\twidth: 2.0d\n");
            out.append("\t\t\theight: 2.0d\n");
            out.append("\t\t\trotation: 0.0d\n");
            out.append("\t\t\timage: \"").append(image.atlasSprite()).append("\"\n");
            out.append("\t\t\thover: [\"{translate:").append(image.hoverKey()).append("}\"]\n");
            out.append("\t\t\tclick: \"\"\n");
            out.append("\t\t\torder: 0\n");
            out.append("\t\t}");
            if (i + 1 < spec.images().size()) {
                out.append(",");
            }
            out.append("\n");
        }
        out.append("\t]\n");
        out.append("\tquests: [\n");
        for (int i = 0; i < nodes.size(); i++) {
            ProgressionNode node = nodes.get(i);
            out.append("\t\t{\n");
            out.append("\t\t\tid: \"").append(node.questHex()).append("\"\n");
            out.append("\t\t\tx: ").append(decimal(node.canvasX())).append("\n");
            out.append("\t\t\ty: ").append(decimal(node.canvasY())).append("\n");
            out.append("\t\t\ttitle: \"{translate:advancements.alcoholic.").append(node.id()).append(".title}\"\n");
            out.append("\t\t\tdescription: [\"{translate:advancements.alcoholic.")
                    .append(node.id())
                    .append(".description}\"]\n");
            out.append("\t\t\ticon: \"alcoholic:").append(node.icon()).append("\"\n");
            if (!node.parents().isEmpty()) {
                out.append("\t\t\tdependencies: [");
                for (int p = 0; p < node.parents().size(); p++) {
                    if (p > 0) {
                        out.append(", ");
                    }
                    out.append("\"").append(questHex.get(node.parents().get(p))).append("\"");
                }
                out.append("]\n");
            }
            if (node.junctionOr()) {
                out.append("\t\t\tmin_required_dependencies: ").append(node.minRequiredDependencies()).append("\n");
            }
            out.append("\t\t\ttasks: [{\n");
            out.append("\t\t\t\tid: \"").append(node.taskHex()).append("\"\n");
            out.append("\t\t\t\ttype: \"advancement\"\n");
            out.append("\t\t\t\tadvancement: \"").append(node.advancementId()).append("\"\n");
            out.append("\t\t\t\tcriterion: \"\"\n");
            out.append("\t\t\t}]\n");
            out.append("\t\t}");
            if (i + 1 < nodes.size()) {
                out.append(",");
            }
            out.append("\n");
        }
        out.append("\t]\n");
        out.append("\tquest_links: [\n");
        out.append("\t]\n");
        out.append("}\n");
        return out.toString();
    }

    private static String decimal(double value) {
        if (value == Math.rint(value)) {
            return String.format(Locale.US, "%.1fd", value);
        }
        return value + "d";
    }
}

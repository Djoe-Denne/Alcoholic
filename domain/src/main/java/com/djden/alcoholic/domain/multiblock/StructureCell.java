package com.djden.alcoholic.domain.multiblock;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A sampled structure cell. Tag ids are {@link com.djden.alcoholic.api.ResourceId}
 * strings so the domain never imports Minecraft tags.
 */
public record StructureCell(
        CellPresence presence,
        Optional<PartRole> role,
        Set<String> tags,
        Optional<String> blockId
) {
    public StructureCell {
        Objects.requireNonNull(presence, "presence");
        role = role == null ? Optional.empty() : role;
        tags = tags == null ? Set.of() : Set.copyOf(tags);
        blockId = blockId == null ? Optional.empty() : blockId;
    }

    public static StructureCell air() {
        return new StructureCell(CellPresence.AIR, Optional.empty(), Set.of(), Optional.empty());
    }

    public static StructureCell unloaded() {
        return new StructureCell(CellPresence.UNLOADED, Optional.empty(), Set.of(), Optional.empty());
    }

    public static StructureCell obstruction(String blockId) {
        return new StructureCell(
                CellPresence.OBSTRUCTION,
                Optional.empty(),
                Set.of(),
                Optional.ofNullable(blockId)
        );
    }

    public static StructureCell structure(PartRole role, Set<String> tags, String blockId) {
        return new StructureCell(
                CellPresence.STRUCTURE,
                Optional.of(role),
                tags,
                Optional.ofNullable(blockId)
        );
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }
}

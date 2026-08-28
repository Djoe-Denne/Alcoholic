package com.djden.alcoholic.application.machine;

import com.djden.alcoholic.domain.multiblock.CellCoord;
import com.djden.alcoholic.domain.multiblock.IndustrialHullPattern;
import com.djden.alcoholic.domain.multiblock.MultiblockConstraints;
import com.djden.alcoholic.domain.multiblock.MultiblockDefinition;
import com.djden.alcoholic.domain.multiblock.PartRole;
import com.djden.alcoholic.domain.multiblock.StructureCell;
import com.djden.alcoholic.domain.multiblock.StructureQuery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Projects each {@link MultiblockDefinition} through {@link IndustrialHullPattern}.
 */
public final class MultiblockDisplayRecipes {
    private MultiblockDisplayRecipes() {
    }

    public static List<MultiblockDisplayRecipe> from(MachineCatalog catalog) {
        Objects.requireNonNull(catalog, "catalog");
        List<MultiblockDisplayRecipe> recipes = new ArrayList<>();
        for (MultiblockDefinition definition : catalog.machines().values()) {
            recipes.add(from(definition));
        }
        return List.copyOf(recipes);
    }

    public static MultiblockDisplayRecipe from(MultiblockDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        MultiblockConstraints constraints = definition.constraints();
        boolean kinetic = definition.kinetic().required();
        StructureQuery query = IndustrialHullPattern.query(
                constraints.minWidth(),
                constraints.minHeight(),
                constraints.minDepth(),
                kinetic,
                first(constraints.casingTags(), "alcoholic:industrial_tank_casing"),
                first(constraints.windowTags(), "alcoholic:valid_machine_windows"),
                first(constraints.portTags(), "alcoholic:industrial_ports"),
                definition.controllerBlockId()
        );
        List<MultiblockDisplayRecipe.Layer> layers = new ArrayList<>();
        Map<String, CountedRole> counts = new LinkedHashMap<>();
        for (int y = 0; y < constraints.minHeight(); y++) {
            List<MultiblockDisplayRecipe.Cell> cells = new ArrayList<>();
            for (int z = 0; z < constraints.minDepth(); z++) {
                for (int x = 0; x < constraints.minWidth(); x++) {
                    StructureCell cell = query.cell(new CellCoord(x, y, z));
                    if (cell.role().isEmpty() || cell.blockId().isEmpty()) {
                        continue;
                    }
                    PartRole role = cell.role().orElseThrow();
                    String blockId = cell.blockId().orElseThrow();
                    cells.add(new MultiblockDisplayRecipe.Cell(x, z, role, blockId));
                    counts.merge(blockId, new CountedRole(role, 1), (left, right) ->
                            new CountedRole(left.role(), left.count() + right.count()));
                }
            }
            layers.add(new MultiblockDisplayRecipe.Layer(y, cells));
        }
        List<MultiblockDisplayRecipe.Ingredient> ingredients = new ArrayList<>();
        counts.forEach((blockId, counted) -> ingredients.add(
                new MultiblockDisplayRecipe.Ingredient(blockId, counted.count(), counted.role())
        ));
        return new MultiblockDisplayRecipe(
                definition.id(),
                definition.controllerBlockId(),
                constraints.minWidth(),
                constraints.minHeight(),
                constraints.minDepth(),
                constraints.maxWidth(),
                constraints.maxHeight(),
                constraints.maxDepth(),
                kinetic,
                layers,
                ingredients
        );
    }

    private static String first(Set<String> tags, String fallback) {
        return tags.stream().sorted().findFirst().orElse(fallback);
    }

    private record CountedRole(PartRole role, int count) {
    }
}

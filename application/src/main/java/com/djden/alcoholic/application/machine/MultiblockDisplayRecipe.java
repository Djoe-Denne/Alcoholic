package com.djden.alcoholic.application.machine;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.multiblock.PartRole;

import java.util.List;
import java.util.Objects;

/**
 * Viewer-facing min-hull layers and ingredient counts. No recipe-viewer types.
 */
public record MultiblockDisplayRecipe(
        ResourceId definitionId,
        String controllerBlockId,
        int minWidth,
        int minHeight,
        int minDepth,
        int maxWidth,
        int maxHeight,
        int maxDepth,
        boolean kineticRequired,
        List<Layer> layers,
        List<Ingredient> ingredients
) {
    public MultiblockDisplayRecipe {
        Objects.requireNonNull(definitionId, "definitionId");
        Objects.requireNonNull(controllerBlockId, "controllerBlockId");
        layers = List.copyOf(Objects.requireNonNull(layers, "layers"));
        ingredients = List.copyOf(Objects.requireNonNull(ingredients, "ingredients"));
    }

    public record Layer(int y, List<Cell> cells) {
        public Layer {
            cells = List.copyOf(Objects.requireNonNull(cells, "cells"));
        }
    }

    public record Cell(int x, int z, PartRole role, String blockId) {
        public Cell {
            Objects.requireNonNull(role, "role");
            Objects.requireNonNull(blockId, "blockId");
        }
    }

    public record Ingredient(String blockId, int count, PartRole role) {
        public Ingredient {
            Objects.requireNonNull(blockId, "blockId");
            Objects.requireNonNull(role, "role");
            if (count < 1) {
                throw new IllegalArgumentException("count must be >= 1");
            }
        }
    }
}

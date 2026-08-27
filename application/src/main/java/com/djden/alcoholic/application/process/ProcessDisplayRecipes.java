package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ProcessDisplaySpec;
import com.djden.alcoholic.api.process.ProcessType;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.domain.beverage.InputReference;
import com.djden.alcoholic.domain.process.ProcessDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Projects catalog processes through the registered {@link ProcessType} display
 * port. Unknown types fall back to declared item inputs only. Decode failures
 * hide the recipe instead of inventing a generic one.
 */
public final class ProcessDisplayRecipes {
    private ProcessDisplayRecipes() {
    }

    public static List<ProcessDisplayRecipe> fromCatalog(BeverageCatalog catalog) {
        return fromCatalog(catalog, AlcoholicApi.shared());
    }

    public static List<ProcessDisplayRecipe> fromCatalog(BeverageCatalog catalog, AlcoholicApi api) {
        Objects.requireNonNull(catalog, "catalog");
        Objects.requireNonNull(api, "api");
        List<ProcessDisplayRecipe> recipes = new ArrayList<>();
        for (ProcessDefinition definition : catalog.processes().values()) {
            from(definition, api).ifPresent(recipes::add);
        }
        return List.copyOf(recipes);
    }

    public static Map<ResourceId, List<ProcessDisplayRecipe>> groupByType(BeverageCatalog catalog) {
        return groupByType(catalog, AlcoholicApi.shared());
    }

    public static Map<ResourceId, List<ProcessDisplayRecipe>> groupByType(BeverageCatalog catalog, AlcoholicApi api) {
        Map<ResourceId, List<ProcessDisplayRecipe>> grouped = new LinkedHashMap<>();
        for (ProcessDisplayRecipe recipe : fromCatalog(catalog, api)) {
            grouped.computeIfAbsent(recipe.processType(), key -> new ArrayList<>()).add(recipe);
        }
        grouped.replaceAll((key, value) -> List.copyOf(value));
        return Map.copyOf(grouped);
    }

    public static Optional<ProcessDisplayRecipe> from(ProcessDefinition definition) {
        return from(definition, AlcoholicApi.shared());
    }

    public static Optional<ProcessDisplayRecipe> from(ProcessDefinition definition, AlcoholicApi api) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(api, "api");
        Optional<ProcessType<?>> type = api.processes().get(definition.processType());
        if (type.isEmpty()) {
            return finish(fromDeclaredInputs(definition));
        }
        try {
            Object decoded = type.get().configCodec().decode(definition.config());
            ProcessDisplaySpec spec = type.get().displayDecoded(decoded);
            ProcessDisplayRecipe typed = new ProcessDisplayRecipe(definition.id(), definition.processType(), spec);
            if (typed.visible()) {
                return Optional.of(typed);
            }
            return finish(fromDeclaredInputs(definition));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private static ProcessDisplayRecipe fromDeclaredInputs(ProcessDefinition definition) {
        ProcessDisplaySpec.Builder builder = ProcessDisplaySpec.builder();
        for (InputReference input : definition.inputs().values()) {
            input.toSelector().ifPresent(selector -> builder.itemIn(selector, 1));
        }
        return new ProcessDisplayRecipe(definition.id(), definition.processType(), builder.build());
    }

    private static Optional<ProcessDisplayRecipe> finish(ProcessDisplayRecipe recipe) {
        return recipe.visible() ? Optional.of(recipe) : Optional.empty();
    }
}

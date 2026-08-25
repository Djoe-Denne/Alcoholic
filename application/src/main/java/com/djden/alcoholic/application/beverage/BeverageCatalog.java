package com.djden.alcoholic.application.beverage;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.beverage.BeverageDefinition;
import com.djden.alcoholic.domain.ingredient.IngredientDefinition;
import com.djden.alcoholic.domain.liquid.LiquidDefinition;
import com.djden.alcoholic.domain.process.ProcessDefinition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record BeverageCatalog(
        Map<ResourceId, IngredientDefinition> ingredients,
        Map<ResourceId, ProcessDefinition> processes,
        Map<ResourceId, BeverageDefinition> beverages,
        Map<ResourceId, LiquidDefinition> liquids
) {
    public BeverageCatalog {
        ingredients = copy(ingredients, "ingredients");
        processes = copy(processes, "processes");
        beverages = copy(beverages, "beverages");
        liquids = copy(liquids, "liquids");
    }

    public BeverageCatalog(
            Map<ResourceId, IngredientDefinition> ingredients,
            Map<ResourceId, ProcessDefinition> processes,
            Map<ResourceId, BeverageDefinition> beverages
    ) {
        this(ingredients, processes, beverages, Map.of());
    }

    public static BeverageCatalog empty() {
        return new BeverageCatalog(Map.of(), Map.of(), Map.of(), Map.of());
    }

    public Optional<IngredientDefinition> ingredient(ResourceId id) {
        return Optional.ofNullable(ingredients.get(id));
    }

    public Optional<ProcessDefinition> process(ResourceId id) {
        return Optional.ofNullable(processes.get(id));
    }

    public Optional<BeverageDefinition> beverage(ResourceId id) {
        return Optional.ofNullable(beverages.get(id));
    }

    public Optional<LiquidDefinition> liquid(ResourceId id) {
        return Optional.ofNullable(liquids.get(id));
    }

    private static <T> Map<ResourceId, T> copy(Map<ResourceId, T> values, String name) {
        Map<ResourceId, T> copy = new LinkedHashMap<>();
        Objects.requireNonNull(values, name).forEach((key, value) -> copy.put(
                Objects.requireNonNull(key, "id"),
                Objects.requireNonNull(value, name)
        ));
        return Map.copyOf(copy);
    }
}

package com.djden.alcoholic.integration.create;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.ingredient.IngredientSelector;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.process.MillConfig;
import com.djden.alcoholic.domain.beverage.InputReference;
import com.djden.alcoholic.domain.process.ProcessDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Translates generic MILL definitions into Create millstone and crushing specs.
 * Transformation stays in the MILL handler; this only maps identifiers.
 */
public final class CreateMillRecipeTranslator {
    private CreateMillRecipeTranslator() {
    }

    public static Optional<MillingRecipeSpec> from(ProcessDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        if (!BuiltinRegistrations.MILL.equals(definition.processType())) {
            return Optional.empty();
        }
        MillConfig config = MillConfig.CODEC.decode(definition.config());
        if (!config.createCompatible() || !config.executable()) {
            return Optional.empty();
        }
        IngredientSelector selector = config.inputSelector()
                .or(() -> firstSelector(definition))
                .orElse(null);
        if (selector == null) {
            return Optional.empty();
        }
        return Optional.of(new MillingRecipeSpec(
                definition.id(),
                selector,
                config.inputAmount(),
                config.outputItem().orElseThrow(),
                config.outputAmount(),
                config.processingTicks()
        ));
    }

    public static String toMillingJson(MillingRecipeSpec spec) {
        return toCreateJson(spec, "create:milling", spec.processingTicks());
    }

    public static String toCrushingJson(MillingRecipeSpec spec) {
        int ticks = Math.max(1, spec.processingTicks() / 2);
        return toCreateJson(spec, "create:crushing", ticks);
    }

    public static List<MillingRecipeSpec> fromCatalog(Iterable<ProcessDefinition> definitions) {
        List<MillingRecipeSpec> specs = new ArrayList<>();
        for (ProcessDefinition definition : definitions) {
            from(definition).ifPresent(specs::add);
        }
        return List.copyOf(specs);
    }

    private static String toCreateJson(MillingRecipeSpec spec, String type, int ticks) {
        Objects.requireNonNull(spec, "spec");
        StringBuilder ingredients = new StringBuilder();
        String ingredient = ingredientJson(spec.input());
        for (int index = 0; index < spec.inputCount(); index++) {
            if (index > 0) {
                ingredients.append(",\n    ");
            }
            ingredients.append(ingredient);
        }
        return """
                {
                  "type": "%s",
                  "processingTime": %d,
                  "ingredients": [
                    %s
                  ],
                  "results": [
                    { "item": "%s", "count": %d }
                  ]
                }
                """.formatted(type, ticks, ingredients, spec.outputItem(), spec.outputCount());
    }

    private static Optional<IngredientSelector> firstSelector(ProcessDefinition definition) {
        return definition.inputs().values().stream()
                .map(InputReference::toSelector)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static String ingredientJson(IngredientSelector selector) {
        if (selector instanceof IngredientSelector.Tag tag) {
            return "{ \"tag\": \"" + tag.id() + "\" }";
        }
        if (selector instanceof IngredientSelector.Item item) {
            return "{ \"item\": \"" + item.id() + "\" }";
        }
        if (selector instanceof IngredientSelector.DefinedIngredient ingredient) {
            return "{ \"item\": \"" + ingredient.id() + "\" }";
        }
        throw new IllegalArgumentException("unsupported selector " + selector);
    }

    public record MillingRecipeSpec(
            ResourceId id,
            IngredientSelector input,
            int inputCount,
            ResourceId outputItem,
            int outputCount,
            int processingTicks
    ) {
        public MillingRecipeSpec {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(input, "input");
            Objects.requireNonNull(outputItem, "outputItem");
        }
    }
}

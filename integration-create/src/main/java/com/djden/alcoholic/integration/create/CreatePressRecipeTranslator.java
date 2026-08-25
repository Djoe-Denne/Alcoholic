package com.djden.alcoholic.integration.create;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.ingredient.IngredientSelector;
import com.djden.alcoholic.api.process.ItemOutput;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.process.PressConfig;
import com.djden.alcoholic.domain.beverage.InputReference;
import com.djden.alcoholic.domain.process.ProcessDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Translates generic PRESS definitions into Create Mechanical Press (basin compacting) specs.
 * Transformation rules stay in the PRESS handler; this only maps identifiers.
 */
public final class CreatePressRecipeTranslator {
    private CreatePressRecipeTranslator() {
    }

    public static Optional<MechanicalPressRecipeSpec> from(ProcessDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        if (!BuiltinRegistrations.PRESS.equals(definition.processType())) {
            return Optional.empty();
        }
        PressConfig config = PressConfig.CODEC.decode(definition.config());
        if (!config.createCompatible() || !config.executable()) {
            return Optional.empty();
        }
        IngredientSelector selector = config.inputSelector()
                .or(() -> firstSelector(definition))
                .orElse(null);
        if (selector == null) {
            return Optional.empty();
        }
        return Optional.of(new MechanicalPressRecipeSpec(
                definition.id(),
                selector,
                config.inputAmount(),
                config.outputLiquid().orElseThrow(),
                (int) Math.round(config.outputVolume() * config.yield()),
                config.byproduct()
        ));
    }

    public static String toCompactingJson(MechanicalPressRecipeSpec spec) {
        Objects.requireNonNull(spec, "spec");
        StringBuilder ingredients = new StringBuilder();
        String ingredient = ingredientJson(spec.input());
        for (int index = 0; index < spec.inputCount(); index++) {
            if (index > 0) {
                ingredients.append(",\n    ");
            }
            ingredients.append(ingredient);
        }
        StringBuilder results = new StringBuilder();
        spec.byproduct().ifPresent(item -> results
                .append("    { \"item\": \"")
                .append(item.item())
                .append("\", \"count\": ")
                .append(Math.max(1, item.amount()))
                .append(" },\n")
        );
        results.append("    { \"fluid\": \"")
                .append(spec.outputFluid())
                .append("\", \"amount\": ")
                .append(spec.outputMillibuckets())
                .append(" }");
        return """
                {
                  "type": "create:compacting",
                  "ingredients": [
                    %s
                  ],
                  "results": [
                %s
                  ]
                }
                """.formatted(ingredients, results);
    }

    public static List<MechanicalPressRecipeSpec> fromCatalog(
            Iterable<ProcessDefinition> definitions
    ) {
        List<MechanicalPressRecipeSpec> specs = new ArrayList<>();
        for (ProcessDefinition definition : definitions) {
            from(definition).ifPresent(specs::add);
        }
        return List.copyOf(specs);
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

    public record MechanicalPressRecipeSpec(
            ResourceId id,
            IngredientSelector input,
            int inputCount,
            ResourceId outputFluid,
            int outputMillibuckets,
            Optional<ItemOutput> byproduct
    ) {
        public MechanicalPressRecipeSpec {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(input, "input");
            Objects.requireNonNull(outputFluid, "outputFluid");
            byproduct = byproduct == null ? Optional.empty() : byproduct;
        }
    }
}

package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ingredient.IngredientSelector;
import com.djden.alcoholic.api.ingredient.SolidInputView;
import com.djden.alcoholic.api.liquid.LiquidBatchView;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@PublicApi
public record ProcessRequest(
        Map<String, IngredientSelector> selectors,
        Map<String, List<SolidInputView>> solids,
        Map<String, LiquidBatchView> liquids
) {
    public ProcessRequest {
        selectors = Map.copyOf(Objects.requireNonNull(selectors, "selectors"));
        solids = ProcessInputs.copyOfSolids(solids);
        liquids = Map.copyOf(Objects.requireNonNull(liquids, "liquids"));
    }

    public ProcessRequest(
            Map<String, IngredientSelector> selectors,
            Map<String, LiquidBatchView> liquids
    ) {
        this(selectors, Map.of(), liquids);
    }

    public static ProcessRequest empty() {
        return new ProcessRequest(Map.of(), Map.of(), Map.of());
    }

    public static ProcessRequest of(ProcessInputs inputs) {
        Objects.requireNonNull(inputs, "inputs");
        return new ProcessRequest(Map.of(), inputs.solids(), inputs.liquids());
    }

    public ProcessInputs asInputs() {
        return new ProcessInputs(solids, liquids);
    }
}

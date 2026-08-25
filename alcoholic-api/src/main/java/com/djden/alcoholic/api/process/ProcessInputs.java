package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ingredient.SolidInputView;
import com.djden.alcoholic.api.liquid.LiquidBatchView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@PublicApi
public record ProcessInputs(
        Map<String, List<SolidInputView>> solids,
        Map<String, LiquidBatchView> liquids
) {
    public ProcessInputs {
        solids = copySolids(solids);
        liquids = Map.copyOf(Objects.requireNonNull(liquids, "liquids"));
    }

    public static ProcessInputs empty() {
        return new ProcessInputs(Map.of(), Map.of());
    }

    public static ProcessInputs ofLiquid(String port, LiquidBatchView batch) {
        return new ProcessInputs(Map.of(), Map.of(port, batch));
    }

    public static ProcessInputs ofSolids(String port, List<? extends SolidInputView> items) {
        return new ProcessInputs(Map.of(port, List.copyOf(items)), Map.of());
    }

    static Map<String, List<SolidInputView>> copyOfSolids(
            Map<String, List<SolidInputView>> solids
    ) {
        return copySolids(solids);
    }

    private static Map<String, List<SolidInputView>> copySolids(
            Map<String, List<SolidInputView>> solids
    ) {
        Objects.requireNonNull(solids, "solids");
        Map<String, List<SolidInputView>> copy = new LinkedHashMap<>();
        solids.forEach((port, items) -> copy.put(
                Objects.requireNonNull(port, "port"),
                List.copyOf(new ArrayList<>(Objects.requireNonNull(items, "items")))
        ));
        return Map.copyOf(copy);
    }
}

package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ingredient.SolidInputView;
import com.djden.alcoholic.api.liquid.LiquidBatchView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

    public static ProcessInputs of(
            String solidPort,
            List<? extends SolidInputView> items,
            String liquidPort,
            LiquidBatchView batch
    ) {
        return new ProcessInputs(Map.of(solidPort, List.copyOf(items)), Map.of(liquidPort, batch));
    }

    /**
     * Returns solids on the first named port that exists. If none of the
     * preferred ports are present and exactly one port exists, that port is
     * used. Multiple unknown ports never flatten together.
     */
    public List<SolidInputView> solidsOn(String... ports) {
        for (String port : ports) {
            List<SolidInputView> found = solids.get(port);
            if (found != null) {
                return found;
            }
        }
        if (solids.size() == 1) {
            return solids.values().iterator().next();
        }
        return List.of();
    }

    /**
     * Returns the liquid on the first named port that exists. If none of the
     * preferred ports are present and exactly one port exists, that port is
     * used.
     */
    public Optional<LiquidBatchView> liquidOn(String... ports) {
        for (String port : ports) {
            LiquidBatchView found = liquids.get(port);
            if (found != null) {
                return Optional.of(found);
            }
        }
        if (liquids.size() == 1) {
            return Optional.of(liquids.values().iterator().next());
        }
        return Optional.empty();
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

package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.ingredient.IngredientSelector;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.api.process.ProcessType;
import com.djden.alcoholic.api.process.SolidAccepting;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.domain.beverage.InputReference;
import com.djden.alcoholic.domain.beverage.ProcessNode;
import com.djden.alcoholic.domain.process.ProcessDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Binds offered inputs to catalog process definitions. No process type is implied.
 */
public final class ProcessRecipeResolver {
    private ProcessRecipeResolver() {
    }

    public static Optional<ProcessInvocation> find(
            BeverageCatalog catalog,
            ResourceId processType,
            SelectorMatcher matcher,
            Optional<ResourceId> item,
            Optional<ResourceId> liquid
    ) {
        return find(catalog, null, processType, matcher, item, liquid);
    }

    public static Optional<ProcessInvocation> find(
            BeverageCatalog catalog,
            AlcoholicApi api,
            ResourceId processType,
            SelectorMatcher matcher,
            Optional<ResourceId> item,
            Optional<ResourceId> liquid
    ) {
        return find(catalog, api, processType, matcher, item, liquid, Optional.empty());
    }

    public static Optional<ProcessInvocation> find(
            BeverageCatalog catalog,
            AlcoholicApi api,
            ResourceId processType,
            SelectorMatcher matcher,
            Optional<ResourceId> item,
            Optional<ResourceId> liquid,
            Optional<ResourceId> definitionId
    ) {
        Objects.requireNonNull(catalog, "catalog");
        Objects.requireNonNull(processType, "processType");
        Objects.requireNonNull(matcher, "matcher");
        Objects.requireNonNull(definitionId, "definitionId");
        Optional<ProcessType<?>> type = api == null ? Optional.empty() : api.processes().get(processType);
        if (definitionId.isPresent()) {
            Optional<ProcessDefinition> selected = catalog.process(definitionId.get());
            if (selected.isEmpty() || !processType.equals(selected.get().processType())) {
                return Optional.empty();
            }
            if (!matches(selected.get(), type, matcher, item, liquid)) {
                return Optional.empty();
            }
            return Optional.of(new ProcessInvocation(
                    selected.get().processType(),
                    selected.get().config(),
                    selected.get().id().toString()
            ));
        }
        List<ProcessInvocation> catalogMatches = new ArrayList<>();
        for (ProcessDefinition definition : catalog.processes().values()) {
            if (!processType.equals(definition.processType())) {
                continue;
            }
            if (matches(definition, type, matcher, item, liquid)) {
                catalogMatches.add(new ProcessInvocation(
                        definition.processType(),
                        definition.config(),
                        definition.id().toString()
                ));
            }
        }
        if (catalogMatches.size() == 1) {
            return Optional.of(catalogMatches.get(0));
        }
        if (catalogMatches.size() > 1) {
            return Optional.empty();
        }
        List<ProcessInvocation> nodeMatches = new ArrayList<>();
        for (var beverage : catalog.beverages().values()) {
            for (ProcessNode node : beverage.graph().nodes()) {
                if (node.processType().filter(processType::equals).isEmpty()) {
                    continue;
                }
                if (matchesNode(node, type, matcher, item, liquid)) {
                    nodeMatches.add(new ProcessInvocation(
                            processType,
                            node.config(),
                            beverage.id() + "/" + node.id()
                    ));
                }
            }
        }
        if (nodeMatches.size() == 1) {
            return Optional.of(nodeMatches.get(0));
        }
        return Optional.empty();
    }

    private static boolean matches(
            ProcessDefinition definition,
            Optional<ProcessType<?>> type,
            SelectorMatcher matcher,
            Optional<ResourceId> item,
            Optional<ResourceId> liquid
    ) {
        if (item.isPresent() && !matchesSolid(definition.inputs().values(), definition.config(), type, matcher, item.get())) {
            return false;
        }
        return liquid.isEmpty() || acceptsLiquid(type, definition.config(), liquid.get());
    }

    private static boolean matchesNode(
            ProcessNode node,
            Optional<ProcessType<?>> type,
            SelectorMatcher matcher,
            Optional<ResourceId> item,
            Optional<ResourceId> liquid
    ) {
        if (item.isPresent() && !matchesSolid(node.inputs().values(), node.config(), type, matcher, item.get())) {
            return false;
        }
        return liquid.isEmpty() || acceptsLiquid(type, node.config(), liquid.get());
    }

    private static boolean matchesSolid(
            Iterable<InputReference> inputs,
            com.djden.alcoholic.api.data.DataNode config,
            Optional<ProcessType<?>> type,
            SelectorMatcher matcher,
            ResourceId item
    ) {
        if (type.isPresent()) {
            try {
                Object decoded = type.get().configCodec().decode(config);
                if (decoded instanceof SolidAccepting accepting && accepting.inputSelector().isPresent()) {
                    return type.get().acceptsDecodedSolid(decoded, item, matcher::matches);
                }
            } catch (RuntimeException ignored) {
                // Fall through to declared graph inputs.
            }
        }
        return matchesSelectors(inputs, matcher, item);
    }

    private static boolean matchesSelectors(
            Iterable<InputReference> inputs,
            SelectorMatcher matcher,
            ResourceId item
    ) {
        for (InputReference input : inputs) {
            Optional<IngredientSelector> selector = input.toSelector();
            if (selector.isPresent() && matcher.matches(selector.get(), item)) {
                return true;
            }
        }
        return false;
    }

    private static boolean acceptsLiquid(
            Optional<ProcessType<?>> type,
            com.djden.alcoholic.api.data.DataNode config,
            ResourceId liquid
    ) {
        if (type.isEmpty()) {
            return false;
        }
        try {
            Object decoded = type.get().configCodec().decode(config);
            return type.get().acceptsDecodedLiquid(decoded, liquid);
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}

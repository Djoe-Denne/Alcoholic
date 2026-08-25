package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.ingredient.IngredientSelector;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.api.process.ProcessType;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.domain.beverage.InputReference;
import com.djden.alcoholic.domain.beverage.ProcessNode;
import com.djden.alcoholic.domain.process.ProcessDefinition;

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
        Objects.requireNonNull(catalog, "catalog");
        Objects.requireNonNull(processType, "processType");
        Objects.requireNonNull(matcher, "matcher");
        Optional<ProcessType<?>> type = api == null ? Optional.empty() : api.processes().get(processType);
        for (ProcessDefinition definition : catalog.processes().values()) {
            if (!processType.equals(definition.processType())) {
                continue;
            }
            if (matches(definition, type, matcher, item, liquid)) {
                return Optional.of(new ProcessInvocation(
                        definition.processType(),
                        definition.config(),
                        definition.id().toString()
                ));
            }
        }
        for (var beverage : catalog.beverages().values()) {
            for (ProcessNode node : beverage.graph().nodes()) {
                if (node.processType().filter(processType::equals).isEmpty()) {
                    continue;
                }
                if (matchesNode(node, type, matcher, item, liquid)) {
                    return Optional.of(new ProcessInvocation(
                            processType,
                            node.config(),
                            beverage.id() + "/" + node.id()
                    ));
                }
            }
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
        if (item.isPresent() && !matchesSelectors(definition.inputs().values(), matcher, item.get())
                && !matchesConfigSelector(definition.config(), matcher, item.get())) {
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
        if (item.isPresent() && !matchesSelectors(node.inputs().values(), matcher, item.get())
                && !matchesConfigSelector(node.config(), matcher, item.get())) {
            return false;
        }
        return liquid.isEmpty() || acceptsLiquid(type, node.config(), liquid.get());
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

    private static boolean matchesConfigSelector(
            com.djden.alcoholic.api.data.DataNode config,
            SelectorMatcher matcher,
            ResourceId item
    ) {
        try {
            PressConfig press = PressConfig.CODEC.decode(config);
            return press.inputSelector().filter(selector -> matcher.matches(selector, item)).isPresent();
        } catch (RuntimeException ignored) {
            return false;
        }
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

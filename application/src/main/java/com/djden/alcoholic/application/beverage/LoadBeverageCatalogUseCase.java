package com.djden.alcoholic.application.beverage;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.application.beverage.codec.BeverageDefinitionCodec;
import com.djden.alcoholic.application.beverage.codec.IngredientDefinitionCodec;
import com.djden.alcoholic.application.beverage.codec.LiquidDefinitionCodec;
import com.djden.alcoholic.application.beverage.codec.ProcessDefinitionCodec;
import com.djden.alcoholic.domain.beverage.BeverageDefinition;
import com.djden.alcoholic.domain.beverage.InputReference;
import com.djden.alcoholic.domain.beverage.ProcessGraph;
import com.djden.alcoholic.domain.beverage.ProcessNode;
import com.djden.alcoholic.domain.ingredient.IngredientDefinition;
import com.djden.alcoholic.domain.liquid.LiquidDefinition;
import com.djden.alcoholic.domain.process.ProcessDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class LoadBeverageCatalogUseCase {
    private final ValidateBeverageCatalogUseCase validator = new ValidateBeverageCatalogUseCase();

    public BeverageCatalog load(
            Map<ResourceId, DataNode> ingredients,
            Map<ResourceId, DataNode> processes,
            Map<ResourceId, DataNode> beverages,
            AlcoholicApi api
    ) {
        return load(ingredients, processes, beverages, Map.of(), api);
    }

    public BeverageCatalog load(
            Map<ResourceId, DataNode> ingredients,
            Map<ResourceId, DataNode> processes,
            Map<ResourceId, DataNode> beverages,
            Map<ResourceId, DataNode> liquids,
            AlcoholicApi api
    ) {
        Objects.requireNonNull(api, "api");
        List<ValidationIssue> issues = new ArrayList<>();
        Map<ResourceId, IngredientDefinition> decodedIngredients = decodeAll(
                ingredients,
                "ingredients",
                (node, path, fallback) -> IngredientDefinitionCodec.INSTANCE.decode(node, path, fallback),
                IngredientDefinition::id,
                issues
        );
        Map<ResourceId, ProcessDefinition> decodedProcesses = decodeAll(
                processes,
                "processes",
                (node, path, fallback) -> ProcessDefinitionCodec.INSTANCE.decode(node, path, fallback),
                ProcessDefinition::id,
                issues
        );
        Map<ResourceId, BeverageDefinition> decodedBeverages = decodeAll(
                beverages,
                "beverages",
                (node, path, fallback) -> BeverageDefinitionCodec.INSTANCE.decode(node, path, fallback),
                BeverageDefinition::id,
                issues
        );
        Map<ResourceId, LiquidDefinition> decodedLiquids = decodeAll(
                liquids,
                "liquids",
                (node, path, fallback) -> LiquidDefinitionCodec.INSTANCE.decode(node, path, fallback),
                LiquidDefinition::id,
                issues
        );
        if (!issues.isEmpty()) {
            throw new IllegalArgumentException(new ValidationResult(issues).format());
        }

        BeverageCatalog raw = new BeverageCatalog(
                decodedIngredients,
                decodedProcesses,
                decodedBeverages,
                decodedLiquids
        );
        BeverageCatalog expanded = expand(raw);
        ValidationResult result = validator.validate(expanded, api);
        result.throwIfInvalid();
        return expanded;
    }

    private static BeverageCatalog expand(BeverageCatalog catalog) {
        Map<ResourceId, BeverageDefinition> beverages = new LinkedHashMap<>();
        catalog.beverages().forEach((id, definition) -> beverages.put(id, expand(definition, catalog)));
        return new BeverageCatalog(
                catalog.ingredients(),
                catalog.processes(),
                beverages,
                catalog.liquids()
        );
    }

    private static BeverageDefinition expand(BeverageDefinition definition, BeverageCatalog catalog) {
        List<ProcessNode> nodes = new ArrayList<>();
        for (ProcessNode node : definition.graph().nodes()) {
            Optional<ProcessDefinition> referenced = node.processDefinition().flatMap(catalog::process);
            if (referenced.isEmpty()) {
                nodes.add(node);
                continue;
            }
            ProcessDefinition process = referenced.get();
            Map<String, InputReference> inputs = new LinkedHashMap<>(process.inputs());
            inputs.putAll(node.inputs());
            List<String> outputs = node.outputs().isEmpty() ? process.outputs() : node.outputs();
            DataNode config = isEmptyObject(node.config()) ? process.config() : node.config();
            nodes.add(new ProcessNode(
                    node.id(),
                    Optional.of(node.processType().orElse(process.processType())),
                    node.processDefinition(),
                    config,
                    inputs,
                    outputs
            ));
        }
        return new BeverageDefinition(
                definition.id(),
                definition.category(),
                new ProcessGraph(nodes, definition.graph().outputs()),
                definition.properties()
        );
    }

    private static boolean isEmptyObject(DataNode node) {
        return node instanceof DataNode.ObjectNode object && object.fields().isEmpty();
    }

    private static <T> Map<ResourceId, T> decodeAll(
            Map<ResourceId, DataNode> sources,
            String folder,
            Decoder<T> decoder,
            java.util.function.Function<T, ResourceId> idAccessor,
            List<ValidationIssue> issues
    ) {
        Map<ResourceId, T> decoded = new LinkedHashMap<>();
        Objects.requireNonNull(sources, folder).forEach((source, node) -> {
            String path = folder + "/" + source;
            try {
                T value = decoder.decode(node, path, ProcessDefinitionCodec.fallbackId(source));
                ResourceId id = idAccessor.apply(value);
                T existing = decoded.putIfAbsent(id, value);
                if (existing != null) {
                    issues.add(new ValidationIssue(path, "duplicate id " + id));
                }
            } catch (RuntimeException exception) {
                issues.add(new ValidationIssue(path, exception.getMessage()));
            }
        });
        return decoded;
    }

    @FunctionalInterface
    private interface Decoder<T> {
        T decode(DataNode node, String path, ResourceId fallbackId);
    }
}

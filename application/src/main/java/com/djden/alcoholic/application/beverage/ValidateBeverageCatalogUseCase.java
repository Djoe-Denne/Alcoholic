package com.djden.alcoholic.application.beverage;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.process.ProcessType;
import com.djden.alcoholic.application.process.ReferencedLiquids;
import com.djden.alcoholic.domain.beverage.BeverageDefinition;
import com.djden.alcoholic.domain.beverage.GraphIssue;
import com.djden.alcoholic.domain.beverage.InputReference;
import com.djden.alcoholic.domain.beverage.ProcessGraphValidator;
import com.djden.alcoholic.domain.beverage.ProcessNode;
import com.djden.alcoholic.domain.process.ProcessDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ValidateBeverageCatalogUseCase {
    public ValidationResult validate(BeverageCatalog catalog, AlcoholicApi api) {
        Objects.requireNonNull(catalog, "catalog");
        Objects.requireNonNull(api, "api");
        List<ValidationIssue> issues = new ArrayList<>();
        catalog.processes().values().forEach(definition ->
                validateProcessDefinition(definition, catalog, api, issues)
        );
        catalog.beverages().values().forEach(definition ->
                validateBeverage(definition, catalog, api, issues)
        );
        catalog.liquids().values().forEach(definition ->
                validateLiquid(definition, api, issues)
        );
        return new ValidationResult(issues);
    }

    private static void validateLiquid(
            com.djden.alcoholic.domain.liquid.LiquidDefinition definition,
            AlcoholicApi api,
            List<ValidationIssue> issues
    ) {
        String path = "liquids/" + definition.id();
        definition.defaults().keySet().forEach(propertyId -> {
            if (api.properties().get(propertyId).isEmpty()) {
                issues.add(new ValidationIssue(path + "/defaults", "unknown property " + propertyId));
            }
        });
    }

    private static void validateProcessDefinition(
            ProcessDefinition definition,
            BeverageCatalog catalog,
            AlcoholicApi api,
            List<ValidationIssue> issues
    ) {
        String path = "processes/" + definition.id();
        Optional<ProcessType<?>> type = api.processes().get(definition.processType());
        if (type.isEmpty()) {
            issues.add(new ValidationIssue(
                    path + "/process",
                    "unknown process type " + definition.processType()
            ));
            return;
        }
        decodeConfig(type.get(), definition.config(), path + "/config", catalog, issues);
        definition.inputs().forEach((name, input) -> {
            if (input instanceof InputReference.NodeOutputInput) {
                issues.add(new ValidationIssue(
                        path + "/inputs/" + name,
                        "process definitions cannot reference graph nodes"
                ));
            }
            validateCatalogInput(input, catalog, path + "/inputs/" + name, issues, false);
        });
    }

    private static void validateBeverage(
            BeverageDefinition definition,
            BeverageCatalog catalog,
            AlcoholicApi api,
            List<ValidationIssue> issues
    ) {
        String path = "beverages/" + definition.id();
        for (ResourceId propertyId : definition.properties()) {
            if (api.properties().get(propertyId).isEmpty()) {
                issues.add(new ValidationIssue(path + "/properties", "unknown property " + propertyId));
            }
        }
        String graphPath = path + "/graph";
        for (GraphIssue issue : ProcessGraphValidator.validate(definition.graph(), graphPath)) {
            issues.add(new ValidationIssue(issue.path(), issue.message()));
        }
        for (ProcessNode node : definition.graph().nodes()) {
            String nodePath = graphPath + "/nodes/" + node.id();
            Optional<ProcessDefinition> referenced = node.processDefinition().flatMap(catalog::process);
            if (node.processDefinition().isPresent() && referenced.isEmpty()) {
                issues.add(new ValidationIssue(
                        nodePath + "/definition",
                        "unknown process definition " + node.processDefinition().orElseThrow()
                ));
            }
            if (referenced.isPresent() && node.processType().isPresent()
                    && !referenced.get().processType().equals(node.processType().orElseThrow())) {
                issues.add(new ValidationIssue(
                        nodePath + "/process",
                        "process type does not match definition " + referenced.get().processType()
                ));
            }
            Optional<ResourceId> processType = node.processType()
                    .or(() -> referenced.map(ProcessDefinition::processType));
            if (processType.isEmpty()) {
                issues.add(new ValidationIssue(nodePath, "node must declare a process type or definition"));
                continue;
            }
            Optional<ProcessType<?>> registered = api.processes().get(processType.get());
            if (registered.isEmpty()) {
                issues.add(new ValidationIssue(nodePath + "/process", "unknown process type " + processType.get()));
            } else {
                decodeConfig(registered.get(), node.config(), nodePath + "/config", catalog, issues);
            }
            if (node.outputs().isEmpty()) {
                issues.add(new ValidationIssue(nodePath + "/outputs", "node must declare at least one output"));
            }
            node.inputs().forEach((port, input) ->
                    validateCatalogInput(input, catalog, nodePath + "/inputs/" + port, issues, true)
            );
        }
    }

    private static void validateCatalogInput(
            InputReference input,
            BeverageCatalog catalog,
            String path,
            List<ValidationIssue> issues,
            boolean allowNode
    ) {
        if (input instanceof InputReference.IngredientInput reference) {
            if (catalog.ingredient(reference.ingredient()).isEmpty()) {
                issues.add(new ValidationIssue(path, "unknown ingredient " + reference.ingredient()));
            }
            return;
        }
        if (input instanceof InputReference.BeverageInput reference) {
            if (catalog.beverage(reference.beverage()).isEmpty()) {
                issues.add(new ValidationIssue(path, "unknown beverage " + reference.beverage()));
            }
            return;
        }
        if (input instanceof InputReference.NodeOutputInput && !allowNode) {
            issues.add(new ValidationIssue(path, "process definitions cannot reference graph nodes"));
        }
    }

    private static void decodeConfig(
            ProcessType<?> type,
            DataNode config,
            String path,
            BeverageCatalog catalog,
            List<ValidationIssue> issues
    ) {
        try {
            Object decoded = type.configCodec().decode(config, path);
            if (decoded instanceof ReferencedLiquids liquids) {
                for (ResourceId id : liquids.liquidIds()) {
                    requireLiquid(catalog, id, path + "/liquid/" + id, issues);
                }
            }
        } catch (RuntimeException exception) {
            issues.add(new ValidationIssue(path, exception.getMessage()));
        }
    }

    private static void requireLiquid(
            BeverageCatalog catalog,
            ResourceId id,
            String path,
            List<ValidationIssue> issues
    ) {
        if (catalog.liquid(id).isEmpty()) {
            issues.add(new ValidationIssue(path, "unknown liquid " + id));
        }
    }
}

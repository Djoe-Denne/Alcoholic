package com.djden.alcoholic.application.machine;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.process.ExecutorModifiers;
import com.djden.alcoholic.domain.multiblock.KineticRequirement;
import com.djden.alcoholic.domain.multiblock.MachineKind;
import com.djden.alcoholic.domain.multiblock.MultiblockConstraints;
import com.djden.alcoholic.domain.multiblock.MultiblockDefinition;
import com.djden.alcoholic.domain.multiblock.PartRole;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public final class MachineDefinitionCodec implements DataCodec<MultiblockDefinition> {
    public static final MachineDefinitionCodec INSTANCE = new MachineDefinitionCodec();

    private MachineDefinitionCodec() {
    }

    public MultiblockDefinition decode(DataNode node, String path, ResourceId fallbackId) {
        DataNode.ObjectNode object = node.asObject(path);
        ResourceId id = object.get("id")
                .map(value -> DataCodecs.RESOURCE_ID.decode(value, child(path, "id")))
                .orElse(fallbackId);
        MachineKind kind = MachineKind.valueOf(
                object.require("kind", path).asString(child(path, "kind")).toUpperCase()
        );
        Optional<ResourceId> process = object.get("process")
                .map(value -> DataCodecs.RESOURCE_ID.decode(value, child(path, "process")));
        DataNode.ObjectNode min = object.require("min_exterior", path).asObject(child(path, "min_exterior"));
        DataNode.ObjectNode max = object.require("max_exterior", path).asObject(child(path, "max_exterior"));
        int capacity = object.get("capacity_per_internal_block")
                .map(value -> value.asNumber(child(path, "capacity_per_internal_block")).intValue())
                .orElse(8_000);
        String controller = object.get("controller")
                .map(value -> value.asString(child(path, "controller")))
                .orElse("");
        Set<String> casing = stringSet(object, path, "casing_tags");
        Set<String> windows = stringSet(object, path, "window_tags");
        Set<String> ports = stringSet(object, path, "port_tags");
        Set<PartRole> requiredPorts = EnumSetLike(object, path, "required_ports");
        ExecutorModifiers modifiers = modifiers(object, path);
        KineticRequirement kinetic = kinetic(object, path);
        MultiblockConstraints constraints = new MultiblockConstraints(
                number(min, "x", child(path, "min_exterior")),
                number(min, "y", child(path, "min_exterior")),
                number(min, "z", child(path, "min_exterior")),
                number(max, "x", child(path, "max_exterior")),
                number(max, "y", child(path, "max_exterior")),
                number(max, "z", child(path, "max_exterior")),
                object.get("required_controllers")
                        .map(value -> value.asNumber(child(path, "required_controllers")).intValue())
                        .orElse(1),
                casing,
                windows,
                ports,
                requiredPorts,
                object.get("hollow_interior")
                        .map(value -> value.asBoolean(child(path, "hollow_interior")))
                        .orElse(true)
        );
        return new MultiblockDefinition(id, kind, process, constraints, capacity, modifiers, kinetic, controller);
    }

    @Override
    public MultiblockDefinition decode(DataNode node, String path) {
        return decode(node, path, ResourceId.parse("alcoholic:unknown_machine"));
    }

    @Override
    public DataNode encode(MultiblockDefinition value) {
        return DataNode.objectBuilder()
                .put("id", DataNode.string(value.id().toString()))
                .put("kind", DataNode.string(value.kind().name().toLowerCase()))
                .build();
    }

    private static ExecutorModifiers modifiers(DataNode.ObjectNode object, String path) {
        if (!object.has("modifiers")) {
            return ExecutorModifiers.identity();
        }
        DataNode.ObjectNode node = object.require("modifiers", path).asObject(child(path, "modifiers"));
        return new ExecutorModifiers(
                node.get("yield").map(value -> value.asNumber(child(path, "modifiers/yield")).doubleValue()).orElse(1.0),
                node.get("speed").map(value -> value.asNumber(child(path, "modifiers/speed")).doubleValue()).orElse(1.0),
                node.get("thermal_stability")
                        .map(value -> value.asNumber(child(path, "modifiers/thermal_stability")).doubleValue())
                        .orElse(1.0),
                node.get("max_batch_units")
                        .map(value -> value.asNumber(child(path, "modifiers/max_batch_units")).intValue())
                        .orElse(1)
        );
    }

    private static KineticRequirement kinetic(DataNode.ObjectNode object, String path) {
        if (object.has("mechanical")) {
            DataNode.ObjectNode node = object.require("mechanical", path).asObject(child(path, "mechanical"));
            boolean required = node.get("required")
                    .map(value -> value.asBoolean(child(path, "mechanical/required")))
                    .orElse(false);
            return new KineticRequirement(
                    numberOr(node, "min_speed", child(path, "mechanical/min_speed"), 0.0),
                    numberOr(node, "max_speed", child(path, "mechanical/max_speed"), 0.0),
                    numberOr(node, "required_capacity", child(path, "mechanical/required_capacity"), required ? 1.0 : 0.0),
                    required
            );
        }
        if (!object.has("kinetic")) {
            return KineticRequirement.none();
        }
        DataNode.ObjectNode node = object.require("kinetic", path).asObject(child(path, "kinetic"));
        boolean required = node.get("required")
                .map(value -> value.asBoolean(child(path, "kinetic/required")))
                .orElse(false);
        return new KineticRequirement(
                numberOr(node, "min_rpm", child(path, "kinetic/min_rpm"), 0.0),
                numberOr(node, "max_rpm", child(path, "kinetic/max_rpm"), 0.0),
                numberOr(node, "required_capacity", child(path, "kinetic/required_capacity"), required ? 1.0 : 0.0),
                required
        );
    }

    private static double numberOr(DataNode.ObjectNode node, String field, String path, double fallback) {
        return node.get(field).map(value -> value.asNumber(path).doubleValue()).orElse(fallback);
    }

    private static Set<String> stringSet(DataNode.ObjectNode object, String path, String field) {
        Set<String> values = new LinkedHashSet<>();
        object.get(field).ifPresent(node -> {
            for (DataNode entry : node.asList(child(path, field)).values()) {
                values.add(entry.asString(child(path, field)));
            }
        });
        return values;
    }

    private static Set<PartRole> EnumSetLike(DataNode.ObjectNode object, String path, String field) {
        Set<PartRole> values = new LinkedHashSet<>();
        object.get(field).ifPresent(node -> {
            for (DataNode entry : node.asList(child(path, field)).values()) {
                values.add(PartRole.valueOf(entry.asString(child(path, field)).toUpperCase()));
            }
        });
        return values;
    }

    private static int number(DataNode.ObjectNode object, String field, String path) {
        return object.require(field, path).asNumber(child(path, field)).intValue();
    }

    private static String child(String path, String field) {
        return DataDecodeException.child(path, field);
    }
}

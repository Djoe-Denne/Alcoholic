package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.process.LiquidAccepting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record BlendConfig(
        Set<ResourceId> acceptedInputs,
        Optional<ResourceId> outputLiquid,
        int minInputs,
        Optional<Double> minFraction
) implements LiquidAccepting, ReferencedLiquids {
    public BlendConfig {
        acceptedInputs = Set.copyOf(new LinkedHashSet<>(
                acceptedInputs == null ? List.of() : acceptedInputs
        ));
        outputLiquid = outputLiquid == null ? Optional.empty() : outputLiquid;
        if (minInputs < 2) {
            minInputs = 2;
        }
        minFraction = minFraction == null ? Optional.empty() : minFraction;
    }

    public static final DataCodec<BlendConfig> CODEC = new DataCodec<>() {
        @Override
        public BlendConfig decode(DataNode node, String path) {
            if (node == null || node.isNull()) {
                return incomplete();
            }
            DataNode.ObjectNode object = node.asObject(path);
            Set<ResourceId> accepted = new LinkedHashSet<>();
            object.get("accepted_inputs")
                    .or(() -> object.get("acceptedInputs"))
                    .ifPresent(value -> DataCodecs.RESOURCE_ID.listOf()
                            .decode(value, DataDecodeException.child(path, "accepted_inputs"))
                            .forEach(accepted::add));
            Optional<ResourceId> output = Optional.empty();
            if (object.has("output")) {
                DataNode.ObjectNode outputNode = object.require("output", path)
                        .asObject(DataDecodeException.child(path, "output"));
                output = outputNode.get("liquid")
                        .map(value -> DataCodecs.RESOURCE_ID.decode(
                                value,
                                DataDecodeException.child(path, "output/liquid")
                        ));
            } else {
                output = object.get("output_liquid")
                        .or(() -> object.get("outputLiquid"))
                        .map(value -> DataCodecs.RESOURCE_ID.decode(
                                value,
                                DataDecodeException.child(path, "output_liquid")
                        ));
            }
            int minInputs = object.get("min_inputs")
                    .map(value -> value.asNumber(DataDecodeException.child(path, "min_inputs")).intValue())
                    .orElse(2);
            Optional<Double> minFraction = object.get("min_fraction")
                    .map(value -> value.asNumber(DataDecodeException.child(path, "min_fraction")).doubleValue());
            return new BlendConfig(accepted, output, minInputs, minFraction);
        }

        @Override
        public DataNode encode(BlendConfig value) {
            DataNode.ObjectBuilder builder = DataNode.objectBuilder();
            builder.put(
                    "accepted_inputs",
                    DataCodecs.RESOURCE_ID.listOf().encode(List.copyOf(value.acceptedInputs()))
            );
            value.outputLiquid().ifPresent(id -> builder.put(
                    "output",
                    DataNode.objectBuilder().put("liquid", DataNode.string(id.toString())).build()
            ));
            builder.put("min_inputs", DataNode.number(value.minInputs()));
            return builder.build();
        }
    };

    public static BlendConfig incomplete() {
        return new BlendConfig(Set.of(), Optional.empty(), 2, Optional.empty());
    }

    @Override
    public boolean acceptsLiquid(ResourceId liquid) {
        return acceptedInputs.isEmpty() || acceptedInputs.contains(liquid);
    }

    @Override
    public Collection<ResourceId> liquidIds() {
        ArrayList<ResourceId> ids = new ArrayList<>(acceptedInputs);
        outputLiquid.ifPresent(ids::add);
        return ids;
    }
}

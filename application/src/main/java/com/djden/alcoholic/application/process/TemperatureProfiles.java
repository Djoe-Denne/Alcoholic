package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.domain.process.TemperatureBand;
import com.djden.alcoholic.domain.process.TemperatureProfile;

final class TemperatureProfiles {
    private TemperatureProfiles() {
    }

    static TemperatureProfile decode(
            DataNode.ObjectNode object,
            String path,
            TemperatureProfile fallback
    ) {
        TemperatureProfile defaults = fallback == null
                ? TemperatureProfile.fermentationDefault()
                : fallback;
        return new TemperatureProfile(
                band(object, path, "preferred_temperature", defaults.preferred()),
                band(object, path, "operating_temperature", defaults.operating()),
                band(object, path, "damaging_temperature", defaults.damaging())
        );
    }

    static void encode(DataNode.ObjectBuilder builder, TemperatureProfile profile) {
        builder.put("preferred_temperature", encodeBand(profile.preferred()));
        builder.put("operating_temperature", encodeBand(profile.operating()));
        builder.put("damaging_temperature", encodeBand(profile.damaging()));
    }

    static TemperatureProfile mashDefault() {
        return new TemperatureProfile(
                new TemperatureBand(62.0, 68.0),
                new TemperatureBand(52.0, 78.0),
                new TemperatureBand(0.0, 95.0)
        );
    }

    static TemperatureProfile boilDefault() {
        return new TemperatureProfile(
                new TemperatureBand(98.0, 105.0),
                new TemperatureBand(90.0, 110.0),
                new TemperatureBand(70.0, 140.0)
        );
    }

    static TemperatureProfile maltDefault() {
        return new TemperatureProfile(
                new TemperatureBand(12.0, 18.0),
                new TemperatureBand(4.0, 28.0),
                new TemperatureBand(-10.0, 45.0)
        );
    }

    static TemperatureProfile conditionDefault() {
        return new TemperatureProfile(
                new TemperatureBand(2.0, 12.0),
                new TemperatureBand(0.0, 20.0),
                new TemperatureBand(-10.0, 30.0)
        );
    }

    private static TemperatureBand band(
            DataNode.ObjectNode object,
            String path,
            String field,
            TemperatureBand fallback
    ) {
        return object.get(field)
                .map(node -> {
                    DataNode.ObjectNode decoded = node.asObject(DataDecodeException.child(path, field));
                    return new TemperatureBand(
                            decoded.get("min").map(value -> value.asNumber(
                                    DataDecodeException.child(path, field + "/min")
                            ).doubleValue()).orElse(fallback.min()),
                            decoded.get("max").map(value -> value.asNumber(
                                    DataDecodeException.child(path, field + "/max")
                            ).doubleValue()).orElse(fallback.max())
                    );
                })
                .orElse(fallback);
    }

    private static DataNode encodeBand(TemperatureBand band) {
        return DataNode.objectBuilder()
                .put("min", DataNode.number(band.min()))
                .put("max", DataNode.number(band.max()))
                .build();
    }
}

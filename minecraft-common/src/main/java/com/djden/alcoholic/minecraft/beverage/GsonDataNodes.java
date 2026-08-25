package com.djden.alcoholic.minecraft.beverage;

import com.djden.alcoholic.api.data.DataNode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GsonDataNodes {
    private GsonDataNodes() {
    }

    public static DataNode from(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return DataNode.nil();
        }
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return DataNode.bool(primitive.getAsBoolean());
            }
            if (primitive.isNumber()) {
                return DataNode.number(primitive.getAsNumber());
            }
            return DataNode.string(primitive.getAsString());
        }
        if (element.isJsonArray()) {
            List<DataNode> values = new ArrayList<>();
            for (JsonElement child : element.getAsJsonArray()) {
                values.add(from(child));
            }
            return DataNode.list(values);
        }
        JsonObject object = element.getAsJsonObject();
        Map<String, DataNode> fields = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            fields.put(entry.getKey(), from(entry.getValue()));
        }
        return DataNode.object(fields);
    }
}

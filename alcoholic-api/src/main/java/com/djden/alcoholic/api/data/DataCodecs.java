package com.djden.alcoholic.api.data;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@PublicApi
public final class DataCodecs {
    public static final DataCodec<Void> UNIT = new DataCodec<>() {
        @Override
        public Void decode(DataNode node, String path) {
            return null;
        }

        @Override
        public DataNode encode(Void value) {
            return DataNode.object(Map.of());
        }
    };

    public static final DataCodec<Boolean> BOOL = new DataCodec<>() {
        @Override
        public Boolean decode(DataNode node, String path) {
            return node.asBoolean(path);
        }

        @Override
        public DataNode encode(Boolean value) {
            return DataNode.bool(value);
        }
    };

    public static final DataCodec<Integer> INT = new DataCodec<>() {
        @Override
        public Integer decode(DataNode node, String path) {
            return node.asNumber(path).intValue();
        }

        @Override
        public DataNode encode(Integer value) {
            return DataNode.number(value);
        }
    };

    public static final DataCodec<Double> DOUBLE = new DataCodec<>() {
        @Override
        public Double decode(DataNode node, String path) {
            return node.asNumber(path).doubleValue();
        }

        @Override
        public DataNode encode(Double value) {
            return DataNode.number(value);
        }
    };

    public static final DataCodec<String> STRING = new DataCodec<>() {
        @Override
        public String decode(DataNode node, String path) {
            return node.asString(path);
        }

        @Override
        public DataNode encode(String value) {
            return DataNode.string(value);
        }
    };

    public static final DataCodec<ResourceId> RESOURCE_ID = STRING.xmap(ResourceId::parse, ResourceId::toString);

    public static final DataCodec<DataNode> NODE = new DataCodec<>() {
        @Override
        public DataNode decode(DataNode node, String path) {
            return Objects.requireNonNull(node, "node");
        }

        @Override
        public DataNode encode(DataNode value) {
            return value;
        }
    };

    public static final DataCodec<Map<String, DataNode>> OBJECT_FIELDS = new DataCodec<>() {
        @Override
        public Map<String, DataNode> decode(DataNode node, String path) {
            return node.asObject(path).fields();
        }

        @Override
        public DataNode encode(Map<String, DataNode> value) {
            return DataNode.object(value);
        }
    };

    private DataCodecs() {
    }

    public static DataNode.ObjectNode requireObject(DataNode node, String path) {
        return node.asObject(path);
    }

    public static Map<String, DataNode> linkedCopy(Map<String, DataNode> fields) {
        return new LinkedHashMap<>(fields);
    }
}

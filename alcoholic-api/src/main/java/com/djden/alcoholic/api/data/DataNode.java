package com.djden.alcoholic.api.data;

import com.djden.alcoholic.api.PublicApi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Loader-neutral structured data tree used by public codecs.
 */
@PublicApi
public sealed interface DataNode
        permits DataNode.NullNode, DataNode.BoolNode, DataNode.NumberNode,
        DataNode.StringNode, DataNode.ListNode, DataNode.ObjectNode {
    static DataNode nil() {
        return NullNode.INSTANCE;
    }

    static DataNode bool(boolean value) {
        return new BoolNode(value);
    }

    static DataNode number(Number value) {
        return new NumberNode(Objects.requireNonNull(value, "value"));
    }

    static DataNode string(String value) {
        return new StringNode(Objects.requireNonNull(value, "value"));
    }

    static DataNode list(List<? extends DataNode> values) {
        return new ListNode(List.copyOf(Objects.requireNonNull(values, "values")));
    }

    static DataNode object(Map<String, ? extends DataNode> fields) {
        return new ObjectNode(copyFields(fields));
    }

    static ObjectBuilder objectBuilder() {
        return new ObjectBuilder();
    }

    default boolean isNull() {
        return this instanceof NullNode;
    }

    default boolean asBoolean(String path) {
        if (this instanceof BoolNode node) {
            return node.value();
        }
        throw new DataDecodeException(path, "expected a boolean");
    }

    default Number asNumber(String path) {
        if (this instanceof NumberNode node) {
            return node.value();
        }
        throw new DataDecodeException(path, "expected a number");
    }

    default String asString(String path) {
        if (this instanceof StringNode node) {
            return node.value();
        }
        throw new DataDecodeException(path, "expected a string");
    }

    default ListNode asList(String path) {
        if (this instanceof ListNode node) {
            return node;
        }
        throw new DataDecodeException(path, "expected a list");
    }

    default ObjectNode asObject(String path) {
        if (this instanceof ObjectNode node) {
            return node;
        }
        throw new DataDecodeException(path, "expected an object");
    }

    record NullNode() implements DataNode {
        private static final NullNode INSTANCE = new NullNode();
    }

    record BoolNode(boolean value) implements DataNode {
    }

    record NumberNode(Number value) implements DataNode {
        public NumberNode {
            Objects.requireNonNull(value, "value");
        }
    }

    record StringNode(String value) implements DataNode {
        public StringNode {
            Objects.requireNonNull(value, "value");
        }
    }

    record ListNode(List<DataNode> values) implements DataNode {
        public ListNode {
            values = List.copyOf(Objects.requireNonNull(values, "values"));
        }

        public int size() {
            return values.size();
        }

        public DataNode get(int index) {
            return values.get(index);
        }
    }

    record ObjectNode(Map<String, DataNode> fields) implements DataNode {
        public ObjectNode {
            fields = copyFields(fields);
        }

        public boolean has(String name) {
            return fields.containsKey(name);
        }

        public Optional<DataNode> get(String name) {
            return Optional.ofNullable(fields.get(name));
        }

        public DataNode require(String name, String path) {
            DataNode value = fields.get(name);
            if (value == null) {
                throw new DataDecodeException(DataDecodeException.child(path, name), "missing field");
            }
            return value;
        }
    }

    final class ObjectBuilder {
        private final Map<String, DataNode> fields = new LinkedHashMap<>();

        public ObjectBuilder put(String name, DataNode value) {
            fields.put(Objects.requireNonNull(name, "name"), Objects.requireNonNull(value, "value"));
            return this;
        }

        public DataNode build() {
            return DataNode.object(fields);
        }
    }

    private static Map<String, DataNode> copyFields(Map<String, ? extends DataNode> fields) {
        Objects.requireNonNull(fields, "fields");
        Map<String, DataNode> copy = new LinkedHashMap<>();
        fields.forEach((key, value) -> copy.put(
                Objects.requireNonNull(key, "field"),
                Objects.requireNonNull(value, "value")
        ));
        return Map.copyOf(copy);
    }

    static List<DataNode> mutableCopy(List<DataNode> values) {
        return new ArrayList<>(values);
    }
}

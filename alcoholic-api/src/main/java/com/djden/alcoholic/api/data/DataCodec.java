package com.djden.alcoholic.api.data;

import com.djden.alcoholic.api.PublicApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@PublicApi
public interface DataCodec<T> {
    T decode(DataNode node, String path);

    DataNode encode(T value);

    default T decode(DataNode node) {
        return decode(node, "$");
    }

    default DataCodec<Optional<T>> optional() {
        DataCodec<T> self = this;
        return new DataCodec<>() {
            @Override
            public Optional<T> decode(DataNode node, String path) {
                if (node == null || node.isNull()) {
                    return Optional.empty();
                }
                return Optional.of(self.decode(node, path));
            }

            @Override
            public DataNode encode(Optional<T> value) {
                return value.map(self::encode).orElseGet(DataNode::nil);
            }
        };
    }

    default DataCodec<List<T>> listOf() {
        DataCodec<T> self = this;
        return new DataCodec<>() {
            @Override
            public List<T> decode(DataNode node, String path) {
                DataNode.ListNode list = node.asList(path);
                List<T> values = new ArrayList<>(list.size());
                for (int index = 0; index < list.size(); index++) {
                    values.add(self.decode(list.get(index), DataDecodeException.index(path, index)));
                }
                return List.copyOf(values);
            }

            @Override
            public DataNode encode(List<T> value) {
                List<DataNode> encoded = new ArrayList<>(value.size());
                for (T element : value) {
                    encoded.add(self.encode(element));
                }
                return DataNode.list(encoded);
            }
        };
    }

    default <R> DataCodec<R> xmap(Function<T, R> to, Function<R, T> from) {
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(from, "from");
        DataCodec<T> self = this;
        return new DataCodec<>() {
            @Override
            public R decode(DataNode node, String path) {
                return to.apply(self.decode(node, path));
            }

            @Override
            public DataNode encode(R value) {
                return self.encode(from.apply(value));
            }
        };
    }
}

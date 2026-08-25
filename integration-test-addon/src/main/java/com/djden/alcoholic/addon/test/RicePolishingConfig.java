package com.djden.alcoholic.addon.test;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;

@PublicApi
public record RicePolishingConfig(double ratio) {
    public static final DataCodec<RicePolishingConfig> CODEC = new DataCodec<>() {
        @Override
        public RicePolishingConfig decode(DataNode node, String path) {
            DataNode.ObjectNode object = node.asObject(path);
            double ratio = object.require("ratio", path)
                    .asNumber(DataDecodeException.child(path, "ratio"))
                    .doubleValue();
            if (ratio < 0.0 || ratio > 1.0) {
                throw new DataDecodeException(DataDecodeException.child(path, "ratio"), "must be between 0 and 1");
            }
            return new RicePolishingConfig(ratio);
        }

        @Override
        public DataNode encode(RicePolishingConfig value) {
            return DataNode.objectBuilder()
                    .put("ratio", DataNode.number(value.ratio()))
                    .build();
        }
    };
}

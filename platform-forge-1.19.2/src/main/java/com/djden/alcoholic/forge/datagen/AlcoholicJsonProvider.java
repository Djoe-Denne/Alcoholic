package com.djden.alcoholic.forge.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

abstract class AlcoholicJsonProvider implements DataProvider {
    private final Path outputRoot;

    AlcoholicJsonProvider(Path outputRoot) {
        this.outputRoot = outputRoot;
    }

    @Override
    public final void run(CachedOutput cache) throws IOException {
        Map<String, JsonElement> files = new TreeMap<>();
        collectJson((relativePath, json) -> {
            JsonElement previous = files.put(
                    relativePath,
                    JsonParser.parseString(json)
            );
            if (previous != null) {
                throw new IllegalStateException("Duplicate generated path: " + relativePath);
            }
        });

        for (Map.Entry<String, JsonElement> entry : files.entrySet()) {
            DataProvider.saveStable(
                    cache,
                    entry.getValue(),
                    outputRoot.resolve(entry.getKey())
            );
        }
    }

    protected abstract void collectJson(JsonSink sink);

    @FunctionalInterface
    protected interface JsonSink {
        void add(String relativePath, String json);
    }
}

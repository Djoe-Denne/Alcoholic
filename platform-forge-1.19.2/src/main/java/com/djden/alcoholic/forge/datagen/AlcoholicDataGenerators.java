package com.djden.alcoholic.forge.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;

import java.nio.file.Path;

public final class AlcoholicDataGenerators {
    private static final String SCOPE_PROPERTY = "alcoholic.datagen.scope";
    private static final String FORGE_OUTPUT_PROPERTY = "alcoholic.datagen.forgeOutput";

    private AlcoholicDataGenerators() {
    }

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        Path output = generator.getOutputFolder();
        String scope = System.getProperty(SCOPE_PROPERTY, "common");

        if ("common".equals(scope)) {
            generator.addProvider(
                    event.includeClient(),
                    new GrapeAssetDataProvider(output)
            );
            generator.addProvider(
                    event.includeClient(),
                    new AlcoholicTextureProvider(output)
            );
            generator.addProvider(
                    event.includeServer(),
                    new GrapeServerDataProvider(output)
            );
            String forgeOutput = System.getProperty(FORGE_OUTPUT_PROPERTY);
            if (forgeOutput == null || forgeOutput.isBlank()) {
                throw new IllegalStateException(
                        "Missing datagen property " + FORGE_OUTPUT_PROPERTY
                );
            }
            generator.addProvider(
                    event.includeServer(),
                    new ForgeCompatibilityDataProvider(Path.of(forgeOutput))
            );
            return;
        }

        throw new IllegalArgumentException("Unknown datagen scope: " + scope);
    }
}

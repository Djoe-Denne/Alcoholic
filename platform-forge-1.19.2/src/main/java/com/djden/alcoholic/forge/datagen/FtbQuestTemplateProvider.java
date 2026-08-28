package com.djden.alcoholic.forge.datagen;

import com.djden.alcoholic.application.progression.FtbQuestSnbt;
import com.djden.alcoholic.application.progression.ProgressionCatalog;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class FtbQuestTemplateProvider implements DataProvider {
    @Override
    public void run(CachedOutput cache) throws IOException {
        FtbQuestSnbt.write(chaptersDir(), ProgressionCatalog.official());
    }

    @Override
    public String getName() {
        return "Alcoholic FTB Quest Templates";
    }

    static Path chaptersDir() {
        Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        for (int i = 0; i < 6; i++) {
            Path candidate = dir.resolve("modpack/ftbquests/quests/chapters");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            Path parent = dir.getParent();
            if (parent == null) {
                break;
            }
            dir = parent;
        }
        throw new IllegalStateException("Cannot find modpack/ftbquests/quests/chapters");
    }
}

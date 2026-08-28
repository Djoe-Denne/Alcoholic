package com.djden.alcoholic.application.progression;

import java.util.List;
import java.util.Objects;

public record ProgressionChapterSpec(
        ProgressionChapter chapter,
        String filename,
        String chapterHex,
        String titleKey,
        String subtitleKey,
        String icon,
        String defaultQuestShape,
        int orderIndex,
        List<ProgressionImage> images
) {
    public ProgressionChapterSpec {
        Objects.requireNonNull(chapter, "chapter");
        Objects.requireNonNull(filename, "filename");
        Objects.requireNonNull(chapterHex, "chapterHex");
        Objects.requireNonNull(titleKey, "titleKey");
        Objects.requireNonNull(subtitleKey, "subtitleKey");
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(defaultQuestShape, "defaultQuestShape");
        images = List.copyOf(Objects.requireNonNull(images, "images"));
    }
}

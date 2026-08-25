package com.djden.alcoholic.domain.beverage;

import java.util.Objects;

public record GraphIssue(String path, String message) {
    public GraphIssue {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(message, "message");
    }
}

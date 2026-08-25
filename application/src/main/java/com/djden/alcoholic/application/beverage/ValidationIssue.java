package com.djden.alcoholic.application.beverage;

import java.util.Objects;

public record ValidationIssue(String path, String message) {
    public ValidationIssue {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(message, "message");
    }

    @Override
    public String toString() {
        return path + ": " + message;
    }
}

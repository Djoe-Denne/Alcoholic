package com.djden.alcoholic.application.beverage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record ValidationResult(List<ValidationIssue> issues) {
    public ValidationResult {
        issues = List.copyOf(new ArrayList<>(Objects.requireNonNull(issues, "issues")));
    }

    public static ValidationResult ok() {
        return new ValidationResult(List.of());
    }

    public boolean success() {
        return issues.isEmpty();
    }

    public void throwIfInvalid() {
        if (!success()) {
            throw new IllegalArgumentException(format());
        }
    }

    public String format() {
        return issues.stream().map(ValidationIssue::toString).collect(Collectors.joining("; "));
    }
}

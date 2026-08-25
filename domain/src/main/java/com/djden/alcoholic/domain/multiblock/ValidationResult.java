package com.djden.alcoholic.domain.multiblock;

import java.util.Objects;
import java.util.Optional;

public record ValidationResult(
        ValidationStatus status,
        String reason,
        Optional<MultiblockGeometry> geometry
) {
    public ValidationResult {
        Objects.requireNonNull(status, "status");
        reason = reason == null ? "" : reason;
        geometry = geometry == null ? Optional.empty() : geometry;
    }

    public static ValidationResult formed(MultiblockGeometry geometry) {
        return new ValidationResult(ValidationStatus.FORMED, "", Optional.of(geometry));
    }

    public static ValidationResult incomplete(String reason) {
        return new ValidationResult(ValidationStatus.INCOMPLETE, reason, Optional.empty());
    }

    public static ValidationResult invalid(String reason) {
        return new ValidationResult(ValidationStatus.INVALID, reason, Optional.empty());
    }

    public static ValidationResult overcapacity(MultiblockGeometry geometry, String reason) {
        return new ValidationResult(ValidationStatus.OVERCAPACITY, reason, Optional.of(geometry));
    }

    public boolean formed() {
        return status == ValidationStatus.FORMED;
    }
}

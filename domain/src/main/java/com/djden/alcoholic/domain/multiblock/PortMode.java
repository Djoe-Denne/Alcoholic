package com.djden.alcoholic.domain.multiblock;

public enum PortMode {
    INPUT,
    OUTPUT,
    BOTH;

    public boolean allowsInsert() {
        return this == INPUT || this == BOTH;
    }

    public boolean allowsExtract() {
        return this == OUTPUT || this == BOTH;
    }

    public PortMode next() {
        return switch (this) {
            case INPUT -> OUTPUT;
            case OUTPUT -> BOTH;
            case BOTH -> INPUT;
        };
    }
}

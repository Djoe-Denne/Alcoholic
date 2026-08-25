package com.djden.alcoholic.platform.api.mod;

@FunctionalInterface
public interface ModPresencePort {
    boolean isLoaded(String modId);
}

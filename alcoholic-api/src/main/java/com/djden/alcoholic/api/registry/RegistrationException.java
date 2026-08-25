package com.djden.alcoholic.api.registry;

import com.djden.alcoholic.api.PublicApi;

@PublicApi
public final class RegistrationException extends RuntimeException {
    public RegistrationException(String message) {
        super(message);
    }
}

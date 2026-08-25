package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ingredient.IngredientSelector;

import java.util.Optional;

/**
 * A decoded process config that can bind to a solid ingredient selector.
 */
@PublicApi
public interface SolidAccepting {
    Optional<IngredientSelector> inputSelector();
}

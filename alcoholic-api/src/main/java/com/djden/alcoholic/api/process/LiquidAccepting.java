package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;

/**
 * A decoded process config that can bind to a concrete liquid definition.
 */
@PublicApi
public interface LiquidAccepting {
    boolean acceptsLiquid(ResourceId liquid);
}

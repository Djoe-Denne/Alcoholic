package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.PublicApi;

/**
 * A decoded process config that can project itself for recipe viewers.
 * Addons implement this on their config type so JEI/REI need no core switch.
 */
@PublicApi
public interface ProcessDisplaying {
    ProcessDisplaySpec display();
}

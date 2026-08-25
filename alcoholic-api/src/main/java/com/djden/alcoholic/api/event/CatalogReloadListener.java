package com.djden.alcoholic.api.event;

import com.djden.alcoholic.api.PublicApi;

@FunctionalInterface
@PublicApi
public interface CatalogReloadListener {
    void onCatalogReloaded();
}

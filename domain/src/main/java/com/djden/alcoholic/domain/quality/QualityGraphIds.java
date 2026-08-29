package com.djden.alcoholic.domain.quality;

import com.djden.alcoholic.api.ResourceId;

/**
 * Shipped quality-graph ids. Graphs themselves live in datapack JSON.
 */
public final class QualityGraphIds {
    public static final ResourceId WINE = ResourceId.parse("alcoholic:wine");
    public static final ResourceId BEER = ResourceId.parse("alcoholic:beer");
    public static final ResourceId GENERIC = ResourceId.parse("alcoholic:generic");
    public static final ResourceId SPIRIT = ResourceId.parse("alcoholic:spirit");

    private QualityGraphIds() {
    }
}

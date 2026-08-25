package com.djden.alcoholic.api.property;

import com.djden.alcoholic.api.PublicApi;

/**
 * How two values of the same property combine when compatible liquid batches mix.
 *
 * <p>{@link #WEIGHTED_AVERAGE} is the volume-weighted average. {@link #CUSTOM}
 * delegates to {@link LiquidProperty#aggregator()}.</p>
 */
@PublicApi
public enum PropertyMerge {
    WEIGHTED_AVERAGE,
    SUM,
    MAX,
    MIN,
    FIRST,
    MATCH_OR_BLENDED,
    IDENTICAL_OR_REJECT,
    COMBINE_SET,
    CUSTOM
}

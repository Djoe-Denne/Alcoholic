package com.djden.alcoholic.application.ingredient;

import com.djden.alcoholic.api.ResourceId;

public final class SemanticTags {
    public static final ResourceId GRAPES = tag("grapes");
    public static final ResourceId RED_GRAPES = tag("grapes/red");
    public static final ResourceId WHITE_GRAPES = tag("grapes/white");
    public static final ResourceId BARLEY = tag("barley");
    public static final ResourceId HOPS = tag("hops");
    public static final ResourceId YEAST = tag("yeast");
    public static final ResourceId APPLES = tag("fruits/apple");
    public static final ResourceId MALTED_GRAIN = tag("malted_grain");
    public static final ResourceId MOLASSES = tag("molasses");
    public static final ResourceId SPIRITS = tag("spirits");

    private SemanticTags() {
    }

    private static ResourceId tag(String path) {
        return new ResourceId("alcoholic", path);
    }
}

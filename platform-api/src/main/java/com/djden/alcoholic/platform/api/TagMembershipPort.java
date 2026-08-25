package com.djden.alcoholic.platform.api;

import com.djden.alcoholic.api.ResourceId;

@FunctionalInterface
public interface TagMembershipPort<T> {
    boolean isIn(T candidate, ResourceId tagId);
}

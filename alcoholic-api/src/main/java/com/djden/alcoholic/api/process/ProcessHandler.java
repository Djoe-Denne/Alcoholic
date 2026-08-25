package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.PublicApi;

@FunctionalInterface
@PublicApi
public interface ProcessHandler<C> {
    ProcessResult apply(ProcessRequest request, C config, ProcessContext context);
}

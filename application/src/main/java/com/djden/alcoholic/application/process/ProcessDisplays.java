package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.process.ProcessDisplaySpec;
import com.djden.alcoholic.domain.process.TemperatureProfile;

final class ProcessDisplays {
    private ProcessDisplays() {
    }

    static ProcessDisplaySpec.Builder preferred(ProcessDisplaySpec.Builder builder, TemperatureProfile profile) {
        return builder.preferred(profile.preferred().min(), profile.preferred().max());
    }
}

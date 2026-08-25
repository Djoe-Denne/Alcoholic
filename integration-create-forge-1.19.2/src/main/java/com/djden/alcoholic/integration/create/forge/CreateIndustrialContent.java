package com.djden.alcoholic.integration.create.forge;

import com.djden.alcoholic.minecraft.content.ContentRegistrationPorts;
import com.djden.alcoholic.minecraft.content.IndustrialContent;
import com.djden.alcoholic.minecraft.content.IndustrialContentRegistrar;

/**
 * Registers the kinetic port as a Create {@code KineticBlock} so shafts
 * and gearboxes connect. Invoked only when Create is loaded.
 */
public final class CreateIndustrialContent {
    private CreateIndustrialContent() {
    }

    public static IndustrialContent register(ContentRegistrationPorts ports) {
        return IndustrialContentRegistrar.register(
                ports,
                CreateKineticPortBlock::new,
                CreateKineticPortBlockEntity::new
        );
    }
}

package com.djden.alcoholic.integration.create.forge;

import com.djden.alcoholic.application.compatibility.CompatibilitySnapshot;
import com.djden.alcoholic.application.compatibility.KnownMod;
import com.djden.alcoholic.minecraft.content.ContentRegistrationPorts;
import com.djden.alcoholic.minecraft.content.IndustrialContent;
import com.djden.alcoholic.minecraft.content.IndustrialContentRegistrar;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

/**
 * Loader-specific Create adapter. Alcoholic core never imports Create types;
 * this module translates heat, milling, crushing, and kinetic power.
 */
public final class ForgeCreateIntegration {
    private ForgeCreateIntegration() {
    }

    public static boolean shouldActivate(CompatibilitySnapshot compatibility) {
        return compatibility.isPresent(KnownMod.CREATE);
    }

    public static void install() {
        CreateHeatProbe.install();
        CreateKineticDriveProbe.install();
        MinecraftForge.EVENT_BUS.register(new CreateMillPropertyBridge());
    }

    public static IndustrialContent registerIndustrial(ContentRegistrationPorts ports) {
        if (!ModList.get().isLoaded("create")) {
            return IndustrialContentRegistrar.register(ports);
        }
        try {
            return (IndustrialContent) Class.forName(
                            "com.djden.alcoholic.integration.create.forge.CreateIndustrialContent"
                    )
                    .getMethod("register", ContentRegistrationPorts.class)
                    .invoke(null, ports);
        } catch (ReflectiveOperationException exception) {
            return IndustrialContentRegistrar.register(ports);
        }
    }
}

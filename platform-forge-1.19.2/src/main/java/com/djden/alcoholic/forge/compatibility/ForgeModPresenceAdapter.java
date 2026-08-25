package com.djden.alcoholic.forge.compatibility;

import com.djden.alcoholic.platform.api.mod.ModPresencePort;
import net.minecraftforge.fml.ModList;

public final class ForgeModPresenceAdapter implements ModPresencePort {
    @Override
    public boolean isLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}

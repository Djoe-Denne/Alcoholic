package com.djden.alcoholic.forge.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Press-specific alias; every industrial controller now uses
 * {@link FormedMultiblockRenderer}.
 */
public final class IndustrialPressRenderer extends FormedMultiblockRenderer {
    public IndustrialPressRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }
}

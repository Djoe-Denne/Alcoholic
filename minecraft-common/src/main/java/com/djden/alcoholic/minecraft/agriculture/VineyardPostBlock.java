package com.djden.alcoholic.minecraft.agriculture;

import net.minecraft.world.level.block.Block;

/**
 * A structural trellis post. End posts use a distinct block ID but share this
 * runtime type so trellis validation can treat both as valid anchors.
 */
public class VineyardPostBlock extends Block {
    public VineyardPostBlock(Properties properties) {
        super(properties);
    }
}

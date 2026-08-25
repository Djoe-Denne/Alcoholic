package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.domain.multiblock.PartRole;

/**
 * Vanilla-side marker so structure sampling never hardcodes one casing block.
 */
public interface MultiblockPart {
    PartRole role();
}

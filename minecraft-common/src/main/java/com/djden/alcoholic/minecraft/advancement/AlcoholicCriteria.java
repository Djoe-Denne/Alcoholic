package com.djden.alcoholic.minecraft.advancement;

import net.minecraft.advancements.CriteriaTriggers;

public final class AlcoholicCriteria {
    public static final CropHarvestedTrigger CROP_HARVESTED =
            CriteriaTriggers.register(new CropHarvestedTrigger());
    public static final ProcessCompletedTrigger PROCESS_COMPLETED =
            CriteriaTriggers.register(new ProcessCompletedTrigger());
    public static final MultiblockFormedTrigger MULTIBLOCK_FORMED =
            CriteriaTriggers.register(new MultiblockFormedTrigger());

    private AlcoholicCriteria() {
    }

    public static void register() {
        // Class initialization registers the vanilla criterion triggers.
    }
}

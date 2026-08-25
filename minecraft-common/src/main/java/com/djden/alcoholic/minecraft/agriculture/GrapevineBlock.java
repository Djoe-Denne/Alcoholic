package com.djden.alcoholic.minecraft.agriculture;

import com.djden.alcoholic.application.viticulture.VineVarieties;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.viticulture.ViticultureRuntime;
import com.djden.alcoholic.api.ResourceId;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.function.Supplier;

/**
 * Source-compatibility alias for older platform tests. New registrations use
 * {@link VineBlock} directly.
 */
@Deprecated
public final class GrapevineBlock extends VineBlock {
    public static final int MAX_AGE = MAX_LEGACY_AGE;
    public static final int REGROWTH_AGE = 2;
    public static final IntegerProperty AGE = VineBlock.AGE;

    public GrapevineBlock(
            Properties properties,
            Supplier<? extends Item> ignoredGrape,
            ResourceId cuttingId
    ) {
        super(
                properties,
                AlcoholicIds.RED_GRAPE_CUTTING.equals(cuttingId)
                        ? VineVarieties.RED_GRAPE
                        : VineVarieties.WHITE_GRAPE,
                ViticultureRuntime.shared(),
                () -> Registry.BLOCK_ENTITY_TYPE.get(
                        ResourceLocation.fromNamespaceAndPath(
                                AlcoholicIds.VINE_BLOCK_ENTITY.namespace(),
                                AlcoholicIds.VINE_BLOCK_ENTITY.path()
                        )
                )
        );
    }

    public int getMaxAge() {
        return MAX_AGE;
    }

    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    public BlockState getStateForAge(int age) {
        int bounded = Math.max(0, Math.min(MAX_AGE, age));
        VineStage stage = switch (bounded) {
            case 0 -> VineStage.PLANTED;
            case 1 -> VineStage.ESTABLISHING;
            case 2 -> VineStage.VEGETATIVE;
            case 3 -> VineStage.RIPENING;
            default -> VineStage.HARVEST_READY;
        };
        return defaultBlockState().setValue(AGE, bounded).setValue(STAGE, stage);
    }
}

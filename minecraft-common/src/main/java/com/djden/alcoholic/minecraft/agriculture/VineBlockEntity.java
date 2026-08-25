package com.djden.alcoholic.minecraft.agriculture;

import com.djden.alcoholic.application.viticulture.VineVarieties;
import com.djden.alcoholic.domain.viticulture.PruningLevel;
import com.djden.alcoholic.domain.viticulture.Vine;
import com.djden.alcoholic.domain.viticulture.VineGrowthStage;
import com.djden.alcoholic.domain.viticulture.VineHealth;
import com.djden.alcoholic.domain.viticulture.VineVariety;
import com.djden.alcoholic.minecraft.viticulture.ViticultureRuntime;
import com.djden.alcoholic.api.ResourceId;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public final class VineBlockEntity extends BlockEntity {
    public static final String VITICULTURE_TAG = "Viticulture";
    public static final int DATA_VERSION = 1;

    private final VineVariety<ResourceId> fallbackVariety;
    private Vine<ResourceId> vine;

    public VineBlockEntity(
            BlockEntityType<?> type,
            BlockPos position,
            BlockState state,
            VineVariety<ResourceId> variety
    ) {
        super(type, position, state);
        fallbackVariety = Objects.requireNonNull(variety, "variety");
        vine = Vine.planted(variety);
    }

    public Vine<ResourceId> vine() {
        return vine;
    }

    public void setVine(Vine<ResourceId> updated) {
        vine = Objects.requireNonNull(updated, "updated");
        setChanged();
        if (level != null) {
            BlockState current = getBlockState();
            if (current.getBlock() instanceof VineBlock vineBlock) {
                BlockState synchronizedState = vineBlock.stateForVine(
                        current,
                        updated
                );
                if (synchronizedState != current) {
                    level.setBlock(worldPosition, synchronizedState, Block.UPDATE_CLIENTS);
                } else {
                    level.sendBlockUpdated(
                            worldPosition,
                            current,
                            current,
                            Block.UPDATE_CLIENTS
                    );
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag data = new CompoundTag();
        data.putInt("Version", DATA_VERSION);
        data.putString("Variety", vine.variety().id().toString());
        data.putString("Stage", vine.growthStage().name());
        data.putInt("AgeCycles", vine.ageCycles());
        data.putBoolean("HasEstablished", vine.hasEstablished());
        data.putDouble("HealthGrowth", vine.health().growthMultiplier());
        data.putDouble("HealthYield", vine.health().yieldMultiplier());
        data.putDouble("HealthQuality", vine.health().qualityModifier());
        data.putString("Pruning", vine.pruningLevel().name());
        data.putDouble("Progress", vine.growthProgress());
        data.putLong("LastHarvest", vine.lastHarvest());
        tag.put(VITICULTURE_TAG, data);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (!tag.contains(VITICULTURE_TAG, Tag.TAG_COMPOUND)) {
            vine = migrateLegacy(fallbackVariety, legacyAge(getBlockState()));
            return;
        }

        try {
            CompoundTag data = tag.getCompound(VITICULTURE_TAG);
            ResourceId varietyId = ResourceId.parse(data.getString("Variety"));
            VineVariety<ResourceId> variety = resolveVariety(varietyId);
            VineGrowthStage stage = VineGrowthStage.valueOf(data.getString("Stage"));
            VineHealth health = new VineHealth(
                    data.getDouble("HealthGrowth"),
                    data.getDouble("HealthYield"),
                    data.getDouble("HealthQuality")
            );
            vine = new Vine<>(
                    variety,
                    stage,
                    data.getInt("AgeCycles"),
                    data.getBoolean("HasEstablished"),
                    health,
                    PruningLevel.valueOf(data.getString("Pruning")),
                    data.getDouble("Progress"),
                    data.getLong("LastHarvest")
            );
        } catch (IllegalArgumentException | NullPointerException ignored) {
            vine = migrateLegacy(fallbackVariety, legacyAge(getBlockState()));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static Vine<ResourceId> migrateLegacy(
            VineVariety<ResourceId> variety,
            int age
    ) {
        int boundedAge = Math.max(0, Math.min(VineBlock.MAX_LEGACY_AGE, age));
        VineGrowthStage stage = switch (boundedAge) {
            case 0 -> VineGrowthStage.PLANTED;
            case 1 -> VineGrowthStage.ESTABLISHING;
            case 2 -> VineGrowthStage.VEGETATIVE;
            case 3 -> VineGrowthStage.RIPENING;
            default -> VineGrowthStage.HARVEST_READY;
        };
        return new Vine<>(
                variety,
                stage,
                0,
                boundedAge == VineBlock.MAX_LEGACY_AGE,
                VineHealth.HEALTHY,
                PruningLevel.BALANCED,
                0.0,
                Vine.NO_HARVEST
        );
    }

    private VineVariety<ResourceId> resolveVariety(ResourceId id) {
        if (VineVarieties.RED_GRAPE.id().equals(id)) {
            return VineVarieties.RED_GRAPE;
        }
        if (VineVarieties.WHITE_GRAPE.id().equals(id)) {
            return VineVarieties.WHITE_GRAPE;
        }
        try {
            return ViticultureRuntime.shared().variety(id);
        } catch (IllegalArgumentException ignored) {
            return fallbackVariety;
        }
    }

    private static int legacyAge(BlockState state) {
        return state.hasProperty(VineBlock.AGE)
                ? state.getValue(VineBlock.AGE)
                : 0;
    }
}

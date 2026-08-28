package com.djden.alcoholic.forge.gametest;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.fluid.BuiltinFluidDefinitions;
import com.djden.alcoholic.minecraft.fluid.FluidDefinition;
import com.djden.alcoholic.minecraft.fluid.FluidFlowProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(AlcoholicIds.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FluidGameTests {
    private static final BlockPos TARGET = new BlockPos(1, 1, 1);

    private FluidGameTests() {
    }

    @GameTest(template = "industrial_pad", timeoutTicks = 100)
    public static void everyBuiltinBucketPlacesAndPicksUpItsSource(GameTestHelper helper) {
        Level level = helper.getLevel();
        BlockPos target = helper.absolutePos(TARGET);
        for (FluidDefinition definition : BuiltinFluidDefinitions.all()) {
            ResourceId id = definition.id();
            ResourceLocation sourceId = location(id);
            ResourceLocation flowingId = ResourceLocation.fromNamespaceAndPath(
                    id.namespace(),
                    "flowing_" + id.path()
            );
            ResourceLocation bucketId = ResourceLocation.fromNamespaceAndPath(
                    id.namespace(),
                    id.path() + "_bucket"
            );

            Fluid registeredSource = requireRegistered(
                    ForgeRegistries.FLUIDS.getValue(sourceId),
                    id + " source fluid"
            );
            Fluid registeredFlowing = requireRegistered(
                    ForgeRegistries.FLUIDS.getValue(flowingId),
                    id + " flowing fluid"
            );
            Block registeredBlock = requireRegistered(
                    ForgeRegistries.BLOCKS.getValue(sourceId),
                    id + " liquid block"
            );
            Item registeredBucket = requireRegistered(
                    ForgeRegistries.ITEMS.getValue(bucketId),
                    id + " bucket"
            );

            require(helper, registeredSource instanceof FlowingFluid, id + " source is not flowing");
            require(helper, registeredFlowing instanceof FlowingFluid, id + " flowing variant is invalid");
            require(helper, registeredBlock instanceof LiquidBlock, id + " block is not a LiquidBlock");
            require(helper, registeredBucket instanceof BucketItem, id + " item is not a BucketItem");
            FlowingFluid source = (FlowingFluid) registeredSource;
            FlowingFluid flowing = (FlowingFluid) registeredFlowing;
            LiquidBlock liquidBlock = (LiquidBlock) registeredBlock;
            BucketItem bucket = (BucketItem) registeredBucket;

            require(helper, source.isSame(flowing), id + " source and flowing variants are not associated");
            require(helper, source.getBucket() == bucket, id + " source points at the wrong bucket");
            require(helper, bucket.getFluid() == source, id + " bucket points at the wrong source");
            require(helper, liquidBlock.getFluid() == source, id + " block points at the wrong source");
            require(
                    helper,
                    source.defaultFluidState().createLegacyBlock().is(liquidBlock),
                    id + " source does not create its liquid block"
            );
            assertProfile(helper, level, target, source, definition.flowProfile(), id);

            level.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
            ItemStack filledBucket = new ItemStack(bucket);
            require(
                    helper,
                    bucket.emptyContents(null, level, target, null, filledBucket),
                    id + " bucket refused to place its source"
            );
            BlockState placed = level.getBlockState(target);
            require(helper, placed.is(liquidBlock), id + " bucket placed the wrong block: " + placed);
            require(helper, placed.getFluidState().isSource(), id + " bucket did not place a source state");

            require(helper, placed.getBlock() instanceof BucketPickup, id + " source block cannot be picked up");
            ItemStack pickedUp = ((BucketPickup) placed.getBlock()).pickupBlock(level, target, placed);
            require(helper, pickedUp.is(bucket), id + " source returned the wrong bucket: " + pickedUp);
            require(helper, level.getBlockState(target).isAir(), id + " source remained after pickup");
        }
        helper.succeed();
    }

    private static void assertProfile(
            GameTestHelper helper,
            Level level,
            BlockPos target,
            FlowingFluid source,
            FluidFlowProfile expected,
            ResourceId id
    ) {
        FluidType type = source.getFluidType();
        require(helper, type.getDensity() == expected.density(), id + " has the wrong density");
        require(helper, type.getTemperature() == expected.temperature(), id + " has the wrong temperature");
        require(helper, type.getViscosity() == expected.viscosity(), id + " has the wrong viscosity");
        require(helper, source.getTickDelay(level) == expected.tickRate(), id + " has the wrong tick rate");
        require(
                helper,
                type.canConvertToSource(source.defaultFluidState(), level, target) == expected.renewableSources(),
                id + " has the wrong source-renewal policy"
        );
    }

    private static ResourceLocation location(ResourceId id) {
        return ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path());
    }

    private static <T> T requireRegistered(T value, String description) {
        if (value == null) {
            throw new IllegalStateException("Missing registered " + description);
        }
        return value;
    }

    private static void require(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }
}

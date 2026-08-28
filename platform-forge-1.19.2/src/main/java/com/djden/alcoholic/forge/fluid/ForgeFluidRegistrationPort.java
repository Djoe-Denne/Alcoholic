package com.djden.alcoholic.forge.fluid;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.fluid.FluidDefinition;
import com.djden.alcoholic.minecraft.fluid.FluidFlowProfile;
import com.djden.alcoholic.minecraft.fluid.FluidRegistrationPort;
import com.djden.alcoholic.platform.api.registry.RegistryPort;
import com.djden.alcoholic.platform.api.registry.RegistryRef;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Forge implementation of the loader-neutral fluid registration boundary.
 */
public final class ForgeFluidRegistrationPort implements FluidRegistrationPort {
    private final DeferredRegister<FluidType> types =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, AlcoholicIds.MOD_ID);
    private final DeferredRegister<Fluid> fluids =
            DeferredRegister.create(ForgeRegistries.FLUIDS, AlcoholicIds.MOD_ID);
    private final RegistryPort<Block> blocks;
    private final RegistryPort<Item> items;

    public ForgeFluidRegistrationPort(RegistryPort<Block> blocks, RegistryPort<Item> items) {
        this.blocks = Objects.requireNonNull(blocks, "blocks");
        this.items = Objects.requireNonNull(items, "items");
    }

    public void attach(IEventBus bus) {
        types.register(bus);
        fluids.register(bus);
    }

    @Override
    public RegistryRef<FlowingFluid> register(FluidDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        ResourceId id = definition.id();
        if (!AlcoholicIds.MOD_ID.equals(id.namespace())) {
            throw new IllegalArgumentException("Forge fluid registrar cannot register " + id);
        }

        FluidFlowProfile profile = definition.flowProfile();
        RegistryObject<FluidType> type = types.register(id.path(), () -> fluidType(definition));
        LateBound<FlowingFluid> source = new LateBound<>(id + " source fluid");
        LateBound<FlowingFluid> flowing = new LateBound<>(id + " flowing fluid");
        LateBound<Item> bucket = new LateBound<>(id + " bucket");
        LateBound<LiquidBlock> block = new LateBound<>(id + " liquid block");

        ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(type, source, flowing)
                .bucket(bucket)
                .block(block)
                .tickRate(profile.tickRate())
                .slopeFindDistance(profile.slopeFindDistance())
                .levelDecreasePerBlock(profile.levelDecreasePerBlock());

        RegistryObject<FlowingFluid> sourceObject = fluids.register(
                id.path(),
                () -> new ForgeFlowingFluid.Source(properties)
        );
        source.bind(sourceObject);
        RegistryObject<FlowingFluid> flowingObject = fluids.register(
                "flowing_" + id.path(),
                () -> new ForgeFlowingFluid.Flowing(properties)
        );
        flowing.bind(flowingObject);

        RegistryRef<Block> blockRef = blocks.register(
                id,
                () -> new LiquidBlock(
                        source,
                        BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()
                )
        );
        block.bind(() -> requireLiquidBlock(blockRef, id));

        RegistryRef<Item> bucketRef = items.register(
                new ResourceId(id.namespace(), id.path() + "_bucket"),
                () -> new BucketItem(
                        source,
                        new Item.Properties()
                                .craftRemainder(Items.BUCKET)
                                .stacksTo(1)
                                .tab(CreativeModeTab.TAB_MISC)
                )
        );
        bucket.bind(bucketRef);
        return new ForgeFluidRef(id, sourceObject);
    }

    private static FluidType fluidType(FluidDefinition definition) {
        FluidFlowProfile profile = definition.flowProfile();
        return new FluidType(
                FluidType.Properties.create()
                        .density(profile.density())
                        .temperature(profile.temperature())
                        .viscosity(profile.viscosity())
                        .canSwim(true)
                        .canDrown(true)
                        .canExtinguish(false)
                        .canHydrate(false)
                        .supportsBoating(false)
                        .canConvertToSource(profile.renewableSources())
                        .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                        .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
        ) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    @Override
                    public ResourceLocation getStillTexture() {
                        return location(definition.stillTexture());
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return location(definition.flowingTexture());
                    }

                    @Override
                    public int getTintColor() {
                        return definition.tintArgb();
                    }
                });
            }
        };
    }

    private static LiquidBlock requireLiquidBlock(RegistryRef<Block> ref, ResourceId id) {
        Block registered = ref.get();
        if (registered instanceof LiquidBlock liquidBlock) {
            return liquidBlock;
        }
        throw new IllegalStateException("Registered fluid block is not a LiquidBlock: " + id);
    }

    private static ResourceLocation location(ResourceId id) {
        return ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path());
    }

    private record ForgeFluidRef(
            ResourceId id,
            RegistryObject<FlowingFluid> delegate
    ) implements RegistryRef<FlowingFluid> {
        @Override
        public FlowingFluid get() {
            return delegate.get();
        }
    }

    private static final class LateBound<T> implements Supplier<T> {
        private final String description;
        private Supplier<? extends T> delegate;

        private LateBound(String description) {
            this.description = description;
        }

        private void bind(Supplier<? extends T> delegate) {
            Objects.requireNonNull(delegate, "delegate");
            if (this.delegate != null) {
                throw new IllegalStateException(description + " was bound more than once");
            }
            this.delegate = delegate;
        }

        @Override
        public T get() {
            Supplier<? extends T> current = delegate;
            if (current == null) {
                throw new IllegalStateException(description + " was resolved before registration completed");
            }
            return current.get();
        }
    }
}

package com.djden.alcoholic.forge.fluid;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ForgeFluidContent {
    private static final ResourceLocation STILL =
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_still");
    private static final ResourceLocation FLOW =
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_flow");

    private final DeferredRegister<FluidType> types =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, AlcoholicIds.MOD_ID);
    private final DeferredRegister<Fluid> fluids =
            DeferredRegister.create(ForgeRegistries.FLUIDS, AlcoholicIds.MOD_ID);
    private final DeferredRegister<Item> extraItems =
            DeferredRegister.create(ForgeRegistries.ITEMS, AlcoholicIds.MOD_ID);
    private final Map<ResourceId, Supplier<Fluid>> sources = new LinkedHashMap<>();

    public final RegistryObject<FlowingFluid> redGrapeMust;
    public final RegistryObject<FlowingFluid> whiteGrapeMust;
    public final RegistryObject<FlowingFluid> youngRedWine;
    public final RegistryObject<FlowingFluid> youngWhiteWine;
    public final RegistryObject<FlowingFluid> redWine;
    public final RegistryObject<FlowingFluid> whiteWine;
    public final RegistryObject<FlowingFluid> wort;
    public final RegistryObject<FlowingFluid> hoppedWort;
    public final RegistryObject<FlowingFluid> beer;

    public ForgeFluidContent() {
        redGrapeMust = fluid(AlcoholicIds.RED_GRAPE_MUST, 0xFF7A1F3A);
        whiteGrapeMust = fluid(AlcoholicIds.WHITE_GRAPE_MUST, 0xFFE6D56A);
        youngRedWine = fluid(AlcoholicIds.YOUNG_RED_WINE, 0xFF5A1226);
        youngWhiteWine = fluid(AlcoholicIds.YOUNG_WHITE_WINE, 0xFFE8D36B);
        redWine = fluid(AlcoholicIds.RED_WINE, 0xFF4A0E1C);
        whiteWine = fluid(AlcoholicIds.WHITE_WINE, 0xFFE6C85A);
        wort = fluid(AlcoholicIds.WORT, 0xFFC9A227);
        hoppedWort = fluid(AlcoholicIds.HOPPED_WORT, 0xFFB8860B);
        beer = fluid(AlcoholicIds.BEER, 0xFFD4A017);
    }

    public void attach(IEventBus bus) {
        types.register(bus);
        fluids.register(bus);
        extraItems.register(bus);
    }

    public Fluid source(ResourceId id) {
        Supplier<Fluid> supplier = sources.get(id);
        return supplier == null ? null : supplier.get();
    }

    private RegistryObject<FlowingFluid> fluid(ResourceId id, int tint) {
        RegistryObject<FluidType> type = types.register(id.path(), () -> new FluidType(
                FluidType.Properties.create()
                        .density(1010)
                        .viscosity(1400)
                        .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                        .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
        ) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    @Override
                    public ResourceLocation getStillTexture() {
                        return STILL;
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return FLOW;
                    }

                    @Override
                    public int getTintColor() {
                        return tint;
                    }
                });
            }
        });
        RegistryObject<FlowingFluid>[] source = new RegistryObject[1];
        RegistryObject<FlowingFluid>[] flowing = new RegistryObject[1];
        RegistryObject<Item>[] bucket = new RegistryObject[1];
        ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(
                type,
                () -> source[0].get(),
                () -> flowing[0].get()
        ).bucket(() -> bucket[0].get());
        source[0] = fluids.register(id.path(), () -> new ForgeFlowingFluid.Source(properties));
        flowing[0] = fluids.register("flowing_" + id.path(), () -> new ForgeFlowingFluid.Flowing(properties));
        bucket[0] = extraItems.register(id.path() + "_bucket", () -> new BucketItem(
                source[0],
                new Item.Properties()
                        .craftRemainder(Items.BUCKET)
                        .stacksTo(1)
                        .tab(CreativeModeTab.TAB_MISC)
        ));
        sources.put(id, () -> source[0].get());
        return source[0];
    }
}

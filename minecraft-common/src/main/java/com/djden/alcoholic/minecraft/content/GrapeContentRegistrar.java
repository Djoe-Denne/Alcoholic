package com.djden.alcoholic.minecraft.content;

import com.djden.alcoholic.application.viticulture.VineVarieties;
import com.djden.alcoholic.minecraft.agriculture.EndPostBlock;
import com.djden.alcoholic.minecraft.agriculture.TrellisWireBlock;
import com.djden.alcoholic.minecraft.agriculture.VineBlock;
import com.djden.alcoholic.minecraft.agriculture.VineBlockEntity;
import com.djden.alcoholic.minecraft.agriculture.VineyardPostBlock;
import com.djden.alcoholic.minecraft.viticulture.PruningShearsItem;
import com.djden.alcoholic.minecraft.viticulture.TrellisSpoolItem;
import com.djden.alcoholic.minecraft.viticulture.ViticultureRuntime;
import com.djden.alcoholic.platform.api.registry.RegistryRef;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class GrapeContentRegistrar {
    private static final FoodProperties GRAPE_FOOD = new FoodProperties.Builder()
            .nutrition(2)
            .saturationMod(0.2F)
            .build();

    private GrapeContentRegistrar() {
    }

    public static AlcoholicContent register(
            ContentRegistrationPorts ports,
            boolean builtinDiscoverable
    ) {
        return register(ports, builtinDiscoverable, ViticultureRuntime.shared());
    }

    public static AlcoholicContent register(
            ContentRegistrationPorts ports,
            boolean builtinDiscoverable,
            ViticultureRuntime runtime
    ) {
        Objects.requireNonNull(ports, "ports");
        Objects.requireNonNull(runtime, "runtime");
        AtomicReference<RegistryRef<BlockEntityType<?>>> vineEntityHolder =
                new AtomicReference<>();
        Supplier<BlockEntityType<?>> vineEntityType = () -> {
            RegistryRef<BlockEntityType<?>> reference = vineEntityHolder.get();
            if (reference == null) {
                throw new IllegalStateException("vine block entity type is not registered yet");
            }
            return reference.get();
        };

        RegistryRef<Item> redGrapesRef = ports.items().register(
                AlcoholicIds.RED_GRAPES,
                () -> new Item(grapeProperties(builtinDiscoverable))
        );
        RegistryRef<Item> whiteGrapesRef = ports.items().register(
                AlcoholicIds.WHITE_GRAPES,
                () -> new Item(grapeProperties(builtinDiscoverable))
        );
        RegistryRef<Block> redVineRef = ports.blocks().register(
                AlcoholicIds.RED_GRAPEVINE,
                () -> new VineBlock(
                        vineProperties(),
                        VineVarieties.RED_GRAPE,
                        runtime,
                        vineEntityType
                )
        );
        RegistryRef<Block> whiteVineRef = ports.blocks().register(
                AlcoholicIds.WHITE_GRAPEVINE,
                () -> new VineBlock(
                        vineProperties(),
                        VineVarieties.WHITE_GRAPE,
                        runtime,
                        vineEntityType
                )
        );
        RegistryRef<Item> redCuttingRef = ports.items().register(
                AlcoholicIds.RED_GRAPE_CUTTING,
                () -> new ItemNameBlockItem(
                        redVineRef.get(),
                        cuttingProperties(builtinDiscoverable)
                )
        );
        RegistryRef<Item> whiteCuttingRef = ports.items().register(
                AlcoholicIds.WHITE_GRAPE_CUTTING,
                () -> new ItemNameBlockItem(
                        whiteVineRef.get(),
                        cuttingProperties(builtinDiscoverable)
                )
        );

        RegistryRef<Block> vineyardPostRef = ports.blocks().register(
                AlcoholicIds.VINEYARD_POST,
                () -> new VineyardPostBlock(postProperties())
        );
        RegistryRef<Block> endPostRef = ports.blocks().register(
                AlcoholicIds.END_POST,
                () -> new EndPostBlock(postProperties())
        );
        RegistryRef<Block> trellisWireRef = ports.blocks().register(
                AlcoholicIds.TRELLIS_WIRE,
                () -> new TrellisWireBlock(wireProperties())
        );
        RegistryRef<Item> vineyardPostItemRef = ports.items().register(
                AlcoholicIds.VINEYARD_POST,
                () -> new BlockItem(vineyardPostRef.get(), infrastructureProperties())
        );
        RegistryRef<Item> endPostItemRef = ports.items().register(
                AlcoholicIds.END_POST,
                () -> new BlockItem(endPostRef.get(), infrastructureProperties())
        );
        RegistryRef<Item> trellisSpoolRef = ports.items().register(
                AlcoholicIds.TRELLIS_SPOOL,
                () -> new TrellisSpoolItem(
                        new Item.Properties()
                                .durability(512)
                                .tab(CreativeModeTab.TAB_TOOLS),
                        trellisWireRef,
                        runtime
                )
        );
        RegistryRef<Item> pruningShearsRef = ports.items().register(
                AlcoholicIds.PRUNING_SHEARS,
                () -> new PruningShearsItem(
                        new Item.Properties()
                                .durability(256)
                                .tab(CreativeModeTab.TAB_TOOLS)
                )
        );
        RegistryRef<BlockEntityType<?>> vineEntityRef =
                ports.blockEntities().register(
                        AlcoholicIds.VINE_BLOCK_ENTITY,
                        () -> BlockEntityType.Builder.of(
                                (position, state) -> {
                                    VineBlock block = (VineBlock) state.getBlock();
                                    return new VineBlockEntity(
                                            vineEntityType.get(),
                                            position,
                                            state,
                                            block.variety()
                                    );
                                },
                                redVineRef.get(),
                                whiteVineRef.get()
                        ).build(null)
                );
        vineEntityHolder.set(vineEntityRef);

        return new AlcoholicContent(
                redVineRef,
                whiteVineRef,
                redGrapesRef,
                whiteGrapesRef,
                redCuttingRef,
                whiteCuttingRef,
                vineyardPostRef,
                endPostRef,
                trellisWireRef,
                vineyardPostItemRef,
                endPostItemRef,
                trellisSpoolRef,
                pruningShearsRef,
                vineEntityRef
        );
    }

    private static Item.Properties grapeProperties(boolean discoverable) {
        Item.Properties properties = new Item.Properties().food(GRAPE_FOOD);
        return discoverable ? properties.tab(CreativeModeTab.TAB_FOOD) : properties;
    }

    private static Item.Properties cuttingProperties(boolean discoverable) {
        Item.Properties properties = new Item.Properties();
        return discoverable ? properties.tab(CreativeModeTab.TAB_MISC) : properties;
    }

    private static Item.Properties infrastructureProperties() {
        return new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS);
    }

    private static BlockBehaviour.Properties vineProperties() {
        return BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH)
                .noCollission()
                .randomTicks()
                .sound(SoundType.VINE);
    }

    private static BlockBehaviour.Properties postProperties() {
        return BlockBehaviour.Properties.copy(Blocks.OAK_FENCE)
                .strength(2.0F, 3.0F)
                .sound(SoundType.WOOD);
    }

    private static BlockBehaviour.Properties wireProperties() {
        return BlockBehaviour.Properties.copy(Blocks.TRIPWIRE)
                .noCollission()
                .noOcclusion()
                .strength(0.2F)
                .sound(SoundType.CHAIN);
    }
}

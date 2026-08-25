package com.djden.alcoholic.minecraft.content;

import com.djden.alcoholic.minecraft.bottle.BeverageBottleItem;
import com.djden.alcoholic.minecraft.process.ArtisanalBlendingCrockBlock;
import com.djden.alcoholic.minecraft.process.ArtisanalBlendingCrockBlockEntity;
import com.djden.alcoholic.minecraft.process.ArtisanalFermenterBlock;
import com.djden.alcoholic.minecraft.process.ArtisanalFermenterBlockEntity;
import com.djden.alcoholic.minecraft.process.ArtisanalPressBlock;
import com.djden.alcoholic.minecraft.process.ArtisanalPressBlockEntity;
import com.djden.alcoholic.minecraft.process.OakBarrelBlock;
import com.djden.alcoholic.minecraft.process.OakBarrelBlockEntity;
import com.djden.alcoholic.platform.api.registry.RegistryRef;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class ProcessingContentRegistrar {
    private ProcessingContentRegistrar() {
    }

    public static ProcessingContent register(ContentRegistrationPorts ports) {
        Objects.requireNonNull(ports, "ports");
        AtomicReference<RegistryRef<BlockEntityType<?>>> pressEntityHolder = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> fermenterEntityHolder = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> barrelEntityHolder = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> crockEntityHolder = new AtomicReference<>();
        Supplier<BlockEntityType<?>> pressType = () -> pressEntityHolder.get().get();
        Supplier<BlockEntityType<?>> fermenterType = () -> fermenterEntityHolder.get().get();
        Supplier<BlockEntityType<?>> barrelType = () -> barrelEntityHolder.get().get();
        Supplier<BlockEntityType<?>> crockType = () -> crockEntityHolder.get().get();

        RegistryRef<Block> pressRef = ports.blocks().register(
                AlcoholicIds.ARTISANAL_PRESS,
                () -> new ArtisanalPressBlock(machineProperties(), pressType)
        );
        RegistryRef<Block> fermenterRef = ports.blocks().register(
                AlcoholicIds.ARTISANAL_FERMENTER,
                () -> new ArtisanalFermenterBlock(machineProperties(), fermenterType)
        );
        RegistryRef<Block> barrelRef = ports.blocks().register(
                AlcoholicIds.OAK_BARREL,
                () -> new OakBarrelBlock(machineProperties(), barrelType)
        );
        RegistryRef<Block> crockRef = ports.blocks().register(
                AlcoholicIds.ARTISANAL_BLENDING_CROCK,
                () -> new ArtisanalBlendingCrockBlock(machineProperties(), crockType)
        );
        RegistryRef<Item> pressItem = ports.items().register(
                AlcoholicIds.ARTISANAL_PRESS,
                () -> new BlockItem(pressRef.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS))
        );
        RegistryRef<Item> fermenterItem = ports.items().register(
                AlcoholicIds.ARTISANAL_FERMENTER,
                () -> new BlockItem(fermenterRef.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS))
        );
        RegistryRef<Item> barrelItem = ports.items().register(
                AlcoholicIds.OAK_BARREL,
                () -> new BlockItem(barrelRef.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS))
        );
        RegistryRef<Item> crockItem = ports.items().register(
                AlcoholicIds.ARTISANAL_BLENDING_CROCK,
                () -> new BlockItem(crockRef.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS))
        );
        RegistryRef<Item> yeast = ports.items().register(
                AlcoholicIds.YEAST,
                () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC))
        );
        RegistryRef<Item> pomace = ports.items().register(
                AlcoholicIds.GRAPE_POMACE,
                () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC))
        );
        RegistryRef<Item> emptyBottle = ports.items().register(
                AlcoholicIds.EMPTY_BOTTLE,
                () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC))
        );
        RegistryRef<Item> beverageBottle = ports.items().register(
                AlcoholicIds.BEVERAGE_BOTTLE,
                () -> new BeverageBottleItem(new Item.Properties().stacksTo(16).tab(CreativeModeTab.TAB_MISC))
        );
        RegistryRef<BlockEntityType<?>> pressEntity = ports.blockEntities().register(
                AlcoholicIds.ARTISANAL_PRESS_ENTITY,
                () -> BlockEntityType.Builder.of(
                        (position, state) -> new ArtisanalPressBlockEntity(pressType.get(), position, state),
                        pressRef.get()
                ).build(null)
        );
        RegistryRef<BlockEntityType<?>> fermenterEntity = ports.blockEntities().register(
                AlcoholicIds.ARTISANAL_FERMENTER_ENTITY,
                () -> BlockEntityType.Builder.of(
                        (position, state) -> new ArtisanalFermenterBlockEntity(
                                fermenterType.get(),
                                position,
                                state
                        ),
                        fermenterRef.get()
                ).build(null)
        );
        RegistryRef<BlockEntityType<?>> barrelEntity = ports.blockEntities().register(
                AlcoholicIds.OAK_BARREL_ENTITY,
                () -> BlockEntityType.Builder.of(
                        (position, state) -> new OakBarrelBlockEntity(barrelType.get(), position, state),
                        barrelRef.get()
                ).build(null)
        );
        RegistryRef<BlockEntityType<?>> crockEntity = ports.blockEntities().register(
                AlcoholicIds.ARTISANAL_BLENDING_CROCK_ENTITY,
                () -> BlockEntityType.Builder.of(
                        (position, state) -> new ArtisanalBlendingCrockBlockEntity(
                                crockType.get(),
                                position,
                                state
                        ),
                        crockRef.get()
                ).build(null)
        );
        pressEntityHolder.set(pressEntity);
        fermenterEntityHolder.set(fermenterEntity);
        barrelEntityHolder.set(barrelEntity);
        crockEntityHolder.set(crockEntity);
        return new ProcessingContent(
                pressRef,
                pressItem,
                pressEntity,
                fermenterRef,
                fermenterItem,
                fermenterEntity,
                barrelRef,
                barrelItem,
                barrelEntity,
                crockRef,
                crockItem,
                crockEntity,
                yeast,
                pomace,
                emptyBottle,
                beverageBottle
        );
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of(Material.WOOD)
                .strength(2.5F)
                .sound(SoundType.WOOD)
                .noOcclusion();
    }
}

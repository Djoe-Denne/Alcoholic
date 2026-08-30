package com.djden.alcoholic.minecraft.content;

import com.djden.alcoholic.minecraft.bottle.BeverageBottleItem;
import com.djden.alcoholic.minecraft.guide.GrimoireItem;
import com.djden.alcoholic.minecraft.guide.GrimoireKind;
import com.djden.alcoholic.minecraft.process.ArtisanalBlendingCrockBlock;
import com.djden.alcoholic.minecraft.process.ArtisanalBlendingCrockBlockEntity;
import com.djden.alcoholic.minecraft.process.ArtisanalFermenterBlock;
import com.djden.alcoholic.minecraft.process.ArtisanalFermenterBlockEntity;
import com.djden.alcoholic.minecraft.process.ArtisanalPressBlock;
import com.djden.alcoholic.minecraft.process.ArtisanalPressBlockEntity;
import com.djden.alcoholic.minecraft.mechanical.ElectricMotorBlock;
import com.djden.alcoholic.minecraft.mechanical.ElectricMotorBlockEntity;
import com.djden.alcoholic.minecraft.mechanical.PrimitiveCombustionEngineBlock;
import com.djden.alcoholic.minecraft.mechanical.PrimitiveCombustionEngineBlockEntity;
import com.djden.alcoholic.minecraft.process.BrewingKettleBlock;
import com.djden.alcoholic.minecraft.process.BrewingKettleBlockEntity;
import com.djden.alcoholic.minecraft.process.MaltMillBlock;
import com.djden.alcoholic.minecraft.process.MaltMillBlockEntity;
import com.djden.alcoholic.minecraft.process.MaltingFloorBlock;
import com.djden.alcoholic.minecraft.process.MaltingFloorBlockEntity;
import com.djden.alcoholic.minecraft.process.MashTunBlock;
import com.djden.alcoholic.minecraft.process.MashTunBlockEntity;
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
        AtomicReference<RegistryRef<BlockEntityType<?>>> maltingEntityHolder = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> mashEntityHolder = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> kettleEntityHolder = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> millEntityHolder = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> engineEntityHolder = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> motorEntityHolder = new AtomicReference<>();
        Supplier<BlockEntityType<?>> pressType = () -> pressEntityHolder.get().get();
        Supplier<BlockEntityType<?>> fermenterType = () -> fermenterEntityHolder.get().get();
        Supplier<BlockEntityType<?>> barrelType = () -> barrelEntityHolder.get().get();
        Supplier<BlockEntityType<?>> crockType = () -> crockEntityHolder.get().get();
        Supplier<BlockEntityType<?>> maltingType = () -> maltingEntityHolder.get().get();
        Supplier<BlockEntityType<?>> mashType = () -> mashEntityHolder.get().get();
        Supplier<BlockEntityType<?>> kettleType = () -> kettleEntityHolder.get().get();
        Supplier<BlockEntityType<?>> millType = () -> millEntityHolder.get().get();
        Supplier<BlockEntityType<?>> engineType = () -> engineEntityHolder.get().get();
        Supplier<BlockEntityType<?>> motorType = () -> motorEntityHolder.get().get();

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
        RegistryRef<Block> maltingRef = ports.blocks().register(
                AlcoholicIds.MALTING_FLOOR,
                () -> new MaltingFloorBlock(machineProperties(), maltingType)
        );
        RegistryRef<Block> mashRef = ports.blocks().register(
                AlcoholicIds.MASH_TUN,
                () -> new MashTunBlock(machineProperties(), mashType)
        );
        RegistryRef<Block> kettleRef = ports.blocks().register(
                AlcoholicIds.BREWING_KETTLE,
                () -> new BrewingKettleBlock(machineProperties(), kettleType)
        );
        RegistryRef<Block> millRef = ports.blocks().register(
                AlcoholicIds.MALT_MILL,
                () -> new MaltMillBlock(machineProperties(), millType)
        );
        RegistryRef<Block> engineRef = ports.blocks().register(
                AlcoholicIds.PRIMITIVE_COMBUSTION_ENGINE,
                () -> new PrimitiveCombustionEngineBlock(engineProperties(), engineType)
        );
        RegistryRef<Block> motorRef = ports.blocks().register(
                AlcoholicIds.ELECTRIC_MOTOR,
                () -> new ElectricMotorBlock(motorProperties(), motorType)
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
        RegistryRef<Item> maltingItem = ports.items().register(
                AlcoholicIds.MALTING_FLOOR,
                () -> new BlockItem(maltingRef.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS))
        );
        RegistryRef<Item> mashItem = ports.items().register(
                AlcoholicIds.MASH_TUN,
                () -> new BlockItem(mashRef.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS))
        );
        RegistryRef<Item> kettleItem = ports.items().register(
                AlcoholicIds.BREWING_KETTLE,
                () -> new BlockItem(kettleRef.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS))
        );
        RegistryRef<Item> millItem = ports.items().register(
                AlcoholicIds.MALT_MILL,
                () -> new BlockItem(millRef.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS))
        );
        RegistryRef<Item> engineItem = ports.items().register(
                AlcoholicIds.PRIMITIVE_COMBUSTION_ENGINE,
                () -> new BlockItem(engineRef.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS))
        );
        RegistryRef<Item> motorItem = ports.items().register(
                AlcoholicIds.ELECTRIC_MOTOR,
                () -> new BlockItem(motorRef.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS))
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
        RegistryRef<Item> spentGrain = ports.items().register(
                AlcoholicIds.SPENT_GRAIN,
                () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC))
        );
        ports.items().register(
                AlcoholicIds.WINE_GRIMOIRE,
                () -> new GrimoireItem(
                        GrimoireKind.WINE,
                        new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC)
                )
        );
        ports.items().register(
                AlcoholicIds.BEER_GRIMOIRE,
                () -> new GrimoireItem(
                        GrimoireKind.BEER,
                        new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC)
                )
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
        RegistryRef<BlockEntityType<?>> maltingEntity = ports.blockEntities().register(
                AlcoholicIds.MALTING_FLOOR_ENTITY,
                () -> BlockEntityType.Builder.of(
                        (position, state) -> new MaltingFloorBlockEntity(maltingType.get(), position, state),
                        maltingRef.get()
                ).build(null)
        );
        RegistryRef<BlockEntityType<?>> mashEntity = ports.blockEntities().register(
                AlcoholicIds.MASH_TUN_ENTITY,
                () -> BlockEntityType.Builder.of(
                        (position, state) -> new MashTunBlockEntity(mashType.get(), position, state),
                        mashRef.get()
                ).build(null)
        );
        RegistryRef<BlockEntityType<?>> kettleEntity = ports.blockEntities().register(
                AlcoholicIds.BREWING_KETTLE_ENTITY,
                () -> BlockEntityType.Builder.of(
                        (position, state) -> new BrewingKettleBlockEntity(kettleType.get(), position, state),
                        kettleRef.get()
                ).build(null)
        );
        RegistryRef<BlockEntityType<?>> millEntity = ports.blockEntities().register(
                AlcoholicIds.MALT_MILL_ENTITY,
                () -> BlockEntityType.Builder.of(
                        (position, state) -> new MaltMillBlockEntity(millType.get(), position, state),
                        millRef.get()
                ).build(null)
        );
        RegistryRef<BlockEntityType<?>> engineEntity = ports.blockEntities().register(
                AlcoholicIds.PRIMITIVE_COMBUSTION_ENGINE_ENTITY,
                () -> BlockEntityType.Builder.of(
                        (position, state) -> new PrimitiveCombustionEngineBlockEntity(
                                engineType.get(),
                                position,
                                state
                        ),
                        engineRef.get()
                ).build(null)
        );
        RegistryRef<BlockEntityType<?>> motorEntity = ports.blockEntities().register(
                AlcoholicIds.ELECTRIC_MOTOR_ENTITY,
                () -> BlockEntityType.Builder.of(
                        (position, state) -> new ElectricMotorBlockEntity(
                                motorType.get(),
                                position,
                                state
                        ),
                        motorRef.get()
                ).build(null)
        );
        pressEntityHolder.set(pressEntity);
        fermenterEntityHolder.set(fermenterEntity);
        barrelEntityHolder.set(barrelEntity);
        crockEntityHolder.set(crockEntity);
        maltingEntityHolder.set(maltingEntity);
        mashEntityHolder.set(mashEntity);
        kettleEntityHolder.set(kettleEntity);
        millEntityHolder.set(millEntity);
        engineEntityHolder.set(engineEntity);
        motorEntityHolder.set(motorEntity);
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
                beverageBottle,
                maltingRef,
                maltingItem,
                maltingEntity,
                mashRef,
                mashItem,
                mashEntity,
                kettleRef,
                kettleItem,
                kettleEntity,
                spentGrain,
                millRef,
                millItem,
                millEntity,
                engineRef,
                engineItem,
                engineEntity,
                motorRef,
                motorItem,
                motorEntity
        );
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of(Material.WOOD)
                .strength(2.5F)
                .sound(SoundType.WOOD)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties engineProperties() {
        return BlockBehaviour.Properties.of(Material.STONE)
                .strength(3.5F)
                .sound(SoundType.STONE)
                .lightLevel(state -> state.getValue(PrimitiveCombustionEngineBlock.LIT) ? 13 : 0);
    }

    private static BlockBehaviour.Properties motorProperties() {
        return BlockBehaviour.Properties.of(Material.METAL)
                .strength(3.5F)
                .sound(SoundType.METAL)
                .lightLevel(state -> state.getValue(ElectricMotorBlock.LIT) ? 7 : 0);
    }
}

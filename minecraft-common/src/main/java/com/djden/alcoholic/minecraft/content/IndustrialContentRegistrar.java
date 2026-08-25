package com.djden.alcoholic.minecraft.content;

import com.djden.alcoholic.application.machine.BuiltinMachines;
import com.djden.alcoholic.domain.multiblock.PartRole;
import com.djden.alcoholic.minecraft.content.KineticEntityFactory;
import com.djden.alcoholic.minecraft.multiblock.FluidPortBlock;
import com.djden.alcoholic.minecraft.multiblock.FluidPortBlockEntity;
import com.djden.alcoholic.minecraft.multiblock.IndustrialPartBlock;
import com.djden.alcoholic.minecraft.multiblock.ItemPortBlock;
import com.djden.alcoholic.minecraft.multiblock.ItemPortBlockEntity;
import com.djden.alcoholic.minecraft.multiblock.KineticPortBlock;
import com.djden.alcoholic.minecraft.multiblock.KineticPortBlockEntity;
import com.djden.alcoholic.minecraft.multiblock.MultiblockControllerBlock;
import com.djden.alcoholic.minecraft.multiblock.MultiblockControllerBlockEntity;
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
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class IndustrialContentRegistrar {
    private IndustrialContentRegistrar() {
    }

    public static IndustrialContent register(ContentRegistrationPorts ports) {
        return register(ports, KineticPortBlock::new, KineticPortBlockEntity::new);
    }

    public static IndustrialContent register(
            ContentRegistrationPorts ports,
            BiFunction<BlockBehaviour.Properties, Supplier<? extends BlockEntityType<?>>, Block> kineticPortFactory
    ) {
        return register(ports, kineticPortFactory, KineticPortBlockEntity::new);
    }

    public static IndustrialContent register(
            ContentRegistrationPorts ports,
            BiFunction<BlockBehaviour.Properties, Supplier<? extends BlockEntityType<?>>, Block> kineticPortFactory,
            KineticEntityFactory kineticEntityFactory
    ) {
        Objects.requireNonNull(ports, "ports");
        Objects.requireNonNull(kineticPortFactory, "kineticPortFactory");
        Objects.requireNonNull(kineticEntityFactory, "kineticEntityFactory");
        AtomicReference<RegistryRef<BlockEntityType<?>>> fluidEntity = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> itemEntity = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> kineticEntity = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> pressEntity = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> vatEntity = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> tankEntity = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> maltHouseEntity = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> rollerMillEntity = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> mashTunEntity = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> kettleEntity = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> conditioningEntity = new AtomicReference<>();

        RegistryRef<Block> casing = ports.blocks().register(
                AlcoholicIds.INDUSTRIAL_CASING,
                () -> new IndustrialPartBlock(steel(), PartRole.CASING)
        );
        RegistryRef<Block> window = ports.blocks().register(
                AlcoholicIds.MACHINE_WINDOW,
                () -> new IndustrialPartBlock(steel().noOcclusion(), PartRole.WINDOW)
        );
        RegistryRef<Block> hatch = ports.blocks().register(
                AlcoholicIds.ACCESS_HATCH,
                () -> new IndustrialPartBlock(steel(), PartRole.HATCH)
        );
        RegistryRef<Block> fluidPort = ports.blocks().register(
                AlcoholicIds.FLUID_PORT,
                () -> new FluidPortBlock(steel(), () -> fluidEntity.get().get())
        );
        RegistryRef<Block> itemPort = ports.blocks().register(
                AlcoholicIds.ITEM_PORT,
                () -> new ItemPortBlock(steel(), () -> itemEntity.get().get())
        );
        RegistryRef<Block> kineticPort = ports.blocks().register(
                AlcoholicIds.KINETIC_PORT,
                () -> kineticPortFactory.apply(steel(), () -> kineticEntity.get().get())
        );
        RegistryRef<Block> press = ports.blocks().register(
                AlcoholicIds.INDUSTRIAL_PRESS_CONTROLLER,
                () -> new MultiblockControllerBlock(
                        steel().noOcclusion(),
                        () -> pressEntity.get().get(),
                        BuiltinMachines.INDUSTRIAL_PRESS
                )
        );
        RegistryRef<Block> vat = ports.blocks().register(
                AlcoholicIds.INDUSTRIAL_VAT_CONTROLLER,
                () -> new MultiblockControllerBlock(
                        steel(),
                        () -> vatEntity.get().get(),
                        BuiltinMachines.INDUSTRIAL_VAT
                )
        );
        RegistryRef<Block> tank = ports.blocks().register(
                AlcoholicIds.INDUSTRIAL_TANK_CONTROLLER,
                () -> new MultiblockControllerBlock(
                        steel(),
                        () -> tankEntity.get().get(),
                        BuiltinMachines.INDUSTRIAL_TANK
                )
        );
        RegistryRef<Block> maltHouse = ports.blocks().register(
                AlcoholicIds.INDUSTRIAL_MALT_HOUSE_CONTROLLER,
                () -> new MultiblockControllerBlock(
                        steel(),
                        () -> maltHouseEntity.get().get(),
                        BuiltinMachines.INDUSTRIAL_MALT_HOUSE
                )
        );
        RegistryRef<Block> rollerMill = ports.blocks().register(
                AlcoholicIds.INDUSTRIAL_ROLLER_MILL_CONTROLLER,
                () -> new MultiblockControllerBlock(
                        steel().noOcclusion(),
                        () -> rollerMillEntity.get().get(),
                        BuiltinMachines.INDUSTRIAL_ROLLER_MILL
                )
        );
        RegistryRef<Block> mashTun = ports.blocks().register(
                AlcoholicIds.INDUSTRIAL_MASH_TUN_CONTROLLER,
                () -> new MultiblockControllerBlock(
                        steel(),
                        () -> mashTunEntity.get().get(),
                        BuiltinMachines.INDUSTRIAL_MASH_TUN
                )
        );
        RegistryRef<Block> kettle = ports.blocks().register(
                AlcoholicIds.INDUSTRIAL_BREWING_KETTLE_CONTROLLER,
                () -> new MultiblockControllerBlock(
                        steel(),
                        () -> kettleEntity.get().get(),
                        BuiltinMachines.INDUSTRIAL_BREWING_KETTLE
                )
        );
        RegistryRef<Block> conditioning = ports.blocks().register(
                AlcoholicIds.INDUSTRIAL_CONDITIONING_VESSEL_CONTROLLER,
                () -> new MultiblockControllerBlock(
                        steel(),
                        () -> conditioningEntity.get().get(),
                        BuiltinMachines.INDUSTRIAL_CONDITIONING_VESSEL
                )
        );

        RegistryRef<Item> casingItem = item(ports, AlcoholicIds.INDUSTRIAL_CASING, casing);
        RegistryRef<Item> windowItem = item(ports, AlcoholicIds.MACHINE_WINDOW, window);
        RegistryRef<Item> hatchItem = item(ports, AlcoholicIds.ACCESS_HATCH, hatch);
        RegistryRef<Item> fluidItem = item(ports, AlcoholicIds.FLUID_PORT, fluidPort);
        RegistryRef<Item> itemPortItem = item(ports, AlcoholicIds.ITEM_PORT, itemPort);
        RegistryRef<Item> kineticItem = item(ports, AlcoholicIds.KINETIC_PORT, kineticPort);
        RegistryRef<Item> pressItem = item(ports, AlcoholicIds.INDUSTRIAL_PRESS_CONTROLLER, press);
        RegistryRef<Item> vatItem = item(ports, AlcoholicIds.INDUSTRIAL_VAT_CONTROLLER, vat);
        RegistryRef<Item> tankItem = item(ports, AlcoholicIds.INDUSTRIAL_TANK_CONTROLLER, tank);
        RegistryRef<Item> maltHouseItem = item(ports, AlcoholicIds.INDUSTRIAL_MALT_HOUSE_CONTROLLER, maltHouse);
        RegistryRef<Item> rollerMillItem = item(ports, AlcoholicIds.INDUSTRIAL_ROLLER_MILL_CONTROLLER, rollerMill);
        RegistryRef<Item> mashTunItem = item(ports, AlcoholicIds.INDUSTRIAL_MASH_TUN_CONTROLLER, mashTun);
        RegistryRef<Item> kettleItem = item(ports, AlcoholicIds.INDUSTRIAL_BREWING_KETTLE_CONTROLLER, kettle);
        RegistryRef<Item> conditioningItem = item(
                ports,
                AlcoholicIds.INDUSTRIAL_CONDITIONING_VESSEL_CONTROLLER,
                conditioning
        );

        fluidEntity.set(ports.blockEntities().register(
                AlcoholicIds.FLUID_PORT_ENTITY,
                () -> BlockEntityType.Builder.of(
                        (position, state) -> new FluidPortBlockEntity(fluidEntity.get().get(), position, state),
                        fluidPort.get()
                ).build(null)
        ));
        itemEntity.set(ports.blockEntities().register(
                AlcoholicIds.ITEM_PORT_ENTITY,
                () -> BlockEntityType.Builder.of(
                        (position, state) -> new ItemPortBlockEntity(itemEntity.get().get(), position, state),
                        itemPort.get()
                ).build(null)
        ));
        kineticEntity.set(ports.blockEntities().register(
                AlcoholicIds.KINETIC_PORT_ENTITY,
                () -> BlockEntityType.Builder.of(
                        (position, state) -> kineticEntityFactory.create(
                                kineticEntity.get().get(),
                                position,
                                state
                        ),
                        kineticPort.get()
                ).build(null)
        ));
        pressEntity.set(ports.blockEntities().register(
                AlcoholicIds.INDUSTRIAL_PRESS_ENTITY,
                () -> BlockEntityType.Builder.of(
                        (position, state) -> new MultiblockControllerBlockEntity(
                                pressEntity.get().get(),
                                position,
                                state,
                                BuiltinMachines.INDUSTRIAL_PRESS
                        ),
                        press.get()
                ).build(null)
        ));
        vatEntity.set(ports.blockEntities().register(
                AlcoholicIds.INDUSTRIAL_VAT_ENTITY,
                () -> BlockEntityType.Builder.of(
                        (position, state) -> new MultiblockControllerBlockEntity(
                                vatEntity.get().get(),
                                position,
                                state,
                                BuiltinMachines.INDUSTRIAL_VAT
                        ),
                        vat.get()
                ).build(null)
        ));
        tankEntity.set(ports.blockEntities().register(
                AlcoholicIds.INDUSTRIAL_TANK_ENTITY,
                () -> BlockEntityType.Builder.of(
                        (position, state) -> new MultiblockControllerBlockEntity(
                                tankEntity.get().get(),
                                position,
                                state,
                                BuiltinMachines.INDUSTRIAL_TANK
                        ),
                        tank.get()
                ).build(null)
        ));
        maltHouseEntity.set(controllerEntity(
                ports,
                AlcoholicIds.INDUSTRIAL_MALT_HOUSE_ENTITY,
                maltHouseEntity,
                maltHouse,
                BuiltinMachines.INDUSTRIAL_MALT_HOUSE
        ));
        rollerMillEntity.set(controllerEntity(
                ports,
                AlcoholicIds.INDUSTRIAL_ROLLER_MILL_ENTITY,
                rollerMillEntity,
                rollerMill,
                BuiltinMachines.INDUSTRIAL_ROLLER_MILL
        ));
        mashTunEntity.set(controllerEntity(
                ports,
                AlcoholicIds.INDUSTRIAL_MASH_TUN_ENTITY,
                mashTunEntity,
                mashTun,
                BuiltinMachines.INDUSTRIAL_MASH_TUN
        ));
        kettleEntity.set(controllerEntity(
                ports,
                AlcoholicIds.INDUSTRIAL_BREWING_KETTLE_ENTITY,
                kettleEntity,
                kettle,
                BuiltinMachines.INDUSTRIAL_BREWING_KETTLE
        ));
        conditioningEntity.set(controllerEntity(
                ports,
                AlcoholicIds.INDUSTRIAL_CONDITIONING_VESSEL_ENTITY,
                conditioningEntity,
                conditioning,
                BuiltinMachines.INDUSTRIAL_CONDITIONING_VESSEL
        ));

        return new IndustrialContent(
                casing, casingItem,
                window, windowItem,
                hatch, hatchItem,
                fluidPort, fluidItem, fluidEntity.get(),
                itemPort, itemPortItem, itemEntity.get(),
                kineticPort, kineticItem, kineticEntity.get(),
                press, pressItem, pressEntity.get(),
                vat, vatItem, vatEntity.get(),
                tank, tankItem, tankEntity.get(),
                maltHouse, maltHouseItem, maltHouseEntity.get(),
                rollerMill, rollerMillItem, rollerMillEntity.get(),
                mashTun, mashTunItem, mashTunEntity.get(),
                kettle, kettleItem, kettleEntity.get(),
                conditioning, conditioningItem, conditioningEntity.get()
        );
    }

    private static RegistryRef<BlockEntityType<?>> controllerEntity(
            ContentRegistrationPorts ports,
            com.djden.alcoholic.api.ResourceId id,
            AtomicReference<RegistryRef<BlockEntityType<?>>> entity,
            RegistryRef<Block> block,
            com.djden.alcoholic.api.ResourceId definition
    ) {
        return ports.blockEntities().register(
                id,
                () -> BlockEntityType.Builder.of(
                        (position, state) -> new MultiblockControllerBlockEntity(
                                entity.get().get(),
                                position,
                                state,
                                definition
                        ),
                        block.get()
                ).build(null)
        );
    }

    private static RegistryRef<Item> item(
            ContentRegistrationPorts ports,
            com.djden.alcoholic.api.ResourceId id,
            RegistryRef<Block> block
    ) {
        return ports.items().register(
                id,
                () -> new BlockItem(block.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS))
        );
    }

    private static BlockBehaviour.Properties steel() {
        return BlockBehaviour.Properties.of(Material.METAL)
                .strength(4.0F, 8.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }
}

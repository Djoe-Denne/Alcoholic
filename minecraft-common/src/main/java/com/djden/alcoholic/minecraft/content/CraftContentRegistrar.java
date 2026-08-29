package com.djden.alcoholic.minecraft.content;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.application.machine.BuiltinCraftMachines;
import com.djden.alcoholic.domain.multiblock.PartRole;
import com.djden.alcoholic.minecraft.multiblock.IndustrialPartBlock;
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

public final class CraftContentRegistrar {
    private CraftContentRegistrar() {
    }

    public static CraftContent register(ContentRegistrationPorts ports) {
        Objects.requireNonNull(ports, "ports");
        AtomicReference<RegistryRef<BlockEntityType<?>>> maltHouseEntity = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> millEntity = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> mashTunEntity = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> kettleEntity = new AtomicReference<>();
        AtomicReference<RegistryRef<BlockEntityType<?>>> vatEntity = new AtomicReference<>();

        RegistryRef<Block> casing = ports.blocks().register(
                AlcoholicIds.CRAFT_CASING,
                () -> new IndustrialPartBlock(copper(), PartRole.CASING)
        );
        RegistryRef<Block> maltHouse = controller(
                ports,
                AlcoholicIds.CRAFT_MALT_HOUSE_CONTROLLER,
                maltHouseEntity,
                BuiltinCraftMachines.CRAFT_MALT_HOUSE
        );
        RegistryRef<Block> mill = controller(
                ports,
                AlcoholicIds.CRAFT_MILL_CONTROLLER,
                millEntity,
                BuiltinCraftMachines.CRAFT_MILL
        );
        RegistryRef<Block> mashTun = controller(
                ports,
                AlcoholicIds.CRAFT_MASH_TUN_CONTROLLER,
                mashTunEntity,
                BuiltinCraftMachines.CRAFT_MASH_TUN
        );
        RegistryRef<Block> kettle = controller(
                ports,
                AlcoholicIds.CRAFT_BREWING_KETTLE_CONTROLLER,
                kettleEntity,
                BuiltinCraftMachines.CRAFT_BREWING_KETTLE
        );
        RegistryRef<Block> vat = controller(
                ports,
                AlcoholicIds.CRAFT_VAT_CONTROLLER,
                vatEntity,
                BuiltinCraftMachines.CRAFT_VAT
        );

        RegistryRef<Item> casingItem = item(ports, AlcoholicIds.CRAFT_CASING, casing);
        RegistryRef<Item> maltHouseItem = item(ports, AlcoholicIds.CRAFT_MALT_HOUSE_CONTROLLER, maltHouse);
        RegistryRef<Item> millItem = item(ports, AlcoholicIds.CRAFT_MILL_CONTROLLER, mill);
        RegistryRef<Item> mashTunItem = item(ports, AlcoholicIds.CRAFT_MASH_TUN_CONTROLLER, mashTun);
        RegistryRef<Item> kettleItem = item(ports, AlcoholicIds.CRAFT_BREWING_KETTLE_CONTROLLER, kettle);
        RegistryRef<Item> vatItem = item(ports, AlcoholicIds.CRAFT_VAT_CONTROLLER, vat);

        maltHouseEntity.set(controllerEntity(
                ports,
                AlcoholicIds.CRAFT_MALT_HOUSE_ENTITY,
                maltHouseEntity,
                maltHouse,
                BuiltinCraftMachines.CRAFT_MALT_HOUSE
        ));
        millEntity.set(controllerEntity(
                ports,
                AlcoholicIds.CRAFT_MILL_ENTITY,
                millEntity,
                mill,
                BuiltinCraftMachines.CRAFT_MILL
        ));
        mashTunEntity.set(controllerEntity(
                ports,
                AlcoholicIds.CRAFT_MASH_TUN_ENTITY,
                mashTunEntity,
                mashTun,
                BuiltinCraftMachines.CRAFT_MASH_TUN
        ));
        kettleEntity.set(controllerEntity(
                ports,
                AlcoholicIds.CRAFT_BREWING_KETTLE_ENTITY,
                kettleEntity,
                kettle,
                BuiltinCraftMachines.CRAFT_BREWING_KETTLE
        ));
        vatEntity.set(controllerEntity(
                ports,
                AlcoholicIds.CRAFT_VAT_ENTITY,
                vatEntity,
                vat,
                BuiltinCraftMachines.CRAFT_VAT
        ));

        return new CraftContent(
                casing, casingItem,
                maltHouse, maltHouseItem, maltHouseEntity.get(),
                mill, millItem, millEntity.get(),
                mashTun, mashTunItem, mashTunEntity.get(),
                kettle, kettleItem, kettleEntity.get(),
                vat, vatItem, vatEntity.get()
        );
    }

    private static RegistryRef<Block> controller(
            ContentRegistrationPorts ports,
            ResourceId id,
            AtomicReference<RegistryRef<BlockEntityType<?>>> entity,
            ResourceId definition
    ) {
        return ports.blocks().register(
                id,
                () -> new MultiblockControllerBlock(copper(), () -> entity.get().get(), definition)
        );
    }

    private static RegistryRef<BlockEntityType<?>> controllerEntity(
            ContentRegistrationPorts ports,
            ResourceId id,
            AtomicReference<RegistryRef<BlockEntityType<?>>> entity,
            RegistryRef<Block> block,
            ResourceId definition
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
            ResourceId id,
            RegistryRef<Block> block
    ) {
        return ports.items().register(
                id,
                () -> new BlockItem(block.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS))
        );
    }

    private static BlockBehaviour.Properties copper() {
        return BlockBehaviour.Properties.of(Material.METAL)
                .strength(3.0F, 6.0F)
                .sound(SoundType.COPPER)
                .requiresCorrectToolForDrops();
    }
}

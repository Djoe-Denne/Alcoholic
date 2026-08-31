package com.djden.alcoholic.minecraft.content;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.minecraft.agriculture.CerealCropBlock;
import com.djden.alcoholic.minecraft.agriculture.HopBineBlock;
import com.djden.alcoholic.minecraft.agriculture.HopCanopyBlock;
import com.djden.alcoholic.minecraft.agriculture.HopStemBlock;
import com.djden.alcoholic.minecraft.agriculture.WildHopsBlock;
import com.djden.alcoholic.minecraft.process.SolidPropertyNbt;
import com.djden.alcoholic.platform.api.registry.RegistryRef;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class GrainContentRegistrar {
    private GrainContentRegistrar() {
    }

    public static GrainContent register(
            ContentRegistrationPorts ports,
            BooleanSupplier barleyDiscoverable,
            BooleanSupplier hopsDiscoverable,
            Supplier<? extends Block> trellisWire
    ) {
        Objects.requireNonNull(ports, "ports");
        Objects.requireNonNull(trellisWire, "trellisWire");
        AtomicReference<RegistryRef<Item>> seedsHolder = new AtomicReference<>();
        AtomicReference<RegistryRef<Item>> hopsHolder = new AtomicReference<>();
        AtomicReference<RegistryRef<Block>> hopStemHolder = new AtomicReference<>();
        AtomicReference<RegistryRef<Block>> hopCanopyHolder = new AtomicReference<>();

        RegistryRef<Block> barleyCrop = ports.blocks().register(
                AlcoholicIds.BARLEY_CROP,
                () -> new CerealCropBlock(cropProperties(), () -> seedsHolder.get().get(), AlcoholicIds.BARLEY)
        );
        RegistryRef<Item> barley = ports.items().register(
                AlcoholicIds.BARLEY,
                () -> new Item(tab(barleyDiscoverable, CreativeModeTab.TAB_MISC))
        );
        RegistryRef<Item> barleySeeds = ports.items().register(
                AlcoholicIds.BARLEY_SEEDS,
                () -> new ItemNameBlockItem(barleyCrop.get(), tab(barleyDiscoverable, CreativeModeTab.TAB_MISC))
        );
        seedsHolder.set(barleySeeds);

        RegistryRef<Block> hopBine = ports.blocks().register(
                AlcoholicIds.HOP_BINE,
                () -> new HopBineBlock(
                        cropProperties(),
                        () -> hopHarvest(hopsHolder.get().get()),
                        () -> hopStemHolder.get().get(),
                        () -> hopCanopyHolder.get().get()
                )
        );
        RegistryRef<Block> hopBineStem = ports.blocks().register(
                AlcoholicIds.HOP_BINE_STEM,
                () -> new HopStemBlock(stemProperties(), hopBine::get)
        );
        RegistryRef<Block> hopBineCanopy = ports.blocks().register(
                AlcoholicIds.HOP_BINE_CANOPY,
                () -> new HopCanopyBlock(stemProperties(), hopBine::get, trellisWire)
        );
        hopStemHolder.set(hopBineStem);
        hopCanopyHolder.set(hopBineCanopy);
        RegistryRef<Item> hops = ports.items().register(
                AlcoholicIds.HOPS,
                () -> new Item(tab(hopsDiscoverable, CreativeModeTab.TAB_MISC))
        );
        hopsHolder.set(hops);
        RegistryRef<Item> hopRhizome = ports.items().register(
                AlcoholicIds.HOP_RHIZOME,
                () -> new ItemNameBlockItem(hopBine.get(), tab(hopsDiscoverable, CreativeModeTab.TAB_MISC))
        );
        RegistryRef<Block> wildHops = ports.blocks().register(
                AlcoholicIds.WILD_HOPS,
                () -> new WildHopsBlock(cropProperties(), hopRhizome::get)
        );
        RegistryRef<Item> maltedBarley = ports.items().register(
                AlcoholicIds.MALTED_BARLEY,
                () -> new Item(tab(barleyDiscoverable, CreativeModeTab.TAB_MISC))
        );
        RegistryRef<Item> grist = ports.items().register(
                AlcoholicIds.GRIST,
                () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC))
        );
        return new GrainContent(
                barleyCrop,
                hopBine,
                hopBineStem,
                hopBineCanopy,
                wildHops,
                barley,
                barleySeeds,
                hops,
                hopRhizome,
                maltedBarley,
                grist
        );
    }

    private static ItemStack hopHarvest(Item hops) {
        ItemStack stack = new ItemStack(hops);
        SolidPropertyNbt.write(stack, Map.of(
                ResourceId.parse("alcoholic:bitterness"), 0.55,
                ResourceId.parse("alcoholic:aroma"), 0.40
        ));
        return stack;
    }

    private static Item.Properties tab(BooleanSupplier discoverable, CreativeModeTab tab) {
        Item.Properties properties = new Item.Properties();
        return discoverable.getAsBoolean() ? properties.tab(tab) : properties;
    }

    private static BlockBehaviour.Properties cropProperties() {
        return BlockBehaviour.Properties.copy(Blocks.WHEAT)
                .noCollission()
                .randomTicks()
                .sound(SoundType.CROP);
    }

    private static BlockBehaviour.Properties stemProperties() {
        return BlockBehaviour.Properties.copy(Blocks.WHEAT)
                .noCollission()
                .sound(SoundType.CROP);
    }
}

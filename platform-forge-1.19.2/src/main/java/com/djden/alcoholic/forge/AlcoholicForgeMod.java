package com.djden.alcoholic.forge;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.application.agriculture.CropKind;
import com.djden.alcoholic.application.agriculture.CropProviderSelectionPolicy;
import com.djden.alcoholic.application.agriculture.GameplaySource;
import com.djden.alcoholic.application.compatibility.CompatibilityService;
import com.djden.alcoholic.application.compatibility.CompatibilitySnapshot;
import com.djden.alcoholic.forge.client.AlcoholicClient;
import com.djden.alcoholic.forge.compatibility.ForgeModPresenceAdapter;
import com.djden.alcoholic.forge.datagen.AlcoholicDataGenerators;
import com.djden.alcoholic.forge.event.ForgeBeverageEvents;
import com.djden.alcoholic.forge.event.ForgeDebugKitCommands;
import com.djden.alcoholic.forge.event.ForgePlaceCommands;
import com.djden.alcoholic.forge.event.ForgeInspectEvents;
import com.djden.alcoholic.forge.event.ForgeViticultureEvents;
import com.djden.alcoholic.forge.registry.ForgeRegistryPort;
import com.djden.alcoholic.forge.energy.ForgeEnergyCapabilities;
import com.djden.alcoholic.integration.create.forge.ForgeCreateIntegration;
import com.djden.alcoholic.integration.crossroads.forge.ForgeCrossroadsIntegration;
import com.djden.alcoholic.integration.vinery.VineryGrapeProvider;
import com.djden.alcoholic.minecraft.advancement.AdvancementHooks;
import com.djden.alcoholic.minecraft.content.AlcoholicContent;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.content.ContentRegistrationPorts;
import com.djden.alcoholic.minecraft.beverage.BeverageRuntime;
import com.djden.alcoholic.minecraft.content.BeverageFrameworkBootstrap;
import com.djden.alcoholic.forge.damage.IndustrialDamageSources;
import com.djden.alcoholic.forge.event.ForgeIndustrialEvents;
import com.djden.alcoholic.forge.item.ForgeItemCapabilities;
import com.djden.alcoholic.minecraft.content.GrapeContentRegistrar;
import com.djden.alcoholic.minecraft.content.GrainContent;
import com.djden.alcoholic.minecraft.content.GrainContentRegistrar;
import com.djden.alcoholic.minecraft.content.IndustrialContent;
import com.djden.alcoholic.minecraft.content.ProcessingContent;
import com.djden.alcoholic.minecraft.content.ProcessingContentRegistrar;
import com.djden.alcoholic.minecraft.menu.MachineMenuContent;
import com.djden.alcoholic.minecraft.menu.MachineMenuRegistrar;
import com.djden.alcoholic.minecraft.viticulture.InternalGrapeProvider;
import com.djden.alcoholic.minecraft.viticulture.ViticultureRuntime;
import com.djden.alcoholic.forge.condition.ItemPresentCondition;
import com.djden.alcoholic.forge.fluid.ForgeFluidCapabilities;
import com.djden.alcoholic.forge.fluid.ForgeFluidInteraction;
import com.djden.alcoholic.forge.fluid.ForgeFluidRegistrationPort;
import com.djden.alcoholic.minecraft.fluid.FluidContent;
import com.djden.alcoholic.minecraft.fluid.FluidContentRegistrar;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

@Mod(AlcoholicIds.MOD_ID)
public final class AlcoholicForgeMod {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final CompatibilitySnapshot compatibility;
    private final AlcoholicContent content;
    private final ProcessingContent processing;
    private final GrainContent grain;
    private final IndustrialContent industrial;
    private final MachineMenuContent menus;
    private final FluidContent fluids;

    public AlcoholicForgeMod(FMLJavaModLoadingContext loadingContext) {
        IEventBus modEventBus = loadingContext.getModEventBus();
        compatibility = new CompatibilityService(new ForgeModPresenceAdapter()).snapshot();
        CraftingHelper.register(new ItemPresentCondition.Serializer());
        CropProviderSelectionPolicy cropPolicy = new CropProviderSelectionPolicy(
                compatibility,
                crop -> switch (crop) {
                    case BARLEY -> itemPresent("brewery:barley") || itemPresent("brewery:barley_seeds");
                    case HOPS -> itemPresent("brewery:hops");
                    default -> true;
                }
        );

        ForgeRegistryPort<Block> blocks =
                new ForgeRegistryPort<>(ForgeRegistries.BLOCKS, AlcoholicIds.MOD_ID);
        ForgeRegistryPort<Item> items =
                new ForgeRegistryPort<>(ForgeRegistries.ITEMS, AlcoholicIds.MOD_ID);
        ForgeRegistryPort<BlockEntityType<?>> blockEntities =
                new ForgeRegistryPort<>(
                        ForgeRegistries.BLOCK_ENTITY_TYPES,
                        AlcoholicIds.MOD_ID
                );
        ForgeRegistryPort<MenuType<?>> menuTypes =
                new ForgeRegistryPort<>(ForgeRegistries.MENU_TYPES, AlcoholicIds.MOD_ID);
        ContentRegistrationPorts ports = new ContentRegistrationPorts(blocks, items, blockEntities, menuTypes);
        ForgeFluidRegistrationPort fluidRegistration = new ForgeFluidRegistrationPort(blocks, items);
        BeverageRuntime beverageRuntime = BeverageRuntime.shared();
        BeverageFrameworkBootstrap.install(beverageRuntime);
        ViticultureRuntime runtime = ViticultureRuntime.shared();
        runtime.configureProviders(
                compatibility,
                new InternalGrapeProvider(),
                new VineryGrapeProvider(id -> {
                    Item item = ForgeRegistries.ITEMS.getValue(
                            ResourceLocation.fromNamespaceAndPath(
                                    id.namespace(),
                                    id.path()
                            )
                    );
                    return item != null && item != Items.AIR;
                })
        );

        boolean grapesDiscoverable = cropPolicy.isBuiltinAcquisitionEnabled(
                CropKind.GRAPES,
                GameplaySource.CREATIVE_DISCOVERY
        );
        content = GrapeContentRegistrar.register(ports, grapesDiscoverable);
        processing = ProcessingContentRegistrar.register(ports);
        grain = GrainContentRegistrar.register(
                ports,
                () -> cropPolicy.isBuiltinAcquisitionEnabled(CropKind.BARLEY, GameplaySource.CREATIVE_DISCOVERY),
                () -> cropPolicy.isBuiltinAcquisitionEnabled(CropKind.HOPS, GameplaySource.CREATIVE_DISCOVERY)
        );
        industrial = registerIndustrial(ports);
        menus = MachineMenuRegistrar.register(ports);
        fluids = FluidContentRegistrar.register(fluidRegistration);

        blocks.attach(modEventBus);
        items.attach(modEventBus);
        blockEntities.attach(modEventBus);
        menuTypes.attach(modEventBus);
        fluidRegistration.attach(modEventBus);
        AdvancementHooks.register();
        modEventBus.addListener(AlcoholicDataGenerators::gatherData);
        modEventBus.addListener(EventPriority.LOWEST, this::freezeBeverageApi);
        MinecraftForge.EVENT_BUS.register(
                new ForgeViticultureEvents(compatibility, content, runtime)
        );
        MinecraftForge.EVENT_BUS.register(new ForgeBeverageEvents(beverageRuntime));
        MinecraftForge.EVENT_BUS.register(new ForgeFluidCapabilities(fluids));
        MinecraftForge.EVENT_BUS.register(new ForgeItemCapabilities());
        MinecraftForge.EVENT_BUS.register(new ForgeEnergyCapabilities());
        MinecraftForge.EVENT_BUS.register(new ForgeFluidInteraction());
        MinecraftForge.EVENT_BUS.register(new ForgeInspectEvents());
        MinecraftForge.EVENT_BUS.register(new ForgeDebugKitCommands(content, processing, grain, industrial));
        MinecraftForge.EVENT_BUS.register(new ForgePlaceCommands());
        MinecraftForge.EVENT_BUS.register(new ForgeIndustrialEvents());

        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> AlcoholicClient.register(modEventBus, content, processing, grain, industrial, menus)
        );

        if (ForgeCreateIntegration.shouldActivate(compatibility)) {
            LOGGER.info("Create integration boundary active for Create {}", "0.5.1.x");
            ForgeCreateIntegration.install();
        }
        if (ForgeCrossroadsIntegration.shouldActivate(compatibility)) {
            LOGGER.info("Crossroads rotary adapter active");
            ForgeCrossroadsIntegration.install();
        }
    }

    public CompatibilitySnapshot compatibility() {
        return compatibility;
    }

    public AlcoholicContent content() {
        return content;
    }

    public ProcessingContent processing() {
        return processing;
    }

    public GrainContent grain() {
        return grain;
    }

    public IndustrialContent industrial() {
        return industrial;
    }

    public MachineMenuContent menus() {
        return menus;
    }

    public FluidContent fluids() {
        return fluids;
    }

    private void freezeBeverageApi(FMLCommonSetupEvent event) {
        BeverageRuntime.shared().freeze();
        IndustrialDamageSources.install();
        event.enqueueWork(AlcoholicForgeMod::registerCompostables);
    }

    private static void registerCompostables() {
        putCompostable(AlcoholicIds.GRAPE_POMACE, 0.3f);
        putCompostable(AlcoholicIds.SPENT_GRAIN, 0.3f);
    }

    private static void putCompostable(ResourceId id, float chance) {
        Item item = ForgeRegistries.ITEMS.getValue(
                ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path())
        );
        if (item != null && item != Items.AIR) {
            ComposterBlock.COMPOSTABLES.put(item, chance);
        }
    }

    private static boolean itemPresent(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            return false;
        }
        Item item = ForgeRegistries.ITEMS.getValue(location);
        return item != null && item != Items.AIR;
    }

    private static IndustrialContent registerIndustrial(ContentRegistrationPorts ports) {
        return ForgeCreateIntegration.registerIndustrial(ports);
    }
}

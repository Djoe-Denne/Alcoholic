package com.djden.alcoholic.minecraft.debug;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.application.machine.BuiltinMachines;
import com.djden.alcoholic.application.machine.MachineCatalog;
import com.djden.alcoholic.domain.multiblock.MultiblockConstraints;
import com.djden.alcoholic.domain.multiblock.MultiblockDefinition;
import com.djden.alcoholic.domain.multiblock.PartRole;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.mechanical.PrimitiveCombustionEngineBlockEntity;
import com.djden.alcoholic.minecraft.multiblock.HollowCuboidPlacer;
import com.djden.alcoholic.minecraft.multiblock.IndustrialRuntime;
import com.djden.alcoholic.minecraft.multiblock.MultiblockControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Debug layouts for the shipped beer line. Specs are machine aliases and
 * neighbor pads, not drink-family branches.
 */
public final class BeerLinePlacer {
    public static final int INDUSTRIAL_STRIDE = 5;

    private static final Map<String, MachineSpec> SPECS = new LinkedHashMap<>();

    static {
        register(MachineSpec.single("malting_floor", AlcoholicIds.MALTING_FLOOR));
        register(MachineSpec.single("malt_mill", AlcoholicIds.MALT_MILL).withEngine());
        register(MachineSpec.single("mash_tun", AlcoholicIds.MASH_TUN).withHeat(HeatPad.MAGMA));
        register(MachineSpec.single("brewing_kettle", AlcoholicIds.BREWING_KETTLE).withHeat(HeatPad.CAMPFIRE));
        register(MachineSpec.single("fermenter", AlcoholicIds.ARTISANAL_FERMENTER));
        register(MachineSpec.industrial("malt_house", BuiltinMachines.INDUSTRIAL_MALT_HOUSE));
        register(MachineSpec.industrial("roller_mill", BuiltinMachines.INDUSTRIAL_ROLLER_MILL));
        register(MachineSpec.industrial("industrial_mash_tun", BuiltinMachines.INDUSTRIAL_MASH_TUN)
                .withHeat(HeatPad.MAGMA));
        register(MachineSpec.industrial("industrial_brewing_kettle", BuiltinMachines.INDUSTRIAL_BREWING_KETTLE)
                .withHeat(HeatPad.CAMPFIRE));
        register(MachineSpec.industrial("vat", BuiltinMachines.INDUSTRIAL_VAT));
        register(MachineSpec.industrial("conditioning", BuiltinMachines.INDUSTRIAL_CONDITIONING_VESSEL));
        register(MachineSpec.industrial("tank", BuiltinMachines.INDUSTRIAL_TANK));
    }

    private static final List<String> ARTISANAL_LINE = List.of(
            "malting_floor",
            "malt_mill",
            "mash_tun",
            "brewing_kettle",
            "fermenter"
    );

    private static final List<String> INDUSTRIAL_LINE = List.of(
            "malt_house",
            "roller_mill",
            "industrial_mash_tun",
            "industrial_brewing_kettle",
            "vat",
            "conditioning",
            "tank"
    );

    private BeerLinePlacer() {
    }

    public static Set<String> aliases() {
        return SPECS.keySet();
    }

    public static Optional<MachineSpec> spec(String alias) {
        if (alias == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(SPECS.get(alias));
    }

    public static List<PlaceResult> placeArtisanal(Level level, BlockPos origin) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(origin, "origin");
        List<PlaceResult> results = new ArrayList<>();
        results.add(placeMachine(level, "malting_floor", origin, null).orElseThrow());
        results.add(placeMachine(level, "malt_mill", origin.offset(2, 0, 0), null).orElseThrow());
        results.add(placeHeatedSingle(
                level,
                spec("mash_tun").orElseThrow(),
                origin.offset(5, 1, 0)
        ));
        results.add(placeHeatedSingle(
                level,
                spec("brewing_kettle").orElseThrow(),
                origin.offset(7, 1, 0)
        ));
        results.add(placeMachine(level, "fermenter", origin.offset(9, 0, 0), null).orElseThrow());
        return List.copyOf(results);
    }

    public static List<PlaceResult> placeIndustrial(Level level, BlockPos origin) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(origin, "origin");
        List<PlaceResult> results = new ArrayList<>();
        int index = 0;
        for (String alias : INDUSTRIAL_LINE) {
            results.add(placeMachine(level, alias, origin.offset(index * INDUSTRIAL_STRIDE, 0, 0), null)
                    .orElseThrow());
            index++;
        }
        return List.copyOf(results);
    }

    public static Optional<PlaceResult> placeMachine(
            Level level,
            String alias,
            BlockPos origin,
            Dimensions size
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(origin, "origin");
        MachineSpec spec = SPECS.get(alias);
        if (spec == null) {
            return Optional.empty();
        }
        if (spec.industrial()) {
            return Optional.of(placeIndustrialMachine(level, spec, origin, size));
        }
        return Optional.of(placeArtisanalMachine(level, spec, origin));
    }

    public static List<String> artisanalLineAliases() {
        return ARTISANAL_LINE;
    }

    public static List<String> industrialLineAliases() {
        return INDUSTRIAL_LINE;
    }

    private static PlaceResult placeArtisanalMachine(Level level, MachineSpec spec, BlockPos origin) {
        if (spec.heat() != HeatPad.NONE) {
            return placeHeatedSingle(level, spec, origin);
        }
        level.setBlock(origin, requireBlock(spec.blockId()).defaultBlockState(), Block.UPDATE_ALL);
        if (spec.engine()) {
            placeEngine(level, origin.east());
        }
        return new PlaceResult(spec.alias(), origin, true, "placed");
    }

    private static PlaceResult placeHeatedSingle(Level level, MachineSpec spec, BlockPos machine) {
        placeHeat(level, machine.below(), spec.heat());
        level.setBlock(machine, requireBlock(spec.blockId()).defaultBlockState(), Block.UPDATE_ALL);
        return new PlaceResult(spec.alias(), machine, true, "placed");
    }

    private static PlaceResult placeIndustrialMachine(
            Level level,
            MachineSpec spec,
            BlockPos origin,
            Dimensions size
    ) {
        MultiblockDefinition definition = definition(spec.definitionId());
        MultiblockConstraints constraints = definition.constraints();
        int width = resolveAxis(size == null ? null : size.width(), constraints.minWidth(), constraints.maxWidth());
        int height = resolveAxis(size == null ? null : size.height(), constraints.minHeight(), constraints.maxHeight());
        int depth = resolveAxis(size == null ? null : size.depth(), constraints.minDepth(), constraints.maxDepth());
        placeHeat(level, origin.below(), spec.heat());
        boolean kinetic = constraints.requiredPorts().contains(PartRole.KINETIC_PORT);
        Block extraPort = kinetic ? requireBlock(AlcoholicIds.KINETIC_PORT) : null;
        HollowCuboidPlacer.place(
                level,
                origin,
                width,
                height,
                depth,
                controllerBlock(definition),
                requireBlock(AlcoholicIds.INDUSTRIAL_CASING),
                extraPort
        );
        if (kinetic || spec.engine()) {
            placeEngine(level, origin.offset(width, 0, 0));
        }
        boolean formed = HollowCuboidPlacer.formNow(level, origin);
        String reason = "placed";
        if (level.getBlockEntity(origin) instanceof MultiblockControllerBlockEntity controller) {
            reason = controller.lastReason();
        }
        return new PlaceResult(spec.alias(), origin, formed, reason);
    }

    private static void placeHeat(Level level, BlockPos pos, HeatPad heat) {
        if (heat == HeatPad.NONE || !level.isInWorldBounds(pos)) {
            return;
        }
        if (heat == HeatPad.MAGMA) {
            level.setBlock(pos, Blocks.MAGMA_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        level.setBlock(
                pos,
                Blocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.LIT, true),
                Block.UPDATE_ALL
        );
    }

    private static void placeEngine(Level level, BlockPos pos) {
        level.setBlock(pos, requireBlock(AlcoholicIds.PRIMITIVE_COMBUSTION_ENGINE).defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof PrimitiveCombustionEngineBlockEntity engine) {
            engine.insertFuel(new ItemStack(Items.COAL, 1));
        }
    }

    private static int resolveAxis(Integer requested, int min, int max) {
        if (requested == null) {
            return min;
        }
        return Math.max(min, Math.min(max, requested));
    }

    private static MultiblockDefinition definition(ResourceId id) {
        return IndustrialRuntime.shared().machines().get(id)
                .or(() -> MachineCatalog.builtins().get(id))
                .orElseThrow(() -> new IllegalStateException("Missing machine " + id));
    }

    private static Block controllerBlock(MultiblockDefinition definition) {
        ResourceLocation location = ResourceLocation.tryParse(definition.controllerBlockId());
        if (location == null) {
            throw new IllegalStateException("Invalid controller id " + definition.controllerBlockId());
        }
        Block block = Registry.BLOCK.get(location);
        if (block == Blocks.AIR) {
            throw new IllegalStateException("Missing controller " + definition.controllerBlockId());
        }
        return block;
    }

    private static Block requireBlock(ResourceId id) {
        Block block = Registry.BLOCK.get(ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path()));
        if (block == Blocks.AIR) {
            throw new IllegalStateException("Missing block " + id.namespace() + ":" + id.path());
        }
        return block;
    }

    private static void register(MachineSpec spec) {
        SPECS.put(spec.alias(), spec);
    }

    public enum HeatPad {
        NONE,
        MAGMA,
        CAMPFIRE
    }

    public record Dimensions(int width, int height, int depth) {
    }

    public record PlaceResult(String id, BlockPos controller, boolean formed, String reason) {
    }

    public static final class MachineSpec {
        private final String alias;
        private final ResourceId blockId;
        private final ResourceId definitionId;
        private final boolean engine;
        private final HeatPad heat;

        private MachineSpec(
                String alias,
                ResourceId blockId,
                ResourceId definitionId,
                boolean engine,
                HeatPad heat
        ) {
            this.alias = alias;
            this.blockId = blockId;
            this.definitionId = definitionId;
            this.engine = engine;
            this.heat = heat;
        }

        private static MachineSpec single(String alias, ResourceId blockId) {
            return new MachineSpec(alias, blockId, null, false, HeatPad.NONE);
        }

        private static MachineSpec industrial(String alias, ResourceId definitionId) {
            return new MachineSpec(alias, null, definitionId, false, HeatPad.NONE);
        }

        private MachineSpec withEngine() {
            return new MachineSpec(alias, blockId, definitionId, true, heat);
        }

        private MachineSpec withHeat(HeatPad heat) {
            return new MachineSpec(alias, blockId, definitionId, engine, heat);
        }

        public String alias() {
            return alias;
        }

        public ResourceId blockId() {
            return blockId;
        }

        public ResourceId definitionId() {
            return definitionId;
        }

        public boolean industrial() {
            return definitionId != null;
        }

        public boolean engine() {
            return engine;
        }

        public HeatPad heat() {
            return heat;
        }
    }
}

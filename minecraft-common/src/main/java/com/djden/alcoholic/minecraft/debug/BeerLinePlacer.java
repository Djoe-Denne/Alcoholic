package com.djden.alcoholic.minecraft.debug;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.application.machine.BuiltinCraftMachines;
import com.djden.alcoholic.application.machine.BuiltinMachines;
import com.djden.alcoholic.application.machine.MachineCatalog;
import com.djden.alcoholic.domain.multiblock.CellCoord;
import com.djden.alcoholic.domain.multiblock.IndustrialHullPattern;
import com.djden.alcoholic.domain.multiblock.MachineScale;
import com.djden.alcoholic.domain.multiblock.MultiblockConstraints;
import com.djden.alcoholic.domain.multiblock.MultiblockDefinition;
import com.djden.alcoholic.domain.multiblock.PartRole;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.mechanical.PrimitiveCombustionEngineBlock;
import com.djden.alcoholic.minecraft.mechanical.PrimitiveCombustionEngineBlockEntity;
import com.djden.alcoholic.minecraft.multiblock.HollowCuboidPlacer;
import com.djden.alcoholic.minecraft.multiblock.IndustrialHullPlacer;
import com.djden.alcoholic.minecraft.multiblock.IndustrialRuntime;
import com.djden.alcoholic.minecraft.multiblock.MultiblockControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
    public static final int INDUSTRIAL_GAP = 2;

    private static final Map<String, MachineSpec> SPECS = new LinkedHashMap<>();

    static {
        register(MachineSpec.single("malting_floor", AlcoholicIds.MALTING_FLOOR));
        register(MachineSpec.single("malt_mill", AlcoholicIds.MALT_MILL).withEngine());
        register(MachineSpec.single("mash_tun", AlcoholicIds.MASH_TUN).withHeat(HeatPad.MAGMA));
        register(MachineSpec.single("brewing_kettle", AlcoholicIds.BREWING_KETTLE).withHeat(HeatPad.CAMPFIRE));
        register(MachineSpec.single("fermenter", AlcoholicIds.ARTISANAL_FERMENTER));
        register(MachineSpec.industrial("malt_house", BuiltinMachines.INDUSTRIAL_MALT_HOUSE).sized(5, 4, 5));
        register(MachineSpec.industrial("roller_mill", BuiltinMachines.INDUSTRIAL_ROLLER_MILL)
                .withEngine()
                .sized(3, 4, 3));
        register(MachineSpec.industrial("industrial_mash_tun", BuiltinMachines.INDUSTRIAL_MASH_TUN)
                .withHeat(HeatPad.MAGMA)
                .sized(5, 5, 5));
        register(MachineSpec.industrial("industrial_brewing_kettle", BuiltinMachines.INDUSTRIAL_BREWING_KETTLE)
                .withHeat(HeatPad.CAMPFIRE)
                .sized(5, 6, 5));
        register(MachineSpec.industrial("vat", BuiltinMachines.INDUSTRIAL_VAT).sized(3, 5, 3));
        register(MachineSpec.industrial("conditioning", BuiltinMachines.INDUSTRIAL_CONDITIONING_VESSEL).sized(3, 6, 3));
        register(MachineSpec.industrial("tank", BuiltinMachines.INDUSTRIAL_TANK).sized(3, 5, 3));
        register(MachineSpec.industrial("craft_malt_house", BuiltinCraftMachines.CRAFT_MALT_HOUSE).sized(3, 3, 3));
        register(MachineSpec.industrial("craft_mill", BuiltinCraftMachines.CRAFT_MILL).withEngine().sized(3, 3, 3));
        register(MachineSpec.industrial("craft_mash_tun", BuiltinCraftMachines.CRAFT_MASH_TUN)
                .withHeat(HeatPad.MAGMA)
                .sized(3, 3, 3));
        register(MachineSpec.industrial("craft_brewing_kettle", BuiltinCraftMachines.CRAFT_BREWING_KETTLE)
                .withHeat(HeatPad.CAMPFIRE)
                .sized(3, 3, 3));
        register(MachineSpec.industrial("craft_vat", BuiltinCraftMachines.CRAFT_VAT).sized(3, 3, 3));
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

    private static final List<String> CRAFT_LINE = List.of(
            "craft_malt_house",
            "craft_mill",
            "craft_mash_tun",
            "craft_brewing_kettle",
            "craft_vat"
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

    public static List<PlaceResult> placeCraft(Level level, BlockPos origin) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(origin, "origin");
        List<PlaceResult> results = new ArrayList<>();
        List<Integer> offsets = craftLineOffsets();
        int index = 0;
        for (String alias : CRAFT_LINE) {
            results.add(placeMachine(level, alias, origin.offset(offsets.get(index), 0, 0), null).orElseThrow());
            index++;
        }
        return List.copyOf(results);
    }

    public static List<Integer> craftLineOffsets() {
        int x = 0;
        List<Integer> offsets = new ArrayList<>();
        for (String alias : CRAFT_LINE) {
            MachineSpec spec = SPECS.get(alias);
            offsets.add(x);
            Dimensions size = resolvedSize(spec, null);
            x += size.width() + INDUSTRIAL_GAP + (needsEngine(spec) ? 1 : 0);
        }
        return List.copyOf(offsets);
    }

    public static List<PlaceResult> placeIndustrial(Level level, BlockPos origin) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(origin, "origin");
        List<PlaceResult> results = new ArrayList<>();
        List<Integer> offsets = industrialLineOffsets();
        int index = 0;
        for (String alias : INDUSTRIAL_LINE) {
            results.add(placeMachine(level, alias, origin.offset(offsets.get(index), 0, 0), null).orElseThrow());
            index++;
        }
        return List.copyOf(results);
    }

    public static List<Integer> industrialLineOffsets() {
        int x = 0;
        List<Integer> offsets = new ArrayList<>();
        for (String alias : INDUSTRIAL_LINE) {
            MachineSpec spec = SPECS.get(alias);
            offsets.add(x);
            Dimensions size = resolvedSize(spec, null);
            x += size.width() + INDUSTRIAL_GAP + (needsEngine(spec) ? 1 : 0);
        }
        return List.copyOf(offsets);
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

    public static List<String> craftLineAliases() {
        return CRAFT_LINE;
    }

    private static PlaceResult placeArtisanalMachine(Level level, MachineSpec spec, BlockPos origin) {
        if (spec.heat() != HeatPad.NONE) {
            return placeHeatedSingle(level, spec, origin);
        }
        level.setBlock(origin, requireBlock(spec.blockId()).defaultBlockState(), Block.UPDATE_ALL);
        if (spec.engine()) {
            placeEngine(level, origin.east(), Direction.WEST);
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
        Dimensions resolved = resolvedSize(spec, size);
        int width = resolved.width();
        int height = resolved.height();
        int depth = resolved.depth();
        placeHeatPad(level, origin, width, depth, spec.heat());
        boolean kinetic = definition.constraints().requiredPorts().contains(PartRole.KINETIC_PORT);
        BlockPos controllerPos = IndustrialHullPlacer.place(
                level,
                origin,
                width,
                height,
                depth,
                controllerBlock(definition),
                definition.scale() == MachineScale.CRAFT
                        ? requireBlock(AlcoholicIds.CRAFT_CASING)
                        : requireBlock(AlcoholicIds.INDUSTRIAL_CASING),
                requireBlock(AlcoholicIds.MACHINE_WINDOW),
                requireBlock(AlcoholicIds.ACCESS_HATCH),
                requireBlock(AlcoholicIds.ITEM_PORT),
                requireBlock(AlcoholicIds.FLUID_PORT),
                kinetic ? requireBlock(AlcoholicIds.KINETIC_PORT) : null,
                kinetic
        );
        if (kinetic) {
            CellCoord port = IndustrialHullPattern.kineticPort(width, height, depth);
            placeEngine(level, origin.offset(port.x() + 1, port.y(), port.z()), Direction.WEST);
        } else if (spec.engine()) {
            placeEngine(level, origin.offset(width, 0, 0), Direction.WEST);
        }
        boolean formed = HollowCuboidPlacer.formNow(level, controllerPos);
        String reason = "placed";
        if (level.getBlockEntity(controllerPos) instanceof MultiblockControllerBlockEntity controller) {
            reason = controller.lastReason();
        }
        return new PlaceResult(spec.alias(), controllerPos, formed, reason);
    }

    private static void placeHeatPad(Level level, BlockPos origin, int width, int depth, HeatPad heat) {
        if (heat == HeatPad.NONE) {
            return;
        }
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                placeHeat(level, origin.offset(x, -1, z), heat);
            }
        }
    }

    private static Dimensions resolvedSize(MachineSpec spec, Dimensions requested) {
        MultiblockConstraints constraints = definition(spec.definitionId()).constraints();
        Dimensions source = requested != null ? requested : spec.size();
        if (source == null) {
            return new Dimensions(constraints.minWidth(), constraints.minHeight(), constraints.minDepth());
        }
        return new Dimensions(
                resolveAxis(source.width(), constraints.minWidth(), constraints.maxWidth()),
                resolveAxis(source.height(), constraints.minHeight(), constraints.maxHeight()),
                resolveAxis(source.depth(), constraints.minDepth(), constraints.maxDepth())
        );
    }

    private static boolean needsEngine(MachineSpec spec) {
        if (spec.engine()) {
            return true;
        }
        return spec.industrial()
                && definition(spec.definitionId()).constraints().requiredPorts().contains(PartRole.KINETIC_PORT);
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

    private static void placeEngine(Level level, BlockPos pos, Direction driveToward) {
        level.setBlock(
                pos,
                PrimitiveCombustionEngineBlock.withDriveToward(
                        requireBlock(AlcoholicIds.PRIMITIVE_COMBUSTION_ENGINE).defaultBlockState(),
                        driveToward
                ),
                Block.UPDATE_ALL
        );
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
        private final Dimensions size;

        private MachineSpec(
                String alias,
                ResourceId blockId,
                ResourceId definitionId,
                boolean engine,
                HeatPad heat,
                Dimensions size
        ) {
            this.alias = alias;
            this.blockId = blockId;
            this.definitionId = definitionId;
            this.engine = engine;
            this.heat = heat;
            this.size = size;
        }

        private static MachineSpec single(String alias, ResourceId blockId) {
            return new MachineSpec(alias, blockId, null, false, HeatPad.NONE, null);
        }

        private static MachineSpec industrial(String alias, ResourceId definitionId) {
            return new MachineSpec(alias, null, definitionId, false, HeatPad.NONE, null);
        }

        private MachineSpec withEngine() {
            return new MachineSpec(alias, blockId, definitionId, true, heat, size);
        }

        private MachineSpec withHeat(HeatPad heat) {
            return new MachineSpec(alias, blockId, definitionId, engine, heat, size);
        }

        private MachineSpec sized(int width, int height, int depth) {
            return new MachineSpec(alias, blockId, definitionId, engine, heat, new Dimensions(width, height, depth));
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

        public Dimensions size() {
            return size;
        }
    }
}

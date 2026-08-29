package com.djden.alcoholic.forge.event;

import com.djden.alcoholic.minecraft.debug.BeerLinePlacer;
import com.djden.alcoholic.minecraft.debug.PortAuditPlacer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Optional;

/**
 * Operator-only world placement for the shipped beer line.
 */
public final class ForgePlaceCommands {
    private static final SuggestionProvider<CommandSourceStack> MACHINE_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggest(BeerLinePlacer.aliases(), builder);

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("alcoholic")
                        .then(Commands.literal("debug")
                                .requires(source -> source.hasPermission(2))
                                .then(placeCommand())
                                .then(portsCommand()))
        );
    }

    private LiteralArgumentBuilder<CommandSourceStack> placeCommand() {
        return Commands.literal("place")
                .then(Commands.literal("beer")
                        .then(Commands.literal("artisanal")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(this::placeArtisanalLine)))
                        .then(Commands.literal("industrial")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(this::placeIndustrialLine)))
                        .then(Commands.literal("craft")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(this::placeCraftLine))))
                .then(Commands.argument("machine", StringArgumentType.word())
                        .suggests(MACHINE_SUGGESTIONS)
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(this::placeMachine)
                                .then(Commands.argument("w", IntegerArgumentType.integer(3))
                                        .then(Commands.argument("h", IntegerArgumentType.integer(3))
                                                .then(Commands.argument("d", IntegerArgumentType.integer(3))
                                                        .executes(this::placeMachineSized))))));
    }

    private LiteralArgumentBuilder<CommandSourceStack> portsCommand() {
        return Commands.literal("ports")
                .then(Commands.literal("fluid")
                        .executes(this::placeFluidAtPlayer)
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(this::placeFluidAtPos)))
                .then(Commands.literal("energy")
                        .executes(this::placeEnergyAtPlayer)
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(this::placeEnergyAtPos)));
    }

    private int placeFluidAtPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return reportPorts(
                context,
                "fluid",
                PortAuditPlacer.placeFluid(context.getSource().getLevel(), playerOrigin(context))
        );
    }

    private int placeFluidAtPos(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return reportPorts(
                context,
                "fluid",
                PortAuditPlacer.placeFluid(
                        context.getSource().getLevel(),
                        BlockPosArgument.getLoadedBlockPos(context, "pos")
                )
        );
    }

    private int placeEnergyAtPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return reportPorts(
                context,
                "energy",
                PortAuditPlacer.placeEnergy(context.getSource().getLevel(), playerOrigin(context))
        );
    }

    private int placeEnergyAtPos(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return reportPorts(
                context,
                "energy",
                PortAuditPlacer.placeEnergy(
                        context.getSource().getLevel(),
                        BlockPosArgument.getLoadedBlockPos(context, "pos")
                )
        );
    }

    private static BlockPos playerOrigin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return context.getSource().getPlayerOrException().blockPosition();
    }

    private static int reportPorts(CommandContext<CommandSourceStack> context, String kind, int count) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
                Component.translatable("command.alcoholic.debug.ports.placed", kind, count),
                true
        );
        return Command.SINGLE_SUCCESS;
    }

    private int placeArtisanalLine(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return reportLine(context, "artisanal", BeerLinePlacer.placeArtisanal(
                context.getSource().getLevel(),
                BlockPosArgument.getLoadedBlockPos(context, "pos")
        ));
    }

    private int placeIndustrialLine(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return reportLine(context, "industrial", BeerLinePlacer.placeIndustrial(
                context.getSource().getLevel(),
                BlockPosArgument.getLoadedBlockPos(context, "pos")
        ));
    }

    private int placeCraftLine(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return reportLine(context, "craft", BeerLinePlacer.placeCraft(
                context.getSource().getLevel(),
                BlockPosArgument.getLoadedBlockPos(context, "pos")
        ));
    }

    private int placeMachine(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return place(context, null);
    }

    private int placeMachineSized(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return place(
                context,
                new BeerLinePlacer.Dimensions(
                        IntegerArgumentType.getInteger(context, "w"),
                        IntegerArgumentType.getInteger(context, "h"),
                        IntegerArgumentType.getInteger(context, "d")
                )
        );
    }

    private int place(CommandContext<CommandSourceStack> context, BeerLinePlacer.Dimensions size)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String alias = StringArgumentType.getString(context, "machine");
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        Level level = source.getLevel();
        Optional<BeerLinePlacer.PlaceResult> result = BeerLinePlacer.placeMachine(level, alias, pos, size);
        if (result.isEmpty()) {
            source.sendFailure(Component.translatable("command.alcoholic.debug.place.unknown", alias));
            return 0;
        }
        reportMachine(source, result.get());
        return Command.SINGLE_SUCCESS;
    }

    private static int reportLine(
            CommandContext<CommandSourceStack> context,
            String line,
            List<BeerLinePlacer.PlaceResult> results
    ) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        BlockPos origin = BlockPosArgument.getLoadedBlockPos(context, "pos");
        long formed = results.stream().filter(BeerLinePlacer.PlaceResult::formed).count();
        source.sendSuccess(
                Component.translatable(
                        "command.alcoholic.debug.place.line",
                        line,
                        formatPos(origin),
                        results.size(),
                        formed
                ),
                true
        );
        for (BeerLinePlacer.PlaceResult result : results) {
            reportMachine(source, result);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static void reportMachine(CommandSourceStack source, BeerLinePlacer.PlaceResult result) {
        source.sendSuccess(
                Component.translatable(
                        "command.alcoholic.debug.place.machine",
                        result.id(),
                        formatPos(result.controller()),
                        result.formed(),
                        result.reason()
                ),
                false
        );
    }

    private static String formatPos(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }
}

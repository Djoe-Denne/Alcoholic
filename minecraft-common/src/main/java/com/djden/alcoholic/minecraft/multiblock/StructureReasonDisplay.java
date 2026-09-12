package com.djden.alcoholic.minecraft.multiblock;

import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns the validator's English {@code lastReason} into overlay copy.
 */
public final class StructureReasonDisplay {
    private static final Pattern DIMENSIONS =
            Pattern.compile("dimensions (\\d+)x(\\d+)x(\\d+) outside allowed range");
    private static final Pattern CONTROLLER_COUNT =
            Pattern.compile("controller count (\\d+) != (\\d+)");
    private static final Pattern MISSING_PORT =
            Pattern.compile("missing required port (.+)");
    private static final Pattern OVERCAPACITY =
            Pattern.compile("stored (\\d+) mB exceeds new capacity (\\d+)");

    private StructureReasonDisplay() {
    }

    public static boolean present(String reason) {
        return reason != null && !reason.isBlank() && !"unformed".equals(reason);
    }

    public static Component component(String reason) {
        if (!present(reason)) {
            return Component.empty();
        }
        return switch (reason) {
            case "controller chunk unloaded" ->
                    Component.translatable("gui.alcoholic.structure.reason.controller_chunk");
            case "origin is not this machine's controller" ->
                    Component.translatable("gui.alcoholic.structure.reason.wrong_origin");
            case "structure spans an unloaded chunk" ->
                    Component.translatable("gui.alcoholic.structure.reason.unloaded_chunk");
            case "connected shell exceeds dimension budget" ->
                    Component.translatable("gui.alcoholic.structure.reason.over_budget");
            case "shell cell unloaded" ->
                    Component.translatable("gui.alcoholic.structure.reason.shell_unloaded");
            case "shell is not a closed connected frame" ->
                    Component.translatable("gui.alcoholic.structure.reason.open_shell");
            case "unsupported block on shell" ->
                    Component.translatable("gui.alcoholic.structure.reason.unsupported_shell");
            case "foreign controller on shell" ->
                    Component.translatable("gui.alcoholic.structure.reason.foreign_controller");
            case "interior cell unloaded" ->
                    Component.translatable("gui.alcoholic.structure.reason.interior_unloaded");
            case "interior is obstructed" ->
                    Component.translatable("gui.alcoholic.structure.reason.interior_blocked");
            case "contents exceed resized capacity" ->
                    Component.translatable("gui.alcoholic.structure.reason.resize_failed");
            default -> parameterized(reason);
        };
    }

    private static Component parameterized(String reason) {
        Matcher dimensions = DIMENSIONS.matcher(reason);
        if (dimensions.matches()) {
            return Component.translatable(
                    "gui.alcoholic.structure.reason.dimensions",
                    dimensions.group(1),
                    dimensions.group(2),
                    dimensions.group(3)
            );
        }
        Matcher controllers = CONTROLLER_COUNT.matcher(reason);
        if (controllers.matches()) {
            return Component.translatable(
                    "gui.alcoholic.structure.reason.controller_count",
                    controllers.group(1),
                    controllers.group(2)
            );
        }
        Matcher port = MISSING_PORT.matcher(reason);
        if (port.matches()) {
            return Component.translatable(
                    "gui.alcoholic.structure.reason.missing_port",
                    portLabel(port.group(1))
            );
        }
        Matcher capacity = OVERCAPACITY.matcher(reason);
        if (capacity.matches()) {
            return Component.translatable(
                    "gui.alcoholic.structure.reason.overcapacity",
                    capacity.group(1),
                    capacity.group(2)
            );
        }
        return Component.literal(reason);
    }

    private static Component portLabel(String role) {
        return Component.translatable("block.alcoholic." + role.trim().toLowerCase(Locale.ROOT));
    }
}

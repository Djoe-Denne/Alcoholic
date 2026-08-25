package com.djden.alcoholic.forge.event;

import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.fluid.LiquidBatchNbt;
import com.djden.alcoholic.minecraft.inspect.AlcoholicInspect;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

/**
 * Debug inspect command and foreign-tank metadata-loss tooltip.
 */
public final class ForgeInspectEvents {
    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("alcoholic")
                        .then(Commands.literal("inspect").executes(this::inspect))
        );
    }

    @SubscribeEvent
    public void tooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
            if (handler.getTanks() <= 0) {
                return;
            }
            FluidStack fluid = handler.getFluidInTank(0);
            if (fluid.isEmpty()) {
                return;
            }
            ResourceLocation key = ForgeRegistries.FLUIDS.getKey(fluid.getFluid());
            if (key == null || !AlcoholicIds.MOD_ID.equals(key.getNamespace())) {
                return;
            }
            if (!LiquidBatchNbt.hasVersionTag(fluid.getTag())) {
                event.getToolTip().add(Component.translatable("tooltip.alcoholic.metadata.lost"));
            }
        });
    }

    private int inspect(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Player player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.translatable("command.alcoholic.inspect.no_player"));
            return 0;
        }
        HitResult hit = player.pick(20.0, 0.0F, false);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos position = ((BlockHitResult) hit).getBlockPos();
            BlockEntity entity = player.level.getBlockEntity(position);
            Optional<String> dump = AlcoholicInspect.inspectBlock(entity);
            if (dump.isPresent()) {
                source.sendSuccess(Component.literal(dump.get()), false);
                return Command.SINGLE_SUCCESS;
            }
        }
        Optional<String> held = AlcoholicInspect.inspectItem(player.getMainHandItem());
        if (held.isPresent()) {
            source.sendSuccess(Component.literal(held.get()), false);
            return Command.SINGLE_SUCCESS;
        }
        source.sendFailure(Component.translatable("command.alcoholic.inspect.nothing"));
        return 0;
    }
}

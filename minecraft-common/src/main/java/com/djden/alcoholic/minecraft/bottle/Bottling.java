package com.djden.alcoholic.minecraft.bottle;

import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.process.BottleConfig;
import com.djden.alcoholic.application.process.ProcessRecipeResolver;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.fluid.LiquidTank;
import com.djden.alcoholic.minecraft.process.MinecraftSelectorMatcher;
import com.djden.alcoholic.minecraft.process.ProcessRuntime;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class Bottling {
    private Bottling() {
    }

    public static boolean isEmptyBottle(ItemStack stack) {
        return !stack.isEmpty()
                && Registry.ITEM.getKey(stack.getItem()).equals(
                ResourceLocation.fromNamespaceAndPath(AlcoholicIds.MOD_ID, AlcoholicIds.EMPTY_BOTTLE.path())
        );
    }

    public static boolean bottle(Player player, ItemStack held, LiquidTank tank) {
        if (!isEmptyBottle(held) || tank.contents().isEmpty()) {
            return false;
        }
        LiquidBatch batch = tank.contents().orElseThrow();
        ProcessRuntime runtime = ProcessRuntime.shared();
        Optional<ProcessInvocation> invocation = ProcessRecipeResolver.find(
                runtime.beverages().catalog(),
                runtime.beverages().api(),
                BuiltinRegistrations.BOTTLE,
                MinecraftSelectorMatcher.create(runtime.beverages()),
                Optional.empty(),
                batch.baseLiquid()
        );
        ProcessInvocation bottle = invocation.orElseGet(() -> new ProcessInvocation(
                BuiltinRegistrations.BOTTLE,
                BottleConfig.CODEC.encode(BottleConfig.incomplete()),
                "bottle"
        ));
        ProcessResult result = runtime.engine().execute(
                runtime.bottleExecutor(),
                bottle,
                ProcessInputs.ofLiquid("source", batch),
                com.djden.alcoholic.api.process.ProcessContext.empty()
        );
        if (!result.success() || result.outputs().isEmpty()) {
            return false;
        }
        LiquidBatch remaining = (LiquidBatch) result.outputs().get(0);
        LiquidBatch extracted = batch.split(batch.volume() - remaining.volume()).extracted();
        Item filled = Registry.ITEM.get(ResourceLocation.fromNamespaceAndPath(
                AlcoholicIds.MOD_ID,
                AlcoholicIds.BEVERAGE_BOTTLE.path()
        ));
        ItemStack bottleStack = new ItemStack(filled);
        BottleSnapshotNbt.write(bottleStack, extracted);
        if (remaining.volume() <= 0.0) {
            tank.clear();
        } else {
            tank.set(remaining);
        }
        held.shrink(1);
        if (!player.getInventory().add(bottleStack)) {
            player.drop(bottleStack, false);
        }
        return true;
    }
}

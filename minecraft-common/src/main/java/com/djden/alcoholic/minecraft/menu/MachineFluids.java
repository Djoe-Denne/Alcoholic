package com.djden.alcoholic.minecraft.menu;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.minecraft.fluid.LiquidTank;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

final class MachineFluids {
    private MachineFluids() {
    }

    static int id(LiquidTank tank) {
        return tank.contents()
                .flatMap(LiquidBatch::baseLiquid)
                .map(MachineFluids::id)
                .orElse(0);
    }

    static int id(ResourceId id) {
        Fluid fluid = Registry.FLUID.get(ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path()));
        if (fluid == Fluids.EMPTY) {
            return 0;
        }
        return Registry.FLUID.getId(fluid);
    }
}

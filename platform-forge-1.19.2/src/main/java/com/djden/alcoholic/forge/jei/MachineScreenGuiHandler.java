package com.djden.alcoholic.forge.jei;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.application.process.ProcessDisplayRecipe;
import com.djden.alcoholic.forge.client.AlcoholicMachineScreen;
import com.djden.alcoholic.minecraft.menu.MachineLayout;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class MachineScreenGuiHandler implements IGuiContainerHandler<AlcoholicMachineScreen> {
    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(
            AlcoholicMachineScreen screen,
            double mouseX,
            double mouseY
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        List<ResourceId> processTypes = screen.getMenu().displayedProcessTypes(
                minecraft == null ? null : minecraft.level
        );
        if (processTypes.isEmpty()) {
            return List.of();
        }
        List<RecipeType<ProcessDisplayRecipe>> types = new ArrayList<>();
        for (ResourceId processType : processTypes) {
            types.add(JeiProcessSpec.of(processType).recipeType());
        }
        MachineLayout layout = screen.getMenu().layout();
        MachineLayout.ArrowPos arrow = layout.arrow().present()
                ? layout.arrow()
                : new MachineLayout.ArrowPos(76, 34);
        return List.of(IGuiClickableArea.createBasic(
                arrow.x(),
                arrow.y(),
                MachineLayout.ARROW_WIDTH,
                MachineLayout.ARROW_HEIGHT,
                types.toArray(RecipeType[]::new)
        ));
    }
}

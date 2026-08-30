package com.djden.alcoholic.forge.jei;

import com.djden.alcoholic.api.process.ProcessDisplaySpec;
import com.djden.alcoholic.application.process.ProcessDisplayRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

final class ProcessRecipeCategory implements IRecipeCategory<ProcessDisplayRecipe> {
    static final int WIDTH = JeiProcessLayout.WIDTH;
    static final int HEIGHT = JeiProcessLayout.HEIGHT;

    private final JeiProcessSpec spec;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    ProcessRecipeCategory(IGuiHelper guiHelper, JeiProcessSpec spec) {
        this.spec = spec;
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.slot = guiHelper.getSlotDrawable();
        List<ItemStack> catalysts = spec.catalystStacks();
        this.icon = catalysts.isEmpty()
                ? guiHelper.createBlankDrawable(16, 16)
                : guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, catalysts.get(0));
    }

    @Override
    public RecipeType<ProcessDisplayRecipe> getRecipeType() {
        return spec.recipeType();
    }

    @Override
    public Component getTitle() {
        return spec.title();
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ProcessDisplayRecipe recipe, IFocusGroup focuses) {
        JeiProcessLayout layout = JeiProcessLayout.of(recipe);
        int itemIndex = 0;
        for (ProcessDisplaySpec.ItemPart part : recipe.itemInputs()) {
            addItem(builder, RecipeIngredientRole.INPUT, layout.itemIn(itemIndex++), JeiIngredients.items(part));
        }
        itemIndex = 0;
        for (ProcessDisplaySpec.ItemPart part : recipe.itemOutputs()) {
            addItem(builder, RecipeIngredientRole.OUTPUT, layout.itemOut(itemIndex++), JeiIngredients.items(part));
        }
        int fluidIndex = 0;
        for (ProcessDisplaySpec.FluidPart part : recipe.fluidInputs()) {
            addFluid(builder, RecipeIngredientRole.INPUT, layout.fluidIn(fluidIndex++), part);
        }
        fluidIndex = 0;
        for (ProcessDisplaySpec.FluidPart part : recipe.fluidOutputs()) {
            addFluid(builder, RecipeIngredientRole.OUTPUT, layout.fluidOut(fluidIndex++), part);
        }
    }

    @Override
    public void draw(
            ProcessDisplayRecipe recipe,
            IRecipeSlotsView slots,
            PoseStack pose,
            double mouseX,
            double mouseY
    ) {
        JeiProcessLayout layout = JeiProcessLayout.of(recipe);
        for (int index = 0; index < recipe.itemInputs().size(); index++) {
            JeiProcessLayout.SlotPos pos = layout.itemIn(index);
            slot.draw(pose, pos.x() - 1, pos.y() - 1);
        }
        for (int index = 0; index < recipe.itemOutputs().size(); index++) {
            JeiProcessLayout.SlotPos pos = layout.itemOut(index);
            slot.draw(pose, pos.x() - 1, pos.y() - 1);
        }
        for (int index = 0; index < recipe.fluidInputs().size(); index++) {
            drawTankFrame(pose, layout.fluidIn(index));
        }
        for (int index = 0; index < recipe.fluidOutputs().size(); index++) {
            drawTankFrame(pose, layout.fluidOut(index));
        }
        if (!recipe.fluidInputs().isEmpty() || !recipe.fluidOutputs().isEmpty()
                || !recipe.itemInputs().isEmpty() || !recipe.itemOutputs().isEmpty()) {
            drawArrow(pose, layout.arrow());
        }
        Font font = Minecraft.getInstance().font;
        boolean hasDuration = recipe.durationTicks().isPresent();
        if (hasDuration) {
            font.draw(
                    pose,
                    Component.translatable("jei.alcoholic.duration", formatSeconds(recipe.durationTicks().getAsInt())),
                    8,
                    4,
                    0x404040
            );
        }
        recipe.preferredTemperature().ifPresent(band -> font.draw(
                pose,
                Component.translatable(
                        "gui.alcoholic.temperature",
                        String.format(java.util.Locale.ROOT, "%.0f-%.0f", band.min(), band.max())
                ),
                8,
                hasDuration ? 14 : 4,
                0x404040
        ));
        int hintIndex = 0;
        for (ProcessDisplaySpec.ItemPart part : recipe.itemInputs()) {
            if (part.hint().isPresent()) {
                JeiProcessLayout.SlotPos pos = layout.itemIn(hintIndex);
                font.draw(pose, part.hint().get(), pos.x(), pos.y() + JeiProcessLayout.SLOT, 0x555555);
            }
            hintIndex++;
        }
    }

    private static void addItem(
            IRecipeLayoutBuilder builder,
            RecipeIngredientRole role,
            JeiProcessLayout.SlotPos slot,
            List<ItemStack> stacks
    ) {
        if (stacks.isEmpty()) {
            return;
        }
        builder.addSlot(role, slot.x(), slot.y()).addIngredients(VanillaTypes.ITEM_STACK, stacks);
    }

    private static void addFluid(
            IRecipeLayoutBuilder builder,
            RecipeIngredientRole role,
            JeiProcessLayout.SlotPos pos,
            ProcessDisplaySpec.FluidPart part
    ) {
        FluidStack stack = JeiIngredients.fluid(part);
        if (stack.isEmpty()) {
            return;
        }
        IRecipeSlotBuilder slot = builder.addSlot(role, pos.x(), pos.y());
        slot.setFluidRenderer(
                JeiIngredients.tankCapacity(part),
                false,
                JeiProcessLayout.TANK_WIDTH,
                JeiProcessLayout.TANK_HEIGHT
        );
        slot.addIngredient(ForgeTypes.FLUID_STACK, stack);
        if (!JeiIngredients.volumeKnown(part)) {
            slot.addTooltipCallback((recipeSlotView, tooltip) ->
                    tooltip.add(Component.translatable("jei.alcoholic.volume.unspecified")));
        }
    }

    private static void drawTankFrame(PoseStack pose, JeiProcessLayout.SlotPos pos) {
        int x = pos.x() - 1;
        int y = pos.y() - 1;
        int width = JeiProcessLayout.TANK_WIDTH + 2;
        int height = JeiProcessLayout.TANK_HEIGHT + 2;
        int color = 0xFF8B8B8B;
        GuiComponent.fill(pose, x, y, x + width, y + 1, color);
        GuiComponent.fill(pose, x, y + height - 1, x + width, y + height, color);
        GuiComponent.fill(pose, x, y, x + 1, y + height, color);
        GuiComponent.fill(pose, x + width - 1, y, x + width, y + height, color);
    }

    private static void drawArrow(PoseStack pose, JeiProcessLayout.SlotPos pos) {
        int x = pos.x();
        int y = pos.y();
        int color = 0xFF8B8B8B;
        GuiComponent.fill(pose, x, y + 7, x + 16, y + 10, color);
        GuiComponent.fill(pose, x + 14, y + 4, x + 17, y + 13, color);
        GuiComponent.fill(pose, x + 17, y + 5, x + 20, y + 12, color);
        GuiComponent.fill(pose, x + 20, y + 6, x + 23, y + 11, color);
    }

    private static String formatSeconds(int ticks) {
        return String.format(java.util.Locale.ROOT, "%.1f", ticks / 20.0);
    }
}

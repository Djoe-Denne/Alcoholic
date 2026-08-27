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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

final class ProcessRecipeCategory implements IRecipeCategory<ProcessDisplayRecipe> {
    static final int WIDTH = 176;
    static final int HEIGHT = 90;
    private static final int SLOT = 18;
    private static final int ROW_Y = 40;
    private static final int FLUID_Y = 16;

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
        Layout layout = Layout.of(recipe);
        int itemIndex = 0;
        for (ProcessDisplaySpec.ItemPart part : recipe.itemInputs()) {
            addItem(builder, RecipeIngredientRole.INPUT, layout.item(itemIndex++), JeiIngredients.items(part));
        }
        for (ProcessDisplaySpec.ItemPart part : recipe.itemOutputs()) {
            addItem(builder, RecipeIngredientRole.OUTPUT, layout.item(itemIndex++), JeiIngredients.items(part));
        }
        int fluidIndex = 0;
        for (ProcessDisplaySpec.FluidPart part : recipe.fluidInputs()) {
            addFluid(builder, RecipeIngredientRole.INPUT, layout.fluid(fluidIndex++), part);
        }
        for (ProcessDisplaySpec.FluidPart part : recipe.fluidOutputs()) {
            addFluid(builder, RecipeIngredientRole.OUTPUT, layout.fluid(fluidIndex++), part);
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
        Layout layout = Layout.of(recipe);
        int itemSlots = recipe.itemInputs().size() + recipe.itemOutputs().size();
        for (int index = 0; index < itemSlots; index++) {
            SlotPos pos = layout.item(index);
            slot.draw(pose, pos.x() - 1, pos.y() - 1);
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
                SlotPos pos = layout.item(hintIndex);
                font.draw(pose, part.hint().get(), pos.x(), pos.y() + SLOT, 0x555555);
            }
            hintIndex++;
        }
    }

    private static void addItem(
            IRecipeLayoutBuilder builder,
            RecipeIngredientRole role,
            SlotPos slot,
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
            SlotPos pos,
            ProcessDisplaySpec.FluidPart part
    ) {
        FluidStack stack = JeiIngredients.fluid(part);
        if (stack.isEmpty()) {
            return;
        }
        IRecipeSlotBuilder slot = builder.addSlot(role, pos.x(), pos.y());
        int rendererCapacity = JeiIngredients.volumeKnown(part)
                ? Math.max(stack.getAmount(), 1000)
                : 1000;
        slot.setFluidRenderer(rendererCapacity, false, 16, 32);
        slot.addIngredient(ForgeTypes.FLUID_STACK, stack);
        if (!JeiIngredients.volumeKnown(part)) {
            slot.addTooltipCallback((recipeSlotView, tooltip) ->
                    tooltip.add(Component.translatable("jei.alcoholic.volume.unspecified")));
        }
    }

    private static String formatSeconds(int ticks) {
        return String.format(java.util.Locale.ROOT, "%.1f", ticks / 20.0);
    }

    private record SlotPos(int x, int y) {
    }

    private static final class Layout {
        private final int itemInputs;
        private final int fluids;

        private Layout(int itemInputs, int fluids) {
            this.itemInputs = itemInputs;
            this.fluids = fluids;
        }

        static Layout of(ProcessDisplayRecipe recipe) {
            return new Layout(
                    recipe.itemInputs().size(),
                    recipe.fluidInputs().size() + recipe.fluidOutputs().size()
            );
        }

        SlotPos item(int index) {
            int column = index < itemInputs ? index : index - itemInputs;
            int x = index < itemInputs ? 8 + column * 22 : 116 + column * 22;
            int row = 0;
            if (x > WIDTH - SLOT - 8) {
                row = 1;
                x = (index < itemInputs ? 8 : 116) + (column % 3) * 22;
            }
            return new SlotPos(Math.min(x, WIDTH - SLOT - 8), ROW_Y + row * 22);
        }

        SlotPos fluid(int index) {
            int start = Math.max(8, WIDTH - 8 - fluids * 22);
            return new SlotPos(start + index * 22, FLUID_Y);
        }
    }
}

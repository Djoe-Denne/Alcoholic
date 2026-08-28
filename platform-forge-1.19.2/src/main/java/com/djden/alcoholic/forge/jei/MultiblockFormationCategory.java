package com.djden.alcoholic.forge.jei;

import com.djden.alcoholic.application.machine.MultiblockDisplayRecipe;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

final class MultiblockFormationCategory implements IRecipeCategory<MultiblockDisplayRecipe> {
    static final RecipeType<MultiblockDisplayRecipe> TYPE = RecipeType.create(
            AlcoholicIds.MOD_ID,
            "multiblock_formation",
            MultiblockDisplayRecipe.class
    );
    static final int WIDTH = 176;
    static final int HEIGHT = 154;
    private static final int SLOT = 18;
    private static final int GRID = 3 * SLOT;
    private static final int LEFT_0 = 8;
    private static final int LEFT_1 = 8 + GRID + 10;
    private static final int TOP_0 = 28;
    private static final int TOP_1 = 28 + GRID + 14;
    private static final int INGREDIENT_Y = 128;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    MultiblockFormationCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.slot = guiHelper.getSlotDrawable();
        ItemStack casing = stack("alcoholic:industrial_casing");
        this.icon = casing.isEmpty()
                ? guiHelper.createBlankDrawable(16, 16)
                : guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, casing);
    }

    @Override
    public RecipeType<MultiblockDisplayRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.alcoholic.category.multiblock_formation");
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
    public void setRecipe(IRecipeLayoutBuilder builder, MultiblockDisplayRecipe recipe, IFocusGroup focuses) {
        for (MultiblockDisplayRecipe.Layer layer : recipe.layers()) {
            int originX = layer.y() % 2 == 0 ? LEFT_0 : LEFT_1;
            int originY = layer.y() < 2 ? TOP_0 : TOP_1;
            for (MultiblockDisplayRecipe.Cell cell : layer.cells()) {
                ItemStack item = stack(cell.blockId());
                if (item.isEmpty()) {
                    continue;
                }
                builder.addSlot(
                        RecipeIngredientRole.INPUT,
                        originX + cell.x() * SLOT,
                        originY + cell.z() * SLOT
                ).addItemStack(item);
            }
        }
        int index = 0;
        for (MultiblockDisplayRecipe.Ingredient ingredient : recipe.ingredients()) {
            ItemStack item = stack(ingredient.blockId());
            if (item.isEmpty()) {
                continue;
            }
            item.setCount(ingredient.count());
            builder.addSlot(RecipeIngredientRole.INPUT, 8 + index * SLOT, INGREDIENT_Y).addItemStack(item);
            index++;
        }
    }

    @Override
    public void draw(
            MultiblockDisplayRecipe recipe,
            IRecipeSlotsView slots,
            PoseStack pose,
            double mouseX,
            double mouseY
    ) {
        Font font = Minecraft.getInstance().font;
        font.draw(
                pose,
                Component.translatable(
                        "jei.alcoholic.formation.size",
                        recipe.minWidth(),
                        recipe.minHeight(),
                        recipe.minDepth(),
                        recipe.maxWidth(),
                        recipe.maxHeight(),
                        recipe.maxDepth()
                ),
                8,
                4,
                0x404040
        );
        for (MultiblockDisplayRecipe.Layer layer : recipe.layers()) {
            int originX = layer.y() % 2 == 0 ? LEFT_0 : LEFT_1;
            int originY = layer.y() < 2 ? TOP_0 : TOP_1;
            font.draw(
                    pose,
                    Component.translatable("jei.alcoholic.formation.layer", layer.y()),
                    originX,
                    originY - 10,
                    0x555555
            );
            for (int z = 0; z < 3; z++) {
                for (int x = 0; x < 3; x++) {
                    slot.draw(pose, originX + x * SLOT - 1, originY + z * SLOT - 1);
                }
            }
        }
        int index = 0;
        for (MultiblockDisplayRecipe.Ingredient ignored : recipe.ingredients()) {
            slot.draw(pose, 8 + index * SLOT - 1, INGREDIENT_Y - 1);
            index++;
        }
    }

    static List<ItemStack> catalysts() {
        return List.of(
                stack("alcoholic:industrial_casing"),
                stack("alcoholic:industrial_press_controller"),
                stack("alcoholic:industrial_vat_controller"),
                stack("alcoholic:industrial_tank_controller"),
                stack("alcoholic:industrial_malt_house_controller"),
                stack("alcoholic:industrial_roller_mill_controller"),
                stack("alcoholic:industrial_mash_tun_controller"),
                stack("alcoholic:industrial_brewing_kettle_controller"),
                stack("alcoholic:industrial_conditioning_vessel_controller")
        ).stream().filter(stack -> !stack.isEmpty()).toList();
    }

    private static ItemStack stack(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            return ItemStack.EMPTY;
        }
        var item = ForgeRegistries.ITEMS.getValue(location);
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }
}

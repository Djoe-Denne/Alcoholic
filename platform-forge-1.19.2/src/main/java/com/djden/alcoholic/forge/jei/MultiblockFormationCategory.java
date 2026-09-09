package com.djden.alcoholic.forge.jei;

import com.djden.alcoholic.application.machine.MultiblockDisplayRecipe;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.mojang.blaze3d.platform.InputConstants;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class MultiblockFormationCategory implements IRecipeCategory<MultiblockDisplayRecipe> {
    static final RecipeType<MultiblockDisplayRecipe> TYPE = RecipeType.create(
            AlcoholicIds.MOD_ID,
            "multiblock_formation",
            MultiblockDisplayRecipe.class
    );
    static final int WIDTH = 176;
    static final int HEIGHT = 154;
    private static final int SLOT = 18;
    private static final int MOSAIC_GRID = 3 * SLOT;
    private static final int LEFT_0 = 8;
    private static final int LEFT_1 = 8 + MOSAIC_GRID + 10;
    private static final int TOP_0 = 28;
    private static final int TOP_1 = 28 + MOSAIC_GRID + 14;
    private static final int INGREDIENT_Y = 128;
    private static final int PAGED_GRID_Y = 28;
    private static final int LAYER_LABEL_Y = 16;
    private static final int PREV_X = 8;
    private static final int NEXT_WIDTH = 8;

    private final IGuiHelper guiHelper;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;
    private final Map<String, IDrawable> itemIcons = new HashMap<>();
    private final Map<MultiblockDisplayRecipe, Integer> layerIndex = new HashMap<>();

    MultiblockFormationCategory(IGuiHelper guiHelper) {
        this.guiHelper = guiHelper;
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
        if (!paged(recipe)) {
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
        font.draw(pose, sizeLabel(recipe), 8, 4, 0x404040);
        if (paged(recipe)) {
            drawPaged(recipe, pose, font);
            return;
        }
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
            for (int z = 0; z < recipe.depth(); z++) {
                for (int x = 0; x < recipe.width(); x++) {
                    slot.draw(pose, originX + x * SLOT - 1, originY + z * SLOT - 1);
                }
            }
        }
        drawIngredientSlots(recipe, pose);
    }

    @Override
    public List<Component> getTooltipStrings(
            MultiblockDisplayRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            double mouseX,
            double mouseY
    ) {
        if (!paged(recipe)) {
            return List.of();
        }
        MultiblockDisplayRecipe.Layer layer = currentLayer(recipe);
        int originX = pagedOriginX(recipe);
        for (MultiblockDisplayRecipe.Cell cell : layer.cells()) {
            int x = originX + cell.x() * SLOT;
            int y = PAGED_GRID_Y + cell.z() * SLOT;
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                ItemStack item = stack(cell.blockId());
                if (item.isEmpty()) {
                    return List.of();
                }
                return List.of(item.getHoverName());
            }
        }
        return List.of();
    }

    @Override
    public boolean handleInput(
            MultiblockDisplayRecipe recipe,
            double mouseX,
            double mouseY,
            InputConstants.Key input
    ) {
        if (!paged(recipe) || input.getType() != InputConstants.Type.MOUSE || input.getValue() != 0) {
            return false;
        }
        int layers = recipe.layers().size();
        int nextX = nextButtonX(recipe);
        if (in(mouseX, mouseY, PREV_X, LAYER_LABEL_Y, 8, fontHeight())) {
            layerIndex.put(recipe, Math.floorMod(layerOf(recipe) - 1, layers));
            return true;
        }
        if (in(mouseX, mouseY, nextX, LAYER_LABEL_Y, NEXT_WIDTH, fontHeight())) {
            layerIndex.put(recipe, Math.floorMod(layerOf(recipe) + 1, layers));
            return true;
        }
        return false;
    }

    static List<ItemStack> catalysts() {
        return List.of(
                stack("alcoholic:industrial_casing"),
                stack("alcoholic:craft_casing"),
                stack("alcoholic:industrial_press_controller"),
                stack("alcoholic:industrial_vat_controller"),
                stack("alcoholic:industrial_tank_controller"),
                stack("alcoholic:industrial_malt_house_controller"),
                stack("alcoholic:industrial_roller_mill_controller"),
                stack("alcoholic:industrial_mash_tun_controller"),
                stack("alcoholic:industrial_brewing_kettle_controller"),
                stack("alcoholic:industrial_conditioning_vessel_controller"),
                stack("alcoholic:industrial_aging_vessel_controller"),
                stack("alcoholic:craft_malt_house_controller"),
                stack("alcoholic:craft_mill_controller"),
                stack("alcoholic:craft_mash_tun_controller"),
                stack("alcoholic:craft_brewing_kettle_controller"),
                stack("alcoholic:craft_vat_controller")
        ).stream().filter(stack -> !stack.isEmpty()).toList();
    }

    private void drawPaged(MultiblockDisplayRecipe recipe, PoseStack pose, Font font) {
        int originX = pagedOriginX(recipe);
        font.draw(pose, "<", PREV_X, LAYER_LABEL_Y, 0x404040);
        font.draw(
                pose,
                Component.translatable("jei.alcoholic.formation.layer", currentLayer(recipe).y()),
                PREV_X + 10,
                LAYER_LABEL_Y,
                0x555555
        );
        font.draw(pose, ">", nextButtonX(recipe), LAYER_LABEL_Y, 0x404040);
        for (int z = 0; z < recipe.depth(); z++) {
            for (int x = 0; x < recipe.width(); x++) {
                slot.draw(pose, originX + x * SLOT - 1, PAGED_GRID_Y + z * SLOT - 1);
            }
        }
        for (MultiblockDisplayRecipe.Cell cell : currentLayer(recipe).cells()) {
            IDrawable item = iconFor(cell.blockId());
            if (item != null) {
                item.draw(pose, originX + cell.x() * SLOT, PAGED_GRID_Y + cell.z() * SLOT);
            }
        }
        drawIngredientSlots(recipe, pose);
    }

    private void drawIngredientSlots(MultiblockDisplayRecipe recipe, PoseStack pose) {
        for (int index = 0; index < recipe.ingredients().size(); index++) {
            slot.draw(pose, 8 + index * SLOT - 1, INGREDIENT_Y - 1);
        }
    }

    private static Component sizeLabel(MultiblockDisplayRecipe recipe) {
        boolean exact = recipe.width() == recipe.minWidth()
                && recipe.height() == recipe.minHeight()
                && recipe.depth() == recipe.minDepth()
                && recipe.width() == recipe.maxWidth()
                && recipe.height() == recipe.maxHeight()
                && recipe.depth() == recipe.maxDepth();
        boolean maxShowcase = recipe.width() == recipe.maxWidth()
                && recipe.height() == recipe.maxHeight()
                && recipe.depth() == recipe.maxDepth();
        if (exact || maxShowcase) {
            return Component.translatable(
                    "jei.alcoholic.formation.size",
                    recipe.width(),
                    recipe.height(),
                    recipe.depth()
            );
        }
        return Component.translatable(
                "jei.alcoholic.formation.size_max",
                recipe.width(),
                recipe.height(),
                recipe.depth(),
                recipe.maxWidth(),
                recipe.maxHeight(),
                recipe.maxDepth()
        );
    }

    private static boolean paged(MultiblockDisplayRecipe recipe) {
        return recipe.width() > 3 || recipe.depth() > 3;
    }

    private MultiblockDisplayRecipe.Layer currentLayer(MultiblockDisplayRecipe recipe) {
        return recipe.layers().get(layerOf(recipe));
    }

    private int layerOf(MultiblockDisplayRecipe recipe) {
        return Math.floorMod(layerIndex.getOrDefault(recipe, 0), recipe.layers().size());
    }

    private static int pagedOriginX(MultiblockDisplayRecipe recipe) {
        return (WIDTH - recipe.width() * SLOT) / 2;
    }

    private int nextButtonX(MultiblockDisplayRecipe recipe) {
        String label = Component.translatable("jei.alcoholic.formation.layer", currentLayer(recipe).y()).getString();
        return PREV_X + 10 + Minecraft.getInstance().font.width(label) + 6;
    }

    private static int fontHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }

    private static boolean in(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private IDrawable iconFor(String blockId) {
        return itemIcons.computeIfAbsent(blockId, id -> {
            ItemStack item = stack(id);
            if (item.isEmpty()) {
                return guiHelper.createBlankDrawable(16, 16);
            }
            return guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, item);
        });
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

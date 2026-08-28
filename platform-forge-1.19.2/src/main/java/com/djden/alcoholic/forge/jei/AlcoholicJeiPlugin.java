package com.djden.alcoholic.forge.jei;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ProcessType;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.application.machine.MultiblockDisplayRecipe;
import com.djden.alcoholic.application.machine.MultiblockDisplayRecipes;
import com.djden.alcoholic.application.process.ProcessDisplayRecipe;
import com.djden.alcoholic.application.process.ProcessDisplayRecipes;
import com.djden.alcoholic.forge.client.AlcoholicMachineScreen;
import com.djden.alcoholic.minecraft.multiblock.IndustrialRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JeiPlugin
public final class AlcoholicJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath("alcoholic", "jei");

    private IJeiRuntime runtime;
    private final Map<RecipeType<ProcessDisplayRecipe>, List<ProcessDisplayRecipe>> published = new LinkedHashMap<>();
    private List<MultiblockDisplayRecipe> formations = List.of();

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        for (JeiProcessSpec spec : specs()) {
            registration.addRecipeCategories(new ProcessRecipeCategory(guiHelper, spec));
        }
        registration.addRecipeCategories(new MultiblockFormationCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        published.clear();
        Map<ResourceId, List<ProcessDisplayRecipe>> grouped =
                ProcessDisplayRecipes.groupByType(ClientProcessCatalog.load(), AlcoholicApi.shared());
        for (Map.Entry<ResourceId, List<ProcessDisplayRecipe>> entry : grouped.entrySet()) {
            RecipeType<ProcessDisplayRecipe> type = JeiProcessSpec.of(entry.getKey()).recipeType();
            registration.addRecipes(type, entry.getValue());
            published.put(type, List.copyOf(entry.getValue()));
        }
        formations = MultiblockDisplayRecipes.from(IndustrialRuntime.shared().machines());
        registration.addRecipes(MultiblockFormationCategory.TYPE, formations);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (JeiProcessSpec spec : specs()) {
            spec.catalystStacks().forEach(stack -> registration.addRecipeCatalyst(stack, spec.recipeType()));
        }
        MultiblockFormationCategory.catalysts().forEach(stack ->
                registration.addRecipeCatalyst(stack, MultiblockFormationCategory.TYPE));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(AlcoholicMachineScreen.class, new MachineScreenGuiHandler());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        Runnable rebind = () -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null) {
                minecraft.execute(this::syncRecipes);
            } else {
                syncRecipes();
            }
        };
        AlcoholicApi.shared().addReloadListener(rebind::run);
        IndustrialRuntime.shared().store().addListener(rebind);
    }

    private void syncRecipes() {
        if (runtime == null) {
            return;
        }
        IRecipeManager manager = runtime.getRecipeManager();
        published.forEach(manager::hideRecipes);
        published.clear();
        if (!formations.isEmpty()) {
            manager.hideRecipes(MultiblockFormationCategory.TYPE, formations);
            formations = List.of();
        }
        BeverageCatalog catalog = ClientProcessCatalog.load();
        Map<ResourceId, List<ProcessDisplayRecipe>> grouped =
                ProcessDisplayRecipes.groupByType(catalog, AlcoholicApi.shared());
        grouped.forEach((typeId, recipes) -> {
            RecipeType<ProcessDisplayRecipe> type = JeiProcessSpec.of(typeId).recipeType();
            manager.addRecipes(type, recipes);
            published.put(type, List.copyOf(recipes));
        });
        formations = MultiblockDisplayRecipes.from(IndustrialRuntime.shared().machines());
        manager.addRecipes(MultiblockFormationCategory.TYPE, formations);
    }

    private static List<JeiProcessSpec> specs() {
        Set<ResourceId> types = new LinkedHashSet<>();
        for (ProcessType<?> type : AlcoholicApi.shared().processView().values()) {
            types.add(type.id());
        }
        types.addAll(ProcessDisplayRecipes.groupByType(ClientProcessCatalog.load(), AlcoholicApi.shared()).keySet());
        return JeiProcessSpec.allKnown(types);
    }
}

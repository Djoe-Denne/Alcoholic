package com.djden.alcoholic.forge.jei;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ProcessType;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.application.process.ProcessDisplayRecipe;
import com.djden.alcoholic.application.process.ProcessDisplayRecipes;
import com.djden.alcoholic.forge.client.AlcoholicMachineScreen;
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
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (JeiProcessSpec spec : specs()) {
            spec.catalystStacks().forEach(stack -> registration.addRecipeCatalyst(stack, spec.recipeType()));
        }
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(AlcoholicMachineScreen.class, new MachineScreenGuiHandler());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        AlcoholicApi.shared().addReloadListener(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null) {
                minecraft.execute(this::syncRecipes);
            } else {
                syncRecipes();
            }
        });
    }

    private void syncRecipes() {
        if (runtime == null) {
            return;
        }
        IRecipeManager manager = runtime.getRecipeManager();
        published.forEach(manager::hideRecipes);
        published.clear();
        BeverageCatalog catalog = ClientProcessCatalog.load();
        Map<ResourceId, List<ProcessDisplayRecipe>> grouped =
                ProcessDisplayRecipes.groupByType(catalog, AlcoholicApi.shared());
        grouped.forEach((typeId, recipes) -> {
            RecipeType<ProcessDisplayRecipe> type = JeiProcessSpec.of(typeId).recipeType();
            manager.addRecipes(type, recipes);
            published.put(type, List.copyOf(recipes));
        });
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

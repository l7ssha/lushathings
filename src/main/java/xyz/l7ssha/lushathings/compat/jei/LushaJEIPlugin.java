package xyz.l7ssha.lushathings.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.recipe.ReprocessorRecipe;

import java.util.List;

@JeiPlugin
public class LushaJEIPlugin implements IModPlugin {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(lushathings.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ReprocessorRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        List<ReprocessorRecipe> recipes = recipeManager.getAllRecipesFor(lushathings.REPROCESSOR_RECIPE_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();

        registration.addRecipes(ReprocessorRecipeCategory.RECIPE_TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(lushathings.REPROCESSOR_CONTROLLER_BLOCK.get()), ReprocessorRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(lushathings.REPROCESSOR_INPUT_BLOCK.get()), ReprocessorRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(lushathings.REPROCESSOR_OUTPUT_BLOCK.get()), ReprocessorRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(lushathings.REPROCESSOR_INPUT_OUTPUT_BLOCK.get()), ReprocessorRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(lushathings.REPROCESSOR_ENERGY_INPUT_BLOCK.get()), ReprocessorRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(lushathings.REPROCESSOR_STRUCTURE_BLOCK.get()), ReprocessorRecipeCategory.RECIPE_TYPE);
    }
}

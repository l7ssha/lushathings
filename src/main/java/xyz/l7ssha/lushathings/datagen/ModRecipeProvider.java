package xyz.l7ssha.lushathings.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;
import xyz.l7ssha.lushathings.datagen.builder.ReprocessorRecipeBuilder;
import xyz.l7ssha.lushathings.lushathings;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, lushathings.REPROCESSOR_BLOCK.get())
                .pattern("GGG")
                .pattern("F F")
                .pattern("NCN")
                .define('G', Items.GLASS)
                .define('F', Items.BLAST_FURNACE)
                .define('N', Items.NETHERITE_BLOCK)
                .define('C', Items.COBBLESTONE_SLAB)
                .unlockedBy("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                .save(output);

        new ReprocessorRecipeBuilder(new ItemStack(Items.ANCIENT_DEBRIS))
                .addInput(Ingredient.of(Items.NETHERITE_INGOT), 4)
                .unlockedBy("has_reprocessor", has(lushathings.REPROCESSOR_BLOCK.get().asItem()))
                .save(output);
    }
}

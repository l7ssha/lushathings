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
import net.minecraft.world.level.block.Block;
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
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, lushathings.REPROCESSOR_CONTROLLER_BLOCK.get())
                .pattern("NNN")
                .pattern("IFI")
                .pattern("IFI")
                .define('N', Items.NETHER_BRICKS)
                .define('F', Items.FURNACE)
                .define('I', Items.NETHERITE_INGOT)
                .unlockedBy("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, lushathings.REPROCESSOR_STRUCTURE_BLOCK.get())
                .pattern("NNN")
                .pattern("INI")
                .pattern("NNN")
                .define('N', Items.NETHER_BRICKS)
                .define('I', Items.NETHERITE_INGOT)
                .unlockedBy("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, lushathings.REPROCESSOR_INPUT_BLOCK.get())
                .pattern("HHH")
                .pattern(" S ")
                .pattern(" N ")
                .define('H', Items.HOPPER)
                .define('S', lushathings.REPROCESSOR_STRUCTURE_BLOCK.get())
                .define('N', Items.NETHER_BRICKS)
                .unlockedBy("has_structure_block", has(lushathings.REPROCESSOR_STRUCTURE_BLOCK.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, lushathings.REPROCESSOR_OUTPUT_BLOCK.get())
                .pattern(" N ")
                .pattern(" S ")
                .pattern("HHH")
                .define('H', Items.HOPPER)
                .define('S', lushathings.REPROCESSOR_STRUCTURE_BLOCK.get())
                .define('N', Items.NETHER_BRICKS)
                .unlockedBy("has_structure_block", has(lushathings.REPROCESSOR_STRUCTURE_BLOCK.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, lushathings.REPROCESSOR_ENERGY_INPUT_BLOCK.get())
                .pattern("RRR")
                .pattern("DID")
                .pattern("RRR")
                .define('R', Items.REDSTONE_BLOCK)
                .define('D', Items.DIAMOND)
                .define('I', lushathings.REPROCESSOR_INPUT_BLOCK.get())
                .unlockedBy("has_reprocessor_input", has(lushathings.REPROCESSOR_INPUT_BLOCK.get()))
                .save(output);

        new ReprocessorRecipeBuilder(new ItemStack(Items.ANCIENT_DEBRIS), new ItemStack(Items.GOLD_INGOT), 600, 50000)
                .addInput(Ingredient.of(Items.NETHERITE_INGOT), 4)
                .addInput(Ingredient.of(Items.NETHERRACK), 16)
                .unlockedBy("has_reprocessor", has(lushathings.REPROCESSOR_CONTROLLER_BLOCK.get().asItem()))
                .save(output);

        ReprocessorRecipeBuilder.create(new ItemStack(Items.CRYING_OBSIDIAN), 300, 20000)
                .addInput(Ingredient.of(Items.OBSIDIAN), 1)
                .addInput(Ingredient.of(Items.GHAST_TEAR), 4)
                .unlockedBy("has_reprocessor", has(lushathings.REPROCESSOR_CONTROLLER_BLOCK.get().asItem()))
                .save(output);

        ReprocessorRecipeBuilder.create(new ItemStack(Items.COBWEB), 100, 5000)
                .addInput(Ingredient.of(Items.STRING), 16)
                .addInput(Ingredient.of(Items.SLIME_BALL), 2)
                .unlockedBy("has_reprocessor", has(lushathings.REPROCESSOR_CONTROLLER_BLOCK.get().asItem()))
                .save(output);
    }
}

package xyz.l7ssha.lushathings.datagen;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;
import xyz.l7ssha.lushathings.datagen.builder.ReprocessorRecipeBuilder;
import xyz.l7ssha.lushathings.lushathings;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
  public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
    super(output, registries);
  }

  private static Item item(String id) {
    return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
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

    ReprocessorRecipeBuilder.create(
            new ItemStack(lushathings.REPROCESSOR_BULK_PROCESSING_BLOCK.get()), 300, 50000)
        .addInput(Ingredient.of(Items.NETHERITE_BLOCK), 1)
        .addInput(Ingredient.of(Items.DIAMOND), 4)
        .addInput(Ingredient.of(lushathings.REPROCESSOR_STRUCTURE_BLOCK.get()), 2)
        .unlockedBy("has_reprocessor", has(lushathings.REPROCESSOR_CONTROLLER_BLOCK.get().asItem()))
        .save(output);

    ReprocessorRecipeBuilder.create(
            new ItemStack(lushathings.REPROCESSOR_PARALLEL_PROCESSOR_BLOCK.get()), 300, 50000)
        .addInput(Ingredient.of(lushathings.REPROCESSOR_BULK_PROCESSING_BLOCK.get()), 2)
        .addInput(Ingredient.of(lushathings.REPROCESSOR_CONTROLLER_BLOCK.get()), 2)
        .addInput(Ingredient.of(Items.REDSTONE_BLOCK), 2)
        .addInput(Ingredient.of(Items.REPEATER), 4)
        .addInput(Ingredient.of(Items.COMPARATOR), 2)
        .unlockedBy("has_reprocessor", has(lushathings.REPROCESSOR_CONTROLLER_BLOCK.get().asItem()))
        .save(output);

    ReprocessorRecipeBuilder.create(
            new ItemStack(lushathings.REPROCESSOR_INPUT_OUTPUT_BLOCK.get()), 300, 50000)
        .addInput(Ingredient.of(lushathings.REPROCESSOR_INPUT_BLOCK.get()), 1)
        .addInput(Ingredient.of(lushathings.REPROCESSOR_OUTPUT_BLOCK.get()), 1)
        .addInput(Ingredient.of(Items.PISTON), 2)
        .addInput(Ingredient.of(Items.STICKY_PISTON), 2)
        .addInput(Ingredient.of(Items.QUARTZ), 12)
        .addInput(Ingredient.of(Items.REDSTONE), 3)
        .unlockedBy("has_reprocessor", has(lushathings.REPROCESSOR_CONTROLLER_BLOCK.get().asItem()))
        .save(output);

    if (ModList.get().isLoaded("ae2")) {
      ReprocessorRecipeBuilder.create(
              new ItemStack(lushathings.REPROCESSOR_ME_BLOCK.get()), 300, 50000)
          .addInput(Ingredient.of(lushathings.REPROCESSOR_INPUT_OUTPUT_BLOCK.get()), 2)
          .addInput(Ingredient.of(AEItems.LOGIC_PROCESSOR), 4)
          .addInput(Ingredient.of(AEItems.BLANK_PATTERN), 32)
          .addInput(Ingredient.of(AEBlocks.PATTERN_PROVIDER), 4)
          .addInput(Ingredient.of(AEBlocks.INTERFACE), 2)
          .unlockedBy(
              "has_reprocessor", has(lushathings.REPROCESSOR_CONTROLLER_BLOCK.get().asItem()))
          .save(output);
    }

    if (ModList.get().isLoaded("computercraft")) {
      ReprocessorRecipeBuilder.create(
              new ItemStack(lushathings.REPROCESSOR_CC_ADAPTER_BLOCK.get()), 300, 50000)
          .addInput(Ingredient.of(lushathings.REPROCESSOR_STRUCTURE_BLOCK.get()), 2)
          .addInput(Ingredient.of(Items.REDSTONE_BLOCK), 2)
          .addInput(Ingredient.of(dan200.computercraft.api.ComputerCraftTags.Items.WIRED_MODEM), 2)
          .unlockedBy(
              "has_reprocessor", has(lushathings.REPROCESSOR_CONTROLLER_BLOCK.get().asItem()))
          .save(output);
    }

    ShapedRecipeBuilder.shaped(
            RecipeCategory.MISC, lushathings.REPROCESSOR_ENERGY_INPUT_BLOCK.get())
        .pattern("RRR")
        .pattern("DID")
        .pattern("RRR")
        .define('R', Items.REDSTONE_BLOCK)
        .define('D', Items.DIAMOND)
        .define('I', lushathings.REPROCESSOR_INPUT_BLOCK.get())
        .unlockedBy("has_reprocessor_input", has(lushathings.REPROCESSOR_INPUT_BLOCK.get()))
        .save(output);

    new ReprocessorRecipeBuilder(
            new ItemStack(Items.ANCIENT_DEBRIS), new ItemStack(Items.GOLD_INGOT), 600, 50000)
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

    ShapedRecipeBuilder.shaped(
            RecipeCategory.MISC, lushathings.ETERNAL_FIRE_SOURCE_RECOMBINATOR.get())
        .pattern("gMg")
        .pattern("SGS")
        .pattern("FAF")
        .define('F', Items.FURNACE)
        .define('S', Items.SOUL_SAND)
        .define('M', BlockRegistry.SOURCE_JAR.get())
        .define('A', BlockRegistry.VOLCANIC_BLOCK.get())
        .define('g', ItemsRegistry.SOURCE_GEM.get())
        .define('G', BlockRegistry.SOURCE_GEM_BLOCK.get())
        .unlockedBy("has_nether_star", has(Items.NETHER_STAR))
        .save(output, "eternal_fire_source_recombinator");
  }
}

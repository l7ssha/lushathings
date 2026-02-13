package xyz.l7ssha.lushathings.blockentity.util;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import xyz.l7ssha.lushathings.blockentity.ReprocessorIOHatch;
import xyz.l7ssha.lushathings.recipe.ReprocessorRecipe;
import xyz.l7ssha.lushathings.recipe.util.SizedIngredient;

/**
 * Utility class for reprocessor inventory operations.
 * Handles common inventory interaction patterns across the mod.
 */
public class ReprocessorInventoryUtil {

  /**
   * Counts the total number of matching items across all slots in an item handler.
   *
   * @param handler The item handler to search in
   * @param ingredient The ingredient to match against
   * @return The total count of matching items
   */
  public static int countMatchingItems(IItemHandler handler, SizedIngredient ingredient) {
    int foundCount = 0;
    for (int i = 0; i < handler.getSlots(); i++) {
      ItemStack stack = handler.getStackInSlot(i);
      if (!stack.isEmpty() && ingredient.ingredient().test(stack)) {
        foundCount += stack.getCount();
      }
    }
    return foundCount;
  }

  /**
   * Checks if all ingredients for a recipe are available in a specific inventory.
   *
   * @param recipe The recipe to check ingredients for
   * @param handler The inventory to check
   * @return true if all ingredients are available, false otherwise
   */
  public static boolean hasAllIngredients(ReprocessorRecipe recipe, IItemHandler handler) {
    if (recipe == null || handler == null) {
      return false;
    }

    for (SizedIngredient ingredient : recipe.inputs()) {
      int required = ingredient.count();
      int foundCount = countMatchingItems(handler, ingredient);
      if (foundCount < required) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if all ingredients for a recipe (multiplied by a factor) are available.
   *
   * @param recipe The recipe to check ingredients for
   * @param handler The inventory to check
   * @param multiplier How many times the recipe should be craftable (for parallel processing)
   * @return true if all ingredients * multiplier are available, false otherwise
   */
  public static boolean hasAllIngredientsMultiplied(
      ReprocessorRecipe recipe, IItemHandler handler, int multiplier) {
    if (recipe == null || handler == null) {
      return false;
    }

    for (SizedIngredient ingredient : recipe.inputs()) {
      int required = ingredient.count() * multiplier;
      int foundCount = countMatchingItems(handler, ingredient);
      if (foundCount < required) {
        return false;
      }
    }
    return true;
  }

  /**
   * Computes how many parallel operations could be performed with the given ingredients.
   * Limited by the maximum parallel count.
   *
   * @param recipe The recipe to calculate for
   * @param handler The inventory containing ingredients
   * @param maxParallel The maximum parallel operations allowed
   * @return The number of operations possible (minimum 1, maximum as specified)
   */
  public static int computeMaxParallelCount(
      ReprocessorRecipe recipe, IItemHandler handler, int maxParallel) {
    if (recipe == null || handler == null) {
      return 1;
    }

    int possible = maxParallel;
    for (SizedIngredient ingredient : recipe.inputs()) {
      int foundCount = countMatchingItems(handler, ingredient);
      possible = Math.min(possible, foundCount / ingredient.count());
      if (possible <= 1) {
        possible = Math.max(1, possible);
        break;
      }
    }
    return Math.max(1, Math.min(maxParallel, possible));
  }

  /**
   * Checks if a hatch can fit all the recipe outputs, potentially with parallel processing.
   *
   * @param recipe The recipe whose outputs to check
   * @param hatches List of output hatches
   * @param level The current world level
   * @param parallelCount How many copies of output to check for
   * @return true if outputs can fit, false otherwise
   */
  public static boolean canFitOutputs(
      ReprocessorRecipe recipe, List<BlockPos> outputHatches, Level level, int parallelCount) {
    if (recipe == null || outputHatches == null || outputHatches.isEmpty() || level == null) {
      return false;
    }

    int n = Math.max(1, Math.min(4, parallelCount));

    for (int i = 0; i < n; i++) {
      ItemStack out1 = recipe.output().copy();
      if (!simulateInsertAcrossHatches(out1, outputHatches, level)) {
        return false;
      }

      ItemStack out2 = recipe.output2().copy();
      if (!out2.isEmpty() && !simulateInsertAcrossHatches(out2, outputHatches, level)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Simulates inserting an item into any available hatch without modifying inventories.
   *
   * @param stack The item to insert
   * @param outputHatches List of output hatch positions
   * @param level The current world
   * @return true if the item could be fully inserted, false otherwise
   */
  public static boolean simulateInsertAcrossHatches(
      ItemStack stack, List<BlockPos> outputHatches, Level level) {
    ItemStack remaining = stack.copy();

    for (BlockPos outputPos : outputHatches) {
      if (level.getBlockEntity(outputPos) instanceof ReprocessorIOHatch hatch) {
        IItemHandler handler = hatch.getOutputInventory();
        if (handler == null) {
          continue;
        }

        ItemStack simRemaining = ItemHandlerHelper.insertItemStacked(handler, remaining, true);

        if (simRemaining.isEmpty()) {
          return true;
        } else if (simRemaining.getCount() < remaining.getCount()) {
          remaining = simRemaining;
        }
      }
    }

    return remaining.isEmpty();
  }

  /**
   * Actually inserts an item into available output hatches.
   *
   * @param stack Item to insert
   * @param outputHatches List of output hatch positions
   * @param level The current world
   */
  public static void insertOutput(ItemStack stack, List<BlockPos> outputHatches, Level level) {
    if (stack.isEmpty() || outputHatches == null || outputHatches.isEmpty() || level == null) {
      return;
    }

    ItemStack remaining = stack.copy();

    for (BlockPos outputPos : outputHatches) {
      if (level.getBlockEntity(outputPos) instanceof ReprocessorIOHatch hatch) {
        IItemHandler handler = hatch.getOutputInventory();
        if (handler == null) {
          continue;
        }

        remaining = ItemHandlerHelper.insertItemStacked(handler, remaining, false);
        if (remaining.isEmpty()) {
          return;
        }
      }
    }
  }

  /**
   * Consumes recipe ingredients (multiplied by a factor) from an input inventory.
   *
   * @param recipe The recipe to consume ingredients for
   * @param handler The inventory to extract from
   * @param multiplier How many times to consume ingredients (for parallel)
   * @return true if all ingredients were successfully consumed, false otherwise
   */
  public static boolean consumeIngredients(
      ReprocessorRecipe recipe, IItemHandlerModifiable handler, int multiplier) {
    if (recipe == null || handler == null || !hasAllIngredientsMultiplied(recipe, handler, multiplier)) {
      return false;
    }

    for (SizedIngredient ingredient : recipe.inputs()) {
      int required = ingredient.count() * multiplier;

      for (int i = 0; i < handler.getSlots(); i++) {
        ItemStack stack = handler.getStackInSlot(i);
        if (!stack.isEmpty() && ingredient.ingredient().test(stack)) {
          int toTake = Math.min(stack.getCount(), required);
          handler.extractItem(i, toTake, false);
          required -= toTake;
          if (required <= 0) {
            break;
          }
        }
      }
    }

    return true;
  }
}

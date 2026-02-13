package xyz.l7ssha.lushathings.blockentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.blockentity.util.ReprocessorInventoryUtil;
import xyz.l7ssha.lushathings.recipe.ReprocessorRecipe;
import xyz.l7ssha.lushathings.screen.ReprocessorControllerMenu;

public class ReprocessorControllerBlockEntity extends BlockEntity implements MenuProvider {
  public static final int STATUS_OK = 0;
  public static final int STATUS_NO_INPUT_HATCH = 1;
  public static final int STATUS_NO_OUTPUT_HATCH = 2;
  public static final int STATUS_NO_RECIPE = 3;
  public static final int STATUS_NO_ENERGY = 4;
  public static final int STATUS_OUTPUT_FULL = 5;

  private List<BlockPos> inputHatches = new ArrayList<>();
  private List<BlockPos> outputHatches = new ArrayList<>();
  private List<BlockPos> energyInputs = new ArrayList<>();
  private List<BlockPos> bulkProcessingBlocks = new ArrayList<>();
  private List<BlockPos> parallelProcessorBlocks = new ArrayList<>();

  protected final ContainerData data;

  private int progress = 0;
  private int maxProgress = 600;
  private BlockPos centerPos = null;
  private int status = STATUS_NO_RECIPE;
  private String currentRecipeOutputName = "";
  private int energyCostLastTick = 0;

  public ReprocessorControllerBlockEntity(BlockPos pos, BlockState blockState) {
    super(lushathings.REPROCESSOR_CONTROLLER_BLOCK_ENTITY.get(), pos, blockState);

    data =
        new ContainerData() {
          @Override
          public int get(int i) {
            return switch (i) {
              case 0 -> ReprocessorControllerBlockEntity.this.progress;
              case 1 -> ReprocessorControllerBlockEntity.this.maxProgress;
              case 2 -> ReprocessorControllerBlockEntity.this.status;
              case 3 -> ReprocessorControllerBlockEntity.this.energyCostLastTick;
              default -> 0;
            };
          }

          @Override
          public void set(int i, int value) {
            switch (i) {
              case 0 -> ReprocessorControllerBlockEntity.this.progress = value;
              case 1 -> ReprocessorControllerBlockEntity.this.maxProgress = value;
              case 2 -> ReprocessorControllerBlockEntity.this.status = value;
              case 3 -> ReprocessorControllerBlockEntity.this.energyCostLastTick = value;
            }
          }

          @Override
          public int getCount() {
            return 4;
          }
        };
  }

  @Override
  public @NotNull Component getDisplayName() {
    return Component.translatable("block.lushathings.reprocessor_controller_block");
  }

  @Override
  public @Nullable AbstractContainerMenu createMenu(
      int containerId, Inventory inventory, Player player) {
    return new ReprocessorControllerMenu(containerId, inventory, this, this.data);
  }

  public void setCenterPos(BlockPos pos) {
    this.centerPos = pos;
    setChanged();
  }

  public BlockPos getCenterPos() {
    return this.centerPos;
  }

  public void setInputHatches(List<BlockPos> inputHatches) {
    this.inputHatches = inputHatches;
    setChanged();
  }

  public void setOutputHatches(List<BlockPos> outputHatches) {
    this.outputHatches = outputHatches;
    setChanged();
  }

  public void setEnergyInputs(List<BlockPos> energyInputs) {
    this.energyInputs = energyInputs;
    setChanged();
  }

  public void setBulkProcessingBlocks(List<BlockPos> bulkProcessingBlocks) {
    this.bulkProcessingBlocks = bulkProcessingBlocks;
    setChanged();
  }

  public void setParallelProcessorBlocks(List<BlockPos> parallelProcessorBlocks) {
    this.parallelProcessorBlocks = parallelProcessorBlocks;
    setChanged();
  }

  public boolean hasParallelProcessor() {
    return parallelProcessorBlocks != null && !parallelProcessorBlocks.isEmpty();
  }

  public List<BlockPos> getBulkProcessingBlocks() {
    return bulkProcessingBlocks;
  }

  private static final int DEFAULT_PROGRESS_PER_TICK = 1;
  private static final int BULK_BLOCK_SPEED_MULTIPLIER = 2;
  private static final int BULK_BLOCK_ENERGY_MULTIPLIER = 4;

  private int getBulkProcessingBlockCount() {
    return bulkProcessingBlocks == null ? 0 : bulkProcessingBlocks.size();
  }

  private int getProgressPerTick() {
    int bulkCount = getBulkProcessingBlockCount();
    if (bulkCount <= 0) {
      return DEFAULT_PROGRESS_PER_TICK;
    }

    long speed = DEFAULT_PROGRESS_PER_TICK;
    for (int i = 0; i < bulkCount; i++) {
      speed *= BULK_BLOCK_SPEED_MULTIPLIER;

      if (speed >= Integer.MAX_VALUE) {
        return Integer.MAX_VALUE;
      }
    }

    return (int) speed;
  }

  private int getEnergyCostPerTick(ReprocessorRecipe recipe) {
    if (recipe == null) {
      return 0;
    }

    int base = Math.max(0, recipe.energyCost());
    int bulkCount = getBulkProcessingBlockCount();
    if (bulkCount <= 0) {
      return base;
    }

    long cost = base;
    for (int i = 0; i < bulkCount; i++) {
      cost *= BULK_BLOCK_ENERGY_MULTIPLIER;
      if (cost >= Integer.MAX_VALUE) {
        return Integer.MAX_VALUE;
      }
    }

    return (int) cost;
  }

  private int computeParallelCount(ReprocessorRecipe recipe) {
    if (recipe == null) return 1;
    if (!hasParallelProcessor()) return 1;
    if (level == null) return 1;

    int best = 1;
    for (BlockPos inputPos : inputHatches) {
      if (!(level.getBlockEntity(inputPos) instanceof ReprocessorIOHatch hatch)) {
        continue;
      }

      var handler = hatch.getInputInventory();
      if (handler == null) {
        continue;
      }

      int possible = ReprocessorInventoryUtil.computeMaxParallelCount(recipe, handler, 4);
      if (possible > best) {
        best = possible;
        if (best >= 4) {
          return 4;
        }
      }
    }

    return Math.max(1, Math.min(4, best));
  }

  private int getEnergyCostPerTick(ReprocessorRecipe recipe, int parallelCount) {
    int base = getEnergyCostPerTick(recipe);
    int n = Math.max(1, Math.min(4, parallelCount));

    return (int) Math.ceil(base * (1.0 + 0.5 * (n - 1)));
  }

  private boolean canFitOutputs(ReprocessorRecipe recipe, int parallelCount) {
    if (recipe == null) {
      return false;
    }

    return ReprocessorInventoryUtil.canFitOutputs(recipe, outputHatches, level, parallelCount);
  }

  public void tick(Level level, BlockPos blockPos, BlockState blockState) {
    validateMultiblock();

    Optional<RecipeHolder<ReprocessorRecipe>> currentRecipe = findRecipe();
    if (currentRecipe.isEmpty()) {
      this.status = STATUS_NO_RECIPE;
      this.progress = 0;
      setCurrentRecipeOutputName("");
      this.energyCostLastTick = 0;
      return;
    }

    setCurrentRecipeOutputName(currentRecipe.get().value().output().getHoverName().getString());

    this.status = getCannotCraftReason(currentRecipe.get().value());
    if (this.status != STATUS_OK) {
      this.energyCostLastTick = 0;
      return;
    }

    var recipe = currentRecipe.get().value();
    this.maxProgress = recipe.craftingTime();

    int parallelCount = computeParallelCount(recipe);

    progress += getProgressPerTick();
    int energyCost = getEnergyCostPerTick(recipe, parallelCount);
    this.energyCostLastTick = energyCost;

    if (!extractEnergyFromHatches(energyCost)) {
      this.status = STATUS_NO_ENERGY;
      progress = Math.max(0, progress - getProgressPerTick());
      return;
    }

    if (progress >= maxProgress) {
      progress = 0;
      craftItem(recipe, parallelCount);
    }

    setChanged(level, blockPos, blockState);
  }

  private void validateMultiblock() {
    if (inputHatches.isEmpty()) {
      this.status = STATUS_NO_INPUT_HATCH;
      this.progress = 0;
      setCurrentRecipeOutputName("");
      this.energyCostLastTick = 0;
      return;
    }

    if (outputHatches.isEmpty()) {
      this.status = STATUS_NO_OUTPUT_HATCH;
      this.progress = 0;
      setCurrentRecipeOutputName("");
      this.energyCostLastTick = 0;
      return;
    }

    // TODO: Properly validate rest of structure
  }

  public String getCurrentRecipeOutputName() {
    return currentRecipeOutputName;
  }

  private void setCurrentRecipeOutputName(String name) {
    if (!this.currentRecipeOutputName.equals(name)) {
      this.currentRecipeOutputName = name;
      if (level != null && !level.isClientSide()) {
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
      }
    }
  }

  private Optional<RecipeHolder<ReprocessorRecipe>> findRecipe() {
    if (level == null) {
      return Optional.empty();
    }

    var recipes =
        level.getRecipeManager().getAllRecipesFor(lushathings.REPROCESSOR_RECIPE_TYPE.get());
    for (var recipeHolder : recipes) {
      ReprocessorRecipe recipe = recipeHolder.value();
      if (hasIngredients(recipe)) {
        return Optional.of(recipeHolder);
      }
    }
    return Optional.empty();
  }

  private boolean hasIngredients(ReprocessorRecipe recipe) {
    for (BlockPos inputPos : inputHatches) {
      if (level.getBlockEntity(inputPos) instanceof ReprocessorIOHatch hatch) {
        var handler = hatch.getInputInventory();
        if (handler == null) {
          continue;
        }

        if (ReprocessorInventoryUtil.hasAllIngredients(recipe, handler)) {
          return true;
        }
      }
    }

    return false;
  }

  private void craftItem(ReprocessorRecipe recipe, int parallelCount) {
    int n = Math.max(1, Math.min(4, parallelCount));

    for (BlockPos inputPos : inputHatches) {
      if (!(level.getBlockEntity(inputPos) instanceof ReprocessorIOHatch hatch)) continue;
      var handler = hatch.getInputInventory();
      if (handler == null) continue;

      if (!ReprocessorInventoryUtil.hasAllIngredientsMultiplied(recipe, handler, n)) {
        continue;
      }

      ReprocessorInventoryUtil.consumeIngredients(recipe, handler, n);

      for (int i = 0; i < n; i++) {
        ReprocessorInventoryUtil.insertOutput(recipe.output().copy(), outputHatches, level);
        if (!recipe.output2().isEmpty()) {
          ReprocessorInventoryUtil.insertOutput(recipe.output2().copy(), outputHatches, level);
        }
      }

      return;
    }
  }

  private int getCannotCraftReason(ReprocessorRecipe recipe) {
    int parallelCount = computeParallelCount(recipe);

    if (!hasEnergy(getEnergyCostPerTick(recipe, parallelCount))) {
      return STATUS_NO_ENERGY;
    }

    return canFitOutputs(recipe, parallelCount) ? STATUS_OK : STATUS_OUTPUT_FULL;
  }

  private boolean hasEnergy(int required) {
    int total = 0;
    for (BlockPos energyPos : energyInputs) {
      if (level.getBlockEntity(energyPos) instanceof ReprocessorEnergyHatch hatch) {
        total += Math.max(0, hatch.getEnergyStored());
        if (total < required) {
          total += hatch.extractEnergyInternal(required - total, true);
        }

        if (total >= required) {
          return true;
        }
      }
    }

    return total >= required;
  }

  private boolean extractEnergyFromHatches(int required) {
    int remaining = required;
    for (BlockPos energyPos : energyInputs) {
      if (level.getBlockEntity(energyPos) instanceof ReprocessorEnergyHatch hatch) {
        int extracted = hatch.extractEnergyInternal(remaining, false);
        remaining -= extracted;
        if (remaining <= 0) {
          return true;
        }
      }
    }

    return remaining <= 0;
  }

  private boolean simulateInsert(ItemStack stack) {
    return ReprocessorInventoryUtil.simulateInsertAcrossHatches(stack, outputHatches, level);
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);

    tag.putInt("reprocessor.progress", progress);
    tag.putInt("reprocessor.maxProgress", maxProgress);
    tag.putString("reprocessor.currentRecipeOutputName", currentRecipeOutputName);
    if (centerPos != null) {
      tag.putLong("reprocessor.centerPos", centerPos.asLong());
    }

    tag.putLongArray(
        "reprocessor.inputHatches", inputHatches.stream().mapToLong(BlockPos::asLong).toArray());
    tag.putLongArray(
        "reprocessor.outputHatches", outputHatches.stream().mapToLong(BlockPos::asLong).toArray());
    tag.putLongArray(
        "reprocessor.energyInputs", energyInputs.stream().mapToLong(BlockPos::asLong).toArray());
    tag.putLongArray(
        "reprocessor.bulkProcessingBlocks",
        bulkProcessingBlocks.stream().mapToLong(BlockPos::asLong).toArray());
    tag.putLongArray(
        "reprocessor.parallelProcessorBlocks",
        parallelProcessorBlocks.stream().mapToLong(BlockPos::asLong).toArray());
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);

    progress = tag.getInt("reprocessor.progress");
    maxProgress = tag.getInt("reprocessor.maxProgress");
    currentRecipeOutputName = tag.getString("reprocessor.currentRecipeOutputName");

    if (tag.contains("reprocessor.centerPos")) {
      this.centerPos = BlockPos.of(tag.getLong("reprocessor.centerPos"));
    }

    if (tag.contains("reprocessor.inputHatches")) {
      inputHatches.clear();
      for (long posLong : tag.getLongArray("reprocessor.inputHatches")) {
        inputHatches.add(BlockPos.of(posLong));
      }
    }

    if (tag.contains("reprocessor.outputHatches")) {
      outputHatches.clear();
      for (long posLong : tag.getLongArray("reprocessor.outputHatches")) {
        outputHatches.add(BlockPos.of(posLong));
      }
    }

    if (tag.contains("reprocessor.energyInputs")) {
      energyInputs.clear();
      for (long posLong : tag.getLongArray("reprocessor.energyInputs")) {
        energyInputs.add(BlockPos.of(posLong));
      }
    }

    if (tag.contains("reprocessor.bulkProcessingBlocks")) {
      bulkProcessingBlocks.clear();
      for (long posLong : tag.getLongArray("reprocessor.bulkProcessingBlocks")) {
        bulkProcessingBlocks.add(BlockPos.of(posLong));
      }
    }

    if (tag.contains("reprocessor.parallelProcessorBlocks")) {
      parallelProcessorBlocks.clear();
      for (long posLong : tag.getLongArray("reprocessor.parallelProcessorBlocks")) {
        parallelProcessorBlocks.add(BlockPos.of(posLong));
      }
    }
  }

  @Override
  public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    return saveWithoutMetadata(registries);
  }
}

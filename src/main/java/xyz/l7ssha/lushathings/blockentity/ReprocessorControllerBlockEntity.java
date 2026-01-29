package xyz.l7ssha.lushathings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
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
import xyz.l7ssha.lushathings.recipe.ReprocessorRecipe;
import xyz.l7ssha.lushathings.recipe.util.SizedIngredient;
import xyz.l7ssha.lushathings.screen.ReprocessorControllerMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    protected final ContainerData data;

    private int progress = 0;
    private int maxProgress = 600;
    private BlockPos centerPos = null;
    private int status = STATUS_NO_RECIPE;
    private @Nullable ResourceLocation currentRecipeId = null;
    private int energyCostLastTick = 0;

    public ReprocessorControllerBlockEntity(BlockPos pos, BlockState blockState) {
        super(lushathings.REPROCESSOR_CONTROLLER_BLOCK_ENTITY.get(), pos, blockState);

        data = new ContainerData() {
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
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
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

    public List<BlockPos> getBulkProcessingBlocks() {
        return bulkProcessingBlocks;
    }

    private static final int DEFAULT_PROGRESS_PER_TICK = 1;
    private static final int BULK_BLOCK_SPEED_MULTIPLIER = 2; // 2x faster per bulk block
    private static final int BULK_BLOCK_ENERGY_MULTIPLIER = 4; // 4x energy usage per bulk block

    private int getBulkProcessingBlockCount() {
        // Defensive: callers may pass null via setBulkProcessingBlocks(...)
        return bulkProcessingBlocks == null ? 0 : bulkProcessingBlocks.size();
    }

    private int getProgressPerTick() {
        int bulkCount = getBulkProcessingBlockCount();
        if (bulkCount <= 0) return DEFAULT_PROGRESS_PER_TICK;

        // 2x faster per bulk block (i.e., speed = 1 * 2^bulkCount)
        long speed = DEFAULT_PROGRESS_PER_TICK;
        for (int i = 0; i < bulkCount; i++) {
            speed *= BULK_BLOCK_SPEED_MULTIPLIER;
            if (speed >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        }
        return (int) speed;
    }

    private int getEnergyCostPerTick(ReprocessorRecipe recipe) {
        if (recipe == null) {
            return 0;
        }

        int base = Math.max(0, recipe.energyCost());
        int bulkCount = getBulkProcessingBlockCount();
        if (bulkCount <= 0) return base;

        // 4x energy usage per bulk block (i.e., cost = base * 4^bulkCount)
        long cost = base;
        for (int i = 0; i < bulkCount; i++) {
            cost *= BULK_BLOCK_ENERGY_MULTIPLIER;
            if (cost >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        }
        return (int) cost;
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        validateMultiblock();

        Optional<RecipeHolder<ReprocessorRecipe>> currentRecipe = findRecipe();
        if (currentRecipe.isEmpty()) {
            this.status = STATUS_NO_RECIPE;
            this.progress = 0;
            this.currentRecipeId = null;
            this.energyCostLastTick = 0;
            return;
        }

        this.currentRecipeId = currentRecipe.get().id();

        this.status = getCannotCraftReason(currentRecipe.get().value());
        if (this.status != STATUS_OK) {
            this.energyCostLastTick = 0;
            return;
        }

        var recipe = currentRecipe.get().value();
        this.maxProgress = recipe.craftingTime();

        progress += getProgressPerTick();
        int energyCost = getEnergyCostPerTick(recipe);
        this.energyCostLastTick = energyCost;

        if (!extractEnergyFromHatches(energyCost)) {
            this.status = STATUS_NO_ENERGY;
            progress = Math.max(0, progress - getProgressPerTick());
            return;
        }

        if (progress >= maxProgress) {
            progress = 0;
            craftItem(recipe);
        }

        setChanged(level, blockPos, blockState);
    }

    private void validateMultiblock() {
        if (inputHatches.isEmpty()) {
            this.status = STATUS_NO_INPUT_HATCH;
            this.progress = 0;
            this.currentRecipeId = null;
            this.energyCostLastTick = 0;
            return;
        }

        if (outputHatches.isEmpty()) {
            this.status = STATUS_NO_OUTPUT_HATCH;
            this.progress = 0;
            this.currentRecipeId = null;
            this.energyCostLastTick = 0;
            return;
        }

        // TODO: Properly validate rest of structure
    }

    public String getCurrentRecipeId() {
        return currentRecipeId == null ? "None" : currentRecipeId.toString();
    }

    private Optional<RecipeHolder<ReprocessorRecipe>> findRecipe() {
        if (level == null) return Optional.empty();

        var recipes = level.getRecipeManager().getAllRecipesFor(lushathings.REPROCESSOR_RECIPE_TYPE.get());
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
                if (handler == null) continue;
                boolean allMatched = true;

                for (SizedIngredient ingredient : recipe.inputs()) {
                    int required = ingredient.count();
                    int foundCount = 0;

                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack stack = handler.getStackInSlot(i);
                        if (!stack.isEmpty() && ingredient.ingredient().test(stack)) {
                            foundCount += stack.getCount();
                        }
                    }

                    if (foundCount < required) {
                        allMatched = false;
                        break;
                    }
                }

                if (allMatched) return true;
            }
        }
        return false;
    }

    private void craftItem(ReprocessorRecipe recipe) {
        for (BlockPos inputPos : inputHatches) {
            if (level.getBlockEntity(inputPos) instanceof ReprocessorIOHatch hatch) {
                var handler = hatch.getInputInventory();
                if (handler == null) continue;
                boolean possibleHere = true;
                for (SizedIngredient ingredient : recipe.inputs()) {
                    int required = ingredient.count();
                    int foundCount = 0;
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack stack = handler.getStackInSlot(i);
                        if (!stack.isEmpty() && ingredient.ingredient().test(stack)) {
                            foundCount += stack.getCount();
                        }
                    }
                    if (foundCount < required) {
                        possibleHere = false;
                        break;
                    }
                }

                if (possibleHere) {
                    for (SizedIngredient ingredient : recipe.inputs()) {
                        int required = ingredient.count();
                        for (int i = 0; i < handler.getSlots(); i++) {
                            ItemStack stack = handler.getStackInSlot(i);
                            if (!stack.isEmpty() && ingredient.ingredient().test(stack)) {
                                int toTake = Math.min(stack.getCount(), required);
                                handler.extractItem(i, toTake, false);
                                required -= toTake;
                                if (required <= 0) break;
                            }
                        }
                    }

                    ItemStack result = recipe.output().copy();
                    insertOutput(result);
                    if (!recipe.output2().isEmpty()) {
                        insertOutput(recipe.output2().copy());
                    }

                    return;
                }
            }
        }
    }

    private void insertOutput(ItemStack stack) {
        for (BlockPos outputPos : outputHatches) {
            if (level.getBlockEntity(outputPos) instanceof ReprocessorIOHatch hatch) {
                var handler = hatch.getOutputInventory();
                if (handler == null) continue;
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack slotStack = handler.getStackInSlot(i);
                    if (slotStack.isEmpty()) {
                        int maxInsert = Math.min(stack.getCount(), handler.getSlotLimit(i));
                        if (maxInsert > 0) {
                            ItemStack toInsert = stack.copy();
                            toInsert.setCount(maxInsert);
                            handler.setStackInSlot(i, toInsert);
                            stack.shrink(maxInsert);
                            if (stack.isEmpty()) return;
                        }
                    } else if (ItemStack.isSameItem(slotStack, stack)) {
                        int spaceAvailable = handler.getSlotLimit(i) - slotStack.getCount();
                        if (spaceAvailable > 0) {
                            int toInsert = Math.min(stack.getCount(), spaceAvailable);
                            slotStack.grow(toInsert);
                            stack.shrink(toInsert);
                            if (stack.isEmpty()) return;
                        }
                    }
                }
            }
        }
    }

    private int getCannotCraftReason(ReprocessorRecipe recipe) {
        if (!hasEnergy(getEnergyCostPerTick(recipe))) {
            return STATUS_NO_ENERGY;
        }

        ItemStack result = recipe.output().copy();
        ItemStack result2 = recipe.output2().copy();

        boolean canFit1 = simulateInsert(result);
        boolean canFit2 = result2.isEmpty() || simulateInsert(result2);

        return (canFit1 && canFit2) ? STATUS_OK : STATUS_OUTPUT_FULL;
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
        ItemStack remaining = stack.copy();
        for (BlockPos outputPos : outputHatches) {
            if (level.getBlockEntity(outputPos) instanceof ReprocessorIOHatch hatch) {
                var handler = hatch.getOutputInventory();
                if (handler == null) continue;
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack slotStack = handler.getStackInSlot(i);
                    if (slotStack.isEmpty()) {
                        if (remaining.getCount() <= handler.getSlotLimit(i)) {
                            return true;
                        } else {
                            remaining = remaining.copy();
                            remaining.shrink(handler.getSlotLimit(i));
                            if (remaining.isEmpty()) return true;
                        }
                    } else if (ItemStack.isSameItem(slotStack, remaining)) {
                        int spaceAvailable = handler.getSlotLimit(i) - slotStack.getCount();
                        if (spaceAvailable > 0) {
                            int toInsert = Math.min(remaining.getCount(), spaceAvailable);
                            remaining = remaining.copy();
                            remaining.shrink(toInsert);
                            if (remaining.isEmpty()) return true;
                        }
                    }
                }
            }
        }

        return remaining.isEmpty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putInt("reprocessor.progress", progress);
        tag.putInt("reprocessor.maxProgress", maxProgress);
        if (centerPos != null) {
            tag.putLong("reprocessor.centerPos", centerPos.asLong());
        }

        tag.putLongArray("reprocessor.inputHatches", inputHatches.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putLongArray("reprocessor.outputHatches", outputHatches.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putLongArray("reprocessor.energyInputs", energyInputs.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putLongArray("reprocessor.bulkProcessingBlocks", bulkProcessingBlocks.stream().mapToLong(BlockPos::asLong).toArray());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        progress = tag.getInt("reprocessor.progress");
        maxProgress = tag.getInt("reprocessor.maxProgress");

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

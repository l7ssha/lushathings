package xyz.l7ssha.lushathings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.recipe.ReprocessorRecipe;
import xyz.l7ssha.lushathings.recipe.ReprocessorRecipeInput;
import xyz.l7ssha.lushathings.screen.ReprocessorControllerMenu;
import xyz.l7ssha.lushathings.blockentity.ReprocessorEnergyInputBlockEntity;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.ArrayList;
import java.util.List;
import xyz.l7ssha.lushathings.recipe.util.SizedIngredient;
import xyz.l7ssha.lushathings.blockentity.ReprocessorInputBlockEntity;
import xyz.l7ssha.lushathings.blockentity.ReprocessorOutputBlockEntity;

// TODO: Menu implements MenuProvider
public class ReprocessorControllerBlockEntity extends BlockEntity implements MenuProvider {
    /**
     * Reason codes for why crafting is not progressing.
     * Kept as ints so they can be synced via {@link ContainerData}.
     */
    public static final int STATUS_OK = 0;
    public static final int STATUS_NO_INPUT_HATCH = 1;
    public static final int STATUS_NO_OUTPUT_HATCH = 2;
    public static final int STATUS_NO_RECIPE = 3;
    public static final int STATUS_NO_ENERGY = 4;
    public static final int STATUS_OUTPUT_FULL = 5;

    private List<BlockPos> inputHatches = new ArrayList<>();
    private List<BlockPos> outputHatches = new ArrayList<>();
    private List<BlockPos> energyInputs = new ArrayList<>();

    private static final int INPUT_SLOT = 0;
    private static final int INPUT_SLOT_2 = 1;
    private static final int OUTPUT_SLOT = 2;
    private static final int OUTPUT_SLOT_2 = 3;

    private static final int[] INPUT_SLOTS = {INPUT_SLOT, INPUT_SLOT_2};

    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 600;
    private BlockPos centerPos = null;

    private int status = STATUS_NO_RECIPE;

    private @Nullable ResourceLocation currentRecipeId = null;

    private final BiPredicate<Integer, ItemStack> validator = (slot, stack) -> {
        return true;
    };

    public final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (!validator.test(slot, stack)) {
                return false;
            }

            for (int inputSlot : INPUT_SLOTS) {
                if (slot == inputSlot) {
                    return true;
                }
            }

            return false;
        }
    };

    private int energyCostLastTick = 0;

    public ReprocessorControllerBlockEntity(BlockPos pos, BlockState blockState) {
        super(lushathings.REPROCESSOR_CONTROLLER_BLOCK_ENTITY.get(), pos, blockState); // TODO: Fix

        data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> ReprocessorControllerBlockEntity.this.progress;
                    case 1 -> ReprocessorControllerBlockEntity.this.maxProgress;
                    case 2 -> ReprocessorControllerBlockEntity.this.status;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0 -> ReprocessorControllerBlockEntity.this.progress = value;
                    case 1 -> ReprocessorControllerBlockEntity.this.maxProgress = value;
                    case 2 -> ReprocessorControllerBlockEntity.this.status = value;
                }
            }

            @Override
            public int getCount() {
                return 16;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.lushathings.reprocessor_controller_block");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ReprocessorControllerMenu(containerId, inventory, this, this.data);
    }

    public void setCenterPos(BlockPos pos) { this.centerPos = pos; setChanged(); }
    public BlockPos getCenterPos() { return this.centerPos; }

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

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (inputHatches.isEmpty()) {
            this.status = STATUS_NO_INPUT_HATCH;
            this.progress = 0;
            this.currentRecipeId = null;
            return;
        }

        if (outputHatches.isEmpty()) {
            this.status = STATUS_NO_OUTPUT_HATCH;
            this.progress = 0;
            this.currentRecipeId = null;
            return;
        }

        Optional<RecipeHolder<ReprocessorRecipe>> currentRecipe = findRecipe();
        if (currentRecipe.isEmpty()) {
            this.status = STATUS_NO_RECIPE;
            this.progress = 0;
            this.currentRecipeId = null;
            return;
        }

        this.currentRecipeId = currentRecipe.get().id();

        int cannotCraftReason = getCannotCraftReason(currentRecipe.get().value());
        if (cannotCraftReason != STATUS_OK) {
            this.status = cannotCraftReason;
            return;
        }

        this.status = STATUS_OK;

        ReprocessorRecipe recipe = currentRecipe.get().value();
        this.maxProgress = recipe.craftingTime();

        progress++;
        this.energyCostLastTick = recipe.energyCost();
        if (!extractEnergyFromHatches(recipe.energyCost())) {
            // Not enough energy; halt progress.
            this.status = STATUS_NO_ENERGY;
            progress = Math.max(0, progress - 1);
            return;
        }

        if (progress >= maxProgress) {
            progress = 0;
            craftItem(recipe);
        }

        setChanged(level, blockPos, blockState);
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
            if (level.getBlockEntity(inputPos) instanceof ReprocessorInputBlockEntity inputBe) {
                var handler = inputBe.getItemHandler();
                boolean allMatched = true;
                
                // Copy logic to simulate extraction or check presence
                // Since we have multiple ingredients, we need to ensure we don't count the same item for multiple ingredients if they overlap (rare but possible with tags)
                // For simplicity, strict check
                List<Integer> usedSlots = new ArrayList<>();

                for (SizedIngredient ingredient : recipe.inputs()) {
                    boolean ingredientFound = false;
                    int required = ingredient.count();
                    int foundCount = 0;

                    for (int i = 0; i < handler.getSlots(); i++) {
                        // Don't reuse slots for different ingredients in the same recipe check if we want strictness,
                        // but usually different ingredients are different items.
                        // Assuming simple matching first.
                        ItemStack stack = handler.getStackInSlot(i);
                        if (!stack.isEmpty() && ingredient.test(stack)) {
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
        // Consume inputs
        for (BlockPos inputPos : inputHatches) {
            if (level.getBlockEntity(inputPos) instanceof ReprocessorInputBlockEntity inputBe) {
                var handler = inputBe.getItemHandler();
                
                // We need to verify we can still match in this specific hatch (in case multiple hatches)
                // For now assuming first matching hatch is the one we use
                boolean possibleHere = true;
                 for (SizedIngredient ingredient : recipe.inputs()) {
                    int required = ingredient.count();
                    int foundCount = 0;
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack stack = handler.getStackInSlot(i);
                        if (!stack.isEmpty() && ingredient.test(stack)) {
                             foundCount += stack.getCount();
                        }
                    }
                    if (foundCount < required) {
                        possibleHere = false;
                        break;
                    }
                }

                if (possibleHere) {
                    // Execute consumption
                     for (SizedIngredient ingredient : recipe.inputs()) {
                        int required = ingredient.count();
                        for (int i = 0; i < handler.getSlots(); i++) {
                            ItemStack stack = handler.getStackInSlot(i);
                            if (!stack.isEmpty() && ingredient.test(stack)) {
                                int toTake = Math.min(stack.getCount(), required);
                                handler.extractItem(i, toTake, false);
                                required -= toTake;
                                if (required <= 0) break;
                            }
                        }
                    }
                    
                    // Output
                    ItemStack result = recipe.output().copy();
                    insertOutput(result);
                    if (!recipe.output2().isEmpty()) {
                        insertOutput(recipe.output2().copy());
                    }
                    
                    return; // Crafted once
                }
            }
        }
    }
    
    private void insertOutput(ItemStack stack) {
        for (BlockPos outputPos : outputHatches) {
            if (level.getBlockEntity(outputPos) instanceof ReprocessorOutputBlockEntity outputBe) {
                var handler = outputBe.getItemHandler();
                for (int i = 0; i < handler.getSlots(); i++) {
                    stack = handler.insertItem(i, stack, false);
                    if (stack.isEmpty()) return;
                }
            }
        }
    }

    /**
     * Returns {@link #STATUS_OK} when crafting can start, otherwise a status code explaining why it can't.
     */
    private int getCannotCraftReason(ReprocessorRecipe recipe) {
        if (!hasEnergy(recipe.energyCost())) {
            return STATUS_NO_ENERGY;
        }

        // Check output space via simulated insertion.
        ItemStack result = recipe.output().copy();
        ItemStack result2 = recipe.output2().copy();

        boolean canFit1 = simulateInsert(result);
        boolean canFit2 = result2.isEmpty() || simulateInsert(result2);

        return (canFit1 && canFit2) ? STATUS_OK : STATUS_OUTPUT_FULL;
    }

    private boolean hasEnergy(int required) {
        int total = 0;
        for (BlockPos energyPos : energyInputs) {
            if (level.getBlockEntity(energyPos) instanceof ReprocessorEnergyInputBlockEntity be) {
                total += be.getEnergyStorage().getEnergyStored();
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
            if (level.getBlockEntity(energyPos) instanceof ReprocessorEnergyInputBlockEntity be) {
                int extracted = be.extractEnergyInternal(remaining, false);
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
            if (level.getBlockEntity(outputPos) instanceof ReprocessorOutputBlockEntity outputBe) {
                var handler = outputBe.getItemHandler();
                for (int i = 0; i < handler.getSlots(); i++) {
                    remaining = handler.insertItem(i, remaining, true);
                    if (remaining.isEmpty()) return true;
                }
            }
        }
        return remaining.isEmpty();
    }

//    @Override
//    public @NotNull Component getDisplayName() {
//        return Component.translatable("block.lushathings.reprocessor_block");
//    }

//    @Override
//    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
//        return new ReprocessorMenu(i, player.getInventory(), this, this.data);
//    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putInt("reprocessor.progress", progress);
        tag.putInt("reprocessor.maxProgress", maxProgress);
        tag.put("reprocessor.inventory", itemHandler.serializeNBT(registries));
        if (centerPos != null) {
            tag.putLong("reprocessor.centerPos", centerPos.asLong());
        }
        
        tag.putLongArray("reprocessor.inputHatches", inputHatches.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putLongArray("reprocessor.outputHatches", outputHatches.stream().mapToLong(BlockPos::asLong).toArray());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        itemHandler.deserializeNBT(registries, tag.getCompound("reprocessor.inventory"));
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

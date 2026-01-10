package xyz.l7ssha.lushathings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.EnergyStorageWrapper;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.recipe.ReprocessorRecipe;
import xyz.l7ssha.lushathings.recipe.ReprocessorRecipeInput;

import java.util.Optional;
import java.util.function.BiPredicate;

// TODO: Menu implements MenuProvider
public class ReprocessorControllerBlockEntity extends BlockEntity {
    private static final int INPUT_SLOT = 0;
    private static final int INPUT_SLOT_2 = 1;
    private static final int OUTPUT_SLOT = 2;
    private static final int OUTPUT_SLOT_2 = 3;

    private static final int[] INPUT_SLOTS = {INPUT_SLOT, INPUT_SLOT_2};

    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 600;
    private BlockPos centerPos = null;

    private final BiPredicate<Integer, ItemStack> validator = (slot, stack) -> {
        if (level == null) {
            return true;
        }

        if (slot == INPUT_SLOT) {
            var recipes = level.getRecipeManager().getAllRecipesFor(lushathings.REPROCESSOR_RECIPE_TYPE.get());
            return recipes.stream().anyMatch(recipe ->
                    recipe.value().inputs().getFirst().test(stack)
            );
        }

        if (slot == INPUT_SLOT_2) {
            return isValidInputSlot2(stack);
        }

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

    private final EnergyStorageWrapper energyStorage = new EnergyStorageWrapper(30000000, 150000, () -> {
        setChanged();

        if (level != null && !level.isClientSide) {
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    });

    public ReprocessorControllerBlockEntity(BlockPos pos, BlockState blockState) {
        super(lushathings.REPROCESSOR_CONTROLLER_BLOCK_ENTITY.get(), pos, blockState); // TODO: Fix

        data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> ReprocessorControllerBlockEntity.this.progress;
                    case 1 -> ReprocessorControllerBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0 -> ReprocessorControllerBlockEntity.this.progress = value;
                    case 1 -> ReprocessorControllerBlockEntity.this.maxProgress = value;
                }
            }

            @Override
            public int getCount() {
                return 16;
            }
        };
    }

    public void setCenterPos(BlockPos pos) { this.centerPos = pos; setChanged(); }
    public BlockPos getCenterPos() { return this.centerPos; }

    protected boolean isValidInputSlot2(ItemStack stack) {
        ItemStack slot0Stack = itemHandler.getStackInSlot(INPUT_SLOT);
        if (slot0Stack.isEmpty()) {
            return false;
        }

        var recipes = level.getRecipeManager().getAllRecipesFor(lushathings.REPROCESSOR_RECIPE_TYPE.get());
        return recipes.stream().anyMatch(recipe ->
                recipe.value().inputs().size() > 1 &&
                        recipe.value().inputs().getFirst().test(slot0Stack) &&
                        recipe.value().inputs().get(1).test(stack)
        );
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (this.itemHandler.getStackInSlot(INPUT_SLOT).isEmpty()) {
            this.progress = 0;

            return;
        }

        if (!canCraft()) {
            return;
        }

        Optional<RecipeHolder<ReprocessorRecipe>> currentRecipe = getCurrentRecipe();
        if (currentRecipe.isEmpty()) {
            return;
        }

        ReprocessorRecipe recipe = currentRecipe.get().value();
        this.maxProgress = recipe.craftingTime();

        progress++;
        energyStorage.extractEnergy(recipe.energyCost(), false);

        if (progress >= maxProgress) {
            progress = 0;
            craftItem();
        }

        setChanged(level, blockPos, blockState);
    }

    private void craftItem() {
        Optional<RecipeHolder<ReprocessorRecipe>> currentRecipe = getCurrentRecipe();
        if (currentRecipe.isEmpty()) {
            return;
        }

        ReprocessorRecipe recipe = currentRecipe.get().value();
        ItemStack output = recipe.output();
        ItemStack output2 = recipe.output2();

        itemHandler.extractItem(INPUT_SLOT, recipe.inputs().getFirst().count(), false);
        if (recipe.inputs().size() > 1) {
            itemHandler.extractItem(INPUT_SLOT_2, recipe.inputs().get(1).count(), false);
        }
        itemHandler.setStackInSlot(OUTPUT_SLOT, output.copyWithCount(itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + output.getCount()));

        if (!output2.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT_2, output2.copyWithCount(itemHandler.getStackInSlot(OUTPUT_SLOT_2).getCount() + output2.getCount()));
        }
    }

    private boolean canCraft() {
        Optional<RecipeHolder<ReprocessorRecipe>> currentRecipe = getCurrentRecipe();
        if (currentRecipe.isEmpty()) {
            return false;
        }

        ReprocessorRecipe recipe = currentRecipe.get().value();

        if (this.energyStorage.getEnergyStored() < recipe.energyCost()) {
            return false;
        }

        ItemStack recipeOutput = recipe.output();
        ItemStack recipeOutput2 = recipe.output2();

        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        ItemStack outputStack2 = itemHandler.getStackInSlot(OUTPUT_SLOT_2);

        boolean input1Valid = itemHandler.getStackInSlot(INPUT_SLOT).getCount() >= recipe.inputs().getFirst().count();
        boolean input2Valid = true;
        if (recipe.inputs().size() > 1) {
            input2Valid = itemHandler.getStackInSlot(INPUT_SLOT_2).getCount() >= recipe.inputs().get(1).count();
        }

        boolean output1Valid = outputStack.isEmpty() || (outputStack.getItem() == recipeOutput.getItem() && outputStack.getCount() + recipeOutput.getCount() <= 64);
        boolean output2Valid = true;

        if (!recipeOutput2.isEmpty()) {
            output2Valid = outputStack2.isEmpty() || (outputStack2.getItem() == recipeOutput2.getItem() && outputStack2.getCount() + recipeOutput2.getCount() <= 64);
        }

        return input1Valid && input2Valid && output1Valid && output2Valid;
    }

    private Optional<RecipeHolder<ReprocessorRecipe>> getCurrentRecipe() {
        return this.level.getRecipeManager().getRecipeFor(
                lushathings.REPROCESSOR_RECIPE_TYPE.get(),
                new ReprocessorRecipeInput(itemHandler.getStackInSlot(INPUT_SLOT), itemHandler.getStackInSlot(INPUT_SLOT_2)),
                level
        );
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
        tag.put("reprocessor.energy", energyStorage.serializeNBT(registries));

        if (centerPos != null) {
            tag.putLong("reprocessor.centerPos", centerPos.asLong());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        itemHandler.deserializeNBT(registries, tag.getCompound("reprocessor.inventory"));
        progress = tag.getInt("reprocessor.progress");
        maxProgress = tag.getInt("reprocessor.maxProgress");

        if (tag.contains("reprocessor.energy")) {
            energyStorage.deserializeNBT(registries, tag.get("reprocessor.energy"));
        }

        if (tag.contains("reprocessor.centerPos")) {
            this.centerPos = BlockPos.of(tag.getLong("reprocessor.centerPos"));
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

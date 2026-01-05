package xyz.l7ssha.lushathings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.EnergyStorageWrapper;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.recipe.ReprocessorRecipe;
import xyz.l7ssha.lushathings.recipe.ReprocessorRecipeInput;
import xyz.l7ssha.lushathings.screen.ReprocessorMenu;

import xyz.l7ssha.lushathings.util.GenericConfigurableEnergyStorage;
import xyz.l7ssha.lushathings.util.GenericConfigurableItemHandler;
import xyz.l7ssha.lushathings.util.ISideConfigurable;
import xyz.l7ssha.lushathings.util.SideConfigType;

import java.util.Optional;

public class ReprocessorBlockEntity extends BlockEntity implements MenuProvider, ISideConfigurable {
    private static final int ENERGY_USAGE_PER_TICK = 50000;
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 600;

    public final GenericConfigurableItemHandler itemHandler = new GenericConfigurableItemHandler(
            2,
            new int[]{INPUT_SLOT},
            new int[]{OUTPUT_SLOT},
            () -> {
                setChanged();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }
    );

    private final GenericConfigurableEnergyStorage energyStorage = new GenericConfigurableEnergyStorage(30000000, 150000, 0, () -> {
        setChanged();

        if (level != null && !level.isClientSide) {
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    });

    public ReprocessorBlockEntity(BlockPos pos, BlockState blockState) {
        super(lushathings.REPROCESSOR_BLOCK_ENTITY.get(), pos, blockState);

        data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> ReprocessorBlockEntity.this.progress;
                    case 1 -> ReprocessorBlockEntity.this.maxProgress;
                    case 2 -> ReprocessorBlockEntity.this.itemHandler.getSideConfig()[0];
                    case 3 -> ReprocessorBlockEntity.this.itemHandler.getSideConfig()[1];
                    case 4 -> ReprocessorBlockEntity.this.itemHandler.getSideConfig()[2];
                    case 5 -> ReprocessorBlockEntity.this.itemHandler.getSideConfig()[3];
                    case 6 -> ReprocessorBlockEntity.this.itemHandler.getSideConfig()[4];
                    case 7 -> ReprocessorBlockEntity.this.itemHandler.getSideConfig()[5];
                    case 8 -> ReprocessorBlockEntity.this.energyStorage.getSideConfig()[0];
                    case 9 -> ReprocessorBlockEntity.this.energyStorage.getSideConfig()[1];
                    case 10 -> ReprocessorBlockEntity.this.energyStorage.getSideConfig()[2];
                    case 11 -> ReprocessorBlockEntity.this.energyStorage.getSideConfig()[3];
                    case 12 -> ReprocessorBlockEntity.this.energyStorage.getSideConfig()[4];
                    case 13 -> ReprocessorBlockEntity.this.energyStorage.getSideConfig()[5];
                    case 14 -> ReprocessorBlockEntity.this.itemHandler.isAutoPull() ? 1 : 0;
                    case 15 -> ReprocessorBlockEntity.this.itemHandler.isAutoPush() ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0 -> ReprocessorBlockEntity.this.progress = value;
                    case 1 -> ReprocessorBlockEntity.this.maxProgress = value;
                    case 2 -> ReprocessorBlockEntity.this.itemHandler.getSideConfig()[0] = value;
                    case 3 -> ReprocessorBlockEntity.this.itemHandler.getSideConfig()[1] = value;
                    case 4 -> ReprocessorBlockEntity.this.itemHandler.getSideConfig()[2] = value;
                    case 5 -> ReprocessorBlockEntity.this.itemHandler.getSideConfig()[3] = value;
                    case 6 -> ReprocessorBlockEntity.this.itemHandler.getSideConfig()[4] = value;
                    case 7 -> ReprocessorBlockEntity.this.itemHandler.getSideConfig()[5] = value;
                    case 8 -> ReprocessorBlockEntity.this.energyStorage.getSideConfig()[0] = value;
                    case 9 -> ReprocessorBlockEntity.this.energyStorage.getSideConfig()[1] = value;
                    case 10 -> ReprocessorBlockEntity.this.energyStorage.getSideConfig()[2] = value;
                    case 11 -> ReprocessorBlockEntity.this.energyStorage.getSideConfig()[3] = value;
                    case 12 -> ReprocessorBlockEntity.this.energyStorage.getSideConfig()[4] = value;
                    case 13 -> ReprocessorBlockEntity.this.energyStorage.getSideConfig()[5] = value;
                    case 14 -> ReprocessorBlockEntity.this.itemHandler.setAutoPull(value == 1);
                    case 15 -> ReprocessorBlockEntity.this.itemHandler.setAutoPush(value == 1);
                }
            }

            @Override
            public int getCount() {
                return 16;
            }
        };
    }

    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        return energyStorage.getSideStorage(direction);
    }
    
    public IItemHandler getInventoryStorage(@Nullable Direction direction) {
        return itemHandler.getSideHandler(direction);
    }

    @Override
    public void cycleSideConfig(SideConfigType type, Direction direction) {
        if (type == SideConfigType.ITEM) {
            itemHandler.cycleSideConfig(direction);
        } else if (type == SideConfigType.ENERGY) {
            energyStorage.cycleSideConfig(direction);
        }
    }
    
    public int getSideConfig(SideConfigType type, Direction direction) {
        if (type == SideConfigType.ITEM) {
            return itemHandler.getSideConfig(direction);
        } else if (type == SideConfigType.ENERGY) {
            return energyStorage.getSideConfig(direction);
        }
        return 0;
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (level != null && !level.isClientSide) {
            itemHandler.tick(level, blockPos);
        }

        if (this.itemHandler.getStackInSlot(0).isEmpty()) {
            this.progress = 0;

            return;
        }

        if (!canCraft()) {
            return;
        }

        progress++;
        energyStorage.extractEnergy(ENERGY_USAGE_PER_TICK, false);

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

        ItemStack output = currentRecipe.get().value().output();

        itemHandler.extractItem(INPUT_SLOT, currentRecipe.get().value().inputs().getFirst().count(), false);
        itemHandler.setStackInSlot(OUTPUT_SLOT, output.copyWithCount(itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + output.getCount()));
    }

    private boolean canCraft() {
        Optional<RecipeHolder<ReprocessorRecipe>> currentRecipe = getCurrentRecipe();
        if (currentRecipe.isEmpty()) {
            return false;
        }

        if (this.energyStorage.getEnergyStored() <= ENERGY_USAGE_PER_TICK) {
            return false;
        }

        ItemStack recipeOutput = currentRecipe.get().value().output();
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);

        return itemHandler.getStackInSlot(INPUT_SLOT).getCount() >= currentRecipe.get().value().inputs().getFirst().count()
                && (outputStack.isEmpty() || (
                outputStack.getItem() == recipeOutput.getItem() &&
                        outputStack.getCount() + recipeOutput.getCount() <= 64
        ));
    }

    private Optional<RecipeHolder<ReprocessorRecipe>> getCurrentRecipe() {
        return this.level.getRecipeManager().getRecipeFor(
                lushathings.REPROCESSOR_RECIPE_TYPE.get(),
                new ReprocessorRecipeInput(itemHandler.getStackInSlot(0)),
                level
        );
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.lushathings.reprocessor_block");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ReprocessorMenu(i, player.getInventory(), this, this.data);
    }

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

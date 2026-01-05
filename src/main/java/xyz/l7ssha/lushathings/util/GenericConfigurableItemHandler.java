package xyz.l7ssha.lushathings.util;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GenericConfigurableItemHandler extends ItemStackHandler {
    private final int[] inputSlots;
    private final int[] outputSlots;
    private final Runnable onContentsChanged;

    private boolean autoPull = false;
    private boolean autoPush = false;

    // 0 = none, 1 = input, 2 = output, 3 = both
    // Index corresponds to Direction.ordinal()
    private final int[] sideConfig = new int[]{0, 0, 0, 0, 0, 0};

    public GenericConfigurableItemHandler(int size, int[] inputSlots, int[] outputSlots, Runnable onContentsChanged) {
        super(size);
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
        this.onContentsChanged = onContentsChanged;
    }

    public void tick(Level level, BlockPos pos) {
        if (!autoPull && !autoPush) return;
        if (level.isClientSide) return;

        for (Direction dir : Direction.values()) {
            int config = sideConfig[dir.ordinal()];
            BlockPos neighborPos = pos.relative(dir);

            // Auto Pull: Import from neighbor into this handler
            if (autoPull && (config == 1 || config == 3)) { // Input or Both
                IItemHandler neighbor = level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, dir.getOpposite());
                if (neighbor != null) {
                    for (int slot = 0; slot < neighbor.getSlots(); slot++) {
                        ItemStack extracted = neighbor.extractItem(slot, 1, true);
                        if (!extracted.isEmpty()) {
                            ItemStack remainder = ItemHandlerHelper.insertItem(this, extracted, false);
                            int amountTaken = extracted.getCount() - remainder.getCount();
                            if (amountTaken > 0) {
                                neighbor.extractItem(slot, amountTaken, false);
                                break;
                            }
                        }
                    }
                }
            }

            // Auto Push: Export from this handler to neighbor
            if (autoPush && (config == 2 || config == 3)) { // Output or Both
                IItemHandler neighbor = level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, dir.getOpposite());
                if (neighbor != null) {
                    for (int slot : outputSlots) {
                        ItemStack stack = getStackInSlot(slot);
                        if (!stack.isEmpty()) {
                            ItemStack toPush = stack.copyWithCount(1);
                            ItemStack remainder = ItemHandlerHelper.insertItem(neighbor, toPush, false);
                            int pushed = 1 - remainder.getCount();
                            if (pushed > 0) {
                                extractItem(slot, pushed, false);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isAutoPull() {
        return autoPull;
    }

    public void setAutoPull(boolean autoPull) {
        this.autoPull = autoPull;
        onContentsChanged(0);
    }

    public boolean isAutoPush() {
        return autoPush;
    }

    public void setAutoPush(boolean autoPush) {
        this.autoPush = autoPush;
        onContentsChanged(0);
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (onContentsChanged != null) {
            onContentsChanged.run();
        }
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        for (int inputSlot : inputSlots) {
            if (slot == inputSlot) return true;
        }
        return false;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        // Only allow insertion into input slots (prevents player putting items in output)
        for (int inputSlot : inputSlots) {
            if (slot == inputSlot) {
                return super.insertItem(slot, stack, simulate);
            }
        }
        return stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        // Allow extraction from any slot (allows player to take items from input)
        return super.extractItem(slot, amount, simulate);
    }

    public IItemHandler getSideHandler(@Nullable Direction direction) {
        if (direction == null) {
            return this;
        }

        int config = sideConfig[direction.ordinal()];
        return switch (config) {
            case 1 -> new SideWrapper(true, false); // Input
            case 2 -> new SideWrapper(false, true); // Output
            case 3 -> new SideWrapper(true, true); // Both
            default -> EmptyItemHandler.INSTANCE;
        };
    }

    public void cycleSideConfig(Direction direction) {
        int index = direction.ordinal();
        sideConfig[index] = (sideConfig[index] + 1) % 4;
        onContentsChanged(0); // Trigger save/sync
    }

    public int getSideConfig(Direction direction) {
        return sideConfig[direction.ordinal()];
    }
    
    public int[] getSideConfig() {
        return sideConfig;
    }

    public void setSideConfig(int[] config) {
        if (config.length == 6) {
            System.arraycopy(config, 0, this.sideConfig, 0, 6);
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = super.serializeNBT(provider);
        tag.putIntArray("sideConfig", sideConfig);
        tag.putBoolean("autoPull", autoPull);
        tag.putBoolean("autoPush", autoPush);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        super.deserializeNBT(provider, nbt);
        int[] loadedConfig = nbt.getIntArray("sideConfig");
        if (loadedConfig.length == 6) {
            System.arraycopy(loadedConfig, 0, this.sideConfig, 0, 6);
        }
        if (nbt.contains("autoPull")) {
            autoPull = nbt.getBoolean("autoPull");
        }
        if (nbt.contains("autoPush")) {
            autoPush = nbt.getBoolean("autoPush");
        }
    }

    private class SideWrapper implements IItemHandler {
        private final boolean allowInsert;
        private final boolean allowExtract;

        public SideWrapper(boolean allowInsert, boolean allowExtract) {
            this.allowInsert = allowInsert;
            this.allowExtract = allowExtract;
        }

        @Override
        public int getSlots() {
            return GenericConfigurableItemHandler.this.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return GenericConfigurableItemHandler.this.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!allowInsert) {
                return stack;
            }
            return GenericConfigurableItemHandler.this.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!allowExtract) {
                return ItemStack.EMPTY;
            }
            // Only allow extracting from output slots for automation
            for (int outputSlot : outputSlots) {
                if (slot == outputSlot) {
                    return GenericConfigurableItemHandler.this.extractItem(slot, amount, simulate);
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return GenericConfigurableItemHandler.this.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return allowInsert && GenericConfigurableItemHandler.this.isItemValid(slot, stack);
        }
    }
}

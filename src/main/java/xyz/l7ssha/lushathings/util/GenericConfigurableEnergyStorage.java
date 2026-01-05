package xyz.l7ssha.lushathings.util;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class GenericConfigurableEnergyStorage extends EnergyStorage {
    private final Runnable onEnergyChanged;

    // 0 = none, 1 = input, 2 = output, 3 = both
    // Index corresponds to Direction.ordinal()
    private final int[] sideConfig = new int[]{0, 0, 0, 0, 0, 0};

    public GenericConfigurableEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable onEnergyChanged) {
        super(capacity, maxReceive, maxExtract);
        this.onEnergyChanged = onEnergyChanged;
    }

    public GenericConfigurableEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy, Runnable onEnergyChanged) {
        super(capacity, maxReceive, maxExtract, energy);
        this.onEnergyChanged = onEnergyChanged;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (received > 0 && !simulate && onEnergyChanged != null) {
            onEnergyChanged.run();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (extracted > 0 && !simulate && onEnergyChanged != null) {
            onEnergyChanged.run();
        }
        return extracted;
    }

    public IEnergyStorage getSideStorage(@Nullable Direction direction) {
        if (direction == null) {
            return this;
        }

        int config = sideConfig[direction.ordinal()];
        return switch (config) {
            case 1 -> new WrappedEnergyStorage(this, true, false); // Input
            case 2 -> new WrappedEnergyStorage(this, false, true); // Output
            case 3 -> this; // Both
            default -> new WrappedEnergyStorage(this, false, false); // None
        };
    }

    public void cycleSideConfig(Direction direction) {
        int index = direction.ordinal();
        sideConfig[index] = (sideConfig[index] + 1) % 4;
        if (onEnergyChanged != null) {
            onEnergyChanged.run();
        }
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
    public Tag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("Energy", super.serializeNBT(provider));
        tag.putIntArray("sideConfig", sideConfig);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, Tag nbt) {
        if (nbt instanceof CompoundTag compoundTag) {
            if (compoundTag.contains("Energy")) {
                super.deserializeNBT(provider, compoundTag.get("Energy"));
            }
            int[] loadedConfig = compoundTag.getIntArray("sideConfig");
            if (loadedConfig.length == 6) {
                System.arraycopy(loadedConfig, 0, this.sideConfig, 0, 6);
            }
        } else {
            super.deserializeNBT(provider, nbt);
        }
    }

    private static class WrappedEnergyStorage implements IEnergyStorage {
        private final IEnergyStorage parent;
        private final boolean canReceive;
        private final boolean canExtract;

        public WrappedEnergyStorage(IEnergyStorage parent, boolean canReceive, boolean canExtract) {
            this.parent = parent;
            this.canReceive = canReceive;
            this.canExtract = canExtract;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return canReceive ? parent.receiveEnergy(maxReceive, simulate) : 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return canExtract ? parent.extractEnergy(maxExtract, simulate) : 0;
        }

        @Override
        public int getEnergyStored() {
            return parent.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return parent.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return canExtract && parent.canExtract();
        }

        @Override
        public boolean canReceive() {
            return canReceive && parent.canReceive();
        }
    }
}

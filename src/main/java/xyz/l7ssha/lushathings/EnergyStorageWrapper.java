package xyz.l7ssha.lushathings;

import net.neoforged.neoforge.energy.EnergyStorage;

public class EnergyStorageWrapper extends EnergyStorage {
    private final Runnable onEnergyChanged;

    public EnergyStorageWrapper(int capacity, int maxTransfer, Runnable onEnergyChanged) {
        super(capacity, maxTransfer);

        this.onEnergyChanged = onEnergyChanged;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extractedEnergy = super.extractEnergy(maxExtract, simulate);
        if(extractedEnergy != 0) {
            onEnergyChanged.run();
        }

        return extractedEnergy;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int receiveEnergy = super.receiveEnergy(maxReceive, simulate);
        if(receiveEnergy != 0) {
            onEnergyChanged.run();
        }

        return receiveEnergy;
    }

    public int setEnergy(int energy) {
        this.energy = energy;

        return energy;
    }
}

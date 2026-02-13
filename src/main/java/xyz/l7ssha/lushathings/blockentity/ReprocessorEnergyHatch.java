package xyz.l7ssha.lushathings.blockentity;

public interface ReprocessorEnergyHatch {
  int extractEnergyInternal(int maxExtract, boolean simulate);

  int getEnergyStored();
}

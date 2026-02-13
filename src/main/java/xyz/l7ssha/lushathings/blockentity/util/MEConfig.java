package xyz.l7ssha.lushathings.blockentity.util;

import net.minecraft.nbt.CompoundTag;

public class MEConfig {
  private boolean provideBuiltinPatterns;
  private boolean allowNetworkPower;

  public MEConfig(boolean provideBuiltinPatterns, boolean allowNetworkPower) {
    this.provideBuiltinPatterns = provideBuiltinPatterns;
    this.allowNetworkPower = allowNetworkPower;
  }

  public MEConfig() {
    this.provideBuiltinPatterns = true;
    this.allowNetworkPower = false;
  }

  public boolean isProvidingBuiltinPatterns() {
    return provideBuiltinPatterns;
  }

  public void setProvidingBuiltinPatterns(boolean provideBuiltinPatterns) {
    this.provideBuiltinPatterns = provideBuiltinPatterns;
  }

  public boolean isAllowingNetworkPower() {
    return allowNetworkPower;
  }

  public void setAllowingNetworkPower(boolean allowNetworkPower) {
    this.allowNetworkPower = allowNetworkPower;
  }

  public void deserializeNBT(CompoundTag tag) {
    this.provideBuiltinPatterns = tag.getBoolean("provideBuiltinPatterns");
    this.allowNetworkPower = tag.getBoolean("allowNetworkPower");
  }

  public CompoundTag serializeNBT() {
    var tag = new CompoundTag();

    tag.putBoolean("provideBuiltinPatterns", provideBuiltinPatterns);
    tag.putBoolean("allowNetworkPower", allowNetworkPower);

    return tag;
  }
}

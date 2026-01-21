package xyz.l7ssha.lushathings.blockentity.util;

import net.minecraft.nbt.CompoundTag;

public class InventoryConfig {
    private boolean autoPull;
    private boolean autoPush;

    public InventoryConfig(boolean autoPull, boolean autoPush) {
        this.autoPull = autoPull;
        this.autoPush = autoPush;
    }

    public InventoryConfig() {
        this.autoPull = false;
        this.autoPush = false;
    }

    public boolean isAutoPull() {
        return autoPull;
    }

    public void setAutoPull(boolean autoPull) {
        this.autoPull = autoPull;
    }

    public boolean isAutoPush() {
        return autoPush;
    }

    public void setAutoPush(boolean autoPush) {
        this.autoPush = autoPush;
    }

    public void deserializeNBT(CompoundTag tag) {
        this.autoPull = tag.getBoolean("autoPull");
        this.autoPush = tag.getBoolean("autoPush");
    }

    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();

        tag.putBoolean("autoPull", autoPull);
        tag.putBoolean("autoPush", autoPush);

        return tag;
    }
}

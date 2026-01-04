package xyz.l7ssha.lushathings.screen.util;

import it.unimi.dsi.fastutil.ints.IntSet;

public final class QuickMoveConfig {
    private final IntSet inputSlots;
    private final IntSet outputSlots;

    public QuickMoveConfig(IntSet inputSlots, IntSet outputSlots) {
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
    }

    public boolean isInput(int slot) {
        return inputSlots.contains(slot);
    }

    public boolean isOutput(int slot) {
        return outputSlots.contains(slot);
    }

    public IntSet inputs() {
        return inputSlots;
    }
}

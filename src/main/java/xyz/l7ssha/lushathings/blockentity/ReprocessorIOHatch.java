package xyz.l7ssha.lushathings.blockentity;

import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public interface ReprocessorIOHatch {
    @Nullable
    ItemStackHandler getInputInventory();

    @Nullable
    ItemStackHandler getOutputInventory();
}

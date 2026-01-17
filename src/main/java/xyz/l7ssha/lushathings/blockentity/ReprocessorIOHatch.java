package xyz.l7ssha.lushathings.blockentity;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public interface ReprocessorIOHatch {
    @Nullable
    IItemHandlerModifiable getInputInventory();

    @Nullable
    IItemHandlerModifiable getOutputInventory();
}

package xyz.l7ssha.lushathings.blockentity;

import net.neoforged.neoforge.items.IItemHandlerModifiable;
import xyz.l7ssha.lushathings.blockentity.util.InventoryConfig;

import javax.annotation.Nullable;

public interface ReprocessorIOHatch {
    @Nullable
    IItemHandlerModifiable getInputInventory();

    @Nullable
    IItemHandlerModifiable getOutputInventory();

    @Nullable
    InventoryConfig getInventoryConfig();
}

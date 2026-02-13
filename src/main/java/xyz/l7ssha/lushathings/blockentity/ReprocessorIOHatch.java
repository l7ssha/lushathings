package xyz.l7ssha.lushathings.blockentity;

import javax.annotation.Nullable;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import xyz.l7ssha.lushathings.blockentity.util.InventoryConfig;

public interface ReprocessorIOHatch {
  @Nullable
  IItemHandlerModifiable getInputInventory();

  @Nullable
  IItemHandlerModifiable getOutputInventory();

  @Nullable
  InventoryConfig getInventoryConfig();
}

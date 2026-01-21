package xyz.l7ssha.lushathings.blockentity.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import xyz.l7ssha.lushathings.blockentity.ReprocessorIOHatch;

public class ReprocessorItemHandlerHelper {
    public static <T extends ReprocessorIOHatch> void pushToNeighbors(T blockEntity, IItemHandler neighbor) {
        var config = blockEntity.getInventoryConfig();
        if (config == null) {
            return;
        }

        if (!config.isAutoPush()) {
            return;
        }

        for (int slot = 0; slot < blockEntity.getOutputInventory().getSlots(); slot++) {
            ItemStack stack = blockEntity.getOutputInventory().extractItem(slot, 64, true);
            if (!stack.isEmpty()) {
                ItemStack remaining = ItemHandlerHelper.insertItemStacked(neighbor, stack, true);
                int toMove = stack.getCount() - remaining.getCount();

                if (toMove > 0) {
                    ItemStack extracted = blockEntity.getOutputInventory().extractItem(slot, toMove, false);
                    ItemHandlerHelper.insertItemStacked(neighbor, extracted, false);
                }
            }
        }
    }

    public static <T extends ReprocessorIOHatch> void pullFromNeighbors(T blockEntity, IItemHandler neighbor) {
        var config = blockEntity.getInventoryConfig();
        if (config == null) {
            return;
        }

        if (!config.isAutoPull()) {
            return;
        }

        for (int slot = 0; slot < neighbor.getSlots(); slot++) {
            ItemStack stack = neighbor.extractItem(slot, 64, true);
            if (!stack.isEmpty()) {
                ItemStack remaining = ItemHandlerHelper.insertItemStacked(blockEntity.getInputInventory(), stack, true);
                int toMove = stack.getCount() - remaining.getCount();

                if (toMove > 0) {
                    ItemStack extracted = neighbor.extractItem(slot, toMove, false);
                    ItemHandlerHelper.insertItemStacked(blockEntity.getInputInventory(), extracted, false);
                }
            }
        }
    }
}

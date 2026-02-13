package xyz.l7ssha.lushathings.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import xyz.l7ssha.lushathings.blockentity.ReprocessorEnergyInputBlockEntity;
import xyz.l7ssha.lushathings.lushathings;

public class ReprocessorEnergyInputMenu extends AbstractContainerMenu {
  public final ReprocessorEnergyInputBlockEntity blockEntity;

  public ReprocessorEnergyInputMenu(
      int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
    this(
        containerId,
        playerInventory,
        (ReprocessorEnergyInputBlockEntity)
            playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
  }

  public ReprocessorEnergyInputMenu(
      int containerId, Inventory playerInventory, ReprocessorEnergyInputBlockEntity blockEntity) {
    super(lushathings.REPROCESSOR_ENERGY_INPUT_MENU.get(), containerId);
    this.blockEntity = blockEntity;
  }

  public IEnergyStorage getEnergyStorage() {
    return blockEntity.getEnergyStorage();
  }

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    return ItemStack.EMPTY;
  }
}

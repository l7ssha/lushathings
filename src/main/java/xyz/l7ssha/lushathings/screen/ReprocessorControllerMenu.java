package xyz.l7ssha.lushathings.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.blockentity.ReprocessorControllerBlockEntity;
import xyz.l7ssha.lushathings.lushathings;

public class ReprocessorControllerMenu extends AbstractContainerMenu {
    public final ReprocessorControllerBlockEntity blockEntity;
    private final ContainerData data;

    public ReprocessorControllerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(buf.readBlockPos()), new SimpleContainerData(16));
    }

    public ReprocessorControllerMenu(int containerId, Inventory playerInventory, @Nullable BlockEntity blockEntity, ContainerData data) {
        super(lushathings.REPROCESSOR_CONTROLLER_MENU.get(), containerId);

        if (!(blockEntity instanceof ReprocessorControllerBlockEntity be)) {
            throw new IllegalStateException("Invalid block entity for controller menu");
        }

        this.blockEntity = be;
        this.data = data;

        this.addDataSlots(this.data);

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    public boolean isProcessing() {
        return data.get(0) > 0;
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getMaxProgress() {
        return data.get(1);
    }

    public int getStatus() {
        return data.get(2);
    }


    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), blockEntity.getBlockPos()), player, lushathings.REPROCESSOR_CONTROLLER_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = this.slots.get(index);
        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copy = sourceStack.copy();

        if (index < 27) {
            if (!this.moveItemStackTo(sourceStack, 27, 36, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(sourceStack, 0, 27, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(player, sourceStack);
        return copy;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}

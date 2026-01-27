package xyz.l7ssha.lushathings.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.blockentity.ReprocessorMEBlockEntity;
import xyz.l7ssha.lushathings.lushathings;

public class ReprocessorMEMenu extends AbstractContainerMenu {
    public final ReprocessorMEBlockEntity blockEntity;
    private final DataSlot providePatterns;
    private final DataSlot allowNetworkPower;

    public ReprocessorMEMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ReprocessorMEMenu(int containerId, Inventory playerInventory, @Nullable BlockEntity blockEntity) {
        super(lushathings.REPROCESSOR_ME_MENU.get(), containerId);

        if (!(blockEntity instanceof ReprocessorMEBlockEntity be)) {
            throw new IllegalStateException("Invalid block entity for ME menu");
        }

        this.blockEntity = be;

        this.providePatterns = new DataSlot() {
            @Override
            public int get() {
                return ReprocessorMEMenu.this.blockEntity.getMEConfig().isProvidingBuiltinPatterns() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                ReprocessorMEMenu.this.blockEntity.getMEConfig().setProvidingBuiltinPatterns(value != 0);
            }
        };

        this.allowNetworkPower = new DataSlot() {
            @Override
            public int get() {
                return ReprocessorMEMenu.this.blockEntity.getMEConfig().isAllowingNetworkPower() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                ReprocessorMEMenu.this.blockEntity.getMEConfig().setAllowingNetworkPower(value != 0);
            }
        };

        this.addDataSlot(this.providePatterns);
        this.addDataSlot(this.allowNetworkPower);

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    public boolean isProvidingBuiltinPatterns() {
        return this.providePatterns.get() != 0;
    }

    public boolean isAllowingNetworkPower() {
        return this.allowNetworkPower.get() != 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            blockEntity.setProvidingBuiltinPatterns(!blockEntity.getMEConfig().isProvidingBuiltinPatterns());
            blockEntity.setChanged();
            return true;
        } else if (id == 1) {
            blockEntity.setAllowingNetworkPower(!blockEntity.getMEConfig().isAllowingNetworkPower());
            blockEntity.setChanged();
            return true;
        }

        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), blockEntity.getBlockPos()),
                         player, lushathings.REPROCESSOR_ME_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = this.slots.get(index);
        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copy = sourceStack.copy();

        final int playerInvEnd = 27;
        final int hotbarEnd = 36;

        if (index < playerInvEnd) {
            // From player inventory to hotbar
            if (!this.moveItemStackTo(sourceStack, playerInvEnd, hotbarEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < hotbarEnd) {
            // From hotbar to player inventory
            if (!this.moveItemStackTo(sourceStack, 0, playerInvEnd, false)) {
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

package xyz.l7ssha.lushathings.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.blockentity.ReprocessorInputOutputBlockEntity;
import xyz.l7ssha.lushathings.lushathings;

public class ReprocessorInputOutputMenu extends AbstractContainerMenu {
    public final ReprocessorInputOutputBlockEntity blockEntity;
    private final IItemHandler inputHandler;
    private final IItemHandler outputHandler;
    private final DataSlot autoIOState;

    public ReprocessorInputOutputMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, (ReprocessorInputOutputBlockEntity) playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ReprocessorInputOutputMenu(int containerId, Inventory playerInventory, @Nullable ReprocessorInputOutputBlockEntity blockEntity) {
        super(lushathings.REPROCESSOR_INPUT_OUTPUT_MENU.get(), containerId);

        if (blockEntity == null) {
            throw new IllegalStateException("Missing block entity for hatch menu");
        }

        this.blockEntity = blockEntity;
        this.inputHandler = blockEntity.inputHandler;
        this.outputHandler = blockEntity.outputHandler;

        this.autoIOState = new DataSlot() {
            @Override
            public int get() {
                int val = 0;
                if (blockEntity.autoPull) val |= 1;
                if (blockEntity.autoPush) val |= 2;
                return val;
            }

            @Override
            public void set(int value) {
                blockEntity.autoPull = (value & 1) != 0;
                blockEntity.autoPush = (value & 2) != 0;
            }
        };
        this.addDataSlot(this.autoIOState);

        addHatchSlots();
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    public boolean isAutoPullEnabled() {
        return (this.autoIOState.get() & 1) != 0;
    }

    public boolean isAutoPushEnabled() {
        return (this.autoIOState.get() & 2) != 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) { // Toggle Pull
            blockEntity.autoPull = !blockEntity.autoPull;
            blockEntity.setChanged();
            return true;
        } else if (id == 1) { // Toggle Push
            blockEntity.autoPush = !blockEntity.autoPush;
            blockEntity.setChanged();
            return true;
        }
        return false;
    }

    private void addHatchSlots() {
        addGrid(inputHandler, 8, 17, true);
        addGrid(outputHandler, 63, 17, false);
    }

    private void addGrid(IItemHandler handler, int startX, int startY, boolean allowInsert) {
        int slot = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                final int slotIndex = slot++;
                this.addSlot(new SlotItemHandler(handler, slotIndex, startX + col * 18, startY + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        if (!allowInsert) {
                            return false;
                        }
                        return super.mayPlace(stack);
                    }
                });
            }
        }
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

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), blockEntity.getBlockPos()), player, player.level().getBlockState(blockEntity.getBlockPos()).getBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = this.slots.get(index);
        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copy = sourceStack.copy();

        final int inputSlotsStart = 0;
        final int inputSlotsEnd = 9;
        final int outputSlotsStart = 9;
        final int outputSlotsEnd = 18;
        final int playerInvStart = 18;
        final int hotbarEnd = playerInvStart + 36;

        if (index < outputSlotsEnd) { // From hatch (either input or output handler)
            if (!this.moveItemStackTo(sourceStack, playerInvStart, hotbarEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else { // From player
            if (!this.moveItemStackTo(sourceStack, inputSlotsStart, inputSlotsEnd, false)) {
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
}

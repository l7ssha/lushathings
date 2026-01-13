package xyz.l7ssha.lushathings.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.blockentity.ReprocessorInputBlockEntity;
import xyz.l7ssha.lushathings.blockentity.ReprocessorOutputBlockEntity;
import xyz.l7ssha.lushathings.lushathings;

/**
 * 9-slot hatch menu (3x3) for both input and output hatches.
 */
public class ReprocessorHatchMenu extends AbstractContainerMenu {
    public final BlockEntity blockEntity;
    private final IItemHandler hatchInventory;

    public ReprocessorHatchMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ReprocessorHatchMenu(int containerId, Inventory playerInventory, @Nullable BlockEntity blockEntity) {
        // NOTE: MenuType is provided by the factory registration; this ctor is used by both input/output menu types.
        super(lushathings.REPROCESSOR_HATCH_MENU.get(), containerId);

        if (blockEntity == null) {
            throw new IllegalStateException("Missing block entity for hatch menu");
        }

        this.blockEntity = blockEntity;
        this.hatchInventory = switch (blockEntity) {
            case ReprocessorInputBlockEntity be -> be.getItemHandler();
            case ReprocessorOutputBlockEntity be -> be.getItemHandler();
            default -> throw new IllegalStateException("Invalid block entity for hatch menu: " + blockEntity.getClass().getName());
        };

        addHatchSlots();
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    private void addHatchSlots() {
        // 3x3 at standard chest-ish positions
        int startX = 62;
        int startY = 17;
        int slot = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                final int slotIndex = slot++;
                this.addSlot(new SlotItemHandler(this.hatchInventory, slotIndex, startX + col * 18, startY + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        if (blockEntity instanceof ReprocessorOutputBlockEntity) {
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
        // Accept either hatch block
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

        final int hatchSlots = 9;
        final int playerInvStart = hatchSlots;
        final int playerInvEnd = playerInvStart + 27;
        final int hotbarStart = playerInvEnd;
        final int hotbarEnd = hotbarStart + 9;

        if (index < hatchSlots) {
            // hatch -> player
            if (!this.moveItemStackTo(sourceStack, playerInvStart, hotbarEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // player -> hatch
            if (this.blockEntity instanceof ReprocessorOutputBlockEntity) {
                return ItemStack.EMPTY;
            }
            if (!this.moveItemStackTo(sourceStack, 0, hatchSlots, false)) {
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

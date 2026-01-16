package xyz.l7ssha.lushathings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.screen.ReprocessorHatchMenu;

public class ReprocessorOutputBlockEntity extends BlockEntity implements MenuProvider, ReprocessorIOHatch {
    public boolean autoPush = false;

    public final ItemStackHandler itemHandler = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            // Disallow manual insertion into output hatch.
            return false;
        }
    };

    public ReprocessorOutputBlockEntity(BlockPos pos, BlockState blockState) {
        super(lushathings.REPROCESSOR_OUTPUT_BLOCK_ENTITY.get(), pos, blockState);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public @Nullable ItemStackHandler getInputInventory() {
        return null;
    }

    @Override
    public @Nullable ItemStackHandler getOutputInventory() {
        return itemHandler;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.lushathings.reprocessor_output_block");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ReprocessorHatchMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putBoolean("autoPush", autoPush);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.contains("autoPush")) {
            autoPush = tag.getBoolean("autoPush");
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ReprocessorOutputBlockEntity be) {
        if (level.isClientSide || !be.autoPush || level.getGameTime() % 20 != 0) {
            return;
        }

        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            IItemHandler neighbor = level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, dir.getOpposite());
            if (neighbor != null) {
                for (int slot = 0; slot < be.itemHandler.getSlots(); slot++) {
                    ItemStack stack = be.itemHandler.extractItem(slot, 64, true);
                    if (!stack.isEmpty()) {
                        ItemStack remaining = ItemHandlerHelper.insertItemStacked(neighbor, stack, true);
                        int toMove = stack.getCount() - remaining.getCount();

                        if (toMove > 0) {
                            ItemStack extracted = be.itemHandler.extractItem(slot, toMove, false);
                            ItemHandlerHelper.insertItemStacked(neighbor, extracted, false);
                        }
                    }
                }
            }
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}

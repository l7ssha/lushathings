package xyz.l7ssha.lushathings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.blockentity.util.InventoryConfig;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.screen.ReprocessorHatchMenu;

import static xyz.l7ssha.lushathings.blockentity.util.ReprocessorItemHandlerHelper.pushToNeighbors;

public class ReprocessorOutputBlockEntity extends BlockEntity implements MenuProvider, ReprocessorIOHatch {
    private final InventoryConfig inventoryConfig = new InventoryConfig();

    public final ItemStackHandler itemHandler = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
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
    public InventoryConfig getInventoryConfig() {
        return inventoryConfig;
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
        tag.put("inventoryConfig", inventoryConfig.serializeNBT());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        inventoryConfig.deserializeNBT(tag.getCompound("inventoryConfig"));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ReprocessorOutputBlockEntity be) {
        if (level.isClientSide || !be.inventoryConfig.isAutoPull() || level.getGameTime() % 20 != 0) {
            return;
        }

        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            IItemHandler neighbor = level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, dir.getOpposite());
            if (neighbor == null) {
                continue;
            }

            pushToNeighbors(be, neighbor);
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

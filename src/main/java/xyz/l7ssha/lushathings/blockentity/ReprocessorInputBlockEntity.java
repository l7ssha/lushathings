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
import xyz.l7ssha.lushathings.recipe.util.SizedIngredient;

public class ReprocessorInputBlockEntity extends BlockEntity implements MenuProvider, ReprocessorIOHatch {
    public boolean autoPull = false;

    public final ItemStackHandler itemHandler = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (level == null) {
                return true;
            }

            var recipes = level.getRecipeManager().getAllRecipesFor(lushathings.REPROCESSOR_RECIPE_TYPE.get());
            for (var holder : recipes) {
                for (SizedIngredient ingredient : holder.value().inputs()) {
                    if (ingredient.test(stack)) {
                        return true;
                    }
                }
            }

            return false;
        }
    };

    public ReprocessorInputBlockEntity(BlockPos pos, BlockState blockState) {
        super(lushathings.REPROCESSOR_INPUT_BLOCK_ENTITY.get(), pos, blockState);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public @Nullable ItemStackHandler getInputInventory() {
        return itemHandler;
    }

    @Override
    public @Nullable ItemStackHandler getOutputInventory() {
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.lushathings.reprocessor_input_block");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ReprocessorHatchMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putBoolean("autoPull", autoPull);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.contains("autoPull")) {
            autoPull = tag.getBoolean("autoPull");
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ReprocessorInputBlockEntity be) {
        if (level.isClientSide || !be.autoPull || level.getGameTime() % 20 != 0) {
            return;
        }

        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            IItemHandler neighbor = level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, dir.getOpposite());
            if (neighbor != null) {
                for (int slot = 0; slot < neighbor.getSlots(); slot++) {
                    ItemStack stack = neighbor.extractItem(slot, 64, true);
                    if (!stack.isEmpty()) {
                        ItemStack remaining = ItemHandlerHelper.insertItemStacked(be.itemHandler, stack, true);
                        int toMove = stack.getCount() - remaining.getCount();

                        if (toMove > 0) {
                            ItemStack extracted = neighbor.extractItem(slot, toMove, false);
                            ItemHandlerHelper.insertItemStacked(be.itemHandler, extracted, false);
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

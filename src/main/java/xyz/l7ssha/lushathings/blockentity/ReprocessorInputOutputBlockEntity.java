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
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.recipe.util.SizedIngredient;
import xyz.l7ssha.lushathings.screen.ReprocessorInputOutputMenu;

public class ReprocessorInputOutputBlockEntity extends BlockEntity implements MenuProvider, ReprocessorIOHatch {
    public boolean autoPull = false;
    public boolean autoPush = false;

    public final ItemStackHandler inputHandler = new ItemStackHandler(9) {
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

    public final ItemStackHandler outputHandler = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
             // Disallow manual insertion into output slots
            return false;
        }
    };

    public ReprocessorInputOutputBlockEntity(BlockPos pos, BlockState blockState) {
        super(lushathings.REPROCESSOR_INPUT_OUTPUT_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public @Nullable ItemStackHandler getInputInventory() {
        return inputHandler;
    }

    @Override
    public @Nullable ItemStackHandler getOutputInventory() {
        return outputHandler;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.lushathings.reprocessor_input_output_block");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ReprocessorInputOutputMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inputInventory", inputHandler.serializeNBT(registries));
        tag.put("outputInventory", outputHandler.serializeNBT(registries));
        tag.putBoolean("autoPull", autoPull);
        tag.putBoolean("autoPush", autoPush);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inputInventory")) {
            inputHandler.deserializeNBT(registries, tag.getCompound("inputInventory"));
        }
        if (tag.contains("outputInventory")) {
            outputHandler.deserializeNBT(registries, tag.getCompound("outputInventory"));
        }
        if (tag.contains("autoPull")) {
            autoPull = tag.getBoolean("autoPull");
        }
        if (tag.contains("autoPush")) {
            autoPush = tag.getBoolean("autoPush");
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ReprocessorInputOutputBlockEntity be) {
        if (level.isClientSide || level.getGameTime() % 20 != 0) {
            return;
        }

        // Logic from both input and output BEs
        if (be.autoPull || be.autoPush) {
             for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                IItemHandler neighbor = level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, dir.getOpposite());
                if (neighbor != null) {
                    // Pull logic
                    if (be.autoPull) {
                         for (int slot = 0; slot < neighbor.getSlots(); slot++) {
                            ItemStack stack = neighbor.extractItem(slot, 64, true);
                            if (!stack.isEmpty()) {
                                ItemStack remaining = ItemHandlerHelper.insertItemStacked(be.inputHandler, stack, true);
                                int toMove = stack.getCount() - remaining.getCount();

                                if (toMove > 0) {
                                    ItemStack extracted = neighbor.extractItem(slot, toMove, false);
                                    ItemHandlerHelper.insertItemStacked(be.inputHandler, extracted, false);
                                }
                            }
                        }
                    }

                    // Push logic
                    if (be.autoPush) {
                        for (int slot = 0; slot < be.outputHandler.getSlots(); slot++) {
                            ItemStack stack = be.outputHandler.extractItem(slot, 64, true);
                            if (!stack.isEmpty()) {
                                ItemStack remaining = ItemHandlerHelper.insertItemStacked(neighbor, stack, true);
                                int toMove = stack.getCount() - remaining.getCount();

                                if (toMove > 0) {
                                    ItemStack extracted = be.outputHandler.extractItem(slot, toMove, false);
                                    ItemHandlerHelper.insertItemStacked(neighbor, extracted, false);
                                }
                            }
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

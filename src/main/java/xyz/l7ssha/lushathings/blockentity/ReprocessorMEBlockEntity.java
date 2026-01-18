package xyz.l7ssha.lushathings.blockentity;

import appeng.api.networking.*;
import appeng.api.networking.security.IActionHost;
import appeng.block.IOwnerAwareBlockEntity;
import appeng.me.Grid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.recipe.util.SizedIngredient;

import java.util.EnumSet;

public class ReprocessorMEBlockEntity extends BlockEntity implements ReprocessorIOHatch, IActionHost, IOwnerAwareBlockEntity, IInWorldGridNodeHost, IGridNodeListener<ReprocessorMEBlockEntity> {
    protected final IManagedGridNode gridNode;

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
            return false;
        }
    };

    public ReprocessorMEBlockEntity(BlockPos pos, BlockState blockState) {
        super(lushathings.REPROCESSOR_ME_BLOCK_ENTITY.get(), pos, blockState);

        this.gridNode = GridHelper.createManagedNode(this, this)
                .setVisualRepresentation(lushathings.REPROCESSOR_ME_BLOCK.get())
                .setInWorldNode(true)
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setTagName("proxy")
                .setIdlePowerUsage(4)
                .setExposedOnSides(EnumSet.allOf(Direction.class));
    }

    public void onReady() {
        this.gridNode.create(this.level, getBlockPos());

        this.setChanged();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();

        GridHelper.onFirstTick(this, ReprocessorMEBlockEntity::onReady);
    }

    @Override
    public @Nullable IItemHandlerModifiable getInputInventory() {
        return this.inputHandler;
    }

    @Override
    public @Nullable IItemHandlerModifiable getOutputInventory() {
        return this.outputHandler;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();

        this.gridNode.destroy();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.put("inputInventory", inputHandler.serializeNBT(registries));
        tag.put("outputInventory", outputHandler.serializeNBT(registries));

        gridNode.saveToNBT(tag);
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

        gridNode.loadFromNBT(tag);

        GridHelper.onFirstTick(this, ReprocessorMEBlockEntity::onReady);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable IGridNode getGridNode(Direction dir) {
        return null;
    }

    @Override
    public @Nullable IGridNode getActionableNode() {
        return null;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        this.gridNode.destroy();
    }

    @Override
    public void setOwner(Player owner) {}

    @Override
    public void onSaveChanges(ReprocessorMEBlockEntity nodeOwner, IGridNode node) {
        if (this.level == null) {
            return;
        }

        if (this.level.isClientSide) {
            this.setChanged();
        } else {
            this.level.blockEntityChanged(this.worldPosition);
        }
    }
}

package xyz.l7ssha.lushathings.blockentity;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.*;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.util.AECableType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.recipe.ReprocessorRecipe;

import java.util.*;

public class ReprocessorMEBlockEntity extends BlockEntity implements ReprocessorIOHatch, IActionHost, IInWorldGridNodeHost, IGridNodeListener<ReprocessorMEBlockEntity>, ICraftingProvider {
    protected final IManagedGridNode gridNode;

    public final ItemStackHandler inputHandler = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
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
                .setExposedOnSides(EnumSet.allOf(Direction.class))
                .addService(ICraftingProvider.class, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ReprocessorMEBlockEntity blockEntity) {
        if (level.isClientSide || level.getGameTime() % 20 != 0) {
            return;
        }

        var gridNode = blockEntity.gridNode.getNode().getGrid();
        if (gridNode == null) {
            return;
        }

        var inventory = gridNode.getStorageService().getInventory();

        var actionSource = new ActionSource(blockEntity);

        for (int slot = 0; slot < blockEntity.outputHandler.getSlots(); slot++) {
            var stack = blockEntity.outputHandler.extractItem(slot, 64, true);
            if (stack.isEmpty()) {
                continue;
            }

            var inserted = inventory.insert(AEItemKey.of(stack), stack.getCount(), Actionable.SIMULATE, actionSource);

            if (inserted > 0) {
                ItemStack extracted = blockEntity.outputHandler.extractItem(slot, (int) inserted, false);
                inventory.insert(AEItemKey.of(extracted), extracted.getCount(), Actionable.MODULATE, actionSource);
            }
        }
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
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
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
        return this.gridNode.getNode();
    }

    @Override
    public @Nullable IGridNode getActionableNode() {
        return this.gridNode.getNode();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        this.gridNode.destroy();
    }

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

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        if (level == null) return null;

        var recipes = level.getRecipeManager().getAllRecipesFor(lushathings.REPROCESSOR_RECIPE_TYPE.get());

        return recipes.stream().map(recipe -> (IPatternDetails) new ReprocessorRecipeAe2Wrapper(recipe.value())).toList();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        var pushed = false;

        for (var input : patternDetails.getInputs()) {
            var stack = ((AEItemKey) input.getPossibleInputs()[0].what()).toStack((int) input.getPossibleInputs()[0].amount());

            ItemStack remaining = ItemHandlerHelper.insertItemStacked(inputHandler, stack, true);
            int toMove = stack.getCount() - remaining.getCount();

            if (toMove > 0) {
                ItemHandlerHelper.insertItemStacked(inputHandler, stack, false);
                pushed = true;
            }
        }

        return pushed;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public Set<AEKey> getEmitableItems() {
        return ICraftingProvider.super.getEmitableItems();
    }

    public static class ReprocessorRecipeAe2Wrapper implements IPatternDetails {
        private final ReprocessorRecipe reprocessorRecipe;

        public ReprocessorRecipeAe2Wrapper(ReprocessorRecipe reprocessorRecipe) {
            this.reprocessorRecipe = reprocessorRecipe;
        }

        @Override
        public AEItemKey getDefinition() {
            return AEItemKey.of(reprocessorRecipe.output());
        }

        @Override
        public IInput[] getInputs() {
            return reprocessorRecipe.inputs().stream()
                    .map(ing -> new Input(new GenericStack(AEItemKey.of(ing.ingredient().getItems()[0]), ing.count())))
                    .toArray(IInput[]::new);
        }

        @Override
        public List<GenericStack> getOutputs() {
            var list = new ArrayList<>(List.of(reprocessorRecipe.output()));

            if (!reprocessorRecipe.output2().isEmpty()) {
                list.add(reprocessorRecipe.output2());
            }

            return list.stream().map(item -> new GenericStack(AEItemKey.of(item), item.getCount())).toList();
        }
    }

    private static class Input implements IPatternDetails.IInput {
        private final GenericStack[] template;

        private Input(GenericStack stack) {
            this.template = new GenericStack[]{new GenericStack(stack.what(), stack.amount())};
        }

        @Override
        public GenericStack[] getPossibleInputs() {
            return template;
        }

        @Override
        public long getMultiplier() {
            return 1;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            return input.matches(template[0]);
        }

        @Nullable
        @Override
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }

    private static class ActionSource implements IActionSource {
        private final IActionHost actionHost;

        private ActionSource(IActionHost blockEntity) {
            this.actionHost = blockEntity;
        }

        @Override
        public Optional<Player> player() {
            return Optional.empty();
        }

        @Override
        public Optional<IActionHost> machine() {
            return Optional.of(actionHost);
        }

        @Override
        public <T> Optional<T> context(Class<T> key) {
            return Optional.empty();
        }
    }
}

package xyz.l7ssha.lushathings.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.blockentity.ReprocessorControllerBlockEntity;
import xyz.l7ssha.lushathings.blockentity.ReprocessorIOHatch;
import xyz.l7ssha.lushathings.blockentity.ReprocessorEnergyHatch;

import java.util.ArrayList;
import java.util.List;

public class ReprocessorControllerBlock extends BaseEntityBlock implements ReprocessorMultiblock {
    public static final MapCodec<ReprocessorControllerBlock> CODEC = simpleCodec(ReprocessorControllerBlock::new);

    public ReprocessorControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(MUTLIBLOCK_FORMED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MUTLIBLOCK_FORMED);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReprocessorControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // Tick only on the server.
        if (level.isClientSide) {
            return null;
        }

        return createTickerHelper(type,
                xyz.l7ssha.lushathings.lushathings.REPROCESSOR_CONTROLLER_BLOCK_ENTITY.get(),
                (lvl, pos, st, be) -> be.tick(lvl, pos, st));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // Multiblock formation tool.
        // NOTE: Return SUCCESS on *both* sides to prevent default interaction (GUI opening) while using the stick.
        if (player.getItemInHand(hand).getItem() == Items.STICK) {
            if (!level.isClientSide) {
                if (state.getValue(MUTLIBLOCK_FORMED)) {
                    player.displayClientMessage(Component.literal("Multiblock already formed"), true);
                    return ItemInteractionResult.SUCCESS;
                }

                BlockPos cubeCenter = pos.relative(hitResult.getDirection().getOpposite());

                if (isAreaValid(level, cubeCenter)) {
                    if (level.getBlockEntity(pos) instanceof ReprocessorControllerBlockEntity be) {
                        be.setCenterPos(cubeCenter);
                        formArea(level, cubeCenter, true, be);
                        player.displayClientMessage(Component.literal("Multiblock Formed!"), true);
                    }
                } else {
                    player.displayClientMessage(Component.literal("Structure incomplete!"), true);
                }
            }

            return ItemInteractionResult.SUCCESS;
        }

        // Controller GUI only when formed
        if (!level.isClientSide && state.getValue(MUTLIBLOCK_FORMED)) {
            if (level.getBlockEntity(pos) instanceof ReprocessorControllerBlockEntity be) {
                player.openMenu(be, pos);
            }
            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof ReprocessorControllerBlockEntity be) {
                BlockPos center = be.getCenterPos();
                if (center != null) {
                    formArea(level, center, false, be);
                }
            }

            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    private boolean isAreaValid(Level level, BlockPos center) {
        int bulkProcessingBlocks = 0;
        for (BlockPos target : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            var block = level.getBlockState(target).getBlock();
            if (!(block instanceof ReprocessorMultiblock)) {
                return false;
            }

            if (block instanceof ReprocessorBulkProcessingBlock) {
                bulkProcessingBlocks++;
                if (bulkProcessingBlocks > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public void formArea(Level level, BlockPos center, boolean formed, @Nullable ReprocessorControllerBlockEntity controllerBE) {
        List<BlockPos> inputHatches = new ArrayList<>();
        List<BlockPos> outputHatches = new ArrayList<>();
        List<BlockPos> energyInputs = new ArrayList<>();
        List<BlockPos> bulkProcessingBlocks = new ArrayList<>();

        for (BlockPos target : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            BlockState state = level.getBlockState(target);

            if (state.getBlock() instanceof ReprocessorMultiblock multiblock) {
                BlockState newState = state.setValue(MUTLIBLOCK_FORMED, formed);
                multiblock.manipulateMutliblock(level, newState, target.immutable(), formed);

                if (formed && controllerBE != null) {
                    BlockEntity be = level.getBlockEntity(target);
                    if (be instanceof ReprocessorIOHatch hatch) {
                        if (hatch.getInputInventory() != null) {
                            inputHatches.add(target.immutable());
                        }
                        if (hatch.getOutputInventory() != null) {
                            outputHatches.add(target.immutable());
                        }
                    }

                    if (be instanceof ReprocessorEnergyHatch) {
                        energyInputs.add(target.immutable());
                    }

                    if (state.getBlock() instanceof ReprocessorBulkProcessingBlock) {
                        bulkProcessingBlocks.add(target.immutable());
                    }
                }
            }
        }

        if (formed && controllerBE != null) {
            controllerBE.setInputHatches(inputHatches);
            controllerBE.setOutputHatches(outputHatches);
            controllerBE.setEnergyInputs(energyInputs);
            controllerBE.setBulkProcessingBlocks(bulkProcessingBlocks);
        }
    }
}

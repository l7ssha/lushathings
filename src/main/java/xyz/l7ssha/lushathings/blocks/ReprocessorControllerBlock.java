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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.blockentity.ReprocessorControllerBlockEntity;

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

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide || player.getItemInHand(hand).getItem() != Items.STICK) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (state.getValue(MUTLIBLOCK_FORMED)) {
            player.displayClientMessage(Component.literal("Multiblock already formed"), true);
            return ItemInteractionResult.SUCCESS;
        }

        BlockPos cubeCenter = pos.relative(hitResult.getDirection().getOpposite());

        if (isAreaValid(level, cubeCenter)) {
            if (level.getBlockEntity(pos) instanceof ReprocessorControllerBlockEntity be) {
                be.setCenterPos(cubeCenter);
                formArea(level, cubeCenter, true);
                player.displayClientMessage(Component.literal("Multiblock Formed!"), true);
            }
        } else {
            player.displayClientMessage(Component.literal("Structure incomplete!"), true);
        }

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof ReprocessorControllerBlockEntity be) {
                BlockPos center = be.getCenterPos();
                if (center != null) {
                    formArea(level, center, false);
                }
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    private boolean isAreaValid(Level level, BlockPos center) {
        for (BlockPos target : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            if (!(level.getBlockState(target).getBlock() instanceof ReprocessorMultiblock)) {
                return false;
            }
        }
        return true;
    }

    public void formArea(Level level, BlockPos center, boolean formed) {
        for (BlockPos target : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            BlockState state = level.getBlockState(target);

            if (state.getBlock() instanceof ReprocessorMultiblock multiblock) {
                BlockState newState = state.setValue(MUTLIBLOCK_FORMED, formed);
                multiblock.manipulateMutliblock(level, newState, target.immutable(), formed);
            }
        }
    }
}

package xyz.l7ssha.lushathings.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import xyz.l7ssha.lushathings.blockentity.ReprocessorControllerBlockEntity;

public class ReprocessorStructureBlock extends Block implements ReprocessorMultiblock {
    public static final MapCodec<ReprocessorStructureBlock> CODEC = simpleCodec(ReprocessorStructureBlock::new);

    public ReprocessorStructureBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MUTLIBLOCK_FORMED, false));
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (state.getValue(MUTLIBLOCK_FORMED)) {
                unformEntireMultiblock(level, pos);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    private void unformEntireMultiblock(Level level, BlockPos pos) {
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
            BlockState checkState = level.getBlockState(checkPos);

            if (checkState.getBlock() instanceof ReprocessorControllerBlock controller) {
                if (checkState.getValue(ReprocessorControllerBlock.MUTLIBLOCK_FORMED)) {
                    if (level.getBlockEntity(checkPos) instanceof ReprocessorControllerBlockEntity be) {
                        BlockPos center = be.getCenterPos();
                        if (center != null) {
                            controller.formArea(level, center, false);
                            be.setCenterPos(null);
                        }
                    }

                    break;
                }
            }
        }
    }

    @Override
    public void manipulateMutliblock(Level level, BlockState newState, BlockPos blockPos, boolean flag) {
        level.setBlockAndUpdate(blockPos, newState);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MUTLIBLOCK_FORMED);
    }
}

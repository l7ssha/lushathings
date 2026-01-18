package xyz.l7ssha.lushathings.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import xyz.l7ssha.lushathings.blockentity.ReprocessorControllerBlockEntity;

public interface ReprocessorMultiblock {
    BooleanProperty MUTLIBLOCK_FORMED = BooleanProperty.create("multiblock_formed");

    default void manipulateMutliblock(Level level, BlockState currentBlockState, BlockPos blockPos, boolean flag) {
        level.setBlockAndUpdate(blockPos, currentBlockState.setValue(MUTLIBLOCK_FORMED, flag));
    }

    default void unformEntireMultiblock(Level level, BlockPos pos) {
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
            BlockState checkState = level.getBlockState(checkPos);

            if (checkState.getBlock() instanceof ReprocessorControllerBlock controller) {
                if (checkState.getValue(ReprocessorControllerBlock.MUTLIBLOCK_FORMED)) {
                    if (level.getBlockEntity(checkPos) instanceof ReprocessorControllerBlockEntity be) {
                        BlockPos center = be.getCenterPos();
                        if (center != null) {
                            controller.formArea(level, center, false, be);
                            be.setCenterPos(null);
                        }
                    }

                    break;
                }
            }
        }
    }

}

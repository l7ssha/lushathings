package xyz.l7ssha.lushathings.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ReprocessorMultiblock {
    void manipulateMutliblock(Level level, BlockState currentBlockState, BlockPos blockPos, boolean flag);

    default void formMultiblock(Level level, BlockState currentBlockState, BlockPos blockPos) {
        manipulateMutliblock(level, currentBlockState, blockPos, true);
    }

    default void unformMultiblock(Level level, BlockState currentBlockState, BlockPos blockPos) {
        manipulateMutliblock(level, currentBlockState, blockPos, false);
    }
}

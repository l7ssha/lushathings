package xyz.l7ssha.lushathings.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public interface ReprocessorMultiblock {
    BooleanProperty MUTLIBLOCK_FORMED = BooleanProperty.create("multiblock_formed");

    default void manipulateMutliblock(Level level, BlockState currentBlockState, BlockPos blockPos, boolean flag) {
        level.setBlockAndUpdate(blockPos, currentBlockState.setValue(MUTLIBLOCK_FORMED, flag));
    }
}

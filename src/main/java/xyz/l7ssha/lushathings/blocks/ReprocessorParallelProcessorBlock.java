package xyz.l7ssha.lushathings.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class ReprocessorParallelProcessorBlock extends Block implements ReprocessorMultiblock {
  public static final MapCodec<ReprocessorParallelProcessorBlock> CODEC =
      simpleCodec(ReprocessorParallelProcessorBlock::new);

  public ReprocessorParallelProcessorBlock(Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(MUTLIBLOCK_FORMED, false));
  }

  @Override
  protected void onRemove(
      BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
    if (!state.is(newState.getBlock())) {
      if (state.getValue(MUTLIBLOCK_FORMED)) {
        unformEntireMultiblock(level, pos);
      }
      super.onRemove(state, level, pos, newState, movedByPiston);
    }
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

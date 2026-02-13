package xyz.l7ssha.lushathings.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.blockentity.ReprocessorCCAdapterBlockEntity;

public class ReprocessorCCAdapterBlock extends BaseEntityBlock implements ReprocessorMultiblock {
  public static final MapCodec<ReprocessorCCAdapterBlock> CODEC =
      simpleCodec(ReprocessorCCAdapterBlock::new);

  public ReprocessorCCAdapterBlock(Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(MUTLIBLOCK_FORMED, false));
  }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
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
    return new ReprocessorCCAdapterBlockEntity(pos, state);
  }
}

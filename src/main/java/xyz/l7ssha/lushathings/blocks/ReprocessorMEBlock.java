package xyz.l7ssha.lushathings.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import xyz.l7ssha.lushathings.blockentity.ReprocessorMEBlockEntity;
import xyz.l7ssha.lushathings.lushathings;

public class ReprocessorMEBlock extends BaseEntityBlock implements ReprocessorMultiblock {
  public static final MapCodec<ReprocessorMEBlock> CODEC = simpleCodec(ReprocessorMEBlock::new);

  public ReprocessorMEBlock(Properties properties) {
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
    return new ReprocessorMEBlockEntity(pos, state);
  }

  @Override
  protected ItemInteractionResult useItemOn(
      ItemStack stack,
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hitResult) {
    if (!state.getValue(MUTLIBLOCK_FORMED)) {
      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    if (!level.isClientSide) {
      if (level.getBlockEntity(pos) instanceof ReprocessorMEBlockEntity be) {
        player.openMenu(be, pos);
      }
    }

    return ItemInteractionResult.SUCCESS;
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> blockEntityType) {
    if (level.isClientSide()) {
      return null;
    }
    return createTickerHelper(
        blockEntityType,
        lushathings.REPROCESSOR_ME_BLOCK_ENTITY.get(),
        ReprocessorMEBlockEntity::tick);
  }
}

package xyz.l7ssha.lushathings.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Optional multiblock component.
 *
 * Constraint: at most 1 per formed Reprocessor multiblock.
 * Effect: 2x processing speed, 4x energy usage.
 */
public class ReprocessorBulkProcessingBlock extends Block implements ReprocessorMultiblock {
    public static final MapCodec<ReprocessorBulkProcessingBlock> CODEC = simpleCodec(ReprocessorBulkProcessingBlock::new);

    public ReprocessorBulkProcessingBlock(Properties properties) {
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

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MUTLIBLOCK_FORMED);
    }
}

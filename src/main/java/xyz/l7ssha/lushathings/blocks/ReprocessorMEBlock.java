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
import xyz.l7ssha.lushathings.blockentity.ReprocessorMEBlockEntity;

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
    public void manipulateMutliblock(Level level, BlockState newState, BlockPos blockPos, boolean flag) {
        level.setBlockAndUpdate(blockPos, newState);
    }
}

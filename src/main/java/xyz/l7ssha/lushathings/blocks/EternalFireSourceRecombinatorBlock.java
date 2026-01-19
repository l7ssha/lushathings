package xyz.l7ssha.lushathings.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.blockentity.EternalFireSourceRecombinatorBlockEntity;
import xyz.l7ssha.lushathings.lushathings;

public class EternalFireSourceRecombinatorBlock extends BaseEntityBlock {
    public static final MapCodec<EternalFireSourceRecombinatorBlock> CODEC = simpleCodec(EternalFireSourceRecombinatorBlock::new);

    public EternalFireSourceRecombinatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EternalFireSourceRecombinatorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }

        return createTickerHelper(
                blockEntityType,
                lushathings.ETERNAL_FIRE_SOURCE_RECOMBINATOR_BLOCK_ENTITY.get(),
                EternalFireSourceRecombinatorBlockEntity::tick
        );
    }
}

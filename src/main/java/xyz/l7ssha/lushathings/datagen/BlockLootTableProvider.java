package xyz.l7ssha.lushathings.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import xyz.l7ssha.lushathings.lushathings;

import java.util.Set;

public class BlockLootTableProvider extends BlockLootSubProvider {
    public BlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return lushathings.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }

    @Override
    protected void generate() {
        dropSelf(lushathings.REPROCESSOR_CONTROLLER_BLOCK.get());
        dropSelf(lushathings.REPROCESSOR_ENERGY_INPUT_BLOCK.get());
        dropSelf(lushathings.REPROCESSOR_INPUT_BLOCK.get());
        dropSelf(lushathings.REPROCESSOR_OUTPUT_BLOCK.get());
        dropSelf(lushathings.REPROCESSOR_INPUT_OUTPUT_BLOCK.get());
        dropSelf(lushathings.REPROCESSOR_ME_BLOCK.get());
        dropSelf(lushathings.REPROCESSOR_STRUCTURE_BLOCK.get());
        dropSelf(lushathings.ETERNAL_FIRE_SOURCE_RECOMBINATOR.get());
    }
}

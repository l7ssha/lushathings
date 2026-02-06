package xyz.l7ssha.lushathings.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.*;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import xyz.l7ssha.lushathings.blocks.EternalFireSourceRecombinatorBlock;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.blocks.ReprocessorMultiblock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, lushathings.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        setupFormedVariant(lushathings.REPROCESSOR_CONTROLLER_BLOCK);
        setupFormedVariant(lushathings.REPROCESSOR_STRUCTURE_BLOCK);
        setupFormedVariant(lushathings.REPROCESSOR_BULK_PROCESSING_BLOCK);
        setupFormedVariant(lushathings.REPROCESSOR_INPUT_BLOCK);
        setupFormedVariant(lushathings.REPROCESSOR_OUTPUT_BLOCK);
        setupFormedVariant(lushathings.REPROCESSOR_INPUT_OUTPUT_BLOCK);
        setupFormedVariant(lushathings.REPROCESSOR_ENERGY_INPUT_BLOCK);
        setupFormedVariant(lushathings.REPROCESSOR_ME_BLOCK);
        setupFormedVariant(lushathings.REPROCESSOR_CC_ADAPTER_BLOCK);

        setupEternalFireSourceRecombinator(lushathings.ETERNAL_FIRE_SOURCE_RECOMBINATOR);
        }

    private void setupFormedVariant(DeferredBlock<? extends Block> deferred) {
        if (deferred == null) {
            return;
        }

        Block block = deferred.get();
        String name = deferred.getId().getPath();

        // Some optional compat blocks may not have dedicated textures yet.
        // Fall back to the structure block textures to keep datagen runnable.
        boolean fallbackToStructureTexture = name.equals("reprocessor_cc_adapter_block");
        var offTexture = fallbackToStructureTexture ? modLoc("block/reprocessor_structure_block") : blockTexture(block);
        var onTexture = fallbackToStructureTexture ? modLoc("block/reprocessor_structure_block_formed") : modLoc("block/" + name + "_formed");

        ModelFile modelOff = models().cubeAll(name, offTexture);
        ModelFile modelOn = models().cubeAll(name + "_formed", onTexture);

        getVariantBuilder(block).partialState()
                .with(ReprocessorMultiblock.MUTLIBLOCK_FORMED, false)
                .modelForState().modelFile(modelOff).addModel()
                .partialState()
                .with(ReprocessorMultiblock.MUTLIBLOCK_FORMED, true)
                .modelForState().modelFile(modelOn).addModel();

        simpleBlockItem(block, modelOff);
    }

    private void setupEternalFireSourceRecombinator(DeferredBlock<? extends Block> deferred) {
        Block block = deferred.get();
        String name = deferred.getId().getPath();

        var modelOff = models().cubeBottomTop(
                name,
                modLoc("block/" + name + "_side"),
                modLoc("block/" + name + "_aux"),
                modLoc("block/" + name + "_aux")
        );

        var modelOn = models().cubeBottomTop(
                name + "_lit",
                modLoc("block/" + name + "_side_lit"),
                modLoc("block/" + name + "_aux"),
                modLoc("block/" + name + "_aux")
        );

        getVariantBuilder(block).partialState()
                .with(EternalFireSourceRecombinatorBlock.LIT, false)
                .modelForState().modelFile(modelOff).addModel()
                .partialState().with(EternalFireSourceRecombinatorBlock.LIT, true)
                .modelForState().modelFile(modelOn).addModel();

        simpleBlockItem(block, modelOff);
    }
}

package xyz.l7ssha.lushathings.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.*;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
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
    }

    private void setupFormedVariant(DeferredBlock<? extends Block> deferred) {
        Block block = deferred.get();
        String name = deferred.getId().getPath();

        ModelFile modelOff = models().cubeAll(name, blockTexture(block));
        ModelFile modelOn = models().cubeAll(name + "_formed", modLoc("block/" + name + "_formed"));

        getVariantBuilder(block).partialState()
                .with(ReprocessorMultiblock.MUTLIBLOCK_FORMED, false)
                .modelForState().modelFile(modelOff).addModel()
                .partialState()
                .with(ReprocessorMultiblock.MUTLIBLOCK_FORMED, true)
                .modelForState().modelFile(modelOn).addModel();

        simpleBlockItem(block, modelOff);
    }
}

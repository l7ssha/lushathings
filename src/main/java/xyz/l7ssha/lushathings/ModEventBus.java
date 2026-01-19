package xyz.l7ssha.lushathings;

import appeng.api.AECapabilities;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import xyz.l7ssha.lushathings.blockentity.EternalFireSourceRecombinatorBlockEntity;
import xyz.l7ssha.lushathings.datagen.ModBlockStateProvider;
import xyz.l7ssha.lushathings.datagen.ModRecipeProvider;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = lushathings.MODID)
public class ModEventBus {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ModBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModRecipeProvider(packOutput, lookupProvider));
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, lushathings.REPROCESSOR_INPUT_BLOCK_ENTITY.get(), (be, context) -> be.getItemHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, lushathings.REPROCESSOR_OUTPUT_BLOCK_ENTITY.get(), (be, context) -> be.getItemHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, lushathings.REPROCESSOR_INPUT_OUTPUT_BLOCK_ENTITY.get(), (be, context) -> be.getCombinedInventory());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, lushathings.REPROCESSOR_ENERGY_INPUT_BLOCK_ENTITY.get(), (be, context) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, lushathings.ETERNAL_FIRE_SOURCE_RECOMBINATOR_BLOCK_ENTITY.get(), (be, context) -> ((EternalFireSourceRecombinatorBlockEntity) be).getEnergyStorage());

        if (ModList.get().isLoaded("ae2")) {
            event.registerBlockEntity(
                    AECapabilities.IN_WORLD_GRID_NODE_HOST,
                    lushathings.REPROCESSOR_ME_BLOCK_ENTITY.get(),
                    (be, context) -> be
            );
        }

        if (ModList.get().isLoaded("ars_nouveau")) {
            event.registerBlockEntity(
                    CapabilityRegistry.SOURCE_CAPABILITY,
                    lushathings.ETERNAL_FIRE_SOURCE_RECOMBINATOR_BLOCK_ENTITY.get(),
                    (be, context) -> be
            );
        }
    }
}

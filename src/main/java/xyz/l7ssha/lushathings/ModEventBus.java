package xyz.l7ssha.lushathings;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import xyz.l7ssha.lushathings.blockentity.ReprocessorBlockEntity;
import xyz.l7ssha.lushathings.datagen.ModBlockStateProvider;
import xyz.l7ssha.lushathings.datagen.ModRecipeProvider;

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
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, lushathings.REPROCESSOR_BLOCK_ENTITY.get(), ReprocessorBlockEntity::getEnergyStorage);

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, lushathings.REPROCESSOR_BLOCK_ENTITY.get(), ReprocessorBlockEntity::getInventoryStorage);
    }
}

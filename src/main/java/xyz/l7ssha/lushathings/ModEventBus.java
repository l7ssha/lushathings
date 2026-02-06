package xyz.l7ssha.lushathings;

import appeng.api.AECapabilities;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import xyz.l7ssha.lushathings.blockentity.EternalFireSourceRecombinatorBlockEntity;
import xyz.l7ssha.lushathings.datagen.BlockLootTableProvider;
import xyz.l7ssha.lushathings.datagen.ModBlockStateProvider;
import xyz.l7ssha.lushathings.datagen.ModRecipeProvider;

import java.util.Collections;
import java.util.List;
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

        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(), List.of(new LootTableProvider.SubProviderEntry(
                BlockLootTableProvider::new, LootContextParamSets.BLOCK)), lookupProvider));
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

        if (ModList.get().isLoaded("computercraft")) {
            event.registerBlockEntity(
                    PeripheralCapability.get(),
                    lushathings.REPROCESSOR_CC_ADAPTER_BLOCK_ENTITY.get(),
                    (be, context) -> be.getPeripheral()
            );
        }
    }
}

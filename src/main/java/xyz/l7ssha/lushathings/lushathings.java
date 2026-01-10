package xyz.l7ssha.lushathings;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.IContainerFactory;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import xyz.l7ssha.lushathings.blockentity.ReprocessorControllerBlockEntity;
import xyz.l7ssha.lushathings.blocks.ReprocessorControllerBlock;
import xyz.l7ssha.lushathings.blocks.ReprocessorStructureBlock;
import xyz.l7ssha.lushathings.recipe.ReprocessorRecipe;

import java.util.function.Supplier;

@Mod(lushathings.MODID)
public class lushathings {
    public static final String MODID = "lushathings";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, lushathings.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, lushathings.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ReprocessorRecipe>> REPROCESSOR_RECIPE_SERIALIZER =
            SERIALIZERS.register("reprocessor", ReprocessorRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<ReprocessorRecipe>> REPROCESSOR_RECIPE_TYPE =
            RECIPE_TYPES.register("reprocessor", () -> new RecipeType<ReprocessorRecipe>() {
                @Override
                public String toString() {
                    return "reprocessor";
                }
            });

//    public static final DeferredHolder<MenuType<?>, MenuType<ReprocessorMenu>> REPROCESSOR_MENU =
//            registerMenuType("reprocessor_menu", ReprocessorMenu::new);

    public static final DeferredBlock<Block> REPROCESSOR_CONTROLLER_BLOCK = registerBlock("reprocessor_controller_block", () -> new ReprocessorControllerBlock(BlockBehaviour.Properties.of()));
    public static final DeferredBlock<Block> REPROCESSOR_STRUCTURE_BLOCK = registerBlock("reprocessor_structure_block", () -> new ReprocessorStructureBlock(BlockBehaviour.Properties.of()));

    public static final Supplier<BlockEntityType<ReprocessorControllerBlockEntity>> REPROCESSOR_CONTROLLER_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "reprocessor_controller_block_entity",
            () -> BlockEntityType.Builder.of(ReprocessorControllerBlockEntity::new, REPROCESSOR_CONTROLLER_BLOCK.get()).build(null)
    );

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> blockSupplier) {
        DeferredBlock<T> registeredBlock = BLOCKS.register(name, blockSupplier);
        registerBlockItem(name, registeredBlock);

        return registeredBlock;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> registeredBlock) {
        ITEMS.register(name, () -> new BlockItem(registeredBlock.get(), new Item.Properties()));
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("lushathings_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.lushathings"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> REPROCESSOR_CONTROLLER_BLOCK.get().asItem().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(REPROCESSOR_CONTROLLER_BLOCK.get());
                output.accept(REPROCESSOR_STRUCTURE_BLOCK.get());
            }).build());

    public lushathings(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MENUS.register(modEventBus);
        SERIALIZERS.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
//        LOGGER.info("HELLO FROM COMMON SETUP");
//
//        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
//            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
//        }
//
//        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());
//
//        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}

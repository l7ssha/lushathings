package xyz.l7ssha.lushathings;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import xyz.l7ssha.lushathings.screen.ReprocessorScreen;

@Mod(value = lushathings.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = lushathings.MODID, value = Dist.CLIENT)
public class lushathingsClient {
    public lushathingsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(lushathings.REPROCESSOR_MENU.get(), ReprocessorScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
//        lushathings.LOGGER.info("HELLO FROM CLIENT SETUP");
//        lushathings.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}

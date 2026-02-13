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
import xyz.l7ssha.lushathings.screen.ReprocessorControllerScreen;
import xyz.l7ssha.lushathings.screen.ReprocessorEnergyInputScreen;
import xyz.l7ssha.lushathings.screen.ReprocessorHatchScreen;
import xyz.l7ssha.lushathings.screen.ReprocessorInputOutputScreen;
import xyz.l7ssha.lushathings.screen.ReprocessorMEScreen;

@Mod(value = lushathings.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = lushathings.MODID, value = Dist.CLIENT)
public class lushathingsClient {
  public lushathingsClient(ModContainer container) {
    container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
  }

  @SubscribeEvent
  static void registerScreens(RegisterMenuScreensEvent event) {
    event.register(lushathings.REPROCESSOR_HATCH_MENU.get(), ReprocessorHatchScreen::new);
    event.register(
        lushathings.REPROCESSOR_INPUT_OUTPUT_MENU.get(), ReprocessorInputOutputScreen::new);
    event.register(lushathings.REPROCESSOR_CONTROLLER_MENU.get(), ReprocessorControllerScreen::new);
    event.register(
        lushathings.REPROCESSOR_ENERGY_INPUT_MENU.get(), ReprocessorEnergyInputScreen::new);
    event.register(lushathings.REPROCESSOR_ME_MENU.get(), ReprocessorMEScreen::new);
  }

  @SubscribeEvent
  static void onClientSetup(FMLClientSetupEvent event) {
    // lushathings.LOGGER.info("HELLO FROM CLIENT SETUP");
    // lushathings.LOGGER.info("MINECRAFT NAME >> {}",
    // Minecraft.getInstance().getUser().getName());
  }
}

package xyz.l7ssha.lushathings.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.network.packet.CycleSideConfigPayload;
import xyz.l7ssha.lushathings.network.packet.ToggleAutoIOPayload;

@EventBusSubscriber(modid = lushathings.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModMessages {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                CycleSideConfigPayload.TYPE,
                CycleSideConfigPayload.STREAM_CODEC,
                CycleSideConfigPayload::handle
        );
        registrar.playToServer(
                ToggleAutoIOPayload.TYPE,
                ToggleAutoIOPayload.STREAM_CODEC,
                ToggleAutoIOPayload::handle
        );
    }
}

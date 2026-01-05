package xyz.l7ssha.lushathings.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.util.ISideConfigurable;
import xyz.l7ssha.lushathings.util.SideConfigType;

public record CycleSideConfigPayload(BlockPos pos, Direction direction, SideConfigType configType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CycleSideConfigPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(lushathings.MODID, "cycle_side_config"));

    public static final StreamCodec<ByteBuf, CycleSideConfigPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, CycleSideConfigPayload::pos,
            Direction.STREAM_CODEC, CycleSideConfigPayload::direction,
            SideConfigType.STREAM_CODEC, CycleSideConfigPayload::configType,
            CycleSideConfigPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CycleSideConfigPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(payload.pos) instanceof ISideConfigurable sideConfigurable) {
                sideConfigurable.cycleSideConfig(payload.configType, payload.direction);
            }
        });
    }
}

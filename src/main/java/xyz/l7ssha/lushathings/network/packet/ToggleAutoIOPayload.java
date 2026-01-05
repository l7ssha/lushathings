package xyz.l7ssha.lushathings.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xyz.l7ssha.lushathings.blockentity.ReprocessorBlockEntity;
import xyz.l7ssha.lushathings.lushathings;

public record ToggleAutoIOPayload(BlockPos pos, boolean isPull) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ToggleAutoIOPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(lushathings.MODID, "toggle_auto_io"));

    public static final StreamCodec<ByteBuf, ToggleAutoIOPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ToggleAutoIOPayload::pos,
            ByteBufCodecs.BOOL, ToggleAutoIOPayload::isPull,
            ToggleAutoIOPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleAutoIOPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(payload.pos) instanceof ReprocessorBlockEntity reprocessor) {
                if (payload.isPull) {
                    reprocessor.itemHandler.setAutoPull(!reprocessor.itemHandler.isAutoPull());
                } else {
                    reprocessor.itemHandler.setAutoPush(!reprocessor.itemHandler.isAutoPush());
                }
            }
        });
    }
}

package xyz.l7ssha.lushathings.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum SideConfigType implements StringRepresentable {
    ITEM("item", 0),
    ENERGY("energy", 1),
    FLUID("fluid", 2);

    private final String name;
    private final int id;

    SideConfigType(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
    
    public int getId() {
        return id;
    }

    public static final IntFunction<SideConfigType> BY_ID = id -> switch (id) {
        case 0 -> ITEM;
        case 1 -> ENERGY;
        case 2 -> FLUID;
        default -> ITEM;
    };
    
    public static final StreamCodec<ByteBuf, SideConfigType> STREAM_CODEC = StreamCodec.of(
            (buf, val) -> buf.writeInt(val.id),
            buf -> BY_ID.apply(buf.readInt())
    );
}

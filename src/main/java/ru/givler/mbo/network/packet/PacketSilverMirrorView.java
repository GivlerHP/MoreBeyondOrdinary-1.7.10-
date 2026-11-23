package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import ru.givler.mbo.network.EditorPacketAccess;
import ru.givler.mbo.mirror.SilverMirrorStreaming;

public final class PacketSilverMirrorView implements IMessage {
    private int dimension, x, y, z;
    private long streamId;

    public PacketSilverMirrorView() {}
    public PacketSilverMirrorView(int dimension, int x, int y, int z, long streamId) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.streamId = streamId;
    }

    @Override public void fromBytes(ByteBuf in) {
        dimension = in.readInt(); x = in.readInt(); y = in.readInt(); z = in.readInt(); streamId = in.readLong();
    }

    @Override public void toBytes(ByteBuf out) {
        out.writeInt(dimension); out.writeInt(x); out.writeInt(y); out.writeInt(z); out.writeLong(streamId);
    }

    public static final class Handler implements IMessageHandler<PacketSilverMirrorView,IMessage> {
        @Override public IMessage onMessage(PacketSilverMirrorView message, MessageContext context) {
            EditorPacketAccess.schedule(context, () -> SilverMirrorStreaming.request(context.getServerHandler().playerEntity,
                message.dimension,message.x,message.y,message.z,message.streamId));
            return null;
        }
    }
}

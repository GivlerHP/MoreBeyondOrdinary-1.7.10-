package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.givler.mbo.container.ContainerDoll;
import ru.givler.mbo.network.EditorPacketAccess;

public final class PacketDollPose implements IMessage {
    private int window, axis, value;

    public PacketDollPose() {}

    public PacketDollPose(int window, int axis, int value) {
        this.window = window;
        this.axis = axis;
        this.value = value;
    }

    @Override public void fromBytes(ByteBuf buffer) {
        window = buffer.readInt();
        axis = buffer.readUnsignedByte();
        value = buffer.readShort();
    }

    @Override public void toBytes(ByteBuf buffer) {
        buffer.writeInt(window);
        buffer.writeByte(axis);
        buffer.writeShort(value);
    }

    public static final class Handler implements IMessageHandler<PacketDollPose, IMessage> {
        @Override public IMessage onMessage(PacketDollPose message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().playerEntity;
            EditorPacketAccess.schedule(context, () -> {
                if (player.openContainer instanceof ContainerDoll && player.openContainer.windowId == message.window) {
                    ((ContainerDoll) player.openContainer).setPose(player, message.axis, message.value);
                }
            });
            return null;
        }
    }
}

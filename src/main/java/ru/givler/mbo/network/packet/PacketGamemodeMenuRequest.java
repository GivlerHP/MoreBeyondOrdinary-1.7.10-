package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import ru.givler.mbo.network.PacketManager;

public final class PacketGamemodeMenuRequest implements IMessage {
    @Override public void fromBytes(ByteBuf buffer) {}
    @Override public void toBytes(ByteBuf buffer) {}

    public static final class Handler implements IMessageHandler<PacketGamemodeMenuRequest, IMessage> {
        @Override public IMessage onMessage(PacketGamemodeMenuRequest message, MessageContext context) {
            boolean allowed = context.getServerHandler().playerEntity.canCommandSenderUseCommand(2, "gamemode");
            PacketManager.INSTANCE.sendTo(new PacketGamemodeMenuPermission(allowed),
                    context.getServerHandler().playerEntity);
            return null;
        }
    }
}

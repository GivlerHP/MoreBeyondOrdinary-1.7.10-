package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.givler.mbo.entity.EntityHangingAnchor;
import ru.givler.mbo.network.EditorPacketAccess;

public final class PacketHangingEscape implements IMessage {
    @Override public void fromBytes(ByteBuf bytes) {}
    @Override public void toBytes(ByteBuf bytes) {}
    public static final class Handler implements IMessageHandler<PacketHangingEscape, IMessage> {
        @Override public IMessage onMessage(PacketHangingEscape message, final MessageContext context) {
            EditorPacketAccess.schedule(context, new Runnable() {
                @Override public void run() {
                    EntityPlayerMP player = context.getServerHandler().playerEntity;
                    if (player.ridingEntity instanceof EntityHangingAnchor)
                        ((EntityHangingAnchor) player.ridingEntity).attemptEscape(player);
                }
            });
            return null;
        }
    }
}

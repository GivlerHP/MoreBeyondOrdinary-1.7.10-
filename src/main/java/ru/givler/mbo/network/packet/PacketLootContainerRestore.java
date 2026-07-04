package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.item.ItemBlockLootContainer;
import ru.givler.mbo.tileentity.TileEntityLootContainer;

public class PacketLootContainerRestore implements IMessage {
    private int x;
    private int y;
    private int z;

    public PacketLootContainerRestore() {}

    public static PacketLootContainerRestore at(int x, int y, int z) {
        PacketLootContainerRestore pkt = new PacketLootContainerRestore();
        pkt.x = x;
        pkt.y = y;
        pkt.z = z;
        return pkt;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public static class Handler implements IMessageHandler<PacketLootContainerRestore, IMessage> {
        @Override
        public IMessage onMessage(PacketLootContainerRestore message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (!ItemBlockLootContainer.isEditor(player)) {
                return null;
            }
            if (player == null || player.worldObj == null) return null;
            TileEntity tile = player.worldObj.getTileEntity(message.x, message.y, message.z);
            if (tile instanceof TileEntityLootContainer) {
                ((TileEntityLootContainer) tile).restoreNow();
            }
            return null;
        }
    }
}

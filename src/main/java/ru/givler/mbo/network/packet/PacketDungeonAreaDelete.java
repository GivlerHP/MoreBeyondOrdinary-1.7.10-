package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import ru.givler.mbo.dungeon.DungeonAreaRecord;
import ru.givler.mbo.dungeon.DungeonAreaSavedData;
import ru.givler.mbo.network.EditorPacketAccess;
import ru.givler.mbo.registry.ItemRegistry;

public final class PacketDungeonAreaDelete implements IMessage {
    private String areaId="";
    public PacketDungeonAreaDelete(){}
    public PacketDungeonAreaDelete(String areaId){this.areaId=areaId;}
    @Override public void fromBytes(ByteBuf buf){int n=buf.readUnsignedShort();areaId=buf.readBytes(n).toString(io.netty.util.CharsetUtil.UTF_8);}
    @Override public void toBytes(ByteBuf buf){byte[] v=areaId.getBytes(io.netty.util.CharsetUtil.UTF_8);buf.writeShort(v.length);buf.writeBytes(v);}
    public static final class Handler implements IMessageHandler<PacketDungeonAreaDelete,IMessage>{
        @Override public IMessage onMessage(final PacketDungeonAreaDelete message,MessageContext context){final EntityPlayerMP player=context.getServerHandler().playerEntity;EditorPacketAccess.schedule(context,new Runnable(){@Override public void run(){ItemStack held=EditorPacketAccess.heldEditor(player);if(held==null||held.getItem()!=ItemRegistry.DungeonEditor)return;DungeonAreaSavedData data=DungeonAreaSavedData.get(player.worldObj);DungeonAreaRecord area=data.byId(message.areaId);if(area!=null&&area.isNear(player,64D)){area.deleteBlocks(player.worldObj);data.remove(player.worldObj,area);player.addChatMessage(new ChatComponentTranslation("mbo.dungeon.area.deleted"));}}});return null;}
    }
}

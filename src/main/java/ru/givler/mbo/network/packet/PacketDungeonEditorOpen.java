package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import ru.givler.mbo.client.gui.GuiDungeonEditor;
import ru.givler.mbo.network.EditorPacketAccess;

public final class PacketDungeonEditorOpen implements IMessage {
    private String areaId="";private int type,restoreMode,restoreSeconds=10;
    public PacketDungeonEditorOpen(){}
    public PacketDungeonEditorOpen(String areaId,int type,int restoreMode,int restoreSeconds){this.areaId=areaId==null?"":areaId;this.type=type;this.restoreMode=restoreMode;this.restoreSeconds=restoreSeconds;}
    @Override public void fromBytes(ByteBuf buf) {int n=buf.readUnsignedShort();areaId=buf.readBytes(n).toString(io.netty.util.CharsetUtil.UTF_8);type=buf.readByte();restoreMode=buf.readByte();restoreSeconds=buf.readInt();}
    @Override public void toBytes(ByteBuf buf) {byte[] v=areaId.getBytes(io.netty.util.CharsetUtil.UTF_8);buf.writeShort(v.length);buf.writeBytes(v);buf.writeByte(type);buf.writeByte(restoreMode);buf.writeInt(restoreSeconds);}
    public static final class Handler implements IMessageHandler<PacketDungeonEditorOpen,IMessage> {
        @Override public IMessage onMessage(final PacketDungeonEditorOpen message,MessageContext context){EditorPacketAccess.scheduleClient(new Runnable(){@Override public void run(){Minecraft.getMinecraft().displayGuiScreen(new GuiDungeonEditor(message.areaId,message.type,message.restoreMode,message.restoreSeconds));}});return null;}
    }
}

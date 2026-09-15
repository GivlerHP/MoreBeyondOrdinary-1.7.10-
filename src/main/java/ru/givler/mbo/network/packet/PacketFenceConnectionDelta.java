package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.client.state.ClientFenceConnections;
import ru.givler.mbo.network.EditorPacketAccess;
import ru.givler.mbo.network.PacketManager;

public final class PacketFenceConnectionDelta implements IMessage {
  private int dimension, x, y, z; private byte mask;
  public PacketFenceConnectionDelta() {}
  private PacketFenceConnectionDelta(World w, int x, int y, int z, byte mask) {
    dimension=w.provider.dimensionId; this.x=x; this.y=y; this.z=z; this.mask=mask;
  }
  public static void broadcast(World w, int x, int y, int z, byte mask) {
    PacketManager.INSTANCE.sendToDimension(new PacketFenceConnectionDelta(w,x,y,z,mask), w.provider.dimensionId);
  }
  @Override public void fromBytes(ByteBuf b) { dimension=b.readInt(); x=b.readInt(); y=b.readUnsignedByte(); z=b.readInt(); mask=b.readByte(); }
  @Override public void toBytes(ByteBuf b) { b.writeInt(dimension); b.writeInt(x); b.writeByte(y); b.writeInt(z); b.writeByte(mask); }
  public static final class Handler implements IMessageHandler<PacketFenceConnectionDelta,IMessage> {
    @Override public IMessage onMessage(final PacketFenceConnectionDelta m, MessageContext c) {
      EditorPacketAccess.scheduleClient(new Runnable(){ public void run(){
        World w=MoreBeyondOrdinary.proxy.getClientWorld(); if(w==null||w.provider.dimensionId!=m.dimension)return;
        ClientFenceConnections.set(m.dimension,m.x,m.y,m.z,m.mask);
        w.markBlockRangeForRenderUpdate(m.x-1,m.y-1,m.z-1,m.x+1,m.y+1,m.z+1);
      }}); return null;
    }
  }
}

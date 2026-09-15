package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.network.*;
import ru.givler.mbo.client.state.ClientTrapdoorLatches;

public final class PacketTrapdoorLatchDelta implements IMessage {
  private int dimension, x, y, z; private boolean latched;
  public PacketTrapdoorLatchDelta() {}
  private PacketTrapdoorLatchDelta(World w,int x,int y,int z,boolean latched){dimension=w.provider.dimensionId;this.x=x;this.y=y;this.z=z;this.latched=latched;}
  public static void broadcast(World w,int x,int y,int z,boolean latched){PacketManager.INSTANCE.sendToDimension(new PacketTrapdoorLatchDelta(w,x,y,z,latched),w.provider.dimensionId);}
  @Override public void fromBytes(ByteBuf b){dimension=b.readInt();x=b.readInt();y=b.readUnsignedByte();z=b.readInt();latched=b.readBoolean();}
  @Override public void toBytes(ByteBuf b){b.writeInt(dimension);b.writeInt(x);b.writeByte(y);b.writeInt(z);b.writeBoolean(latched);}
  public static final class Handler implements IMessageHandler<PacketTrapdoorLatchDelta,IMessage>{
    @Override public IMessage onMessage(final PacketTrapdoorLatchDelta m,MessageContext c){EditorPacketAccess.scheduleClient(new Runnable(){public void run(){World w=MoreBeyondOrdinary.proxy.getClientWorld();if(w!=null&&w.provider.dimensionId==m.dimension)ClientTrapdoorLatches.set(m.dimension,m.x,m.y,m.z,m.latched);}});return null;}
  }
}

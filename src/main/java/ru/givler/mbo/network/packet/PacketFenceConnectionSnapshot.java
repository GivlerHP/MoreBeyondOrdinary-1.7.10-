package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import java.util.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.client.state.ClientFenceConnections;
import ru.givler.mbo.data.world.FenceConnectionData;
import ru.givler.mbo.network.*;

public final class PacketFenceConnectionSnapshot implements IMessage {
  private static final int MAX=1000000;
  private int dimension; private List<FenceConnectionData.Entry> entries=new ArrayList<FenceConnectionData.Entry>();
  public PacketFenceConnectionSnapshot() {}
  private PacketFenceConnectionSnapshot(World w){dimension=w.provider.dimensionId;entries=FenceConnectionData.get(w).all();}
  public static void send(EntityPlayerMP p){PacketManager.INSTANCE.sendTo(new PacketFenceConnectionSnapshot(p.worldObj),p);}
  @Override public void fromBytes(ByteBuf b){dimension=b.readInt();int n=b.readInt();if(n<0||n>MAX||b.readableBytes()<n*10)return;entries=new ArrayList<FenceConnectionData.Entry>(n);for(int i=0;i<n;i++)entries.add(new FenceConnectionData.Entry(b.readInt(),b.readUnsignedByte(),b.readInt(),b.readByte()));}
  @Override public void toBytes(ByteBuf b){b.writeInt(dimension);b.writeInt(entries.size());for(FenceConnectionData.Entry e:entries){b.writeInt(e.x);b.writeByte(e.y);b.writeInt(e.z);b.writeByte(e.mask);}}
  public static final class Handler implements IMessageHandler<PacketFenceConnectionSnapshot,IMessage>{
    @Override public IMessage onMessage(final PacketFenceConnectionSnapshot m,MessageContext c){EditorPacketAccess.scheduleClient(new Runnable(){public void run(){World w=MoreBeyondOrdinary.proxy.getClientWorld();if(w!=null&&w.provider.dimensionId==m.dimension){ClientFenceConnections.replace(m.dimension,m.entries);for(FenceConnectionData.Entry e:m.entries)if(w.blockExists(e.x,e.y,e.z))w.markBlockRangeForRenderUpdate(e.x-1,e.y-1,e.z-1,e.x+1,e.y+1,e.z+1);}}});return null;}
  }
}

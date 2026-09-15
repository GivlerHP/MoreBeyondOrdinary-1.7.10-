package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import java.util.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.network.*;
import ru.givler.mbo.client.state.ClientTrapdoorLatches;
import ru.givler.mbo.data.world.TrapdoorLatchData;

public final class PacketTrapdoorLatchSnapshot implements IMessage {
  private static final int MAX=1000000; private int dimension; private List<Long> positions=new ArrayList<Long>();
  public PacketTrapdoorLatchSnapshot(){}
  private PacketTrapdoorLatchSnapshot(World w){dimension=w.provider.dimensionId;positions=TrapdoorLatchData.get(w).all();}
  public static void send(EntityPlayerMP p){PacketManager.INSTANCE.sendTo(new PacketTrapdoorLatchSnapshot(p.worldObj),p);}
  @Override public void fromBytes(ByteBuf b){dimension=b.readInt();int n=b.readInt();if(n<0||n>MAX||b.readableBytes()<n*8)return;positions=new ArrayList<Long>(n);for(int i=0;i<n;i++)positions.add(Long.valueOf(b.readLong()));}
  @Override public void toBytes(ByteBuf b){b.writeInt(dimension);b.writeInt(positions.size());for(Long p:positions)b.writeLong(p.longValue());}
  public static final class Handler implements IMessageHandler<PacketTrapdoorLatchSnapshot,IMessage>{
    @Override public IMessage onMessage(final PacketTrapdoorLatchSnapshot m,MessageContext c){EditorPacketAccess.scheduleClient(new Runnable(){public void run(){World w=MoreBeyondOrdinary.proxy.getClientWorld();if(w!=null&&w.provider.dimensionId==m.dimension)ClientTrapdoorLatches.replace(m.dimension,m.positions);}});return null;}
  }
}

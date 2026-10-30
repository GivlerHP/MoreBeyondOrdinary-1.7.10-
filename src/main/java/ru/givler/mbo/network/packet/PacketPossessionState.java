package ru.givler.mbo.network.packet;
import cpw.mods.fml.common.network.simpleimpl.*;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.magic.possession.PossessionManager.Session;
import ru.givler.mbo.network.EditorPacketAccess;

public final class PacketPossessionState implements IMessage {
  public static final int ATTEMPT=0,START=1,UPDATE=2,STOP=3,REVEAL=4,HIDE=5;
  public int kind,id,dimension;public long token;public UUID owner;public String name;
  public double x,y,z;public float yaw,pitch,health,maxHealth;public int air;
  public PacketPossessionState(){}
  public PacketPossessionState(Session s,int k){kind=k;id=s.animal.getEntityId();dimension=s.animal.worldObj.provider.dimensionId;token=s.token;owner=s.player.getUniqueID();name=s.player.getCommandSenderName();x=s.animal.posX;y=s.animal.posY;z=s.animal.posZ;yaw=s.animal.rotationYaw;pitch=s.animal.rotationPitch;health=s.animal.getHealth();maxHealth=s.animal.getMaxHealth();air=s.animal.getAir();}
  public void toBytes(ByteBuf b){b.writeByte(kind);b.writeInt(id);b.writeInt(dimension);b.writeLong(token);b.writeLong(owner.getMostSignificantBits());b.writeLong(owner.getLeastSignificantBits());ByteBufUtils.writeUTF8String(b,name);b.writeDouble(x);b.writeDouble(y);b.writeDouble(z);b.writeFloat(yaw);b.writeFloat(pitch);b.writeFloat(health);b.writeFloat(maxHealth);b.writeInt(air);}
  public void fromBytes(ByteBuf b){kind=b.readUnsignedByte();id=b.readInt();dimension=b.readInt();token=b.readLong();owner=new UUID(b.readLong(),b.readLong());name=ByteBufUtils.readUTF8String(b);x=b.readDouble();y=b.readDouble();z=b.readDouble();yaw=b.readFloat();pitch=b.readFloat();health=b.readFloat();maxHealth=b.readFloat();air=b.readInt();}
  public static final class Handler implements IMessageHandler<PacketPossessionState,IMessage>{public IMessage onMessage(final PacketPossessionState p,MessageContext c){EditorPacketAccess.scheduleClient(()->MoreBeyondOrdinary.proxy.possessionState(p));return null;}}
}

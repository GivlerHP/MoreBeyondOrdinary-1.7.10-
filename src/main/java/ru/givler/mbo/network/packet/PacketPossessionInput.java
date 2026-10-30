package ru.givler.mbo.network.packet;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import ru.givler.mbo.magic.possession.PossessionManager;
import ru.givler.mbo.network.EditorPacketAccess;

public final class PacketPossessionInput implements IMessage {
  private long token;private float forward,strafe,yaw,pitch;private int flags;
  public PacketPossessionInput(){}
  public PacketPossessionInput(long t,float f,float s,float y,float p,int bits){token=t;forward=f;strafe=s;yaw=y;pitch=p;flags=bits;}
  public void fromBytes(ByteBuf b){token=b.readLong();forward=b.readFloat();strafe=b.readFloat();yaw=b.readFloat();pitch=b.readFloat();flags=b.readUnsignedByte();}
  public void toBytes(ByteBuf b){b.writeLong(token);b.writeFloat(forward);b.writeFloat(strafe);b.writeFloat(yaw);b.writeFloat(pitch);b.writeByte(flags);}
  public static final class Handler implements IMessageHandler<PacketPossessionInput,IMessage> {
    public IMessage onMessage(final PacketPossessionInput p,final MessageContext c){EditorPacketAccess.schedule(c,()->PossessionManager.input(c.getServerHandler().playerEntity,p.token,p.forward,p.strafe,p.yaw,p.pitch,p.flags));return null;}
  }
}

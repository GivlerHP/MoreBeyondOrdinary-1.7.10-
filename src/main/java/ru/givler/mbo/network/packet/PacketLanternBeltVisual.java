package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.*;
import cpw.mods.fml.relauncher.*;
import io.netty.buffer.ByteBuf;

public final class PacketLanternBeltVisual implements IMessage {
  private int entityId,state;
  public PacketLanternBeltVisual() {}
  public PacketLanternBeltVisual(int entityId,int state) {this.entityId=entityId;this.state=state;}
  @Override public void toBytes(ByteBuf out) {out.writeInt(entityId);out.writeByte(state);}
  @Override public void fromBytes(ByteBuf in) {entityId=in.readInt();state=Math.min(2,in.readUnsignedByte());}
  public static final class Handler implements IMessageHandler<PacketLanternBeltVisual,IMessage> {
    @Override public IMessage onMessage(PacketLanternBeltVisual message,MessageContext context) {Client.receive(message);return null;}
  }
  @SideOnly(Side.CLIENT) private static final class Client {
    static void receive(final PacketLanternBeltVisual message) {
      final net.minecraft.world.World world=net.minecraft.client.Minecraft.getMinecraft().theWorld;
      net.minecraft.client.Minecraft.getMinecraft().func_152344_a(new Runnable() {public void run() {
        if(world!=null && world==net.minecraft.client.Minecraft.getMinecraft().theWorld)
          ru.givler.mbo.client.render.light.PortableLights.beltState(world,message.entityId,message.state);
      }});
    }
  }
}

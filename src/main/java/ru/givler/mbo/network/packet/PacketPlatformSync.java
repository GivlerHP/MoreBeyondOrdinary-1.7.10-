package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;
import ru.givler.mbo.network.EditorPacketAccess;

public class PacketPlatformSync implements IMessage {
  private int entityId;
  private NBTTagCompound tag;

  public PacketPlatformSync() {}

  public PacketPlatformSync(EntityMovingPlatform platform) {
    entityId = platform.getEntityId();
    tag = platform.writePlatformTag();
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    entityId = buf.readInt();
    tag = ByteBufUtils.readTag(buf);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(entityId);
    ByteBufUtils.writeTag(buf, tag);
  }

  public static class Handler implements IMessageHandler<PacketPlatformSync, IMessage> {
    @Override
    public IMessage onMessage(final PacketPlatformSync message, MessageContext context) {
      EditorPacketAccess.scheduleClient(new Runnable(){@Override public void run(){apply(message);}});return null;
    }
    private void apply(PacketPlatformSync message){
      net.minecraft.world.World world = ru.givler.mbo.MoreBeyondOrdinary.proxy.getClientWorld();
      if (world == null) return;
      Entity entity = world.getEntityByID(message.entityId);
      if (entity instanceof EntityMovingPlatform && message.tag != null)
        ((EntityMovingPlatform) entity).readPlatformTag(message.tag);
    }
  }
}

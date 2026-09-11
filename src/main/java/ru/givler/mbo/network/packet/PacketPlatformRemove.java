package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.Entity;
import ru.givler.mbo.network.EditorPacketAccess;

public class PacketPlatformRemove implements IMessage {
  private String platformId = "";

  public PacketPlatformRemove() {}

  public PacketPlatformRemove(UUID platformId) {
    this.platformId = platformId.toString();
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    platformId = cpw.mods.fml.common.network.ByteBufUtils.readUTF8String(buf);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    cpw.mods.fml.common.network.ByteBufUtils.writeUTF8String(buf, platformId);
  }

  public static class Handler implements IMessageHandler<PacketPlatformRemove, IMessage> {
    @Override
    public IMessage onMessage(final PacketPlatformRemove message, MessageContext context) {
      EditorPacketAccess.scheduleClient(new Runnable(){@Override public void run(){apply(message);}});return null;
    }
    private void apply(PacketPlatformRemove message){
      net.minecraft.world.World world = ru.givler.mbo.MoreBeyondOrdinary.proxy.getClientWorld();
      if (world == null) return;
      UUID id;
      try {
        id = UUID.fromString(message.platformId);
      } catch (Exception ignored) {
        return;
      }
      for (Object object : new java.util.ArrayList(world.loadedEntityList))
        if (object instanceof ru.givler.mbo.movingplatform.EntityMovingPlatform
            && id.equals(
                ((ru.givler.mbo.movingplatform.EntityMovingPlatform) object).getPlatformId())) {
          Entity entity = (Entity) object;
          entity.setDead();
          world.removeEntity(entity);
        }
    }
  }
}

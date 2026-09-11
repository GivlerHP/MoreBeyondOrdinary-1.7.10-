package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.client.gui.GuiMovingPlatform;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;

public class PacketPlatformOpen implements IMessage {
  private int entityId;
  private NBTTagCompound data;

  public PacketPlatformOpen() {}

  public PacketPlatformOpen(EntityMovingPlatform platform) {
    entityId = platform.getEntityId();
    data = platform.writePlatformTag();
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    entityId = buf.readInt();
    data = ByteBufUtils.readTag(buf);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(entityId);
    ByteBufUtils.writeTag(buf, data);
  }

  public static class Handler implements IMessageHandler<PacketPlatformOpen, IMessage> {
    @Override
    public IMessage onMessage(PacketPlatformOpen message, MessageContext context) {
      net.minecraft.world.World world = MoreBeyondOrdinary.proxy.getClientWorld();
      if (world == null) return null;
      Entity found = world.getEntityByID(message.entityId);
      EntityMovingPlatform platform;
      if (found instanceof EntityMovingPlatform) platform = (EntityMovingPlatform) found;
      else {
        platform = new EntityMovingPlatform(world);
        platform.setEntityId(message.entityId);
      }
      if (message.data != null) platform.readPlatformTag(message.data);
      net.minecraft.client.Minecraft.getMinecraft()
          .displayGuiScreen(new GuiMovingPlatform(platform));
      return null;
    }
  }
}

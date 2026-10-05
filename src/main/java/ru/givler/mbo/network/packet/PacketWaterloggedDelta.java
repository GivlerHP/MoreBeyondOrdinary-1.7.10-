package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.network.EditorPacketAccess;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.waterlogging.ClientWaterloggedBlocks;

public final class PacketWaterloggedDelta implements IMessage {
  private int dimension, x, y, z;
  private boolean waterlogged;

  public PacketWaterloggedDelta() {}

  private PacketWaterloggedDelta(World world, int x, int y, int z, boolean waterlogged) {
    this.dimension = world.provider.dimensionId;
    this.x = x;
    this.y = y;
    this.z = z;
    this.waterlogged = waterlogged;
  }

  public static void broadcast(World world, int x, int y, int z, boolean waterlogged) {
    net.minecraft.world.WorldServer server=(net.minecraft.world.WorldServer)world;
    PacketWaterloggedDelta message=new PacketWaterloggedDelta(world,x,y,z,waterlogged);
    for (Object object:world.playerEntities) {
      net.minecraft.entity.player.EntityPlayerMP player=(net.minecraft.entity.player.EntityPlayerMP)object;
      if (server.getPlayerManager().isPlayerWatchingChunk(player,x >> 4,z >> 4))
        PacketManager.INSTANCE.sendTo(message,player);
    }
  }

  @Override
  public void fromBytes(ByteBuf buffer) {
    dimension = buffer.readInt();
    x = buffer.readInt();
    y = buffer.readShort();
    z = buffer.readInt();
    waterlogged = buffer.readBoolean();
  }

  @Override
  public void toBytes(ByteBuf buffer) {
    buffer.writeInt(dimension);
    buffer.writeInt(x);
    buffer.writeShort(y);
    buffer.writeInt(z);
    buffer.writeBoolean(waterlogged);
  }

  public static final class Handler implements IMessageHandler<PacketWaterloggedDelta, IMessage> {
    @Override
    public IMessage onMessage(final PacketWaterloggedDelta message, MessageContext context) {
      EditorPacketAccess.scheduleClient(
          new Runnable() {
            @Override
            public void run() {
              World world = MoreBeyondOrdinary.proxy.getClientWorld();
              if (world == null || world.provider.dimensionId != message.dimension) return;
              ClientWaterloggedBlocks.set(
                  message.dimension, message.x, message.y, message.z, message.waterlogged);
              world.markBlockRangeForRenderUpdate(
                  message.x - 1,
                  message.y - 1,
                  message.z - 1,
                  message.x + 1,
                  message.y + 1,
                  message.z + 1);
            }
          });
      return null;
    }
  }
}

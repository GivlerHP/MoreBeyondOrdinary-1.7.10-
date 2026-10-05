package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.network.EditorPacketAccess;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.waterlogging.ClientWaterloggedBlocks;
import ru.givler.mbo.waterlogging.WaterloggedWorldData;

public final class PacketWaterloggedSnapshot implements IMessage {
  private static final int MAX_POSITIONS = 1_000_000;
  private int dimension;
  private boolean chunkScoped;
  private int chunkX,chunkZ;
  private List<WaterloggedWorldData.Position> positions =
      new ArrayList<WaterloggedWorldData.Position>();

  public PacketWaterloggedSnapshot() {}

  private PacketWaterloggedSnapshot(EntityPlayerMP player) {
    dimension = player.worldObj.provider.dimensionId;
    positions = WaterloggedWorldData.get(player.worldObj).watchedBy(player);
  }

  public static void send(EntityPlayerMP player) {
    PacketManager.INSTANCE.sendTo(new PacketWaterloggedSnapshot(player), player);
  }

  public static void sendChunk(EntityPlayerMP player,int x,int z,boolean loaded) {
    PacketWaterloggedSnapshot message=new PacketWaterloggedSnapshot();
    message.dimension=player.worldObj.provider.dimensionId;
    message.chunkScoped=true; message.chunkX=x; message.chunkZ=z;
    if (loaded) message.positions=WaterloggedWorldData.get(player.worldObj).inChunk(x,z);
    PacketManager.INSTANCE.sendTo(message,player);
  }

  @Override
  public void fromBytes(ByteBuf buffer) {
    dimension = buffer.readInt();
    chunkScoped=buffer.readBoolean();
    if (chunkScoped) { chunkX=buffer.readInt(); chunkZ=buffer.readInt(); }
    int count = buffer.readInt();
    if (count < 0 || count > MAX_POSITIONS || buffer.readableBytes() < count * 10) return;
    positions = new ArrayList<WaterloggedWorldData.Position>(count);
    for (int i = 0; i < count; i++)
      positions.add(
          new WaterloggedWorldData.Position(
              buffer.readInt(), buffer.readShort(), buffer.readInt()));
  }

  @Override
  public void toBytes(ByteBuf buffer) {
    buffer.writeInt(dimension);
    buffer.writeBoolean(chunkScoped);
    if (chunkScoped) { buffer.writeInt(chunkX); buffer.writeInt(chunkZ); }
    buffer.writeInt(positions.size());
    for (WaterloggedWorldData.Position position : positions) {
      buffer.writeInt(position.x);
      buffer.writeShort(position.y);
      buffer.writeInt(position.z);
    }
  }

  public static final class Handler
      implements IMessageHandler<PacketWaterloggedSnapshot, IMessage> {
    @Override
    public IMessage onMessage(final PacketWaterloggedSnapshot message, MessageContext context) {
      EditorPacketAccess.scheduleClient(
          new Runnable() {
            @Override
            public void run() {
              World world = MoreBeyondOrdinary.proxy.getClientWorld();
              if (world != null && world.provider.dimensionId == message.dimension) {
                if (message.chunkScoped) {
                  ClientWaterloggedBlocks.replaceChunk(message.dimension,message.chunkX,message.chunkZ,message.positions);
                  world.markBlockRangeForRenderUpdate((message.chunkX << 4)-1,-64,(message.chunkZ << 4)-1,
                      (message.chunkX << 4)+16,world.getHeight()-1,(message.chunkZ << 4)+16);
                  return;
                }
                ClientWaterloggedBlocks.replace(message.dimension, message.positions);
                for (WaterloggedWorldData.Position position : message.positions) {
                  if (world.blockExists(position.x, position.y, position.z))
                    world.markBlockRangeForRenderUpdate(
                        position.x - 1,
                        position.y - 1,
                        position.z - 1,
                        position.x + 1,
                        position.y + 1,
                        position.z + 1);
                }
              }
            }
          });
      return null;
    }
  }
}

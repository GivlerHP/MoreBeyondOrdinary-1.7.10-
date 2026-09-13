package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.movingplatform.PlatformAccess;
import ru.givler.mbo.tileentity.TileEntityPlatformStation;
import ru.givler.mbo.network.EditorPacketAccess;

public final class PacketPlatformStationConfig implements IMessage {
  private int x, y, z, mode;
  private String platformId = "";

  public PacketPlatformStationConfig() {}

  public PacketPlatformStationConfig(int x, int y, int z, int mode, String platformId) {
    this.x = x; this.y = y; this.z = z; this.mode = mode;
    this.platformId = platformId == null ? "" : platformId;
  }

  public void fromBytes(ByteBuf buffer) {
    x = buffer.readInt(); y = buffer.readInt(); z = buffer.readInt(); mode = buffer.readByte();
    platformId = ByteBufUtils.readUTF8String(buffer);
  }

  public void toBytes(ByteBuf buffer) {
    buffer.writeInt(x); buffer.writeInt(y); buffer.writeInt(z); buffer.writeByte(mode);
    ByteBufUtils.writeUTF8String(buffer, platformId);
  }

  public static final class Handler implements IMessageHandler<PacketPlatformStationConfig, IMessage> {
    public IMessage onMessage(final PacketPlatformStationConfig message, MessageContext context) {
      final EntityPlayerMP player = context.getServerHandler().playerEntity;
      EditorPacketAccess.schedule(context, new Runnable() {
        public void run() {
          if (!PlatformAccess.canEdit(player)
              || player.getDistanceSq(message.x + 0.5D, message.y + 0.5D, message.z + 0.5D) > 64D) return;
          TileEntity tile = player.worldObj.getTileEntity(message.x, message.y, message.z);
          if (tile instanceof TileEntityPlatformStation) {
            java.util.UUID id;
            try { id = java.util.UUID.fromString(message.platformId); }
            catch (IllegalArgumentException invalid) { return; }
            TileEntityPlatformStation station = (TileEntityPlatformStation) tile;
            station.setPlatformId(id);
            station.setMode(message.mode);
          }
        }
      });
      return null;
    }
  }
}

package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.gui.MboGuiType;
import ru.givler.mbo.lockable.ILockableTile;
import ru.givler.mbo.lockable.LockableAccess;
import ru.givler.mbo.network.EditorPacketAccess;

public class PacketLockOpenConfig implements IMessage {
  private int x, y, z;

  public PacketLockOpenConfig() {}

  public PacketLockOpenConfig(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  @Override
  public void fromBytes(ByteBuf b) {
    x = b.readInt();
    y = b.readInt();
    z = b.readInt();
  }

  @Override
  public void toBytes(ByteBuf b) {
    b.writeInt(x);
    b.writeInt(y);
    b.writeInt(z);
  }

  public static class Handler implements IMessageHandler<PacketLockOpenConfig, IMessage> {
    @Override
    public IMessage onMessage(final PacketLockOpenConfig m, MessageContext ctx) {
      final EntityPlayerMP p = ctx.getServerHandler().playerEntity;EditorPacketAccess.schedule(ctx,new Runnable(){@Override public void run(){apply(m,p);}});return null;
    }
    private void apply(PacketLockOpenConfig m,EntityPlayerMP p){
      ILockableTile tile = LockableAccess.get(p.worldObj, m.x, m.y, m.z);
      if (tile != null
          && LockableAccess.isAdminKey(p)
          && p.getDistanceSq(m.x + .5, m.y + .5, m.z + .5) <= 64)
        p.openGui(
            MoreBeyondOrdinary.instance, MboGuiType.LOCK_CONFIG.id, p.worldObj, m.x, m.y, m.z);
    }
  }
}

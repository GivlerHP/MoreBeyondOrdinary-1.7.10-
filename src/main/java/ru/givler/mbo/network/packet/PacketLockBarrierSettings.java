package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.givler.mbo.lockable.*;
import ru.givler.mbo.tileentity.TileEntityLockableChest;
import ru.givler.mbo.network.EditorPacketAccess;

public class PacketLockBarrierSettings implements IMessage {
  private int x, y, z, difficulty, delay, radius;

  public PacketLockBarrierSettings() {}

  public PacketLockBarrierSettings(int x, int y, int z, int difficulty, int delay, int radius) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.difficulty = difficulty;
    this.delay = delay;
    this.radius = radius;
  }

  @Override
  public void fromBytes(ByteBuf b) {
    x = b.readInt();
    y = b.readInt();
    z = b.readInt();
    difficulty = b.readInt();
    delay = b.readInt();
    radius = b.readInt();
  }

  @Override
  public void toBytes(ByteBuf b) {
    b.writeInt(x);
    b.writeInt(y);
    b.writeInt(z);
    b.writeInt(difficulty);
    b.writeInt(delay);
    b.writeInt(radius);
  }

  public static class Handler implements IMessageHandler<PacketLockBarrierSettings, IMessage> {
    @Override
    public IMessage onMessage(final PacketLockBarrierSettings m, MessageContext ctx) {
      final EntityPlayerMP p = ctx.getServerHandler().playerEntity;EditorPacketAccess.schedule(ctx,new Runnable(){@Override public void run(){apply(m,p);}});return null;
    }
    private void apply(PacketLockBarrierSettings m,EntityPlayerMP p){
      ILockableTile tile = LockableAccess.get(p.worldObj, m.x, m.y, m.z);
      if (tile != null
          && LockableAccess.isAdminKey(p)
          && p.getDistanceSq(m.x + .5, m.y + .5, m.z + .5) <= 64) {
        LockDifficulty value = LockDifficulty.byOrdinal(m.difficulty);
        if (tile instanceof TileEntityLockableChest
            && !((TileEntityLockableChest) tile).canChangeDifficulty(value)) return;
        tile.getLockData().setDifficulty(value, p.worldObj.rand);
        tile.getLockData().setRelockDelaySec(Math.max(0, Math.min(86400, m.delay)));
        tile.getLockData().setPlayerRadius(m.radius);
        tile.asTileEntity().markDirty();
        p.worldObj.markBlockForUpdate(m.x, m.y, m.z);
      }
    }
  }
}

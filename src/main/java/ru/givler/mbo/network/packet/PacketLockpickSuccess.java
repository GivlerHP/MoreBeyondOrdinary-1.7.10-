package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.givler.mbo.lockable.*;

public class PacketLockpickSuccess implements IMessage {
    private int x,y,z;
    public PacketLockpickSuccess(){}
    public PacketLockpickSuccess(int x,int y,int z){this.x=x;this.y=y;this.z=z;}
    @Override public void fromBytes(ByteBuf b){x=b.readInt();y=b.readInt();z=b.readInt();}
    @Override public void toBytes(ByteBuf b){b.writeInt(x);b.writeInt(y);b.writeInt(z);}
    public static class Handler implements IMessageHandler<PacketLockpickSuccess,IMessage>{
        @Override public IMessage onMessage(PacketLockpickSuccess m,MessageContext ctx){
            EntityPlayerMP p=ctx.getServerHandler().playerEntity;
            ILockableTile tile=LockableAccess.get(p.worldObj,m.x,m.y,m.z);
            if(tile==null || !LockableAccess.hasLockpick(p) || !tile.getLockData().hasCompleted(p.getUniqueID()) || p.getDistanceSq(m.x+.5,m.y+.5,m.z+.5)>64) return null;
            tile.getLockData().clearAttempt(p.getUniqueID()); tile.getLockData().unlock(); tile.onUnlocked(p); tile.asTileEntity().markDirty();
            p.worldObj.playSoundEffect(m.x+.5,m.y+.5,m.z+.5,"mbo:lock_open",1F,1F);
            return null;
        }
    }
}

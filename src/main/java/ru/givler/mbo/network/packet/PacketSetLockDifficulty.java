package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.givler.mbo.lockable.*;
import ru.givler.mbo.tileentity.TileEntityLockableChest;

public class PacketSetLockDifficulty implements IMessage {
    private int x,y,z,difficulty;
    public PacketSetLockDifficulty(){}
    public PacketSetLockDifficulty(int x,int y,int z,int difficulty){this.x=x;this.y=y;this.z=z;this.difficulty=difficulty;}
    @Override public void fromBytes(ByteBuf b){x=b.readInt();y=b.readInt();z=b.readInt();difficulty=b.readInt();}
    @Override public void toBytes(ByteBuf b){b.writeInt(x);b.writeInt(y);b.writeInt(z);b.writeInt(difficulty);}
    public static class Handler implements IMessageHandler<PacketSetLockDifficulty,IMessage>{
        @Override public IMessage onMessage(PacketSetLockDifficulty m,MessageContext ctx){
            EntityPlayerMP p=ctx.getServerHandler().playerEntity;
            ILockableTile tile=LockableAccess.get(p.worldObj,m.x,m.y,m.z);
            if(tile==null || !LockableAccess.isAdminKey(p) || p.getDistanceSq(m.x+.5,m.y+.5,m.z+.5)>64) return null;
            LockDifficulty value=LockDifficulty.byOrdinal(m.difficulty);
            if(tile instanceof TileEntityLockableChest&&!((TileEntityLockableChest)tile).canChangeDifficulty(value))return null;
            tile.getLockData().setDifficulty(value,p.worldObj.rand);
            tile.asTileEntity().markDirty();
            p.worldObj.markBlockForUpdate(m.x,m.y,m.z);
            return null;
        }
    }
}

package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import ru.givler.mbo.lockable.LockableAccess;
import ru.givler.mbo.tileentity.TileEntityLockableChest;

public class PacketLockLootSettings implements IMessage {
    private int x,y,z;private NBTTagCompound tag;
    public PacketLockLootSettings(){}
    public PacketLockLootSettings(int x,int y,int z,NBTTagCompound tag){this.x=x;this.y=y;this.z=z;this.tag=tag;}
    @Override public void fromBytes(ByteBuf b){x=b.readInt();y=b.readInt();z=b.readInt();tag=ByteBufUtils.readTag(b);}
    @Override public void toBytes(ByteBuf b){b.writeInt(x);b.writeInt(y);b.writeInt(z);ByteBufUtils.writeTag(b,tag);}
    public static class Handler implements IMessageHandler<PacketLockLootSettings,IMessage>{@Override public IMessage onMessage(PacketLockLootSettings m,MessageContext ctx){EntityPlayerMP p=ctx.getServerHandler().playerEntity;Object tile=p.worldObj.getTileEntity(m.x,m.y,m.z);if(tile instanceof TileEntityLockableChest&&LockableAccess.isAdminKey(p)&&p.getDistanceSq(m.x+.5,m.y+.5,m.z+.5)<=64){((TileEntityLockableChest)tile).applyLootSettings(m.tag==null?new NBTTagCompound():m.tag);p.worldObj.markBlockForUpdate(m.x,m.y,m.z);}return null;}}
}

package ru.givler.mbo.network.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import ru.givler.mbo.lockable.LockDifficulty;
import ru.givler.mbo.lockable.LockableAccess;
import ru.givler.mbo.tileentity.TileEntityLockableChest;

public class PacketApplyLockTemplate implements IMessage {
    private int x,y,z; private NBTTagCompound template;
    public PacketApplyLockTemplate() {}
    public PacketApplyLockTemplate(int x,int y,int z,NBTTagCompound template){this.x=x;this.y=y;this.z=z;this.template=template;}
    @Override public void fromBytes(ByteBuf b){x=b.readInt();y=b.readInt();z=b.readInt();template=ByteBufUtils.readTag(b);}
    @Override public void toBytes(ByteBuf b){b.writeInt(x);b.writeInt(y);b.writeInt(z);ByteBufUtils.writeTag(b,template);}

    public static class Handler implements IMessageHandler<PacketApplyLockTemplate,IMessage>{
        @Override public IMessage onMessage(PacketApplyLockTemplate m,MessageContext ctx){
            EntityPlayerMP p=ctx.getServerHandler().playerEntity;
            Object value=p.worldObj.getTileEntity(m.x,m.y,m.z);
            if(!(value instanceof TileEntityLockableChest)||m.template==null||!LockableAccess.isAdminKey(p)
                    ||p.getDistanceSq(m.x+.5,m.y+.5,m.z+.5)>64)return null;
            TileEntityLockableChest chest=(TileEntityLockableChest)value;
            chest.getLockData().setDifficulty(LockDifficulty.byOrdinal(m.template.getInteger("Difficulty")),p.worldObj.rand);
            chest.getLockData().setRelockDelaySec(m.template.getInteger("RelockDelay"));
            chest.applyLootSettings(m.template.getCompoundTag("Settings"));
            for(int i=0;i<14;i++)chest.setLootTemplate(i,null);
            NBTTagList items=m.template.getTagList("Items",10);
            for(int i=0;i<items.tagCount();i++){
                NBTTagCompound item=items.getCompoundTagAt(i);int slot=item.getByte("Slot")&255;
                if(slot<14)chest.setLootTemplate(slot,ItemStack.loadItemStackFromNBT(item));
            }
            chest.markDirty();p.worldObj.markBlockForUpdate(m.x,m.y,m.z);return null;
        }
    }
}

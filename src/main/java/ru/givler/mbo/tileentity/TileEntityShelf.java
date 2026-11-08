package ru.givler.mbo.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

/** No ticking and no automation inventory: all transfers require a checked player interaction. */
public final class TileEntityShelf extends TileEntity {
    private final ItemStack[] stacks = new ItemStack[3];
    public ItemStack get(int slot) { return stacks[slot]; }
    public ItemStack swap(int slot, ItemStack incoming) {
        ItemStack old = stacks[slot]; stacks[slot] = incoming;
        if (!ItemStack.areItemStacksEqual(old, incoming)) {
            markDirty();
            if (worldObj != null && !worldObj.isRemote) {
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                worldObj.func_147453_f(xCoord, yCoord, zCoord, getBlockType());
                ru.givler.mbo.core.ObserverHooks.onBlockChanged(worldObj, xCoord, yCoord, zCoord);
            }
        }
        return old;
    }
    /** Original shelf output encodes occupied slots, not stack fullness. */
    public int comparatorSignal() {
        int signal = 0;
        for (int i = 0; i < 3; i++) if (stacks[i] != null && stacks[i].stackSize > 0) signal |= 1 << i;
        return signal;
    }
    @Override public boolean canUpdate() { return false; }
    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        for(int i=0;i<3;i++) {
            tag.removeTag("Item"+i);
            if(stacks[i]==null)continue;
            NBTTagCompound item=new NBTTagCompound(); stacks[i].writeToNBT(item); tag.setTag("Item"+i,item);
        }
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        for(int i=0;i<3;i++) stacks[i]=tag.hasKey("Item"+i,10)?ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Item"+i)):null;
    }
    @Override public Packet getDescriptionPacket() {
        NBTTagCompound tag=new NBTTagCompound();writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,0,tag);
    }
    @Override public void onDataPacket(NetworkManager manager,S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }
}

package ru.givler.mbo.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.lockable.ILockableTile;
import ru.givler.mbo.lockable.LockData;

public abstract class TileEntityLockableBarrier extends TileEntity implements ILockableTile {
    protected final LockData lock = new LockData();

    @Override public LockData getLockData() { return lock; }
    @Override public TileEntity asTileEntity() { return this; }

    @Override
    public void updateEntity() {
        if (worldObj != null && !worldObj.isRemote && lock.shouldRelock(this)) {
            closeBarrier();
            lock.lock(worldObj.rand);
            markDirty();
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public void onUnlocked(EntityPlayer player) {
        openBarrier(player);
        markDirty();
    }

    protected abstract void openBarrier(EntityPlayer player);
    protected abstract void closeBarrier();

    @Override public void writeToNBT(NBTTagCompound tag) { super.writeToNBT(tag); lock.writeToNBT(tag); }
    @Override public void readFromNBT(NBTTagCompound tag) { super.readFromNBT(tag); lock.readFromNBT(tag); }
    @Override public Packet getDescriptionPacket() { NBTTagCompound tag = new NBTTagCompound(); writeToNBT(tag); return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag); }
    @Override public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) { readFromNBT(packet.func_148857_g()); }
}

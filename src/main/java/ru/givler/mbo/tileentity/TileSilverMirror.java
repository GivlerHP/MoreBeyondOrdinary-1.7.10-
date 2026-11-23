package ru.givler.mbo.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public final class TileSilverMirror extends TileMirror {
    private boolean linked;
    private int linkX, linkY, linkZ;

    public int[] linkPosition() {
        return linked ? new int[]{linkX, linkY, linkZ} : null;
    }

    public TileSilverMirror destination() {
        if (!linked || !worldObj.blockExists(linkX, linkY, linkZ)) return null;
        TileEntity tile = worldObj.getTileEntity(linkX, linkY, linkZ);
        if (!(tile instanceof TileSilverMirror) || tile == this || tile.isInvalid()) return null;
        TileSilverMirror other = (TileSilverMirror) tile;
        return other.linked && other.linkX == xCoord && other.linkY == yCoord
            && other.linkZ == zCoord ? other : null;
    }

    public boolean available() {
        return !linked || worldObj.blockExists(linkX, linkY, linkZ) && destination() == null;
    }

    public boolean connect(TileSilverMirror other) {
        if (other == this || other.getWorldObj() != worldObj || worldObj.isRemote
                || !available() || !other.available()) return false;
        setDestination(other);
        other.setDestination(this);
        return true;
    }

    private void setDestination(TileSilverMirror other) {
        linked = true;
        linkX = other.xCoord;
        linkY = other.yCoord;
        linkZ = other.zCoord;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (linked) tag.setIntArray("SilverMirrorLink", new int[]{linkX, linkY, linkZ});
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        int[] link = tag.getIntArray("SilverMirrorLink");
        linked = link.length == 3;
        if (linked) {
            linkX = link[0];
            linkY = link[1];
            linkZ = link[2];
        }
    }

    @Override public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
    }

    @Override public void onDataPacket(NetworkManager manager, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }
}

package ru.givler.mbo.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import ru.givler.mbo.block.BlockModels;

public class TileEntityModelCollision extends TileEntity {
    private int ownerX;
    private int ownerY;
    private int ownerZ;
    private float[] bounds = {0, 0, 0, 1, 1, 1};

    public void configure(int x, int y, int z, float[] rotatedBounds) {
        ownerX = x;
        ownerY = y;
        ownerZ = z;
        bounds = rotatedBounds.clone();
        markDirty();
    }

    public float[] getBounds() { return bounds; }
    public int getOwnerX() { return ownerX; }
    public int getOwnerY() { return ownerY; }
    public int getOwnerZ() { return ownerZ; }

    public BlockModels getOwnerBlock() {
        if (worldObj == null) return null;
        return worldObj.getBlock(ownerX, ownerY, ownerZ) instanceof BlockModels
                ? (BlockModels) worldObj.getBlock(ownerX, ownerY, ownerZ) : null;
    }

    @Override
    public void updateEntity() {
        if (!worldObj.isRemote && ticksExisted() % 20 == 0 && getOwnerBlock() == null) {
            worldObj.setBlockToAir(xCoord, yCoord, zCoord);
        }
    }

    private int ticks;
    private int ticksExisted() { return ++ticks; }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("ownerX", ownerX);
        tag.setInteger("ownerY", ownerY);
        tag.setInteger("ownerZ", ownerZ);
        for (int i = 0; i < 6; i++) tag.setFloat("bound" + i, bounds[i]);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        ownerX = tag.getInteger("ownerX");
        ownerY = tag.getInteger("ownerY");
        ownerZ = tag.getInteger("ownerZ");
        for (int i = 0; i < 6; i++) bounds[i] = tag.getFloat("bound" + i);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager network, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
        if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
}

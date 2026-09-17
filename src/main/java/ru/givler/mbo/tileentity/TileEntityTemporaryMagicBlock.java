package ru.givler.mbo.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/** Shared lifetime component for temporary spell blocks. */
public final class TileEntityTemporaryMagicBlock extends TileEntity {
  private int age;
  private int lifetime = 400;

  public int age() {
    return age;
  }

  public int lifetime() {
    return lifetime;
  }

  public void setLifetime(int ticks) {
    lifetime = Math.max(1, ticks);
    markDirty();
    if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
  }

  @Override
  public void updateEntity() {
    age++;
    if (!worldObj.isRemote && age > lifetime) worldObj.setBlockToAir(xCoord, yCoord, zCoord);
  }

  @Override
  public void writeToNBT(NBTTagCompound tag) {
    super.writeToNBT(tag);
    tag.setInteger("Age", age);
    tag.setInteger("Lifetime", lifetime);
  }

  @Override
  public void readFromNBT(NBTTagCompound tag) {
    super.readFromNBT(tag);
    age = tag.getInteger("Age");
    lifetime = Math.max(1, tag.getInteger("Lifetime"));
  }

  @Override
  public Packet getDescriptionPacket() {
    NBTTagCompound tag = new NBTTagCompound();
    writeToNBT(tag);
    return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
  }

  @Override
  public void onDataPacket(NetworkManager manager, S35PacketUpdateTileEntity packet) {
    readFromNBT(packet.func_148857_g());
  }
}

package ru.givler.mbo.movingplatform;

import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityPlatformStation extends TileEntity {
  private UUID platformId;
  private boolean targetB, powered;
  private int dimension, aX, aY, aZ, bX, bY, bZ;
  private boolean hasEndpoints;

  public void readLink(NBTTagCompound tag) {
    platformId = parseUuid(tag.getString("PlatformId"));
    targetB = tag.getBoolean("TargetB");
    dimension = tag.getInteger("Dimension");
    hasEndpoints = tag.hasKey("AX") && tag.hasKey("BX");
    aX = tag.getInteger("AX");
    aY = tag.getInteger("AY");
    aZ = tag.getInteger("AZ");
    bX = tag.getInteger("BX");
    bY = tag.getInteger("BY");
    bZ = tag.getInteger("BZ");
    markDirty();
  }

  private static UUID parseUuid(String value) {
    if (value == null || value.isEmpty()) return null;
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException ignored) {
      return null;
    }
  }

  public void updatePower() {
    if (worldObj == null || worldObj.isRemote) return;
    boolean now = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
    if (now != powered && platformId != null) {
      EntityMovingPlatform platform = findPlatform();
      if (platform == null && hasEndpoints && worldObj.provider.dimensionId == dimension) {
        worldObj.getChunkFromBlockCoords(aX, aZ);
        worldObj.getChunkFromBlockCoords(bX, bZ);
        platform = findPlatform();
      }
      if (platform != null) platform.start(targetB, null);
    }
    powered = now;
    markDirty();
  }

  private EntityMovingPlatform findPlatform() {
    for (Object o : worldObj.loadedEntityList)
      if (o instanceof EntityMovingPlatform
          && platformId.equals(((EntityMovingPlatform) o).getPlatformId()))
        return (EntityMovingPlatform) o;
    return null;
  }

  @Override
  public void readFromNBT(NBTTagCompound tag) {
    super.readFromNBT(tag);
    readLink(tag);
    powered = tag.getBoolean("Powered");
  }

  @Override
  public void writeToNBT(NBTTagCompound tag) {
    super.writeToNBT(tag);
    if (platformId != null) tag.setString("PlatformId", platformId.toString());
    tag.setBoolean("TargetB", targetB);
    tag.setBoolean("Powered", powered);
    tag.setInteger("Dimension", dimension);
    if (hasEndpoints) {
      tag.setInteger("AX", aX);
      tag.setInteger("AY", aY);
      tag.setInteger("AZ", aZ);
      tag.setInteger("BX", bX);
      tag.setInteger("BY", bY);
      tag.setInteger("BZ", bZ);
    }
  }
}

package ru.givler.mbo.tileentity;

import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;

public class TileEntityPlatformStation extends TileEntity {
  public static final int SEND_A = 0, SEND_B = 1, POWERED_A = 2, POWERED_B = 3, MODE_COUNT = 4;
  private UUID platformId;
  private int mode;
  private boolean powered;
  private int dimension, aX, aY, aZ, bX, bY, bZ;
  private boolean hasEndpoints;

  public void readLink(NBTTagCompound tag) {
    platformId = parseUuid(tag.getString("PlatformId"));
    mode = tag.hasKey("Mode") ? clampMode(tag.getInteger("Mode")) : tag.getBoolean("TargetB") ? SEND_B : SEND_A;
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
      if (platform != null) platform.start(targetB(now), null);
    }
    powered = now;
    markDirty();
  }

  private boolean targetB(boolean hasSignal) {
    if (mode == SEND_B) return true;
    if (mode == POWERED_A) return !hasSignal;
    if (mode == POWERED_B) return hasSignal;
    return false;
  }

  public void setMode(int value) {
    mode = clampMode(value);
    powered = worldObj != null && worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
    EntityMovingPlatform platform = platformId == null ? null : findPlatform();
    if (platform != null) platform.start(targetB(powered), null);
    markDirty();
    if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
  }

  public int getMode() { return mode; }

  public String getPlatformIdText() { return platformId == null ? "-" : platformId.toString(); }

  public void setPlatformId(UUID value) {
    platformId = value;
    hasEndpoints = false;
    EntityMovingPlatform platform = value == null ? null : findPlatform();
    if (platform != null) {
      dimension = worldObj.provider.dimensionId;
      aX = platform.getEndpointAX(); aY = platform.getEndpointAY(); aZ = platform.getEndpointAZ();
      bX = platform.getEndpointBX(); bY = platform.getEndpointBY(); bZ = platform.getEndpointBZ();
      hasEndpoints = true;
    }
  }

  private static int clampMode(int value) { return value >= 0 && value < MODE_COUNT ? value : SEND_A; }

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
    tag.setInteger("Mode", mode);
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

  @Override
  public net.minecraft.network.Packet getDescriptionPacket() {
    NBTTagCompound tag = new NBTTagCompound();
    writeToNBT(tag);
    return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
  }

  @Override
  public void onDataPacket(
      net.minecraft.network.NetworkManager network,
      net.minecraft.network.play.server.S35PacketUpdateTileEntity packet) {
    readFromNBT(packet.func_148857_g());
  }
}

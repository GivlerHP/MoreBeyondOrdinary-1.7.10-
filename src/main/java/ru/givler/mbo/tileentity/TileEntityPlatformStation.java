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
  private boolean priority;
  private boolean pendingCommand;
  private boolean pendingTargetB;
  private EntityMovingPlatform cachedPlatform;
  private int dimension, aX, aY, aZ, bX, bY, bZ;
  private boolean hasEndpoints;

  public void readLink(NBTTagCompound tag) {
    platformId = parseUuid(tag.getString("PlatformId"));
    mode = tag.hasKey("Mode") ? clampMode(tag.getInteger("Mode")) : tag.getBoolean("TargetB") ? SEND_B : SEND_A;
    priority = tag.getBoolean("Priority");
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
    boolean now = hasControlSignal();
    boolean changed = now != powered;
    powered = now;
    if (changed) markDirty();
    controlPlatform(changed, false);
  }

  private boolean hasControlSignal() {
    return worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
  }

  @Override
  public void updateEntity() {
    if (worldObj != null && !worldObj.isRemote) updatePower();
  }

  private void controlPlatform(boolean signalChanged, boolean force) {
    if (platformId == null) return;
    boolean followsSignal = mode == POWERED_A || mode == POWERED_B;
    if (force || signalChanged && (followsSignal || powered)) {
      pendingCommand = true;
      pendingTargetB = targetB(powered);
      markDirty();
    }
    if (!pendingCommand) return;
    EntityMovingPlatform platform = findPlatform();
    if (platform == null && signalChanged && hasEndpoints
        && worldObj.provider.dimensionId == dimension) {
      worldObj.getChunkFromBlockCoords(aX, aZ);
      worldObj.getChunkFromBlockCoords(bX, bZ);
      platform = findPlatform();
    }
    if (platform == null) return;
    if (priority && (signalChanged || force)) {
      if (platform.redirect(pendingTargetB)) {
        pendingCommand = false;
        markDirty();
      }
    } else if (!platform.isMoving()) {
      boolean alreadyThere =
          pendingTargetB
              ? platform.getState() == EntityMovingPlatform.STOPPED_B
              : platform.getState() == EntityMovingPlatform.STOPPED_A;
      if (alreadyThere || platform.start(pendingTargetB, null)) {
        pendingCommand = false;
        markDirty();
      }
    }
  }

  private boolean targetB(boolean hasSignal) {
    if (mode == SEND_B) return true;
    if (mode == POWERED_A) return !hasSignal;
    if (mode == POWERED_B) return hasSignal;
    return false;
  }

  public void configure(int value, boolean priorityValue) {
    mode = clampMode(value);
    priority = priorityValue;
    powered = worldObj != null && hasControlSignal();
    controlPlatform(true, true);
    markDirty();
    if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
  }

  public int getMode() { return mode; }

  public boolean isPriority() { return priority; }

  public String getPlatformIdText() { return platformId == null ? "-" : platformId.toString(); }

  public void setPlatformId(UUID value) {
    platformId = value;
    cachedPlatform = null;
    pendingCommand = false;
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
    if (cachedPlatform != null && !cachedPlatform.isDead
        && cachedPlatform.worldObj == worldObj
        && platformId.equals(cachedPlatform.getPlatformId())) return cachedPlatform;
    for (Object o : worldObj.loadedEntityList)
      if (o instanceof EntityMovingPlatform
          && platformId.equals(((EntityMovingPlatform) o).getPlatformId())) {
        cachedPlatform = (EntityMovingPlatform) o;
        return cachedPlatform;
      }
    cachedPlatform = null;
    return null;
  }

  @Override
  public void readFromNBT(NBTTagCompound tag) {
    super.readFromNBT(tag);
    readLink(tag);
    powered = tag.getBoolean("Powered");
    priority = tag.getBoolean("Priority");
    pendingCommand = tag.getBoolean("PendingCommand");
    pendingTargetB = tag.getBoolean("PendingTargetB");
  }

  @Override
  public void writeToNBT(NBTTagCompound tag) {
    super.writeToNBT(tag);
    if (platformId != null) tag.setString("PlatformId", platformId.toString());
    tag.setInteger("Mode", mode);
    tag.setBoolean("Powered", powered);
    tag.setBoolean("Priority", priority);
    tag.setBoolean("PendingCommand", pendingCommand);
    tag.setBoolean("PendingTargetB", pendingTargetB);
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

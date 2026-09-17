package ru.givler.mbo.entity.magic;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** Short-lived visual entity representing one complete lightning arc. */
public final class EntityLightningArc extends Entity implements IEntityAdditionalSpawnData {
  private double startX, startY, startZ;
  private int textureIndex;
  private int lifetime = 3;

  public EntityLightningArc(World world) {
    super(world);
    ignoreFrustumCheck = true;
    textureIndex = rand.nextInt(16);
  }

  public EntityLightningArc(
      World world,
      double startX,
      double startY,
      double startZ,
      double endX,
      double endY,
      double endZ,
      int lifetime) {
    this(world);
    this.startX = startX;
    this.startY = startY;
    this.startZ = startZ;
    this.lifetime = lifetime;
    setPosition(endX, endY, endZ);
  }

  public double startX() {
    return startX;
  }

  public double startY() {
    return startY;
  }

  public double startZ() {
    return startZ;
  }

  public int textureIndex() {
    return textureIndex;
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (ticksExisted >= lifetime) setDead();
  }

  @Override
  protected void entityInit() {}

  @Override
  protected void readEntityFromNBT(NBTTagCompound tag) {}

  @Override
  protected void writeEntityToNBT(NBTTagCompound tag) {}

  @Override
  public void writeSpawnData(ByteBuf data) {
    data.writeDouble(startX);
    data.writeDouble(startY);
    data.writeDouble(startZ);
    data.writeByte(textureIndex);
    data.writeByte(lifetime);
  }

  @Override
  public void readSpawnData(ByteBuf data) {
    startX = data.readDouble();
    startY = data.readDouble();
    startZ = data.readDouble();
    textureIndex = data.readUnsignedByte() % 16;
    lifetime = data.readUnsignedByte();
  }
}

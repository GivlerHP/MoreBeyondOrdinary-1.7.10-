package ru.givler.mbo.entity.fauna;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** A transient riding link, never a separately saved animal. */
public final class EntityMBOCamelSeat extends Entity {
  private EntityMBOCamel camel;
  private int unresolvedTicks;

  public EntityMBOCamelSeat(World world) {
    super(world);
    setSize(0, 0);
    noClip = true;
    setInvisible(true);
  }

  public EntityMBOCamelSeat(World world, EntityMBOCamel animal) {
    this(world);
    camel = animal;
    dataWatcher.updateObject(17, Integer.valueOf(animal.getEntityId()));
    animal.setSecondSeat(this);
    animal.positionPassenger(this, false);
  }

  @Override
  protected void entityInit() {
    dataWatcher.addObject(17, Integer.valueOf(0));
  }

  @Override
  public void onUpdate() {
    if (camel == null) {
      Entity parent = worldObj.getEntityByID(dataWatcher.getWatchableObjectInt(17));
      if (parent instanceof EntityMBOCamel) {
        camel = (EntityMBOCamel) parent;
        camel.setSecondSeat(this);
      }
    }
    if (camel == null) {
      if (++unresolvedTicks > 100) setDead();
      return;
    }
    if (camel.isDead) {
      if (riddenByEntity != null) riddenByEntity.mountEntity(null);
      setDead();
      return;
    }
    if (riddenByEntity != null && riddenByEntity.isDead) riddenByEntity.mountEntity(null);
    camel.positionPassenger(this, false);
    motionX = camel.motionX;
    motionY = camel.motionY;
    motionZ = camel.motionZ;
    updateRiderPosition();
  }

  @Override
  public void updateRiderPosition() {
    if (riddenByEntity == null) return;
    if (camel != null) camel.positionPassenger(riddenByEntity, false);
    else super.updateRiderPosition();
  }

  public EntityMBOCamel getCamel() {
    return camel;
  }

  @Override
  public void setPositionAndRotation2(
      double x, double y, double z, float yaw, float pitch, int ticks) {
    if (camel == null) super.setPositionAndRotation2(x, y, z, yaw, pitch, ticks);
    else camel.positionPassenger(this, false);
  }

  @Override
  public double getMountedYOffset() {
    if (riddenByEntity instanceof EntityZombie || riddenByEntity instanceof EntitySkeleton)
      return -.35D - riddenByEntity.getYOffset();
    return 0D;
  }

  @Override
  public boolean canBeCollidedWith() {
    return false;
  }

  @Override
  public boolean writeToNBTOptional(NBTTagCompound tag) {
    return false;
  }

  @Override
  public boolean writeMountToNBT(NBTTagCompound tag) {
    return false;
  }

  @Override
  protected void readEntityFromNBT(NBTTagCompound tag) {
    setDead();
  }

  @Override
  protected void writeEntityToNBT(NBTTagCompound tag) {}
}

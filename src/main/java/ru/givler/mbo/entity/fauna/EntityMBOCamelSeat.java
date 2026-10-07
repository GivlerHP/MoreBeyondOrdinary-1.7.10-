package ru.givler.mbo.entity.fauna;

import net.minecraft.entity.Entity;
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
    setPosition(camel.posX, camel.posY, camel.posZ);
    updateRiderPosition();
  }

  @Override
  public void updateRiderPosition() {
    if (camel != null && riddenByEntity != null) camel.positionPassenger(riddenByEntity, false);
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

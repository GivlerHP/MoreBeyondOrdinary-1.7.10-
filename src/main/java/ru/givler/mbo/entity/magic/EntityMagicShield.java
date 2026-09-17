package ru.givler.mbo.entity.magic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/** A projectile-blocking plane which follows a channeling player. */
public final class EntityMagicShield extends Entity {
  public enum Kind {
    FORCE,
    SHADOW
  }

  private int staleTicks;

  public EntityMagicShield(World world) {
    super(world);
    noClip = true;
    setSize(1.2F, 1.4F);
  }

  public EntityMagicShield(World world, EntityPlayer owner) {
    this(world, owner, Kind.FORCE);
  }

  public EntityMagicShield(World world, EntityPlayer owner, Kind kind) {
    this(world);
    dataWatcher.updateObject(10, owner.getEntityId());
    dataWatcher.updateObject(11, (byte) kind.ordinal());
    follow(owner);
  }

  public Kind kind() {
    return Kind.values()[dataWatcher.getWatchableObjectByte(11)];
  }

  public void refresh() {
    staleTicks = 0;
  }

  public EntityPlayer owner() {
    Entity entity = worldObj.getEntityByID(dataWatcher.getWatchableObjectInt(10));
    return entity instanceof EntityPlayer ? (EntityPlayer) entity : null;
  }

  @Override
  protected void entityInit() {
    dataWatcher.addObject(10, -1);
    dataWatcher.addObject(11, (byte) 0);
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    EntityPlayer player = owner();
    if (player == null || player.isDead || (!worldObj.isRemote && ++staleTicks > 4)) {
      setDead();
      return;
    }
    follow(player);
  }

  private void follow(EntityPlayer player) {
    setPositionAndRotation(
        player.posX + player.getLookVec().xCoord * 0.3D,
        player.boundingBox.minY + 1 + player.getLookVec().yCoord * 0.3D,
        player.posZ + player.getLookVec().zCoord * 0.3D,
        player.rotationYawHead,
        player.rotationPitch);
  }

  @Override
  public boolean canBeCollidedWith() {
    return !isDead;
  }

  @Override
  public AxisAlignedBB getCollisionBox(Entity other) {
    return other.boundingBox;
  }

  @Override
  public boolean attackEntityFrom(DamageSource source, float amount) {
    if (source != null && source.getSourceOfDamage() instanceof IProjectile) {
      playSound("mbo:effect", 0.3F, 1.3F);
      source.getSourceOfDamage().setDead();
    }
    return false;
  }

  @Override
  public void setPositionAndRotation2(
      double x, double y, double z, float yaw, float pitch, int increments) {
    setPosition(x, y, z);
    setRotation(yaw, pitch);
  }

  @Override
  protected void writeEntityToNBT(NBTTagCompound tag) {
    tag.setInteger("Owner", dataWatcher.getWatchableObjectInt(10));
    tag.setByte("Kind", dataWatcher.getWatchableObjectByte(11));
  }

  @Override
  protected void readEntityFromNBT(NBTTagCompound tag) {
    dataWatcher.updateObject(10, tag.getInteger("Owner"));
    dataWatcher.updateObject(11, tag.getByte("Kind"));
  }
}

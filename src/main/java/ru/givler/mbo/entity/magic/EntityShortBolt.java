package ru.givler.mbo.entity.magic;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Shared flight model for short-lived, gravity-free elemental bolts. */
public abstract class EntityShortBolt extends EntityThrowable {
  protected float damageMultiplier = 1.0F;

  protected EntityShortBolt(World world) {
    super(world);
    setSize(0.2F, 0.2F);
  }

  protected EntityShortBolt(World world, EntityLivingBase caster, float damageMultiplier) {
    super(world, caster);
    setSize(0.2F, 0.2F);
    this.damageMultiplier = damageMultiplier;
    float yaw = caster.rotationYaw * (float) Math.PI / 180.0F;
    float pitch = caster.rotationPitch * (float) Math.PI / 180.0F;
    setThrowableHeading(
        -MathHelper.sin(yaw) * MathHelper.cos(pitch),
        -MathHelper.sin(pitch),
        MathHelper.cos(yaw) * MathHelper.cos(pitch),
        2.5F,
        0.0F);
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (ticksExisted > 8) setDead();
  }

  @Override
  protected float getGravityVelocity() {
    return 0.0F;
  }

  @Override
  public boolean canRenderOnFire() {
    return false;
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setFloat("DamageMultiplier", damageMultiplier);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    damageMultiplier = tag.hasKey("DamageMultiplier") ? tag.getFloat("DamageMultiplier") : 1.0F;
  }
}

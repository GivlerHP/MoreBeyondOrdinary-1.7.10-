package ru.givler.mbo.entity.magic;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;

/** Straight fireball shared by scrolls and Thaumcraft staves. */
public final class EntityFireOrb extends EntityThrowable implements IEntityAdditionalSpawnData {
  private float damageMultiplier = 1.0F;
  private float explosionStrength;

  public EntityFireOrb(World world) {
    super(world);
    setSize(0.35F, 0.35F);
  }

  public EntityFireOrb(World world, EntityLivingBase caster, float damage, float explosion) {
    super(world, caster);
    setSize(0.35F, 0.35F);
    damageMultiplier = damage;
    explosionStrength = explosion;
    setThrowableHeading(
        caster.getLookVec().xCoord,
        caster.getLookVec().yCoord,
        caster.getLookVec().zCoord,
        1.5F,
        0.0F);
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (worldObj.isRemote)
      for (int i = 0; i < 3; i++) {
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.MAGIC_FIRE,
            worldObj,
            posX + rand.nextDouble() * 0.25D - 0.125D,
            posY + rand.nextDouble() * 0.25D - 0.125D,
            posZ + rand.nextDouble() * 0.25D - 0.125D,
            0,
            0,
            0,
            new ParticleSettings(12, 1, 1, 1, 0.9F, false));
      }
    if (ticksExisted > 100) setDead();
  }

  @Override
  protected void onImpact(MovingObjectPosition hit) {
    if (worldObj.isRemote) {
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.VANILLA_LARGE_EXPLODE, worldObj, posX, posY, posZ, 0, 0, 0);
      return;
    }
    if (hit.entityHit != null) {
      hit.entityHit.attackEntityFrom(
          DamageSource.causeIndirectMagicDamage(this, getThrower()).setFireDamage().setProjectile(),
          5.0F * damageMultiplier);
      hit.entityHit.setFire(5);
    }
    if (explosionStrength > 0)
      worldObj.newExplosion(
          this,
          posX,
          posY,
          posZ,
          explosionStrength,
          true,
          worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"));
    else worldObj.playAuxSFX(1009, (int) posX, (int) posY, (int) posZ, 0);
    setDead();
  }

  @Override
  protected float getGravityVelocity() {
    return 0;
  }

  @Override
  public boolean canRenderOnFire() {
    return false;
  }

  @Override
  public void writeSpawnData(ByteBuf buffer) {
    buffer.writeFloat(damageMultiplier);
    buffer.writeFloat(explosionStrength);
  }

  @Override
  public void readSpawnData(ByteBuf buffer) {
    damageMultiplier = buffer.readFloat();
    explosionStrength = buffer.readFloat();
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setFloat("DamageMultiplier", damageMultiplier);
    tag.setFloat("ExplosionStrength", explosionStrength);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    damageMultiplier = tag.getFloat("DamageMultiplier");
    explosionStrength = tag.getFloat("ExplosionStrength");
  }
}

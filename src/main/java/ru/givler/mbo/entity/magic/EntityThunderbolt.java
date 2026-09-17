package ru.givler.mbo.entity.magic;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;

/** Short electric bolt: 3 damage and velocity-scaled knockback. */
public final class EntityThunderbolt extends EntityShortBolt {
  public EntityThunderbolt(World world) {
    super(world);
  }

  public EntityThunderbolt(World world, EntityLivingBase caster, float multiplier) {
    super(world, caster, multiplier);
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (worldObj.isRemote) {
      double x = posX + rand.nextDouble() * 0.2D - 0.1D;
      double y = posY + height * 0.5D + rand.nextDouble() * 0.2D - 0.1D;
      double z = posZ + rand.nextDouble() * 0.2D - 0.1D;
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.SPARK,
          worldObj,
          x,
          y,
          z,
          0,
          0,
          0,
          new ParticleSettings(3, 1, 1, 1, 1.4F, false));
      for (int i = 0; i < 4; i++)
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.VANILLA_SMOKE,
            worldObj,
            posX + rand.nextDouble() * 0.2D - 0.1D,
            posY + height * 0.5D + rand.nextDouble() * 0.2D - 0.1D,
            posZ + rand.nextDouble() * 0.2D - 0.1D,
            0,
            0,
            0);
    }
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
          DamageSource.causeIndirectMagicDamage(this, getThrower()).setProjectile(),
          3.0F * damageMultiplier);
      hit.entityHit.addVelocity(motionX * 0.2D, motionY * 0.2D, motionZ * 0.2D);
    }
    playSound("fireworks.largeBlast", 1.4F, 0.5F + rand.nextFloat() * 0.1F);
    setDead();
  }
}

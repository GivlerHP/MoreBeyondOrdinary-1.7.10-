package ru.givler.mbo.entity.magic;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;

/** Fast fire projectile: 5 damage, five seconds of fire and an eight-tick lifetime. */
public final class EntityFirebolt extends EntityShortBolt {
  public EntityFirebolt(World world) {
    super(world);
  }

  public EntityFirebolt(World world, EntityLivingBase caster, float multiplier) {
    super(world, caster, multiplier);
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (worldObj.isRemote) {
      for (int i = 0; i < 4; i++) {
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.MAGIC_FIRE,
            worldObj,
            posX + rand.nextDouble() * 0.2D - 0.1D,
            posY + height * 0.5D + rand.nextDouble() * 0.2D - 0.1D,
            posZ + rand.nextDouble() * 0.2D - 0.1D,
            0,
            0,
            0,
            new ParticleSettings(10, 1.0F, 1.0F, 1.0F, 0.75F, false));
      }
    }
  }

  @Override
  protected void onImpact(MovingObjectPosition hit) {
    if (worldObj.isRemote) {
      for (int i = 0; i < 8; i++) {
        worldObj.spawnParticle(
            "lava",
            posX + rand.nextDouble() - 0.5D,
            posY + height * 0.5D + rand.nextDouble() - 0.5D,
            posZ + rand.nextDouble() - 0.5D,
            0,
            0,
            0);
      }
      return;
    }
    if (hit.entityHit != null) {
      hit.entityHit.attackEntityFrom(
          DamageSource.causeIndirectMagicDamage(this, getThrower()).setFireDamage().setProjectile(),
          5.0F * damageMultiplier);
      hit.entityHit.setFire(5);
    }
    playSound("liquid.lavapop", 2.0F, 0.8F + rand.nextFloat() * 0.3F);
    setDead();
  }
}

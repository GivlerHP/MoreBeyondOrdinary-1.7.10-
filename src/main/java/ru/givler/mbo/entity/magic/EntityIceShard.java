package ru.givler.mbo.entity.magic;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;
import ru.givler.mbo.potion.SyncedPotionEffects;
import ru.givler.mbo.registry.PotionRegistry;

/** Arrow-like ice shard with gravity, deceleration, six damage and a ten-second slow. */
public final class EntityIceShard extends EntityThrowable {
  private float damageMultiplier = 1.0F;

  public EntityIceShard(World world) {
    super(world);
    setSize(0.25F, 0.25F);
  }

  public EntityIceShard(World world, EntityLivingBase caster, float speed, float multiplier) {
    super(world, caster);
    setSize(0.25F, 0.25F);
    damageMultiplier = multiplier;
    float yaw = caster.rotationYaw * (float) Math.PI / 180.0F;
    float pitch = caster.rotationPitch * (float) Math.PI / 180.0F;
    // The original arrow implementation multiplied the spell speed by 1.5 internally.
    setThrowableHeading(
        -MathHelper.sin(yaw) * MathHelper.cos(pitch),
        -MathHelper.sin(pitch),
        MathHelper.cos(yaw) * MathHelper.cos(pitch),
        speed * 1.5F,
        1.0F);
  }

  @Override
  protected void onImpact(MovingObjectPosition hit) {
    if (worldObj.isRemote && hit.entityHit == null) {
      for (int i = 0; i < 10; i++) {
        double x = posX - 0.25D + rand.nextDouble() * 0.5D;
        double y = posY - 0.25D + rand.nextDouble() * 0.5D;
        double z = posZ - 0.25D + rand.nextDouble() * 0.5D;
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.ICE,
            worldObj,
            x,
            y,
            z,
            x - posX,
            y - posY,
            z - posZ,
            new ParticleSettings(20 + rand.nextInt(10), 1, 1, 1, 0.75F, true));
      }
      return;
    } else if (worldObj.isRemote) {
      return;
    }
    if (hit.entityHit instanceof EntityLivingBase) {
      EntityLivingBase target = (EntityLivingBase) hit.entityHit;
      target.attackEntityFrom(
          DamageSource.causeIndirectMagicDamage(this, getThrower()).setProjectile(),
          6.0F * damageMultiplier);
      SyncedPotionEffects.apply(target, new PotionEffect(PotionRegistry.Frost.id, 200, 0, true));
      playSound("game.neutral.hurt", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
    } else {
      playSound("game.potion.smash", 1.0F, rand.nextFloat() * 0.4F + 1.2F);
    }
    setDead();
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (ticksExisted > 200) setDead();
  }

  @Override
  protected float getGravityVelocity() {
    return 0.05F;
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
    damageMultiplier = tag.getFloat("DamageMultiplier");
  }
}

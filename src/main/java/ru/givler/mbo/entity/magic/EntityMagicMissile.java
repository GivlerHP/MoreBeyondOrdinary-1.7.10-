package ru.givler.mbo.entity.magic;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;

/** Fast arcane projectile used by MBO's magic missile spell. */
public final class EntityMagicMissile extends EntityThrowable {
  public static final float BASE_DAMAGE = 4.0F;
  private static final int MAX_LIFETIME = 20;

  private float damage = BASE_DAMAGE;

  public EntityMagicMissile(World world) {
    super(world);
    setSize(0.25F, 0.25F);
  }

  public EntityMagicMissile(World world, EntityLivingBase caster, float speed, float damage) {
    super(world, caster);
    setSize(0.25F, 0.25F);
    this.damage = damage;
    launch(caster, speed);
  }

  public EntityMagicMissile(
      World world, EntityLivingBase caster, EntityLivingBase target, float speed, float damage) {
    super(world, caster);
    setSize(0.25F, 0.25F);
    this.damage = damage;
    launchAt(target, speed);
  }

  private void launch(EntityLivingBase caster, float speed) {
    float yaw = caster.rotationYaw * (float) Math.PI / 180.0F;
    float pitch = caster.rotationPitch * (float) Math.PI / 180.0F;
    double x = -MathHelper.sin(yaw) * MathHelper.cos(pitch);
    double y = -MathHelper.sin(pitch);
    double z = MathHelper.cos(yaw) * MathHelper.cos(pitch);
    setThrowableHeading(x, y, z, speed, 0.0F);
  }

  private void launchAt(EntityLivingBase target, float speed) {
    double x = target.posX - posX;
    double y = target.posY + target.getEyeHeight() * 0.5D - posY;
    double z = target.posZ - posZ;
    setThrowableHeading(x, y, z, speed, 0.0F);
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (ticksExisted >= MAX_LIFETIME) {
      setDead();
      return;
    }
    if (worldObj.isRemote) {
      float red = 0.5F + rand.nextFloat() * 0.5F;
      float green = 0.5F + rand.nextFloat() * 0.5F;
      float blue = 0.5F + rand.nextFloat() * 0.5F;
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.SPARKLE,
          worldObj,
          posX,
          posY,
          posZ,
          0.0D,
          0.0D,
          0.0D,
          new ParticleSettings(20 + rand.nextInt(10), red, green, blue, 0.75F, false));
    }
  }

  @Override
  protected void onImpact(MovingObjectPosition hit) {
    if (worldObj.isRemote) return;

    if (hit.entityHit != null) {
      hit.entityHit.attackEntityFrom(
          DamageSource.causeIndirectMagicDamage(this, getThrower()), damage);
      playSound("game.neutral.hurt", 1.0F, 1.1F + rand.nextFloat() * 0.2F);
    }
    setDead();
  }

  @Override
  protected float getGravityVelocity() {
    return 0.0F;
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setFloat("Damage", damage);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    if (tag.hasKey("Damage")) damage = tag.getFloat("Damage");
  }
}

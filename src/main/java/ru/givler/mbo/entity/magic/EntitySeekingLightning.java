package ru.givler.mbo.entity.magic;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;

/** Homing electric projectile used by Homing Spark and Lightning Disc. */
public final class EntitySeekingLightning extends EntityThrowable
    implements IEntityAdditionalSpawnData {
  public enum Kind {
    SPARK,
    DISC
  }

  private Kind kind = Kind.SPARK;
  private float damageMultiplier = 1;
  private final Set<Integer> hitEntities = new HashSet<Integer>();

  public EntitySeekingLightning(World world) {
    super(world);
    setSize(0.25F, 0.25F);
  }

  public EntitySeekingLightning(World world, EntityLivingBase caster, Kind kind, float damage) {
    super(world, caster);
    this.kind = kind;
    damageMultiplier = damage;
    setSize(kind == Kind.DISC ? 2F : 0.25F, kind == Kind.DISC ? 0.5F : 0.25F);
    setThrowableHeading(
        caster.getLookVec().xCoord,
        caster.getLookVec().yCoord,
        caster.getLookVec().zCoord,
        kind == Kind.DISC ? 1.2F : 0.5F,
        0);
  }

  public Kind kind() {
    return kind;
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    motionX /= 0.99D;
    motionY /= 0.99D;
    motionZ /= 0.99D;
    if (!worldObj.isRemote) seekTarget();
    else {
      int count = kind == Kind.DISC ? 8 : 1;
      for (int i = 0; i < count; i++)
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.SPARK,
            worldObj,
            posX + (rand.nextDouble() - 0.5D) * (kind == Kind.DISC ? 2 : 0.2D),
            posY,
            posZ + (rand.nextDouble() - 0.5D) * (kind == Kind.DISC ? 1 : 0.2D),
            0,
            0,
            0,
            new ParticleSettings(3, 1, 1, 1, 1.2F, false));
    }
    if (ticksExisted > (kind == Kind.DISC ? 50 : 100)) setDead();
  }

  @SuppressWarnings("unchecked")
  private void seekTarget() {
    List<EntityLivingBase> list =
        worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(5, 5, 5));
    EntityLivingBase closest = null;
    for (EntityLivingBase target : list) {
      if (target == getThrower()
          || !target.isEntityAlive()
          || hitEntities.contains(target.getEntityId())) continue;
      if (getThrower() != null && getThrower().isOnSameTeam(target)) continue;
      if (closest == null || getDistanceSqToEntity(target) < getDistanceSqToEntity(closest))
        closest = target;
    }
    if (closest != null
        && Math.abs(motionX) < 1
        && Math.abs(motionY) < 1
        && Math.abs(motionZ) < 1) {
      motionX += (closest.posX - posX) / 30;
      motionY += (closest.posY + closest.height / 2 - posY) / 30;
      motionZ += (closest.posZ - posZ) / 30;
    }
  }

  @Override
  protected void onImpact(MovingObjectPosition hit) {
    if (hit.entityHit instanceof EntityLivingBase) {
      EntityLivingBase target = (EntityLivingBase) hit.entityHit;
      if (hitEntities.add(target.getEntityId()) && !worldObj.isRemote)
        target.attackEntityFrom(
            DamageSource.causeIndirectMagicDamage(this, getThrower()).setProjectile(),
            (kind == Kind.DISC ? 12F : 6F) * damageMultiplier);
      playSound("mbo:arc", 1, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
      if (kind == Kind.DISC) {
        setPosition(posX + motionX * 0.2D, posY + motionY * 0.2D, posZ + motionZ * 0.2D);
        return;
      }
    }
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
  public void writeSpawnData(ByteBuf b) {
    b.writeByte(kind.ordinal());
    b.writeFloat(damageMultiplier);
  }

  @Override
  public void readSpawnData(ByteBuf b) {
    kind = b.readUnsignedByte() == 1 ? Kind.DISC : Kind.SPARK;
    damageMultiplier = b.readFloat();
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound t) {
    super.writeEntityToNBT(t);
    t.setByte("Kind", (byte) kind.ordinal());
    t.setFloat("DamageMultiplier", damageMultiplier);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound t) {
    super.readEntityFromNBT(t);
    kind = t.getByte("Kind") == 1 ? Kind.DISC : Kind.SPARK;
    damageMultiplier = t.getFloat("DamageMultiplier");
  }
}

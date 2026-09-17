package ru.givler.mbo.entity.magic;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
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

/** Shared arrow flight and impact logic for four MBO projectile spells. */
public final class EntityArcaneArrow extends EntityThrowable implements IEntityAdditionalSpawnData {
  public enum Kind {
    DART,
    FORCE,
    ICE_LANCE,
    LIGHTNING
  }

  private Kind kind = Kind.DART;
  private float damageMultiplier = 1.0F;
  private final Set<Integer> pierced = new HashSet<Integer>();

  public EntityArcaneArrow(World world) {
    super(world);
    setSize(0.25F, 0.25F);
  }

  public EntityArcaneArrow(
      World world, EntityLivingBase caster, Kind kind, float speed, float damage) {
    super(world, caster);
    setSize(0.25F, 0.25F);
    this.kind = kind;
    damageMultiplier = damage;
    float yaw = caster.rotationYaw * (float) Math.PI / 180.0F;
    float pitch = caster.rotationPitch * (float) Math.PI / 180.0F;
    setThrowableHeading(
        -MathHelper.sin(yaw) * MathHelper.cos(pitch),
        -MathHelper.sin(pitch),
        MathHelper.cos(yaw) * MathHelper.cos(pitch),
        speed * 1.5F,
        1.0F);
  }

  public Kind kind() {
    return kind;
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (kind == Kind.FORCE || kind == Kind.LIGHTNING) {
      motionX /= 0.99D;
      motionY /= 0.99D;
      motionZ /= 0.99D;
      if (ticksExisted > 20) setDead();
    } else if (ticksExisted > 200) setDead();
    if (!worldObj.isRemote) return;
    if (kind == Kind.DART)
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.DUST,
          worldObj,
          posX,
          posY,
          posZ,
          0,
          -0.03D,
          0,
          new ParticleSettings(12, 0.3F, 0.75F, 0.2F, 0.55F, true));
    if (kind == Kind.LIGHTNING)
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.SPARK,
          worldObj,
          posX,
          posY,
          posZ,
          0,
          0,
          0,
          new ParticleSettings(3, 1, 1, 1, 1.2F, false));
  }

  @Override
  protected void onImpact(MovingObjectPosition hit) {
    if (hit.entityHit instanceof EntityLivingBase) {
      EntityLivingBase target = (EntityLivingBase) hit.entityHit;
      if (pierced.contains(target.getEntityId())) return;
      pierced.add(target.getEntityId());
      if (!worldObj.isRemote) applyHit(target);
      impactParticles();
      playSound(
          kind == Kind.FORCE
              ? "fireworks.blast"
              : kind == Kind.LIGHTNING ? "mbo:arc" : "game.neutral.hurt",
          1.0F,
          kind == Kind.FORCE || kind == Kind.LIGHTNING
              ? 1.0F
              : 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
      if (kind == Kind.ICE_LANCE) {
        setPosition(posX + motionX * 0.2D, posY + motionY * 0.2D, posZ + motionZ * 0.2D);
        return;
      }
    } else {
      impactParticles();
      playSound(
          kind == Kind.FORCE
              ? "fireworks.blast"
              : kind == Kind.ICE_LANCE ? "game.potion.smash" : "random.bowhit",
          1.0F,
          kind == Kind.FORCE ? 1.0F : 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
    }
    setDead();
  }

  private void applyHit(EntityLivingBase target) {
    target.attackEntityFrom(
        DamageSource.causeIndirectMagicDamage(this, getThrower()).setProjectile(),
        baseDamage() * damageMultiplier);
    if (kind == Kind.DART)
      SyncedPotionEffects.apply(target, new PotionEffect(Potion.weakness.id, 200, 1, true));
    if (kind == Kind.ICE_LANCE) {
      SyncedPotionEffects.apply(target, new PotionEffect(PotionRegistry.Frost.id, 300, 0, true));
      double horizontal = Math.sqrt(motionX * motionX + motionZ * motionZ);
      if (horizontal > 0.01D)
        target.addVelocity(motionX / horizontal * 0.6D, 0.1D, motionZ / horizontal * 0.6D);
    }
    if (kind == Kind.LIGHTNING
        && target instanceof EntityCreeper
        && !((EntityCreeper) target).getPowered()) {
      target.getDataWatcher().updateObject(17, Byte.valueOf((byte) 1));
    }
  }

  private float baseDamage() {
    if (kind == Kind.DART) return 4.0F;
    if (kind == Kind.ICE_LANCE) return 10.0F;
    return 7.0F;
  }

  private void impactParticles() {
    if (!worldObj.isRemote) return;
    if (kind == Kind.ICE_LANCE)
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
            new ParticleSettings(20 + rand.nextInt(10), 1, 1, 1, 0.8F, true));
      }
    if (kind == Kind.LIGHTNING)
      for (int i = 0; i < 8; i++) {
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.SPARK,
            worldObj,
            posX + rand.nextFloat() - 0.5D,
            posY + rand.nextFloat() - 0.5D,
            posZ + rand.nextFloat() - 0.5D,
            0,
            0,
            0,
            new ParticleSettings(3, 1, 1, 1, 1.2F, false));
      }
  }

  @Override
  protected float getGravityVelocity() {
    return kind == Kind.DART || kind == Kind.ICE_LANCE ? 0.05F : 0.0F;
  }

  @Override
  public boolean canRenderOnFire() {
    return false;
  }

  @Override
  public void writeSpawnData(ByteBuf buffer) {
    buffer.writeByte(kind.ordinal());
    buffer.writeFloat(damageMultiplier);
  }

  @Override
  public void readSpawnData(ByteBuf buffer) {
    int id = buffer.readUnsignedByte();
    kind = id < Kind.values().length ? Kind.values()[id] : Kind.DART;
    damageMultiplier = buffer.readFloat();
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setByte("Kind", (byte) kind.ordinal());
    tag.setFloat("DamageMultiplier", damageMultiplier);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    int id = tag.getByte("Kind") & 255;
    kind = id < Kind.values().length ? Kind.values()[id] : Kind.DART;
    damageMultiplier = tag.getFloat("DamageMultiplier");
  }
}

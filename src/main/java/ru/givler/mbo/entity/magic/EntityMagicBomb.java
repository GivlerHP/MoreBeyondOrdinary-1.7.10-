package ru.givler.mbo.entity.magic;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;
import ru.givler.mbo.potion.SyncedPotionEffects;
import ru.givler.mbo.registry.PotionRegistry;

/** Shared projectile for elemental splash bombs and the slow darkness orb. */
public final class EntityMagicBomb extends EntityThrowable implements IEntityAdditionalSpawnData {
  public enum Kind {
    DARKNESS,
    FIRE,
    POISON,
    SMOKE,
    SPARK
  }

  private Kind kind = Kind.FIRE;
  private float damageMultiplier = 1.0F;
  private float blastMultiplier = 1.0F;

  public EntityMagicBomb(World world) {
    super(world);
    setSize(0.35F, 0.35F);
  }

  public EntityMagicBomb(
      World world, EntityLivingBase caster, Kind kind, float damage, float blast) {
    super(world, caster);
    setSize(0.35F, 0.35F);
    this.kind = kind;
    damageMultiplier = damage;
    blastMultiplier = blast;
    if (kind == Kind.DARKNESS)
      setThrowableHeading(
          caster.getLookVec().xCoord,
          caster.getLookVec().yCoord,
          caster.getLookVec().zCoord,
          0.5F,
          0.0F);
  }

  public Kind kind() {
    return kind;
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (kind == Kind.DARKNESS) {
      motionX /= 0.99D;
      motionY /= 0.99D;
      motionZ /= 0.99D;
      if (ticksExisted > 150) setDead();
      if (worldObj.isRemote) {
        float brightness = rand.nextFloat() * 0.2F;
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.SPARKLE,
            worldObj,
            posX,
            posY + height * 0.5D,
            posZ,
            0,
            0,
            0,
            new ParticleSettings(20 + rand.nextInt(10), brightness, 0, brightness, 0.7F, false));
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.DARK_MAGIC,
            worldObj,
            posX,
            posY,
            posZ,
            0,
            0,
            0,
            new ParticleSettings(16, 0.1F, 0, 0, 0.8F, false));
      }
    }
  }

  @Override
  protected void onImpact(MovingObjectPosition hit) {
    EntityLivingBase direct =
        hit.entityHit instanceof EntityLivingBase ? (EntityLivingBase) hit.entityHit : null;
    if (worldObj.isRemote) {
      impactParticles();
      return;
    }
    if (kind == Kind.DARKNESS) {
      if (direct != null) {
        direct.attackEntityFrom(source(true), 8.0F * damageMultiplier);
        SyncedPotionEffects.apply(direct, new PotionEffect(Potion.wither.id, 150, 1));
        playSound("mob.wither.hurt", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
      }
      setDead();
      return;
    }
    if (direct != null) directImpact(direct);
    playSound(
        kind == Kind.SPARK ? "fireworks.blast_far" : "game.potion.smash",
        kind == Kind.SPARK ? 0.5F : 1.5F,
        0.5F + rand.nextFloat() * 0.4F);
    double radius = (kind == Kind.SPARK ? 5.0D : 3.0D) * blastMultiplier;
    @SuppressWarnings("unchecked")
    List<EntityLivingBase> targets =
        worldObj.getEntitiesWithinAABB(
            EntityLivingBase.class, boundingBox.expand(radius, radius, radius));
    int affected = 0;
    for (EntityLivingBase target : targets) {
      if (target == direct || target == getThrower()) continue;
      if (kind == Kind.SPARK && affected++ >= 4) break;
      splash(target);
    }
    setDead();
  }

  private void directImpact(EntityLivingBase target) {
    if (kind == Kind.FIRE) {
      target.attackEntityFrom(source(true).setFireDamage(), 5 * damageMultiplier);
      target.setFire(10);
    }
    if (kind == Kind.POISON && !poisonImmune(target)) {
      target.attackEntityFrom(source(true), 5 * damageMultiplier);
      SyncedPotionEffects.apply(target, new PotionEffect(Potion.poison.id, 120, 1));
    }
    if (kind == Kind.SPARK) target.attackEntityFrom(source(true), 6 * damageMultiplier);
    if (kind == Kind.SMOKE) splash(target);
  }

  private void splash(EntityLivingBase target) {
    if (kind == Kind.FIRE) {
      target.attackEntityFrom(source(false).setFireDamage(), 4 * damageMultiplier);
      target.setFire(7);
    }
    if (kind == Kind.POISON && !poisonImmune(target)) {
      target.attackEntityFrom(source(false), 4 * damageMultiplier);
      SyncedPotionEffects.apply(target, new PotionEffect(Potion.poison.id, 100, 1));
    }
    if (kind == Kind.SPARK) {
      target.attackEntityFrom(source(false), 5 * damageMultiplier);
      playSound("mbo:arc", 1, 1.5F + rand.nextFloat() * 0.4F);
    }
    if (kind == Kind.SMOKE) {
      if (target instanceof EntityPlayer)
        SyncedPotionEffects.apply(target, new PotionEffect(Potion.blindness.id, 120, 0));
      else if (target instanceof EntityLiving) {
        ((EntityLiving) target).setAttackTarget(null);
        if (target instanceof EntityCreature) ((EntityCreature) target).setTarget(null);
        SyncedPotionEffects.apply(target, new PotionEffect(PotionRegistry.MindTrick.id, 120, 0));
      }
    }
  }

  private DamageSource source(boolean projectile) {
    DamageSource source = DamageSource.causeIndirectMagicDamage(this, getThrower());
    return projectile ? source.setProjectile() : source;
  }

  private static boolean poisonImmune(EntityLivingBase target) {
    return target.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD
        || target instanceof EntitySpider;
  }

  private void impactParticles() {
    MoreBeyondOrdinary.proxy.spawnParticle(
        EnumParticleType.VANILLA_LARGE_EXPLODE, worldObj, posX, posY, posZ, 0, 0, 0);
    if (kind == Kind.DARKNESS) return;
    int count = kind == Kind.SPARK ? 16 : (int) (60 * blastMultiplier);
    for (int i = 0; i < count; i++) {
      double x = posX + (rand.nextDouble() * 4 - 2) * blastMultiplier;
      double y = posY + (rand.nextDouble() * 4 - 2) * blastMultiplier;
      double z = posZ + (rand.nextDouble() * 4 - 2) * blastMultiplier;
      if (kind == Kind.FIRE)
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.MAGIC_FIRE,
            worldObj,
            x,
            y,
            z,
            0,
            0,
            0,
            new ParticleSettings(15 + rand.nextInt(5), 1, 0.3F, 0, 1.2F, false));
      else if (kind == Kind.POISON)
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.SPARKLE,
            worldObj,
            x,
            y,
            z,
            0,
            0,
            0,
            new ParticleSettings(35, 0.3F, 0.7F, 0, 0.8F, false));
      else if (kind == Kind.SPARK)
        MoreBeyondOrdinary.proxy.spawnParticle(EnumParticleType.SPARK, worldObj, x, y, z, 0, 0, 0);
      else worldObj.spawnParticle("largesmoke", x, y, z, 0, 0, 0);
    }
  }

  @Override
  protected float getGravityVelocity() {
    return kind == Kind.DARKNESS ? 0 : 0.03F;
  }

  @Override
  public boolean canRenderOnFire() {
    return false;
  }

  @Override
  public void writeSpawnData(ByteBuf b) {
    b.writeByte(kind.ordinal());
    b.writeFloat(damageMultiplier);
    b.writeFloat(blastMultiplier);
  }

  @Override
  public void readSpawnData(ByteBuf b) {
    int id = b.readUnsignedByte();
    kind = id < Kind.values().length ? Kind.values()[id] : Kind.FIRE;
    damageMultiplier = b.readFloat();
    blastMultiplier = b.readFloat();
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound t) {
    super.writeEntityToNBT(t);
    t.setByte("Kind", (byte) kind.ordinal());
    t.setFloat("DamageMultiplier", damageMultiplier);
    t.setFloat("BlastMultiplier", blastMultiplier);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound t) {
    super.readEntityFromNBT(t);
    int id = t.getByte("Kind") & 255;
    kind = id < Kind.values().length ? Kind.values()[id] : Kind.FIRE;
    damageMultiplier = t.getFloat("DamageMultiplier");
    blastMultiplier = t.getFloat("BlastMultiplier");
  }
}

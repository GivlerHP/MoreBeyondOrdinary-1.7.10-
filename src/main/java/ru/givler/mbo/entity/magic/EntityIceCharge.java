package ru.givler.mbo.entity.magic;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;
import ru.givler.mbo.potion.SyncedPotionEffects;
import ru.givler.mbo.registry.PotionRegistry;

/** Frost charge which freezes an area and releases ten ice shards on impact. */
public final class EntityIceCharge extends EntityThrowable implements IEntityAdditionalSpawnData {
  private float damageMultiplier = 1.0F;
  private float blastMultiplier = 1.0F;

  public EntityIceCharge(World world) {
    super(world);
    setSize(0.5F, 0.5F);
  }

  public EntityIceCharge(World world, EntityLivingBase caster, float damage, float blast) {
    super(world, caster);
    setSize(0.5F, 0.5F);
    damageMultiplier = damage;
    blastMultiplier = blast;
  }

  @Override
  protected void onImpact(MovingObjectPosition hit) {
    if (worldObj.isRemote) {
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.VANILLA_LARGE_EXPLODE, worldObj, posX, posY, posZ, 0, 0, 0);
      for (int i = 0; i < (int) (30 * blastMultiplier); i++) {
        double x = posX + (rand.nextDouble() * 4 - 2) * blastMultiplier;
        double y = posY + (rand.nextDouble() * 4 - 2) * blastMultiplier;
        double z = posZ + (rand.nextDouble() * 4 - 2) * blastMultiplier;
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.ICE,
            worldObj,
            x,
            y,
            z,
            0,
            0,
            0,
            new ParticleSettings(35, 1, 1, 1, 0.8F, true));
      }
      return;
    }
    EntityLivingBase direct =
        hit.entityHit instanceof EntityLivingBase ? (EntityLivingBase) hit.entityHit : null;
    if (direct != null) {
      direct.attackEntityFrom(
          DamageSource.causeIndirectMagicDamage(this, getThrower()).setProjectile(),
          4.0F * damageMultiplier);
      SyncedPotionEffects.apply(direct, new PotionEffect(PotionRegistry.Frost.id, 120, 1, true));
    }
    playSound("random.glass", 1.5F, rand.nextFloat() * 0.4F + 0.6F);
    playSound("mbo:ice", 1.2F, rand.nextFloat() * 0.4F + 1.2F);
    double radius = 3.0D * blastMultiplier;
    @SuppressWarnings("unchecked")
    List<EntityLivingBase> targets =
        worldObj.getEntitiesWithinAABB(
            EntityLivingBase.class, boundingBox.expand(radius, radius, radius));
    for (EntityLivingBase target : targets)
      if (target != direct && target != getThrower()) {
        SyncedPotionEffects.apply(target, new PotionEffect(PotionRegistry.Frost.id, 100, 0, true));
      }
    EntityLivingBase caster = getThrower();
    if (caster != null)
      for (int i = 0; i < 10; i++) {
        double dx = rand.nextDouble() - 0.5D;
        double dy = rand.nextDouble() - 0.5D;
        double dz = rand.nextDouble() - 0.5D;
        EntityIceShard shard = new EntityIceShard(worldObj, caster, 0.01F, damageMultiplier);
        shard.setPosition(posX + dx, posY + dy, posZ + dz);
        shard.setThrowableHeading(dx, dy, dz, 1.0F, 0.0F);
        worldObj.spawnEntityInWorld(shard);
      }
    setDead();
  }

  @Override
  public boolean canRenderOnFire() {
    return false;
  }

  @Override
  public void writeSpawnData(ByteBuf buffer) {
    buffer.writeFloat(damageMultiplier);
    buffer.writeFloat(blastMultiplier);
  }

  @Override
  public void readSpawnData(ByteBuf buffer) {
    damageMultiplier = buffer.readFloat();
    blastMultiplier = buffer.readFloat();
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setFloat("DamageMultiplier", damageMultiplier);
    tag.setFloat("BlastMultiplier", blastMultiplier);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    damageMultiplier = tag.getFloat("DamageMultiplier");
    blastMultiplier = tag.getFloat("BlastMultiplier");
  }
}

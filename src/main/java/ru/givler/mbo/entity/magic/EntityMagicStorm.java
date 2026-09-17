package ru.givler.mbo.entity.magic;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;
import ru.givler.mbo.potion.SyncedPotionEffects;
import ru.givler.mbo.registry.PotionRegistry;

/** Shared timed volume for arrow rain, hailstorm and blizzard. */
public final class EntityMagicStorm extends Entity implements IEntityAdditionalSpawnData {
  public enum Kind {
    ARROW_RAIN,
    HAILSTORM,
    BLIZZARD
  }

  private int ownerId = -1, lifetime = 120;
  private float power = 1;

  public EntityMagicStorm(World world) {
    super(world);
    noClip = true;
    setSize(5, 3);
  }

  public EntityMagicStorm(
      World world,
      double x,
      double y,
      double z,
      EntityLivingBase owner,
      Kind kind,
      int lifetime,
      float power,
      float yaw) {
    this(world);
    setPosition(x, y, z);
    ownerId = owner.getEntityId();
    this.lifetime = lifetime;
    this.power = power;
    rotationYaw = yaw;
    dataWatcher.updateObject(10, (byte) kind.ordinal());
    if (kind == Kind.BLIZZARD) setSize(6, 3);
  }

  public Kind kind() {
    return Kind.values()[dataWatcher.getWatchableObjectByte(10)];
  }

  private EntityLivingBase owner() {
    Entity e = worldObj.getEntityByID(ownerId);
    return e instanceof EntityLivingBase ? (EntityLivingBase) e : null;
  }

  @Override
  protected void entityInit() {
    dataWatcher.addObject(10, (byte) 0);
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (ticksExisted >= lifetime) {
      setDead();
      return;
    }
    if (worldObj.isRemote) {
      particles();
      return;
    }
    if (kind() == Kind.ARROW_RAIN) spawnArrow();
    else if (kind() == Kind.HAILSTORM) spawnHail();
    else updateBlizzard();
  }

  private void spawnArrow() {
    EntityArrow arrow =
        new EntityArrow(
            worldObj,
            posX + rand.nextDouble() * 6 - 3,
            posY + rand.nextDouble() * 4 - 2,
            posZ + rand.nextDouble() * 6 - 3);
    arrow.motionX = Math.cos(Math.toRadians(rotationYaw + 90));
    arrow.motionY = -0.6;
    arrow.motionZ = Math.sin(Math.toRadians(rotationYaw + 90));
    arrow.shootingEntity = owner();
    arrow.setDamage(7 * power);
    worldObj.spawnEntityInWorld(arrow);
  }

  private void spawnHail() {
    EntityLivingBase caster = owner();
    if (caster == null) return;
    EntityIceShard shard = new EntityIceShard(worldObj, caster, 0, power);
    shard.setPosition(
        posX + rand.nextDouble() * 6 - 3,
        posY + rand.nextDouble() * 4 - 2,
        posZ + rand.nextDouble() * 6 - 3);
    shard.motionX = Math.cos(Math.toRadians(rotationYaw + 90));
    shard.motionY = -0.6;
    shard.motionZ = Math.sin(Math.toRadians(rotationYaw + 90));
    worldObj.spawnEntityInWorld(shard);
  }

  @SuppressWarnings("unchecked")
  private void updateBlizzard() {
    if (ticksExisted % 120 == 1) playSound("mbo:wind", 1, 1);
    EntityLivingBase caster = owner();
    List<EntityLivingBase> targets =
        worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(3, 1, 3));
    for (EntityLivingBase target : targets) {
      if (target != caster && (caster == null || !caster.isOnSameTeam(target))) {
        double x = target.motionX, y = target.motionY, z = target.motionZ;
        target.attackEntityFrom(
            caster == null
                ? DamageSource.magic
                : DamageSource.causeIndirectMagicDamage(this, caster),
            power);
        target.motionX = x;
        target.motionY = y;
        target.motionZ = z;
      }
      SyncedPotionEffects.apply(target, new PotionEffect(PotionRegistry.Frost.id, 20, 0, true));
    }
  }

  private void particles() {
    if (kind() != Kind.BLIZZARD) return;
    for (int i = 0; i < 9; i++) {
      double y = posY + rand.nextDouble() * 3, radius = rand.nextDouble() * 2.5D + 0.5D;
      float brightness = 0.5F + rand.nextFloat() * 0.5F;
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.BLIZZARD,
          worldObj,
          posX,
          y,
          posZ,
          0,
          0,
          0,
          new ParticleSettings(100, brightness, brightness + 0.1F, 1, (float) radius, false));
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.BLIZZARD,
          worldObj,
          posX,
          y,
          posZ,
          0,
          0,
          0,
          new ParticleSettings(100, 1, 1, 1, (float) radius, false));
    }
  }

  @Override
  protected void writeEntityToNBT(NBTTagCompound t) {
    t.setByte("Kind", (byte) kind().ordinal());
    t.setInteger("Owner", ownerId);
    t.setInteger("Life", lifetime);
    t.setFloat("Power", power);
  }

  @Override
  protected void readEntityFromNBT(NBTTagCompound t) {
    dataWatcher.updateObject(10, t.getByte("Kind"));
    ownerId = t.getInteger("Owner");
    lifetime = t.getInteger("Life");
    power = t.getFloat("Power");
  }

  @Override
  public void writeSpawnData(ByteBuf b) {
    b.writeInt(ownerId);
    b.writeInt(lifetime);
    b.writeFloat(power);
  }

  @Override
  public void readSpawnData(ByteBuf b) {
    ownerId = b.readInt();
    lifetime = b.readInt();
    power = b.readFloat();
  }
}

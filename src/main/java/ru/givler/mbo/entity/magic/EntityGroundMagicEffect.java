package ru.givler.mbo.entity.magic;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;
import ru.givler.mbo.potion.SyncedPotionEffects;
import ru.givler.mbo.registry.PotionRegistry;

/** Shared stationary area effect used by fire rings and patches of decay. */
public final class EntityGroundMagicEffect extends Entity implements IEntityAdditionalSpawnData {
  public enum Kind {
    FIRE_RING,
    DECAY
  }

  private EntityLivingBase owner;
  private UUID ownerId;
  private int lifetime = 400;
  private float power = 1.0F;

  public EntityGroundMagicEffect(World world) {
    super(world);
    noClip = true;
    setSize(2.0F, 0.2F);
  }

  public EntityGroundMagicEffect(
      World world,
      double x,
      double y,
      double z,
      EntityLivingBase owner,
      Kind kind,
      int lifetime,
      float power) {
    this(world);
    setPosition(x, y, z);
    this.owner = owner;
    this.ownerId = owner == null ? null : owner.getUniqueID();
    this.lifetime = lifetime;
    this.power = power;
    dataWatcher.updateObject(10, (byte) kind.ordinal());
    dataWatcher.updateObject(11, (byte) world.rand.nextInt(10));
    setSize(kind == Kind.FIRE_RING ? 5.0F : 2.0F, kind == Kind.FIRE_RING ? 1.0F : 0.2F);
  }

  public Kind kind() {
    return Kind.values()[dataWatcher.getWatchableObjectByte(10)];
  }

  public int textureIndex() {
    return dataWatcher.getWatchableObjectByte(11) & 255;
  }

  public int lifetime() {
    return lifetime;
  }

  @Override
  protected void entityInit() {
    dataWatcher.addObject(10, (byte) 0);
    dataWatcher.addObject(11, (byte) 0);
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (ticksExisted > lifetime) {
      setDead();
      return;
    }
    if (worldObj.isRemote) {
      if (kind() == Kind.DECAY && rand.nextInt(15) == 0) {
        double radius = rand.nextDouble() * 0.8D;
        double angle = rand.nextDouble() * Math.PI * 2.0D;
        float light = rand.nextFloat() * 0.4F;
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.DARK_MAGIC,
            worldObj,
            posX + radius * Math.cos(angle),
            posY,
            posZ + radius * Math.sin(angle),
            0,
            0,
            0,
            new ParticleSettings(12, light, 0, light + 0.1F, 0.8F, false));
      } else if (kind() == Kind.FIRE_RING) {
        double angle = rand.nextDouble() * Math.PI * 2.0D;
        worldObj.spawnParticle(
            "flame",
            posX + Math.cos(angle) * 2.3D,
            posY + rand.nextDouble(),
            posZ + Math.sin(angle) * 2.3D,
            0,
            0.02D,
            0);
      }
      return;
    }
    resolveOwner();
    if (kind() == Kind.FIRE_RING) updateFireRing();
    else updateDecay();
  }

  @SuppressWarnings("unchecked")
  private void updateFireRing() {
    if (ticksExisted % 40 == 1) playSound("fire.fire", 4.0F, 0.7F);
    double radius = 2.5D;
    List<EntityLivingBase> targets =
        worldObj.getEntitiesWithinAABB(
            EntityLivingBase.class, boundingBox.expand(radius, 1.0D, radius));
    for (EntityLivingBase target : targets) {
      if (target == owner || target.getDistanceSq(posX, posY, posZ) > radius * radius) continue;
      double vx = target.motionX, vy = target.motionY, vz = target.motionZ;
      target.setFire(10);
      target.attackEntityFrom(
          owner == null
              ? net.minecraft.util.DamageSource.magic
              : new EntityDamageSource("mbo.fireRing", owner).setFireDamage().setMagicDamage(),
          power);
      target.motionX = vx;
      target.motionY = vy;
      target.motionZ = vz;
    }
  }

  @SuppressWarnings("unchecked")
  private void updateDecay() {
    List<EntityLivingBase> targets =
        worldObj.getEntitiesWithinAABB(
            EntityLivingBase.class,
            AxisAlignedBB.getBoundingBox(
                posX - 1, posY - 0.5D, posZ - 1, posX + 1, posY + 1, posZ + 1));
    for (EntityLivingBase target : targets)
      if (target != owner && !target.isPotionActive(PotionRegistry.Decay)) {
        SyncedPotionEffects.apply(target, new PotionEffect(PotionRegistry.Decay.id, 400, 0, false));
      }
  }

  private void resolveOwner() {
    if (owner != null || ownerId == null) return;
    for (Object object : worldObj.loadedEntityList)
      if (object instanceof EntityLivingBase && ownerId.equals(((Entity) object).getUniqueID())) {
        owner = (EntityLivingBase) object;
        return;
      }
  }

  @Override
  protected void writeEntityToNBT(NBTTagCompound tag) {
    tag.setByte("Kind", (byte) kind().ordinal());
    tag.setByte("Texture", (byte) textureIndex());
    tag.setInteger("Lifetime", lifetime);
    tag.setFloat("Power", power);
    if (ownerId != null) tag.setString("Owner", ownerId.toString());
  }

  @Override
  protected void readEntityFromNBT(NBTTagCompound tag) {
    dataWatcher.updateObject(10, tag.getByte("Kind"));
    dataWatcher.updateObject(11, tag.getByte("Texture"));
    lifetime = Math.max(1, tag.getInteger("Lifetime"));
    power = tag.getFloat("Power");
    try {
      ownerId = UUID.fromString(tag.getString("Owner"));
    } catch (IllegalArgumentException ignored) {
      ownerId = null;
    }
  }

  @Override
  public void writeSpawnData(ByteBuf buffer) {
    buffer.writeInt(lifetime);
    buffer.writeFloat(power);
  }

  @Override
  public void readSpawnData(ByteBuf buffer) {
    lifetime = buffer.readInt();
    power = buffer.readFloat();
  }
}

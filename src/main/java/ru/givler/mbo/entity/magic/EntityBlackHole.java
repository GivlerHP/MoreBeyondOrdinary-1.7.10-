package ru.givler.mbo.entity.magic;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/** Persistent gravity well which pulls enemies towards its centre. */
public final class EntityBlackHole extends Entity implements IEntityAdditionalSpawnData {
  private UUID casterId;
  private int casterEntityId = -1;
  private int lifetime = 600;
  private float damageMultiplier = 1;

  public EntityBlackHole(World world) {
    super(world);
    setSize(6, 3);
    ignoreFrustumCheck = true;
    noClip = true;
  }

  public EntityBlackHole(
      World world,
      double x,
      double y,
      double z,
      EntityLivingBase caster,
      int lifetime,
      float damageMultiplier) {
    this(world);
    setPosition(x, y, z);
    casterId = caster.getUniqueID();
    casterEntityId = caster.getEntityId();
    this.lifetime = lifetime;
    this.damageMultiplier = damageMultiplier;
  }

  public int lifetime() {
    return lifetime;
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (ticksExisted >= lifetime) {
      setDead();
      return;
    }
    if (worldObj.isRemote) {
      if (ticksExisted + 40 < lifetime)
        for (int i = 0; i < 5; i++)
          worldObj.spawnParticle(
              "portal",
              posX,
              posY,
              posZ,
              (rand.nextDouble() - 0.5D) * 4,
              (rand.nextDouble() - 0.5D) * 4 - 1,
              (rand.nextDouble() - 0.5D) * 4);
      return;
    }
    pullTargets();
    if (lifetime - ticksExisted == 75) playSound("portal.trigger", 1.5F, 1);
    else if (ticksExisted % 80 == 1 && ticksExisted + 80 < lifetime)
      playSound("portal.portal", 1.5F, 1);
  }

  @SuppressWarnings("unchecked")
  private void pullTargets() {
    EntityLivingBase caster = caster();
    List<EntityLivingBase> targets =
        worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(6, 6, 6));
    for (EntityLivingBase target : targets) {
      if (target == caster
          || !target.isEntityAlive()
          || caster != null && caster.isOnSameTeam(target)
          || getDistanceSqToEntity(target) > 36) continue;
      target.motionX = approach(target.motionX, posX - target.posX);
      target.motionY = approach(target.motionY, posY - target.posY);
      target.motionZ = approach(target.motionZ, posZ - target.posZ);
      target.velocityChanged = true;
      if (target instanceof EntityPlayerMP)
        ((EntityPlayerMP) target)
            .playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(target));
      if (getDistanceSqToEntity(target) <= 4)
        target.attackEntityFrom(
            caster == null
                ? DamageSource.magic
                : DamageSource.causeIndirectMagicDamage(this, caster),
            2 * damageMultiplier);
    }
  }

  private static double approach(double velocity, double difference) {
    if (difference > 0 && velocity < 1) return velocity + 0.1D;
    if (difference < 0 && velocity > -1) return velocity - 0.1D;
    return velocity;
  }

  private EntityLivingBase caster() {
    Entity entity = casterEntityId < 0 ? null : worldObj.getEntityByID(casterEntityId);
    if (entity instanceof EntityLivingBase) return (EntityLivingBase) entity;
    if (casterId != null)
      for (Object object : worldObj.loadedEntityList)
        if (object instanceof EntityLivingBase && casterId.equals(((Entity) object).getUniqueID()))
          return (EntityLivingBase) object;
    return null;
  }

  @Override
  protected void entityInit() {}

  @Override
  protected void writeEntityToNBT(NBTTagCompound tag) {
    if (casterId != null) tag.setString("Caster", casterId.toString());
    tag.setInteger("Lifetime", lifetime);
    tag.setFloat("Damage", damageMultiplier);
  }

  @Override
  protected void readEntityFromNBT(NBTTagCompound tag) {
    try {
      casterId = UUID.fromString(tag.getString("Caster"));
    } catch (IllegalArgumentException ignored) {
    }
    lifetime = tag.getInteger("Lifetime");
    damageMultiplier = tag.getFloat("Damage");
  }

  @Override
  public void writeSpawnData(ByteBuf b) {
    b.writeInt(lifetime);
    b.writeInt(casterEntityId);
    b.writeFloat(damageMultiplier);
  }

  @Override
  public void readSpawnData(ByteBuf b) {
    lifetime = b.readInt();
    casterEntityId = b.readInt();
    damageMultiplier = b.readFloat();
  }

  @Override
  public boolean canRenderOnFire() {
    return false;
  }
}

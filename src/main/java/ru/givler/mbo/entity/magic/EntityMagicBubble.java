package ru.givler.mbo.entity.magic;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/** Floating prison shared by Bubble and Entrapment. */
public final class EntityMagicBubble extends Entity implements IEntityAdditionalSpawnData {
  private UUID casterId;
  private int casterEntityId = -1;
  private int lifetime = 200;
  private float power = 1.0F;
  private int riderEntityId = -1;

  public EntityMagicBubble(World world) {
    super(world);
    noClip = true;
    setSize(0.2F, 0.2F);
  }

  public EntityMagicBubble(
      World world,
      EntityLivingBase target,
      EntityLivingBase caster,
      int lifetime,
      boolean dark,
      float power) {
    this(world);
    setPosition(target.posX, target.posY, target.posZ);
    casterId = caster.getUniqueID();
    casterEntityId = caster.getEntityId();
    this.lifetime = lifetime;
    this.power = power;
    dataWatcher.updateObject(10, (byte) (dark ? 1 : 0));
  }

  public boolean isDark() {
    return dataWatcher.getWatchableObjectByte(10) != 0;
  }

  @Override
  protected void entityInit() {
    dataWatcher.addObject(10, (byte) 0);
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (ticksExisted >= lifetime) {
      burst();
      return;
    }
    Entity rider = riddenByEntity;
    if (rider == null && riderEntityId >= 0) {
      Entity remembered = worldObj.getEntityByID(riderEntityId);
      if (remembered != null && !remembered.isDead) {
        remembered.mountEntity(this);
        rider = remembered;
      }
    }
    if (rider != null) riderEntityId = rider.getEntityId();
    if (rider == null || rider.isDead) {
      if (ticksExisted > 1) burst();
      return;
    }
    moveEntity(0, 0.03D, 0);
    if (!isDark()
        && ticksExisted > 1
        && rider instanceof EntityLivingBase
        && ((EntityLivingBase) rider).hurtTime > 0) {
      burst();
      return;
    }
    if (isDark()) {
      if (!worldObj.isRemote && ticksExisted % 30 == 0)
        rider.attackEntityFrom(
            caster() == null
                ? DamageSource.magic
                : DamageSource.causeIndirectMagicDamage(this, caster()),
            power);
      if (worldObj.isRemote)
        for (int i = 0; i < 5; i++)
          worldObj.spawnParticle(
              "portal",
              posX,
              posY + rand.nextDouble() + 0.5D,
              posZ,
              (rand.nextDouble() - 0.5D) * 2,
              -rand.nextDouble(),
              (rand.nextDouble() - 0.5D) * 2);
      if (lifetime - ticksExisted == 75) playSound("portal.trigger", 1.5F, 1.0F);
      else if (ticksExisted % 100 == 1 && ticksExisted < 150) {
        playSound("portal.portal", 1.5F, 1.0F);
      }
    }
  }

  @Override
  public double getMountedYOffset() {
    return 0.1D;
  }

  @Override
  public boolean shouldRiderSit() {
    return false;
  }

  @Override
  public boolean canBeCollidedWith() {
    return false;
  }

  private EntityLivingBase caster() {
    Entity entity = casterEntityId < 0 ? null : worldObj.getEntityByID(casterEntityId);
    if (entity instanceof EntityLivingBase) return (EntityLivingBase) entity;
    if (casterId != null)
      for (Object object : worldObj.loadedEntityList) {
        if (object instanceof EntityLivingBase
            && casterId.equals(((Entity) object).getUniqueID())) {
          return (EntityLivingBase) object;
        }
      }
    return null;
  }

  private void burst() {
    if (riddenByEntity != null) riddenByEntity.mountEntity(null);
    if (!isDead && !isDark()) playSound("random.pop", 1.5F, 1.0F);
    setDead();
  }

  @Override
  protected void writeEntityToNBT(NBTTagCompound tag) {
    tag.setBoolean("Dark", isDark());
    tag.setInteger("Lifetime", lifetime);
    tag.setFloat("Power", power);
    tag.setInteger("CasterEntity", casterEntityId);
    tag.setInteger("RiderEntity", riderEntityId);
    if (casterId != null) tag.setString("Caster", casterId.toString());
  }

  @Override
  protected void readEntityFromNBT(NBTTagCompound tag) {
    dataWatcher.updateObject(10, (byte) (tag.getBoolean("Dark") ? 1 : 0));
    lifetime = tag.getInteger("Lifetime");
    power = tag.getFloat("Power");
    casterEntityId = tag.getInteger("CasterEntity");
    riderEntityId = tag.getInteger("RiderEntity");
    try {
      casterId = UUID.fromString(tag.getString("Caster"));
    } catch (IllegalArgumentException ignored) {
      casterId = null;
    }
  }

  @Override
  public void writeSpawnData(ByteBuf data) {
    data.writeInt(lifetime);
    data.writeFloat(power);
    data.writeInt(casterEntityId);
  }

  @Override
  public void readSpawnData(ByteBuf data) {
    lifetime = data.readInt();
    power = data.readFloat();
    casterEntityId = data.readInt();
  }
}

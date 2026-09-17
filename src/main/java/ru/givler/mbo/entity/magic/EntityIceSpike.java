package ru.givler.mbo.entity.magic;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import ru.givler.mbo.potion.SyncedPotionEffects;
import ru.givler.mbo.registry.PotionRegistry;

public final class EntityIceSpike extends Entity implements IEntityAdditionalSpawnData {
  private int ownerId = -1, lifetime = 40;
  private float power = 1;

  public EntityIceSpike(World w) {
    super(w);
    setSize(.5F, 1F);
    noClip = true;
  }

  public EntityIceSpike(
      World w, double x, double y, double z, EntityLivingBase owner, int life, float power) {
    this(w);
    setPosition(x, y, z);
    ownerId = owner.getEntityId();
    lifetime = life;
    this.power = power;
  }

  @Override
  protected void entityInit() {}

  @Override
  public void onUpdate() {
    int left = lifetime - ticksExisted;
    if (left < 15) motionY = -.01D * (ticksExisted - (lifetime - 15));
    else if (left < 25) motionY = 0;
    else if (left < 28) motionY = .25D;
    moveEntity(0, motionY, 0);
    if (left == 30) playSound("mbo:ice", 1, 2);
    if (!worldObj.isRemote) {
      Entity owner = worldObj.getEntityByID(ownerId);
      List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox);
      for (Object o : list)
        if (o instanceof EntityLivingBase && o != owner) {
          EntityLivingBase t = (EntityLivingBase) o;
          if (t.attackEntityFrom(
              owner instanceof EntityLivingBase
                  ? DamageSource.causeIndirectMagicDamage(this, (EntityLivingBase) owner)
                  : DamageSource.magic,
              5 * power))
            SyncedPotionEffects.apply(t, new PotionEffect(PotionRegistry.Frost.id, 100, 0));
        }
    }
    super.onUpdate();
    if (ticksExisted >= lifetime) setDead();
  }

  @Override
  protected void writeEntityToNBT(NBTTagCompound t) {
    t.setInteger("Owner", ownerId);
    t.setInteger("Life", lifetime);
    t.setFloat("Power", power);
  }

  @Override
  protected void readEntityFromNBT(NBTTagCompound t) {
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

package ru.givler.mbo.entity.monster;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import ru.givler.mbo.config.MobSpawnConfig;
import ru.givler.mbo.entity.ai.EntityAIMountedHuskAttack;
import ru.givler.mbo.handler.UndeadEvents;

public final class EntityMBOHusk extends EntityZombie {
  private int waterTicks;
  private int conversionTicks = -1;

  public EntityMBOHusk(World world) {
    super(world);
    tasks.addTask(1, new EntityAIMountedHuskAttack(this));
  }

  @Override
  public boolean canBreatheUnderwater() {
    return true;
  }

  @Override
  public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
    HuskGroup group = data instanceof HuskGroup ? (HuskGroup) data : new HuskGroup(data);
    group.original = super.onSpawnWithEgg(group.original);
    setVillager(false);
    if (getEntityData().getBoolean("MBONaturalUndeadSpawn") && !group.camelAttempted) {
      group.camelAttempted = true;
      if (!isChild() && rand.nextFloat() < .1F) UndeadEvents.createCamelJockey(this);
    }
    getEntityData().removeTag("MBONaturalUndeadSpawn");
    return group;
  }

  private static final class HuskGroup implements IEntityLivingData {
    IEntityLivingData original;
    boolean camelAttempted;

    HuskGroup(IEntityLivingData original) {
      this.original = original;
    }
  }

  @Override
  public boolean getCanSpawnHere() {
    return MobSpawnConfig.allowsUndead("husk", this) && super.getCanSpawnHere();
  }

  @Override
  protected String getLivingSound() {
    return "mbo:entity.husk.ambient";
  }

  @Override
  protected String getHurtSound() {
    return "mbo:entity.husk.hurt";
  }

  @Override
  protected String getDeathSound() {
    return "mbo:entity.husk.death";
  }

  @Override
  protected void func_145780_a(int x, int y, int z, Block block) {
    playSound("mbo:entity.husk.step", .15F, 1F);
  }

  @Override
  public boolean attackEntityAsMob(Entity target) {
    boolean hit = super.attackEntityAsMob(target);
    if (hit && getHeldItem() == null && target instanceof EntityLivingBase) {
      int difficulty =
          (int)
              worldObj.func_147462_b(
                  MathHelper.floor_double(posX),
                  MathHelper.floor_double(posY),
                  MathHelper.floor_double(posZ));
      ((EntityLivingBase) target)
          .addPotionEffect(new PotionEffect(Potion.hunger.id, 140 * difficulty));
    }
    return hit;
  }

  @Override
  public void onLivingUpdate() {
    super.onLivingUpdate();
    if (worldObj.isRemote || !isEntityAlive()) return;
    tickConversion(isInsideOfMaterial(Material.water));
  }

  public void tickConversion(boolean submerged) {
    if (conversionTicks >= 0) {
      if (--conversionTicks < 0 && !UndeadEvents.convert(this, new EntityZombie(worldObj)))
        conversionTicks = 20;
    } else if (submerged) {
      if (++waterTicks >= 600) conversionTicks = 300;
    } else waterTicks = 0;
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setInteger("HuskWaterTime", waterTicks);
    tag.setInteger("HuskConversionTime", conversionTicks);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    setVillager(false);
    waterTicks = Math.max(0, tag.getInteger("HuskWaterTime"));
    conversionTicks = tag.hasKey("HuskConversionTime") ? tag.getInteger("HuskConversionTime") : -1;
  }
}

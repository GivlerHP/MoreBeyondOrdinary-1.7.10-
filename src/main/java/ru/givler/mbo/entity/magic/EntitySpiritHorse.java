package ru.givler.mbo.entity.magic;

import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;

/** Temporary tamed mount created by the MBO spirit-horse spell. */
public final class EntitySpiritHorse extends EntityHorse {
  private int idleTicks;

  public EntitySpiritHorse(World world) {
    super(world);
  }

  @Override
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(24.0D);
  }

  @Override
  public String getCommandSenderName() {
    return hasCustomNameTag()
        ? getCustomNameTag()
        : StatCollector.translateToLocal("entity.mbo.spirit_horse.name");
  }

  public EntityLivingBase getSummoner() {
    String value = func_152119_ch();
    if (value == null || value.isEmpty()) return null;
    try {
      UUID ownerId = UUID.fromString(value);
      for (Object object : worldObj.loadedEntityList) {
        if (object instanceof EntityLivingBase && ownerId.equals(((Entity) object).getUniqueID())) {
          return (EntityLivingBase) object;
        }
      }
    } catch (IllegalArgumentException ignored) {
    }
    return null;
  }

  public boolean belongsTo(EntityPlayer player) {
    EntityLivingBase owner = getSummoner();
    return owner == player;
  }

  @Override
  public boolean interact(EntityPlayer player) {
    ItemStack held = player.getHeldItem();
    if (player.isSneaking() && held == null && belongsTo(player)) {
      if (ticksExisted <= 20) return false;
      vanish();
      return true;
    }
    return super.interact(player);
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    if (worldObj.isRemote) {
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.DUST,
          worldObj,
          posX - width / 2 + rand.nextFloat() * width,
          posY + height * rand.nextFloat() + 0.2D,
          posZ - width / 2 + rand.nextFloat() * width,
          0,
          0,
          0,
          new ParticleSettings(20, 0.8F, 0.8F, 1.0F, 1.0F, false));
    }
    if (riddenByEntity == null) idleTicks++;
    else idleTicks = 0;
    if (idleTicks > 200) vanish();
  }

  private void vanish() {
    if (worldObj.isRemote) {
      for (int i = 0; i < 15; i++) {
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.SPARKLE,
            worldObj,
            posX - width / 2 + rand.nextFloat() * width,
            posY + height * rand.nextFloat() + 0.2D,
            posZ - width / 2 + rand.nextFloat() * width,
            0,
            0,
            0,
            new ParticleSettings(48 + rand.nextInt(12), 0.8F, 0.8F, 1.0F, 1.0F, false));
      }
    }
    playSound("mbo:heal", 0.7F, rand.nextFloat() * 0.4F + 1.0F);
    setDead();
  }

  @Override
  public boolean isChested() {
    return false;
  }

  @Override
  public int getTotalArmorValue() {
    return 0;
  }

  @Override
  protected int getExperiencePoints(EntityPlayer player) {
    return 0;
  }

  @Override
  protected Item getDropItem() {
    return null;
  }

  @Override
  protected void dropFewItems(boolean recentlyHit, int looting) {}

  @Override
  public void openGUI(EntityPlayer player) {}

  @Override
  public boolean canMateWith(EntityAnimal animal) {
    return false;
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setInteger("SpiritIdleTicks", idleTicks);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    idleTicks = tag.getInteger("SpiritIdleTicks");
  }
}

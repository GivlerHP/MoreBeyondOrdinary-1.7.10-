package ru.givler.mbo.entity.magic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;

/** Invulnerable player-shaped illusion used to redirect hostile targets. */
public final class EntityMagicDecoy extends EntityCreature {
  private int lifetime = 600;

  public EntityMagicDecoy(World world) {
    super(world);
    setSize(.6F, 1.8F);
    tasks.addTask(0, new EntityAISwimming(this));
    tasks.addTask(1, new EntityAIWander(this, 1));
    tasks.addTask(2, new EntityAIWatchClosest(this, EntityLivingBase.class, 6));
    tasks.addTask(3, new EntityAILookIdle(this));
  }

  public EntityMagicDecoy(World world, EntityLivingBase caster, int lifetime) {
    this(world);
    this.lifetime = lifetime;
    dataWatcher.updateObject(20, caster.getEntityId());
    setLocationAndAngles(
        caster.posX, caster.posY, caster.posZ, caster.rotationYaw, caster.rotationPitch);
    setCustomNameTag(caster.getCommandSenderName());
    setAlwaysRenderNameTag(caster instanceof net.minecraft.entity.player.EntityPlayer);
  }

  @Override
  protected void entityInit() {
    super.entityInit();
    dataWatcher.addObject(20, -1);
  }

  public EntityLivingBase caster() {
    Entity e = worldObj.getEntityByID(dataWatcher.getWatchableObjectInt(20));
    return e instanceof EntityLivingBase ? (EntityLivingBase) e : null;
  }

  @Override
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.25D);
  }

  @Override
  public void onLivingUpdate() {
    super.onLivingUpdate();
    if (ticksExisted > lifetime || caster() == null || caster().isDead) setDead();
  }

  @Override
  protected boolean isAIEnabled() {
    return true;
  }

  @Override
  public boolean isEntityInvulnerable() {
    return true;
  }

  @Override
  public net.minecraft.item.ItemStack getHeldItem() {
    EntityLivingBase c = caster();
    return c == null ? null : c.getHeldItem();
  }

  @Override
  public ItemStack getEquipmentInSlot(int slot) {
    EntityLivingBase c = caster();
    return c == null ? null : c.getEquipmentInSlot(slot);
  }

  @Override
  public void setDead() {
    if (!isDead && worldObj != null && worldObj.isRemote)
      for (int i = 0; i < 20; i++)
        MoreBeyondOrdinary.proxy.spawnParticle(
            EnumParticleType.DUST,
            worldObj,
            posX + (rand.nextDouble() - .5) * width,
            boundingBox.minY + rand.nextDouble() * height,
            posZ + (rand.nextDouble() - .5) * width,
            0,
            0,
            0,
            new ParticleSettings(40, .2F, 1F, .8F, .8F, false));
    super.setDead();
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setInteger("Life", lifetime);
    tag.setInteger("Caster", dataWatcher.getWatchableObjectInt(20));
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    lifetime = tag.getInteger("Life");
    dataWatcher.updateObject(20, tag.getInteger("Caster"));
  }
}

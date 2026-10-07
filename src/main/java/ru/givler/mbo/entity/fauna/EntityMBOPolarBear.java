package ru.givler.mbo.entity.fauna;

import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import ru.givler.mbo.config.FaunaConfig;

/** Neutral adult, protective parent and fleeing cub; anger survives saving the world. */
public final class EntityMBOPolarBear extends EntityAnimal {
  private int angerTicks, warningTicks;
  private UUID angerTarget;
  private float stand, previousStand;

  public EntityMBOPolarBear(World world) {
    super(world);
    setSize(1.4F, 1.4F);
    tasks.addTask(0, new EntityAISwimming(this));
    tasks.addTask(1, new BearAttack());
    tasks.addTask(
        1,
        new EntityAIPanic(this, 2D) {
          @Override
          public boolean shouldExecute() {
            return (isChild() || isBurning()) && super.shouldExecute();
          }
        });
    tasks.addTask(4, new EntityAIFollowParent(this, 1.25D));
    tasks.addTask(5, new EntityAIWander(this, 1D));
    tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6F));
    tasks.addTask(7, new EntityAILookIdle(this));
  }

  @Override
  protected void entityInit() {
    super.entityInit();
    dataWatcher.addObject(20, Byte.valueOf((byte) 0));
  }

  @Override
  protected boolean isAIEnabled() {
    return true;
  }

  @Override
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30D);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.25D);
    getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(20D);
  }

  @Override
  public EntityAgeable createChild(EntityAgeable parent) {
    return new EntityMBOPolarBear(worldObj);
  }

  @Override
  public boolean isBreedingItem(ItemStack item) {
    return false;
  }

  private void standing(boolean value) {
    dataWatcher.updateObject(20, Byte.valueOf((byte) (value ? 1 : 0)));
  }

  public float standScale(float partial) {
    return (previousStand + (stand - previousStand) * partial) / 6F;
  }

  private void anger(EntityLivingBase target) {
    if (target instanceof EntityPlayer && ((EntityPlayer) target).capabilities.isCreativeMode)
      return;
    angerTarget = target.getUniqueID();
    angerTicks = (20 + rand.nextInt(20)) * 20;
    setAttackTarget(target);
  }

  @Override
  public boolean attackEntityFrom(DamageSource damage, float amount) {
    boolean hurt = super.attackEntityFrom(damage, amount);
    if (hurt && !worldObj.isRemote && damage.getEntity() instanceof EntityLivingBase) {
      EntityLivingBase attacker = (EntityLivingBase) damage.getEntity();
      if (!isChild()) anger(attacker);
      List<EntityMBOPolarBear> bears =
          worldObj.getEntitiesWithinAABB(EntityMBOPolarBear.class, boundingBox.expand(20, 10, 20));
      for (EntityMBOPolarBear bear : bears) if (!bear.isChild()) bear.anger(attacker);
    }
    return hurt;
  }

  @Override
  public boolean attackEntityAsMob(Entity victim) {
    return victim.attackEntityFrom(DamageSource.causeMobDamage(this), 6F);
  }

  @Override
  public void onLivingUpdate() {
    super.onLivingUpdate();
    previousStand = stand;
    stand =
        MathHelper.clamp_float(
            stand + (dataWatcher.getWatchableObjectByte(20) != 0 ? 1 : -1), 0, 6);
    if (warningTicks > 0) warningTicks--;
    if (worldObj.isRemote) return;
    if (angerTicks > 0) angerTicks--;
    if (angerTicks == 0 && angerTarget != null) {
      angerTarget = null;
      setAttackTarget(null);
    }
    EntityLivingBase target = getAttackTarget();
    if (target != null
        && (!target.isEntityAlive()
            || target instanceof EntityPlayer
                && ((EntityPlayer) target).capabilities.isCreativeMode)) {
      setAttackTarget(null);
      angerTarget = null;
      angerTicks = 0;
    }
    if (!isChild() && getAttackTarget() == null && ticksExisted % 20 == 0) {
      EntityPlayer player = worldObj.getClosestPlayerToEntity(this, 10D);
      if (player != null
          && !player.capabilities.isCreativeMode
          && getEntitySenses().canSee(player)) {
        boolean defend = angerTicks > 0 && player.getUniqueID().equals(angerTarget);
        if (!defend) {
          List<EntityMBOPolarBear> nearby =
              worldObj.getEntitiesWithinAABB(EntityMBOPolarBear.class, boundingBox.expand(8, 4, 8));
          for (EntityMBOPolarBear bear : nearby)
            if (bear.isChild()) {
              defend = true;
              break;
            }
        }
        if (defend) anger(player);
      }
      if (getAttackTarget() == null) {
        List<EntityMBOFox> foxes =
            worldObj.getEntitiesWithinAABB(EntityMBOFox.class, boundingBox.expand(16, 4, 16));
        EntityMBOFox closest = null;
        double distance = 256;
        for (EntityMBOFox fox : foxes)
          if (fox.isEntityAlive()
              && getEntitySenses().canSee(fox)
              && getDistanceSqToEntity(fox) < distance) {
            closest = fox;
            distance = getDistanceSqToEntity(fox);
          }
        if (closest != null) setAttackTarget(closest);
      }
    }
  }

  @Override
  public boolean getCanSpawnHere() {
    int x = MathHelper.floor_double(posX),
        y = MathHelper.floor_double(boundingBox.minY),
        z = MathHelper.floor_double(posZ);
    Block ground = worldObj.getBlock(x, y - 1, z);
    return FaunaConfig.allowsPosition("polar_bear", worldObj, x, y, z)
        && (ground == Blocks.grass
            || ground == Blocks.snow
            || ground == Blocks.ice
            || ground == Blocks.packed_ice)
        && worldObj.checkNoEntityCollision(boundingBox)
        && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty()
        && !worldObj.isAnyLiquid(boundingBox);
  }

  @Override
  public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
    super.onSpawnWithEgg(data);
    if (data instanceof Family) setGrowingAge(-24000);
    return data == null ? new Family() : data;
  }

  private static final class Family implements IEntityLivingData {}

  @Override
  protected String getLivingSound() {
    return "mbo:entity.polar_bear." + (isChild() ? "ambient_baby" : "ambient");
  }

  @Override
  protected String getHurtSound() {
    return "mbo:entity.polar_bear.hurt";
  }

  @Override
  protected String getDeathSound() {
    return "mbo:entity.polar_bear.death";
  }

  @Override
  protected void func_145780_a(int x, int y, int z, Block block) {
    playSound("mbo:entity.polar_bear.step", .15F, 1F);
  }

  @Override
  protected void dropFewItems(boolean hit, int looting) {
    int count = rand.nextInt(3) + Math.round(rand.nextFloat() * looting);
    if (!isChild() && count > 0)
      entityDropItem(
          new ItemStack(
              isBurning() ? Items.cooked_fished : Items.fish,
              count,
              rand.nextFloat() < .75F ? 0 : 1),
          0F);
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound data) {
    super.writeEntityToNBT(data);
    data.setInteger("AngerTime", angerTicks);
    if (angerTarget != null) data.setString("AngryAt", angerTarget.toString());
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound data) {
    super.readEntityFromNBT(data);
    angerTicks = MathHelper.clamp_int(data.getInteger("AngerTime"), 0, 780);
    angerTarget = null;
    if (data.hasKey("AngryAt"))
      try {
        angerTarget = UUID.fromString(data.getString("AngryAt"));
      } catch (IllegalArgumentException ignored) {
      }
  }

  private final class BearAttack extends EntityAIBase {
    private int cooldown;

    BearAttack() {
      setMutexBits(3);
    }

    public boolean shouldExecute() {
      return !isChild() && getAttackTarget() != null && getAttackTarget().isEntityAlive();
    }

    public boolean continueExecuting() {
      return shouldExecute() && getDistanceSqToEntity(getAttackTarget()) < 400D;
    }

    public void resetTask() {
      getNavigator().clearPathEntity();
      standing(false);
    }

    public void updateTask() {
      EntityLivingBase target = getAttackTarget();
      getLookHelper().setLookPositionWithEntity(target, 30F, 30F);
      getNavigator().tryMoveToEntityLiving(target, 1.25D);
      if (cooldown > 0) cooldown--;
      double distance = getDistanceSqToEntity(target),
          reach = (width * 2) * (width * 2) + target.width;
      if (distance <= reach && cooldown == 0) {
        cooldown = 20;
        attackEntityAsMob(target);
        standing(false);
      } else if (distance < (target.width + 3) * (target.width + 3)) {
        if (cooldown == 0) {
          cooldown = 20;
          standing(false);
        }
        if (cooldown <= 10) {
          standing(true);
          if (warningTicks == 0) {
            playSound("mbo:entity.polar_bear.warning", 1F, 1F);
            warningTicks = 40;
          }
        }
      } else {
        cooldown = 20;
        standing(false);
      }
    }
  }
}

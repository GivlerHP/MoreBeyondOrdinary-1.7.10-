package ru.givler.mbo.entity.fauna;

import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import ru.givler.mbo.config.MobSpawnConfig;
import ru.givler.mbo.registry.ItemRegistry;

public final class EntityMBOAxolotl extends EntityAnimal implements IBucketableCreature {
  @Override
  public boolean isCreatureType(EnumCreatureType type, boolean forSpawnCount) {
    return false;
  }

  private final AquaticMovement.ClientTurn clientTurn = new AquaticMovement.ClientTurn();

  @Override
  public boolean handleWaterMovement() {
    inWater = AquaticMovement.updateWaterContact(this);
    return inWater;
  }

  public static final String[] VARIANTS = {"lucy", "wild", "gold", "cyan", "blue"};
  private int moisture = 6000, huntCooldown, attackCooldown;
  private double goalX, goalY, goalZ;
  private final float[] poses = new float[4], previousPoses = new float[4];

  public float poseFactor(int state, float partial) {
    return previousPoses[state] + (poses[state] - previousPoses[state]) * partial;
  }

  public EntityMBOAxolotl(World world) {
    super(world);
    setSize(.75F, .42F);
    stepHeight = 1;
    getNavigator().setCanSwim(true);
    tasks.addTask(0, new StillWhenDead());
    tasks.addTask(1, new EntityAIPanic(this, 2D));
    tasks.addTask(2, new EntityAIMate(this, 1D));
    tasks.addTask(3, new EntityAITempt(this, 1D, ItemRegistry.fishBuckets[2], false));
    tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
    tasks.addTask(
        5,
        new EntityAIWander(this, 1D) {
          @Override
          public boolean shouldExecute() {
            return !isInWater() && super.shouldExecute();
          }
        });
    tasks.addTask(6, new EntityAILookIdle(this));
  }

  @Override
  protected void entityInit() {
    super.entityInit();
    dataWatcher.addObject(20, Integer.valueOf(0));
    dataWatcher.addObject(21, Integer.valueOf(0));
    dataWatcher.addObject(22, Byte.valueOf((byte) 0));
  }

  @Override
  protected boolean isAIEnabled() {
    return true;
  }

  @Override
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(14);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.1);
  }

  public int variant() {
    return dataWatcher.getWatchableObjectInt(20);
  }

  public void variant(int value) {
    dataWatcher.updateObject(20, Integer.valueOf(MathHelper.clamp_int(value, 0, 4)));
  }

  public int deadTicks() {
    return dataWatcher.getWatchableObjectInt(21);
  }

  public boolean fromBucket() {
    return dataWatcher.getWatchableObjectByte(22) != 0;
  }

  @Override
  public boolean canBreatheUnderwater() {
    return true;
  }

  @Override
  public boolean allowLeashing() {
    return false;
  }

  @Override
  protected boolean canDespawn() {
    return !fromBucket() && !hasCustomNameTag();
  }

  @Override
  public boolean isBreedingItem(ItemStack stack) {
    return stack != null && stack.getItem() == ItemRegistry.fishBuckets[2];
  }

  @Override
  public EntityAgeable createChild(EntityAgeable partner) {
    EntityMBOAxolotl baby = new EntityMBOAxolotl(worldObj);
    baby.variant(
        rand.nextInt(1200) == 0
            ? 4
            : rand.nextBoolean() ? variant() : ((EntityMBOAxolotl) partner).variant());
    baby.func_110163_bv();
    return baby;
  }

  @Override
  public boolean interact(EntityPlayer player) {
    ItemStack held = player.getCurrentEquippedItem();
    if (held != null && held.getItem() == Items.water_bucket && isEntityAlive()) {
      if (!worldObj.isRemote) {
        ItemStack bucket = new ItemStack(ItemRegistry.axolotlBucket);
        bucket.setTagCompound(bucketData());
        if (hasCustomNameTag()) bucket.setStackDisplayName(getCustomNameTag());
        if (player.capabilities.isCreativeMode) {
          if (!player.inventory.addItemStackToInventory(bucket))
            player.dropPlayerItemWithRandomChoice(bucket, false);
        } else player.inventory.setInventorySlotContents(player.inventory.currentItem, bucket);
        playSound("mbo:entity.axolotl.splash", 1, 1);
        setDead();
      }
      return true;
    }
    boolean feeding = isBreedingItem(held), result = super.interact(player);
    if (result && feeding && !worldObj.isRemote && !player.capabilities.isCreativeMode) {
      ItemStack empty = new ItemStack(Items.water_bucket);
      if (held.stackSize <= 0)
        player.inventory.setInventorySlotContents(player.inventory.currentItem, empty);
      else if (!player.inventory.addItemStackToInventory(empty))
        player.dropPlayerItemWithRandomChoice(empty, false);
    }
    return result;
  }

  @Override
  public boolean attackEntityFrom(DamageSource source, float amount) {
    boolean pretend =
        !worldObj.isRemote
            && rand.nextInt(3) == 0
            && (rand.nextInt(3) < amount || getHealth() / getMaxHealth() < .5F)
            && amount < getHealth()
            && isInWater()
            && source.getEntity() != null
            && deadTicks() == 0;
    boolean hurt = super.attackEntityFrom(source, amount);
    if (hurt && pretend) {
      dataWatcher.updateObject(21, Integer.valueOf(200));
      addPotionEffect(new PotionEffect(Potion.regeneration.id, 200));
      setAttackTarget(null);
      getNavigator().clearPathEntity();
    }
    return hurt;
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    clientTurn.update(this);
  }

  @Override
  public void onLivingUpdate() {
    if (!worldObj.isRemote) {
      if (isWet()) moisture = 6000;
      else if (--moisture < 0) {
        moisture = 0;
        if (ticksExisted % 20 == 0) attackEntityFrom(DamageSource.drown, 2);
      }
      if (huntCooldown > 0) huntCooldown--;
      if (attackCooldown > 0) attackCooldown--;
      if (deadTicks() > 0) {
        dataWatcher.updateObject(21, Integer.valueOf(deadTicks() - 1));
        motionX = motionY = motionZ = 0;
      } else if (isInWater()) {
        EntityLivingBase target = getAttackTarget();
        if (target != null
            && (!target.isEntityAlive()
                || !target.isInWater()
                || getDistanceSqToEntity(target) > 256)) {
          setAttackTarget(null);
          huntCooldown = 2400;
          target = null;
        }
        if (ticksExisted % 20 == 0) {
          if (target == null && huntCooldown == 0 && !isChild() && !isInLove()) {
            List<EntityLivingBase> animals =
                worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(8, 4, 8));
            double nearest = 64;
            for (EntityLivingBase animal : animals)
              if ((animal instanceof EntityMBOFish
                      || animal instanceof EntitySquid
                      || animal instanceof EntityMBOGuardian)
                  && animal.isEntityAlive()
                  && animal.isInWater()
                  && getEntitySenses().canSee(animal)
                  && getDistanceSqToEntity(animal) < nearest) {
                nearest = getDistanceSqToEntity(animal);
                target = animal;
              }
            setAttackTarget(target);
          }
          goalX = posX + rand.nextInt(11) - 5;
          goalY = posY + rand.nextInt(5) - 2;
          goalZ = posZ + rand.nextInt(11) - 5;
          if (!worldObj.blockExists(
                  MathHelper.floor_double(goalX),
                  MathHelper.floor_double(goalY),
                  MathHelper.floor_double(goalZ))
              || worldObj
                      .getBlock(
                          MathHelper.floor_double(goalX),
                          MathHelper.floor_double(goalY),
                          MathHelper.floor_double(goalZ))
                      .getMaterial()
                  != Material.water) {
            goalX = posX;
            goalY = posY;
            goalZ = posZ;
          }
        }
        EntityPlayer tempting = worldObj.getClosestPlayerToEntity(this, 8);
        if (tempting != null && isBreedingItem(tempting.getCurrentEquippedItem())) {
          goalX = tempting.posX;
          goalY = tempting.posY;
          goalZ = tempting.posZ;
          target = null;
          setAttackTarget(null);
        }
        if (isInLove()) {
          List<EntityMBOAxolotl> mates =
              worldObj.getEntitiesWithinAABB(EntityMBOAxolotl.class, boundingBox.expand(8, 4, 8));
          for (EntityMBOAxolotl mate : mates)
            if (mate != this && mate.isInLove()) {
              goalX = mate.posX;
              goalY = mate.posY;
              goalZ = mate.posZ;
              break;
            }
        }
        if (target != null) {
          goalX = target.posX;
          goalY = target.posY;
          goalZ = target.posZ;
          if (getDistanceSqToEntity(target) < 2 && attackCooldown == 0) {
            attackCooldown = 20;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), 2);
            playSound("mbo:entity.axolotl.attack", 1, 1);
          }
        }
        double dx = goalX - posX,
            dy = goalY - posY,
            dz = goalZ - posZ,
            len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len > .5) {
          AquaticMovement.swim(this, dx, dz, .02 * Math.sqrt(dx * dx + dz * dz) / len, 3F);
          motionY += dy / len * .02;
        }
      } else if (ticksExisted % 40 == 0) {
        for (int i = 0; i < 24; i++) {
          int x = MathHelper.floor_double(posX) + rand.nextInt(17) - 8,
              y = MathHelper.floor_double(posY) + rand.nextInt(5) - 2,
              z = MathHelper.floor_double(posZ) + rand.nextInt(17) - 8;
          if (worldObj.blockExists(x, y, z)
              && worldObj.getBlock(x, y, z).getMaterial() == Material.water) {
            getNavigator().tryMoveToXYZ(x + .5, y, z + .5, 1);
            break;
          }
        }
      }
    }
    super.onLivingUpdate();
    boolean[] state = {
      deadTicks() > 0,
      isInWater(),
      motionX * motionX + motionY * motionY + motionZ * motionZ > .0001,
      onGround
    };
    for (int i = 0; i < 4; i++) {
      previousPoses[i] = poses[i];
      poses[i] = MathHelper.clamp_float(poses[i] + (state[i] ? .1F : -.1F), 0, 1);
    }
  }

  @Override
  public void moveEntityWithHeading(float sideways, float forward) {
    if (isInWater()) {
      if (worldObj.isRemote)
        return; // EntityLivingBase interpolates tracked positions on the client.
      if (deadTicks() == 0) {
        moveEntity(motionX, motionY, motionZ);
        motionX *= .9;
        motionY *= .9;
        motionZ *= .9;
      } else moveEntity(0, 0, 0);
    } else super.moveEntityWithHeading(sideways, forward);
  }

  public NBTTagCompound bucketData() {
    NBTTagCompound tag = new NBTTagCompound();
    writeEntityToNBT(tag);
    tag.setFloat("Health", getHealth());
    return tag;
  }

  public void readBucketData(NBTTagCompound tag) {
    readEntityFromNBT(tag);
    dataWatcher.updateObject(22, Byte.valueOf((byte) 1));
    func_110163_bv();
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setInteger("Variant", variant());
    tag.setBoolean("FromBucket", fromBucket());
    tag.setInteger("Moisture", moisture);
    tag.setInteger("HuntingCooldown", huntCooldown);
    tag.setInteger("PlayDeadTicks", deadTicks());
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    variant(tag.getInteger("Variant"));
    dataWatcher.updateObject(22, Byte.valueOf((byte) (tag.getBoolean("FromBucket") ? 1 : 0)));
    moisture =
        tag.hasKey("Moisture") ? MathHelper.clamp_int(tag.getInteger("Moisture"), 0, 6000) : 6000;
    huntCooldown = MathHelper.clamp_int(tag.getInteger("HuntingCooldown"), 0, 2400);
    dataWatcher.updateObject(
        21, Integer.valueOf(MathHelper.clamp_int(tag.getInteger("PlayDeadTicks"), 0, 200)));
  }

  @Override
  public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
    super.onSpawnWithEgg(data);
    variant(rand.nextInt(4));
    if (data != null && rand.nextInt(5) == 0) setGrowingAge(-24000);
    return data == null ? new Family() : data;
  }

  private static final class Family implements IEntityLivingData {}

  @Override
  public boolean getCanSpawnHere() {
    int x = MathHelper.floor_double(posX),
        y = MathHelper.floor_double(posY),
        z = MathHelper.floor_double(posZ);
    return MobSpawnConfig.allowsPosition("axolotl", worldObj, x, y, z)
        && worldObj.getBlock(x, y, z).getMaterial() == Material.water
        && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty()
        && worldObj.checkNoEntityCollision(boundingBox);
  }

  @Override
  protected String getLivingSound() {
    return "mbo:entity.axolotl.idle_" + (isInWater() ? "water" : "air");
  }

  @Override
  protected String getHurtSound() {
    return "mbo:entity.axolotl.hurt";
  }

  @Override
  protected String getDeathSound() {
    return "mbo:entity.axolotl.death";
  }

  @Override
  protected void dropFewItems(boolean hit, int looting) {}

  private final class StillWhenDead extends EntityAIBase {
    StillWhenDead() {
      setMutexBits(7);
    }

    public boolean shouldExecute() {
      return deadTicks() > 0;
    }

    public void updateTask() {
      getNavigator().clearPathEntity();
      moveForward = moveStrafing = 0;
    }
  }
}

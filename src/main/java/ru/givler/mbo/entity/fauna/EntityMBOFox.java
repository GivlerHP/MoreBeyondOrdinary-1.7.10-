package ru.givler.mbo.entity.fauna;

import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import ru.givler.mbo.config.MobSpawnConfig;
import ru.givler.mbo.integration.minefantasy2.FaunaMineFantasy;
import ru.givler.mbo.registry.ItemRegistry;

public final class EntityMBOFox extends EntityAnimal {
  private UUID trusted1, trusted2, loveCause;
  private int stateTicks, eatingTicks, attackCooldown, berryCheck;
  private float crouch, previousCrouch;

  public EntityMBOFox(World world) {
    super(world);
    setSize(.6F, .7F);
    equipmentDropChances[0] = 2F;
    getNavigator().setCanSwim(true);
    tasks.addTask(0, new EntityAISwimming(this));
    tasks.addTask(1, new EntityAIPanic(this, 2.2D));
    tasks.addTask(2, new EntityAIMate(this, 1D));
    tasks.addTask(3, new FoxActivity());
    tasks.addTask(
        4,
        new EntityAIAvoidEntity(this, EntityPlayer.class, 16F, 1.6D, 1.4D) {
          @Override
          public boolean shouldExecute() {
            EntityPlayer player = worldObj.getClosestPlayerToEntity(EntityMBOFox.this, 16);
            return getAttackTarget() == null
                && player != null
                && !player.isSneaking()
                && !trusts(player.getUniqueID())
                && !player.capabilities.isCreativeMode
                && super.shouldExecute();
          }
        });
    tasks.addTask(
        4,
        new EntityAIAvoidEntity(this, EntityWolf.class, 8F, 1.6D, 1.4D) {
          @Override
          public boolean shouldExecute() {
            return getAttackTarget() == null && super.shouldExecute();
          }
        });
    tasks.addTask(4, new EntityAIAvoidEntity(this, EntityMBOPolarBear.class, 8F, 1.6D, 1.4D));
    tasks.addTask(6, new EntityAIFollowParent(this, 1.25D));
    tasks.addTask(7, new EntityAIWander(this, 1D));
    tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 24));
    tasks.addTask(9, new EntityAILookIdle(this));
  }

  @Override
  protected void entityInit() {
    super.entityInit();
    dataWatcher.addObject(20, Byte.valueOf((byte) 0));
    dataWatcher.addObject(21, Byte.valueOf((byte) 0));
  }

  @Override
  protected boolean isAIEnabled() {
    return true;
  }

  @Override
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.3);
  }

  public boolean snowy() {
    return dataWatcher.getWatchableObjectByte(20) != 0;
  }

  public int state() {
    return dataWatcher.getWatchableObjectByte(21);
  }

  private void state(int state, int duration) {
    dataWatcher.updateObject(21, Byte.valueOf((byte) state));
    stateTicks = duration;
    if (state == 0) rotationPitch = 0;
    else getNavigator().clearPathEntity();
    if (state == 4) rotationPitch = 60;
  }

  public float crouch(float partial) {
    return previousCrouch + (crouch - previousCrouch) * partial;
  }

  public boolean trusts(UUID id) {
    return id != null && (id.equals(trusted1) || id.equals(trusted2));
  }

  public void trust(UUID id) {
    if (id == null || trusts(id)) return;
    if (trusted1 == null) trusted1 = id;
    else trusted2 = id;
  }

  @Override
  public boolean isBreedingItem(ItemStack stack) {
    return FaunaMineFantasy.isBerry(stack);
  }

  @Override
  public boolean interact(EntityPlayer player) {
    ItemStack held = player.getCurrentEquippedItem();
    boolean fed = isBreedingItem(held);
    boolean result = super.interact(player);
    if (result && fed && !worldObj.isRemote) {
      loveCause = player.getUniqueID();
      state(0, 0);
    }
    return result;
  }

  @Override
  public EntityAgeable createChild(EntityAgeable other) {
    EntityMBOFox parent = (EntityMBOFox) other, baby = new EntityMBOFox(worldObj);
    baby.dataWatcher.updateObject(
        20, Byte.valueOf((byte) ((rand.nextBoolean() ? snowy() : parent.snowy()) ? 1 : 0)));
    baby.trust(loveCause);
    baby.trust(parent.loveCause);
    return baby;
  }

  private boolean edible(ItemStack stack) {
    return stack != null
        && (stack.getItem() instanceof ItemFood || FaunaMineFantasy.isBerry(stack));
  }

  private boolean prey(EntityLivingBase entity) {
    return entity instanceof EntityChicken
        || entity instanceof EntityRabbit
        || entity instanceof EntityMBOFish
            && !(entity instanceof EntityMBOPufferfish)
            && !(entity instanceof EntityMBOTadpole)
        || entity instanceof EntityMBOTurtle
            && ((EntityMBOTurtle) entity).isChild()
            && !entity.isInWater();
  }

  private boolean alert() {
    List<EntityLivingBase> nearby =
        worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(12, 6, 12));
    for (EntityLivingBase entity : nearby)
      if (entity instanceof EntityPlayer
              && !trusts(entity.getUniqueID())
              && !entity.isSneaking()
              && !((EntityPlayer) entity).capabilities.isCreativeMode
          || entity instanceof EntityWolf
          || entity instanceof EntityMBOPolarBear) return true;
    return false;
  }

  @Override
  public boolean attackEntityFrom(DamageSource source, float amount) {
    boolean result = super.attackEntityFrom(source, amount);
    if (result && !worldObj.isRemote) state(0, 0);
    return result;
  }

  @Override
  public boolean attackEntityAsMob(Entity entity) {
    boolean hit = entity.attackEntityFrom(DamageSource.causeMobDamage(this), 2);
    if (hit) playSound("mbo:entity.fox.bite", 1, 1);
    return hit;
  }

  @Override
  protected void fall(float distance) {
    super.fall(Math.max(0F, distance - 3F));
  }

  @Override
  public void moveEntityWithHeading(float strafe, float forward) {
    super.moveEntityWithHeading(state() == 3 ? 0 : strafe, state() == 3 ? 0 : forward);
  }

  @Override
  public void onLivingUpdate() {
    super.onLivingUpdate();
    previousCrouch = crouch;
    crouch = MathHelper.clamp_float(crouch + (state() == 2 ? .2F : -.2F), 0, 3);
    if (worldObj.isRemote) return;
    if (attackCooldown > 0) attackCooldown--;
    EntityLivingBase target = getAttackTarget();
    if (target != null && (!target.isEntityAlive() || getDistanceSqToEntity(target) > 576))
      setAttackTarget(null);
    if (state() == 3) {
      double horizontal = Math.sqrt(motionX * motionX + motionZ * motionZ);
      rotationPitch = (float) (-Math.atan2(motionY, horizontal) * 180 / Math.PI);
      if (target != null && getDistanceSqToEntity(target) < 4 && attackCooldown == 0) {
        attackEntityAsMob(target);
        attackCooldown = 20;
      }
      if (onGround) {
        if (target != null && getDistanceSqToEntity(target) < 4 && attackCooldown == 0) {
          attackEntityAsMob(target);
          attackCooldown = 20;
        } else if (rotationPitch > 0
            && worldObj.getBlock(
                    MathHelper.floor_double(posX),
                    MathHelper.floor_double(posY),
                    MathHelper.floor_double(posZ))
                == Blocks.snow_layer) {
          state(4, 40);
          setAttackTarget(null);
          return;
        }
        state(0, 0);
        rotationPitch = 0;
      }
    }
    if (state() == 1
        && (!worldObj.isDaytime()
            || isInWater()
            || isBurning()
            || ticksExisted % 20 == 0 && alert())) state(0, 0);
    if (state() == 4 || state() == 5) {
      if (--stateTicks <= 0) state(0, 0);
    }
    if (ticksExisted % 20 == 0) {
      defend(trusted1);
      defend(trusted2);
      if (state() == 0 && getAttackTarget() == null && !isChild() && !isInLove() && !alert()) {
        List<EntityLivingBase> nearby =
            worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(16, 4, 16));
        EntityLivingBase best = null;
        double distance = 256;
        for (EntityLivingBase animal : nearby)
          if (prey(animal) && animal.isEntityAlive() && getEntitySenses().canSee(animal)) {
            double score = getDistanceSqToEntity(animal);
            if (snowy() && animal instanceof EntityMBOFish) score *= .5;
            if (score < distance) {
              distance = score;
              best = animal;
            }
          }
        if (best != null) setAttackTarget(best);
      }
      if (state() == 0
          && getAttackTarget() == null
          && !isInLove()
          && !isInWater()
          && onGround
          && worldObj.isDaytime()
          && !alert()
          && !worldObj.canBlockSeeTheSky(
              MathHelper.floor_double(posX),
              MathHelper.floor_double(boundingBox.maxY),
              MathHelper.floor_double(posZ))
          && rand.nextInt(7) == 0) state(1, 0);
      if (state() == 0
          && getAttackTarget() == null
          && !isInLove()
          && onGround
          && rand.nextInt(100) == 0) state(5, 80);
      if (state() == 0
          && getAttackTarget() == null
          && worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")) pickup();
    }
    ItemStack held = getHeldItem();
    if (edible(held) && getAttackTarget() == null && state() != 1 && state() != 3) {
      if (++eatingTicks > 600) {
        if (--held.stackSize <= 0) setCurrentItemOrArmor(0, null);
        else setCurrentItemOrArmor(0, held);
        eatingTicks = 0;
        playSound("mbo:entity.fox.eat", 1, 1);
        worldObj.setEntityState(this, (byte) 45);
      }
    } else if (held == null) eatingTicks = 0;
    if (berryCheck > 0) berryCheck--;
  }

  private void defend(UUID id) {
    if (id == null) return;
    EntityPlayer player = null;
    for (Object entry : worldObj.playerEntities) {
      EntityPlayer candidate = (EntityPlayer) entry;
      if (id.equals(candidate.getUniqueID())) {
        player = candidate;
        break;
      }
    }
    if (player == null || getDistanceSqToEntity(player) > 1024) return;
    EntityLivingBase attacker = player.getAITarget();
    if (attacker != null
        && attacker != getAttackTarget()
        && attacker.isEntityAlive()
        && !trusts(attacker.getUniqueID())
        && player.ticksExisted - player.func_142015_aE() < 100) {
      state(0, 0);
      setAttackTarget(attacker);
      playSound("mbo:entity.fox.aggro", 1, 1);
    }
  }

  private void pickup() {
    List<EntityItem> items =
        worldObj.getEntitiesWithinAABB(EntityItem.class, boundingBox.expand(6, 3, 6));
    for (EntityItem item : items)
      if (!item.isDead
          && item.delayBeforeCanPickup <= 0
          && (getHeldItem() == null || edible(item.getEntityItem()) && !edible(getHeldItem()))) {
        if (getDistanceSqToEntity(item) < 2) {
          if (getHeldItem() != null) {
            EntityItem spit = entityDropItem(getHeldItem(), .5F);
            if (spit != null) spit.delayBeforeCanPickup = 40;
          }
          ItemStack stack = item.getEntityItem(), one = stack.copy();
          one.stackSize = 1;
          setCurrentItemOrArmor(0, one);
          eatingTicks = 0;
          if (--stack.stackSize <= 0) item.setDead();
          else item.setEntityItemStack(stack);
        } else getNavigator().tryMoveToXYZ(item.posX, item.posY, item.posZ, 1.2D);
        break;
      }
  }

  @Override
  public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
    super.onSpawnWithEgg(data);
    dataWatcher.updateObject(
        20,
        Byte.valueOf(
            (byte)
                (worldObj
                            .getBiomeGenForCoords(
                                MathHelper.floor_double(posX), MathHelper.floor_double(posZ))
                            .getFloatTemperature(
                                MathHelper.floor_double(posX),
                                MathHelper.floor_double(posY),
                                MathHelper.floor_double(posZ))
                        < .2
                    ? 1
                    : 0)));
    if (data != null && rand.nextFloat() < .2) setGrowingAge(-24000);
    if (rand.nextFloat() < .2) {
      float chance = rand.nextFloat();
      setCurrentItemOrArmor(
          0,
          new ItemStack(
              chance < .05
                  ? Items.emerald
                  : chance < .2
                      ? Items.egg
                      : chance < .4
                          ? (rand.nextBoolean() ? ItemRegistry.RabbitFoot : ItemRegistry.RabbitHide)
                          : chance < .6
                              ? Items.wheat
                              : chance < .8 ? Items.leather : Items.feather));
    }
    return data == null ? new Family() : data;
  }

  private static final class Family implements IEntityLivingData {}

  @Override
  public boolean getCanSpawnHere() {
    int x = MathHelper.floor_double(posX),
        y = MathHelper.floor_double(boundingBox.minY),
        z = MathHelper.floor_double(posZ);
    Block floor = worldObj.getBlock(x, y - 1, z);
    return MobSpawnConfig.allowsPosition("fox", worldObj, x, y, z)
        && (floor == Blocks.grass || floor == Blocks.snow || floor == Blocks.dirt)
        && worldObj.checkNoEntityCollision(boundingBox)
        && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty()
        && !worldObj.isAnyLiquid(boundingBox);
  }

  @Override
  protected String getLivingSound() {
    return "mbo:entity.fox." + (state() == 1 ? "sleep" : "ambient");
  }

  @Override
  protected String getHurtSound() {
    return "mbo:entity.fox.hurt";
  }

  @Override
  protected String getDeathSound() {
    return "mbo:entity.fox.death";
  }

  @Override
  public void handleHealthUpdate(byte id) {
    if (id == 45) {
      ItemStack held = getHeldItem();
      if (held != null)
        for (int i = 0; i < 8; i++)
          worldObj.spawnParticle(
              "iconcrack_" + Item.getIdFromItem(held.getItem()) + "_" + held.getItemDamage(),
              posX,
              posY + .4,
              posZ,
              0,
              .05,
              0);
    } else super.handleHealthUpdate(id);
  }

  @Override
  protected void dropFewItems(boolean hit, int looting) {}

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setString("Type", snowy() ? "snow" : "red");
    tag.setBoolean("Sleeping", state() == 1);
    if (trusted1 != null) tag.setString("Trusted1", trusted1.toString());
    if (trusted2 != null) tag.setString("Trusted2", trusted2.toString());
    tag.setInteger("EatingTicks", eatingTicks);
  }

  private UUID uuid(String value) {
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException invalid) {
      return null;
    }
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    dataWatcher.updateObject(
        20, Byte.valueOf((byte) ("snow".equals(tag.getString("Type")) ? 1 : 0)));
    trusted1 = uuid(tag.getString("Trusted1"));
    trusted2 = uuid(tag.getString("Trusted2"));
    state(tag.getBoolean("Sleeping") ? 1 : 0, 0);
    eatingTicks = MathHelper.clamp_int(tag.getInteger("EatingTicks"), 0, 600);
  }

  private final class FoxActivity extends EntityAIBase {
    private int stalkTicks, berryX, berryY, berryZ, berryWait;
    private boolean berries;

    FoxActivity() {
      setMutexBits(3);
    }

    public boolean shouldExecute() {
      if (state() != 0 || getAttackTarget() != null) return true;
      if (berryCheck > 0
          || !worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")
          || isInLove()) return false;
      berryCheck = 100;
      int px = MathHelper.floor_double(posX),
          py = MathHelper.floor_double(posY),
          pz = MathHelper.floor_double(posZ);
      for (int i = 0; i < 40; i++) {
        int x = px + rand.nextInt(17) - 8,
            z = pz + rand.nextInt(17) - 8,
            y = py + rand.nextInt(5) - 2;
        if (FaunaMineFantasy.ripe(worldObj, x, y, z)) {
          berryX = x;
          berryY = y;
          berryZ = z;
          berries = true;
          berryWait = 0;
          return true;
        }
      }
      return false;
    }

    public boolean continueExecuting() {
      return state() != 0
          || getAttackTarget() != null
          || berries && berryWait < 120 && FaunaMineFantasy.ripe(worldObj, berryX, berryY, berryZ);
    }

    public void resetTask() {
      berries = false;
      stalkTicks = 0;
      getNavigator().clearPathEntity();
      if (state() == 2) state(0, 0);
    }

    public void updateTask() {
      if (state() == 1 || state() == 4 || state() == 5) {
        moveForward = moveStrafing = 0;
        getNavigator().clearPathEntity();
        return;
      }
      EntityLivingBase target = getAttackTarget();
      if (target == null) {
        if (state() == 2) state(0, 0);
        if (berries) {
          berryWait++;
          if (getDistanceSq(berryX + .5, berryY, berryZ + .5) < 2
              && berryWait >= 40
              && worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")) {
            FaunaMineFantasy.harvest(worldObj, berryX, berryY, berryZ, EntityMBOFox.this);
            berries = false;
          } else if (berryWait % 10 == 1)
            getNavigator().tryMoveToXYZ(berryX + .5, berryY, berryZ + .5, 1.2);
        }
        return;
      }
      getLookHelper().setLookPositionWithEntity(target, 60, 30);
      double distance = getDistanceSqToEntity(target);
      if (state() == 3) return;
      if (distance < 4 && attackCooldown == 0) {
        attackEntityAsMob(target);
        attackCooldown = 20;
        state(0, 0);
        return;
      }
      if (distance >= 4 && distance < 36 && onGround && prey(target) && !target.isInWater()) {
        if (state() != 2) {
          state(2, 0);
          stalkTicks = 0;
        }
        stalkTicks++;
        if (stalkTicks >= 15) {
          int flightTicks = 14;
          double dx = target.posX - posX, dz = target.posZ - posZ;
          double rise = target.boundingBox.minY - boundingBox.minY;
          double factor = (1 - Math.pow(.91, flightTicks)) / .09;
          double vx = dx / factor, vz = dz / factor;
          double vy = EntityMBOGoat.jumpVelocity(rise, flightTicks);
          boolean clear = target.onGround && Math.abs(rise) <= 1 && vy > 0 && vy <= .7;
          double px = 0, py = 0, pz = 0, sx = vx, sy = vy, sz = vz;
          for (int i = 0; clear && i < flightTicks; i++) {
            px += sx;
            py += sy;
            pz += sz;
            AxisAlignedBB box = boundingBox.copy().offset(px, py + .02, pz);
            if (!worldObj.blockExists(
                    MathHelper.floor_double(box.minX),
                    MathHelper.floor_double(box.minY),
                    MathHelper.floor_double(box.minZ))
                || !worldObj.getCollidingBoundingBoxes(EntityMBOFox.this, box).isEmpty())
              clear = false;
            sx *= .91;
            sz *= .91;
            sy = (sy - .08) * .98;
          }
          if (clear) {
            getNavigator().clearPathEntity();
            moveForward = moveStrafing = 0;
            motionX = vx;
            motionZ = vz;
            motionY = vy;
            onGround = false;
            velocityChanged = true;
            state(3, 0);
          } else {
            state(0, 0);
            getNavigator().tryMoveToEntityLiving(target, 1.2);
          }
        }
      } else {
        if (state() == 2) state(0, 0);
        if (ticksExisted % 10 == 0) getNavigator().tryMoveToEntityLiving(target, 1.2);
      }
    }
  }
}

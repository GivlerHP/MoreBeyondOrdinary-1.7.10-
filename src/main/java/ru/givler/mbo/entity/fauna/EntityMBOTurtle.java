package ru.givler.mbo.entity.fauna;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import ru.givler.mbo.config.MobSpawnConfig;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.registry.ItemRegistry;

/** Egg-carrying amphibious animal. The beach is persistent, not recalculated on chunk load. */
public final class EntityMBOTurtle extends EntityAnimal {
  private final AquaticMovement.ClientTurn clientTurn = new AquaticMovement.ClientTurn();

  @Override
  public boolean handleWaterMovement() {
    inWater = AquaticMovement.updateWaterContact(this);
    return inWater;
  }

  private int homeX, homeY, homeZ, layTicks, searchTicks;
  private int panicTicks;
  private double panicX, panicZ;
  private boolean homeSet, wasBaby, swimGoalSet;
  private double swimX, swimY, swimZ;

  public EntityMBOTurtle(World world) {
    super(world);
    setSize(1.2F, .4F);
    stepHeight = 1F;
    getNavigator().setAvoidsWater(false);
    getNavigator().setCanSwim(true);
    tasks.addTask(0, new EntityAIPanic(this, 1.2D));
    tasks.addTask(1, new Breed());
    tasks.addTask(2, new EntityAITempt(this, 1.1D, ItemRegistry.seagrass, false));
    tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8F));
    tasks.addTask(
        9,
        new EntityAIWander(this, .6D) {
          @Override
          public boolean shouldExecute() {
            return !isInWater() && !hasEgg() && super.shouldExecute();
          }
        });
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
  }

  public boolean hasEgg() {
    return (dataWatcher.getWatchableObjectByte(20) & 1) != 0;
  }

  public boolean isLayingEgg() {
    return (dataWatcher.getWatchableObjectByte(20) & 2) != 0;
  }

  private void eggState(boolean egg, boolean laying) {
    dataWatcher.updateObject(20, Byte.valueOf((byte) ((egg ? 1 : 0) | (laying ? 2 : 0))));
  }

  public void setHome(int x, int y, int z) {
    homeX = x;
    homeY = y;
    homeZ = z;
    homeSet = true;
  }

  @Override
  public boolean canBreatheUnderwater() {
    return true;
  }

  @Override
  public EntityAgeable createChild(EntityAgeable other) {
    return new EntityMBOTurtle(worldObj);
  }

  @Override
  public boolean isBreedingItem(ItemStack stack) {
    return stack != null && stack.getItem() == ItemRegistry.seagrass && !hasEgg();
  }

  @Override
  public boolean allowLeashing() {
    return false;
  }

  @Override
  public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
    setHome(
        MathHelper.floor_double(posX),
        MathHelper.floor_double(posY),
        MathHelper.floor_double(posZ));
    return super.onSpawnWithEgg(data);
  }

  @Override
  public boolean getCanSpawnHere() {
    int x = MathHelper.floor_double(posX),
        y = MathHelper.floor_double(boundingBox.minY),
        z = MathHelper.floor_double(posZ);
    return worldObj.getBlock(x, y - 1, z) == Blocks.sand
        && MobSpawnConfig.allowsPosition("turtle", worldObj, x, y, z)
        && worldObj.checkNoEntityCollision(boundingBox)
        && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty()
        && !worldObj.isAnyLiquid(boundingBox);
  }

  @Override
  public void onUpdate() {
    super.onUpdate();
    clientTurn.update(this);
  }

  @Override
  public void onLivingUpdate() {
    boolean child = isChild();
    super.onLivingUpdate();
    if (width != (isChild() ? .36F : 1.2F))
      setSize(isChild() ? .36F : 1.2F, isChild() ? .12F : .4F);
    if (worldObj.isRemote) return;
    if (!swimGoalSet) {
      swimX = posX;
      swimY = posY;
      swimZ = posZ;
      swimGoalSet = true;
    }
    if ((child || wasBaby)
        && !isChild()
        && worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot"))
      entityDropItem(new ItemStack(ItemRegistry.turtleScute), 0F);
    wasBaby = isChild();
    if (!homeSet)
      setHome(
          MathHelper.floor_double(posX),
          MathHelper.floor_double(posY),
          MathHelper.floor_double(posZ));
    if (panicTicks > 0) {
      panicTicks--;
      eggState(hasEgg(), false);
      layTicks = 0;
      if (ticksExisted % 10 == 0) goTo(panicX, posY, panicZ);
    } else if (hasEgg()) returnHomeAndLay();
    else if (!isInLove() && ticksExisted % 80 == 0) {
      if (!isInWater()) seekWater();
      else if (rand.nextInt(9) == 0 && getDistanceSq(homeX, homeY, homeZ) > 4096D)
        goTo(homeX + .5, homeY, homeZ + .5);
      else {
        for (int i = 0; i < 10; i++) {
          int x = MathHelper.floor_double(posX) + rand.nextInt(17) - 8,
              y = MathHelper.floor_double(posY) + rand.nextInt(7) - 3,
              z = MathHelper.floor_double(posZ) + rand.nextInt(17) - 8;
          if (worldObj.blockExists(x, y, z)
              && worldObj.getBlock(x, y, z).getMaterial() == Material.water) {
            goTo(x + .5, y + .2, z + .5);
            break;
          }
        }
      }
    }
    if (isInWater()) {
      double dx = swimX - posX,
          dy = swimY - posY,
          dz = swimZ - posZ,
          dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (dist > .4) {
        double speed = isChild() ? .004D : .008D;
        AquaticMovement.swim(this, dx, dz, speed * Math.sqrt(dx * dx + dz * dz) / dist, 2F);
        motionY += dy / dist * speed;
      }
      if (hasEgg() && getDistanceSq(homeX, homeY, homeZ) < 256D) motionY += .006D;
    }
  }

  private void goTo(double x, double y, double z) {
    swimX = x;
    swimY = y;
    swimZ = z;
    swimGoalSet = true;
    if (!isInWater()) getNavigator().tryMoveToXYZ(x, y, z, isChild() ? 1D : .6D);
  }

  private void seekWater() {
    int cx = MathHelper.floor_double(posX),
        cy = MathHelper.floor_double(posY),
        cz = MathHelper.floor_double(posZ);
    // Bounded and infrequent search; do not cause distant chunk loads.
    for (int radius = 1; radius <= 16; radius++)
      for (int i = 0; i < 16; i++) {
        int x = cx + rand.nextInt(radius * 2 + 1) - radius,
            z = cz + rand.nextInt(radius * 2 + 1) - radius;
        for (int y = cy - 3; y <= cy + 1; y++)
          if (worldObj.blockExists(x, y, z)
              && worldObj.getBlock(x, y, z).getMaterial() == Material.water) {
            goTo(x + .5, y + .25, z + .5);
            return;
          }
      }
  }

  private void returnHomeAndLay() {
    if (getDistanceSq(homeX + .5, homeY, homeZ + .5) > 81D || isInWater()) {
      layTicks = 0;
      eggState(true, false);
      if (ticksExisted % 20 == 0) goTo(homeX + .5, homeY, homeZ + .5);
      return;
    }
    int x = MathHelper.floor_double(posX),
        y = MathHelper.floor_double(boundingBox.minY),
        z = MathHelper.floor_double(posZ);
    if (worldObj.getBlock(x, y - 1, z) != Blocks.sand || !worldObj.isAirBlock(x, y, z)) {
      layTicks = 0;
      eggState(true, false);
      if (searchTicks-- <= 0) {
        searchTicks = 80;
        for (int i = 0; i < 32; i++) {
          int sx = homeX + rand.nextInt(17) - 8, sz = homeZ + rand.nextInt(17) - 8;
          for (int sy = homeY - 2; sy <= homeY + 2; sy++)
            if (worldObj.blockExists(sx, sy, sz)
                && worldObj.getBlock(sx, sy - 1, sz) == Blocks.sand
                && worldObj.isAirBlock(sx, sy, sz)) {
              goTo(sx + .5, sy, sz + .5);
              return;
            }
        }
      }
      return;
    }
    getNavigator().clearPathEntity();
    eggState(true, true);
    layTicks++;
    if (layTicks % 5 == 0)
      worldObj.playAuxSFX(2001, x, y - 1, z, Block.getIdFromBlock(Blocks.sand));
    if (layTicks > 200 && worldObj.setBlock(x, y, z, BlockRegistry.turtleEgg, rand.nextInt(4), 3)) {
      playSound("mbo:entity.turtle.lay_egg", .3F, .9F + rand.nextFloat() * .2F);
      eggState(false, false);
      layTicks = 0;
      setGrowingAge(6000);
    }
  }

  @Override
  public void moveEntityWithHeading(float strafe, float forward) {
    if (isInWater()) {
      if (!worldObj.isRemote) {
        moveEntity(motionX, motionY, motionZ);
        motionX *= .9;
        motionY *= .9;
        motionZ *= .9;
      }
    } else super.moveEntityWithHeading(strafe, forward);
  }

  @Override
  protected String getLivingSound() {
    return !isChild() && !isInWater() ? "mbo:entity.turtle.ambient_land" : null;
  }

  @Override
  public boolean attackEntityFrom(DamageSource source, float damage) {
    boolean hurt = super.attackEntityFrom(source, damage);
    if (hurt && !worldObj.isRemote && source.getEntity() != null) {
      double dx = posX - source.getEntity().posX,
          dz = posZ - source.getEntity().posZ,
          dist = Math.sqrt(dx * dx + dz * dz);
      if (dist < .01) {
        dx = rand.nextDouble() - .5;
        dz = rand.nextDouble() - .5;
        dist = Math.max(.01, Math.sqrt(dx * dx + dz * dz));
      }
      panicX = posX + dx / dist * 8;
      panicZ = posZ + dz / dist * 8;
      panicTicks = 100;
    }
    return hurt;
  }

  @Override
  protected void dropFewItems(boolean hit, int looting) {
    int count = rand.nextInt(3) + Math.round(rand.nextFloat() * looting);
    if (!isChild() && count > 0) entityDropItem(new ItemStack(ItemRegistry.seagrass, count), 0F);
  }

  @Override
  public void onStruckByLightning(EntityLightningBolt lightning) {
    if (!worldObj.isRemote && isEntityAlive()) {
      attackEntityFrom(new DamageSource("mbo.lightning").setDamageBypassesArmor(), Float.MAX_VALUE);
      if (worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot"))
        entityDropItem(new ItemStack(Items.bowl), 0F);
    }
  }

  @Override
  protected String getHurtSound() {
    return "mbo:entity.turtle.hurt" + (isChild() ? "_baby" : "");
  }

  @Override
  protected String getDeathSound() {
    return "mbo:entity.turtle.death" + (isChild() ? "_baby" : "");
  }

  @Override
  protected void func_145780_a(int x, int y, int z, Block block) {
    playSound("mbo:entity.turtle.shamble" + (isChild() ? "_baby" : ""), .15F, 1F);
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setInteger("HomeX", homeX);
    tag.setInteger("HomeY", homeY);
    tag.setInteger("HomeZ", homeZ);
    tag.setBoolean("HomeSet", homeSet);
    tag.setBoolean("HasEgg", hasEgg());
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    homeX = tag.getInteger("HomeX");
    homeY = tag.getInteger("HomeY");
    homeZ = tag.getInteger("HomeZ");
    homeSet = tag.getBoolean("HomeSet");
    eggState(tag.getBoolean("HasEgg"), false);
    wasBaby = isChild();
    swimX = posX;
    swimY = posY;
    swimZ = posZ;
    swimGoalSet = true;
  }

  private final class Breed extends EntityAIBase {
    private EntityMBOTurtle mate;
    private int timer;

    Breed() {
      setMutexBits(3);
    }

    public boolean shouldExecute() {
      if (hasEgg() || !isInLove()) return false;
      List<EntityMBOTurtle> nearby =
          worldObj.getEntitiesWithinAABB(EntityMBOTurtle.class, boundingBox.expand(8, 4, 8));
      for (EntityMBOTurtle turtle : nearby)
        if (turtle != EntityMBOTurtle.this && !turtle.hasEgg() && canMateWith(turtle)) {
          mate = turtle;
          return true;
        }
      return false;
    }

    public boolean continueExecuting() {
      return mate != null && mate.isEntityAlive() && mate.isInLove() && isInLove() && timer < 60;
    }

    public void startExecuting() {
      timer = 0;
    }

    public void resetTask() {
      mate = null;
      timer = 0;
    }

    public void updateTask() {
      getLookHelper().setLookPositionWithEntity(mate, 10F, 30F);
      goTo(mate.posX, mate.posY, mate.posZ);
      timer++;
      if (timer >= 60
          && isInLove()
          && mate.isInLove()
          && !mate.hasEgg()
          && getDistanceSqToEntity(mate) < 9D) {
        eggState(true, false);
        setGrowingAge(6000);
        mate.setGrowingAge(6000);
        resetInLove();
        mate.resetInLove();
        if (worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot"))
          worldObj.spawnEntityInWorld(
              new EntityXPOrb(worldObj, posX, posY, posZ, rand.nextInt(7) + 1));
      }
    }
  }
}
